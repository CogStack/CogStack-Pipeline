package uk.ac.kcl.itemHandlers;


import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service("pdfFileItemWriter")
@Profile("pdfFileWriter")
public class PDFFileItemWriter implements ItemWriter<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(PDFFileItemWriter.class);

    @Autowired
    Environment env;

    String outputPath;

    @PostConstruct
    public void init()  {
        this.outputPath = env.getProperty("fileOutputDirectory.pdf");
    }

    @PreDestroy
    public void destroy(){

    }

    @Override
    public final void write(List<? extends Document> documents) throws Exception {

        for (Document doc : documents) {
            String contentType = ((String) doc.getAssociativeArray()
                                  .getOrDefault("X-TL-CONTENT-TYPE", "TL_CONTENT_TYPE_UNKNOWN")
                                  ).toLowerCase();
            switch (contentType) {
            case "application/pdf":
                handlePdf(doc);
                break;
            case "application/msword":
                handleMSWord(doc);
                break;
            default:
                break;
            }
        }
    }

    private void handlePdf(Document doc) throws IOException {
        FileUtils.writeByteArrayToFile(
            new File(outputPath + File.separator + doc.getDocName() + ".pdf"),
            doc.getBinaryContent()
            );
    }

    private void handleMSWord(Document doc) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        // Create a temp directory for each input document
        Path tempPath = Files.createTempDirectory(doc.getDocName());

        // Dump the MS Word content to a file in the temp directory
        File tempInputFile = new File(tempPath + File.separator + "file.doc");
        FileUtils.writeByteArrayToFile(tempInputFile, doc.getBinaryContent());

        String[] cmd = { getLibreOfficeProg(), "--convert-to", "pdf",
                         tempInputFile.getAbsolutePath(), "--headless",
                         "--outdir", tempPath.toString()};
        Process process = new ProcessBuilder(cmd).start();
        IOUtils.closeQuietly(process.getOutputStream());
        InputStream processInputStream = process.getInputStream();
        logStream(processInputStream);
        FutureTask<Integer> waitTask = new FutureTask<>(process::waitFor);
        Thread waitThread = new Thread(waitTask);
        waitThread.start();
        waitTask.get(30, TimeUnit.SECONDS);

        // Move the file to the configured output path
        Path tempOutputFile = tempPath.resolve("file.pdf");
        Path outputFile = Paths.get(outputPath, doc.getDocName() + ".pdf");
        Files.move(tempOutputFile, outputFile);
    }

    private String getLibreOfficeProg() {
        return System.getProperty("os.name").startsWith("Windows") ? "soffice.exe" : "soffice";
    }

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
