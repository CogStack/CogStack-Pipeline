package uk.ac.kcl.itemProcessors;

import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import javax.annotation.PostConstruct;


@Profile("thumbnailGeneration")
@Service("thumbnailGenerationItemProcessor")
public class ThumbnailGenerationItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final Logger LOG = LoggerFactory.getLogger(ThumbnailGenerationItemProcessor.class);

    @Autowired
    Environment env;

    String outputPath;
    String pdfOutputPath;
    String imageMagickProg;
    String thumbnailDensity;

    @PostConstruct
    public void init() {
      this.outputPath = env.getProperty("thumbnailGenerationItemProcessor.fileOutputDirectory");
      this.imageMagickProg = System.getProperty("os.name").startsWith("Windows") ? "convert.exe" : "convert";
      this.thumbnailDensity = env.getProperty("thumbnailGenerationItemProcessor.thumbnailDensity", "150");

      this.pdfOutputPath = env.getProperty("pdfGenerationItemProcessor.fileOutputDirectory");
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        Map<String, Object> associativeArray = doc.getAssociativeArray();
        try {
            long startTime = System.currentTimeMillis();

            if (((String) associativeArray.getOrDefault("X-TL-PDF-GENERATION", "FAIL")).equals("FAIL")) {
               throw new Exception("Thumbnail generation failed because previous failure in PDF generation");
            }

            String[] cmd = {
              imageMagickProg,
              "-density",
              thumbnailDensity,
              "-depth",
              "8",
              "-quality",
              "85",
              "-resize",
              "1600x800",
              pdfOutputPath + File.separator + doc.getDocName() + ".pdf[0]",
              outputPath + File.separator + doc.getDocName() + ".png"
            };

            try {
                Process process = new ProcessBuilder(cmd).start();
                IOUtils.closeQuietly(process.getOutputStream());
                InputStream processInputStream = process.getInputStream();
                logStream(processInputStream);
                FutureTask<Integer> waitTask = new FutureTask<>(process::waitFor);
                Thread waitThread = new Thread(waitTask);
                waitThread.start();
                try {
                    waitTask.get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                    waitThread.interrupt();
                    process.destroy();
                    waitTask.cancel(true);
                } finally {
                    IOUtils.closeQuietly(processInputStream);
                    process.destroy();
                    waitThread.interrupt();
                    waitTask.cancel(true);
                }
            } catch (IOException e) {

            }

            if (Files.notExists(Paths.get(outputPath, doc.getDocName() + ".png"))) {
                String expectedPath = outputPath + File.separator + doc.getDocName() + ".png";
                LOG.error("Expected output file for document PK: {} does not exist in path: {}.", doc.getPrimaryKeyFieldValue(), expectedPath);
                throw new NoSuchFileException(expectedPath);
            }
            long endTime = System.currentTimeMillis();
            String contentType = ((String) doc.getAssociativeArray()
                                  .getOrDefault("X-TL-CONTENT-TYPE", "TL_CONTENT_TYPE_UNKNOWN")
                                  ).toLowerCase();
            LOG.info("{};Content-Type:{};Time:{} ms",
                     this.getClass().getSimpleName(),
                     contentType,
                     endTime - startTime);
            associativeArray.put("X-TL-THUMBNAIL-GENERATION", "SUCCESS");
        } catch (Exception e) {
           // Consider this processor as optional - any exception will not
           // cause the processing to fail
           associativeArray.put("X-TL-THUMBNAIL-GENERATION", "FAIL");
           LOG.error("Exception caught for optional processor {} for document: {}. Exception: {}",
                     this.getClass().getSimpleName(),
                     doc.getDocName(),
                     e);
        }
        LOG.debug("finished " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        return doc;
    }

    // Copied directly from timeliner
    private void logStream(final InputStream stream) {
        new Thread() {
            public void run() {
                Reader reader = new InputStreamReader(stream, IOUtils.UTF_8);
                StringBuilder out = new StringBuilder();
                char[] buffer = new char[1024];
                try {
                    for (int n = reader.read(buffer); n != -1; n = reader.read(buffer)) {
                        out.append(buffer, 0, n);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                } finally {
                    IOUtils.closeQuietly(stream);
                    IOUtils.closeQuietly(reader);
                }
                LOG.debug(out.toString());
            }
        }.start();
    }

}
