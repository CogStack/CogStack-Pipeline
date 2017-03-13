package uk.ac.kcl.itemWriters;

import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import uk.ac.kcl.model.Document;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Service("thumbnailFileItemWriter")
@Profile("thumbnailFileWriter")
public class ThumbnailFileItemWriter implements ItemWriter<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(JSONFileItemWriter.class);

    @Autowired
    Environment env;

    String outputPath;
    String pdfOutputPath;
    String imageMagickProg;
    String thumbnailDensity;

    @PostConstruct
    public void init()  {
        this.outputPath = env.getProperty("fileOutputDirectory.thumbnail");
        this.imageMagickProg = System.getProperty("os.name").startsWith("Windows") ? "convert.exe" : "convert";
        this.thumbnailDensity = env.getProperty("thumbnailDensity");

        this.pdfOutputPath = env.getProperty("fileOutputDirectory.pdf");
    }

    @PreDestroy
    public void destroy(){

    }

    @Override
    public final void write(List<? extends Document> documents) throws Exception {

        for (Document doc : documents) {
            long startTime = System.currentTimeMillis();
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
            long endTime = System.currentTimeMillis();
            String contentType = ((String) doc.getAssociativeArray()
                                  .getOrDefault("X-TL-CONTENT-TYPE", "TL_CONTENT_TYPE_UNKNOWN")
                                  ).toLowerCase();
            LOG.info("{};Content-Type:{};Time:{} ms",
                     this.getClass().getSimpleName(),
                     contentType,
                     endTime - startTime);
        }
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
