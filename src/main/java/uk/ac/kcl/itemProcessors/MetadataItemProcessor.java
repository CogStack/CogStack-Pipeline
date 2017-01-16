package uk.ac.kcl.itemProcessors;

import org.apache.commons.io.FileUtils;
import org.apache.tika.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import uk.ac.kcl.model.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.PostConstruct;


@Profile("metadata")
@Service("metadataItemProcessor")
public class MetadataItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MetadataItemProcessor.class);

    private boolean eagerGetPageCount;
    private String binaryFieldName;

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
        this.eagerGetPageCount = env.getProperty("eagerGetPageCount")
                                 .equalsIgnoreCase("true");
    }

    @Override
    public Document process(final Document doc) throws Exception {
        LOG.debug("starting " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        Map<String, Object> associativeArray = doc.getAssociativeArray();
        if (eagerGetPageCount) {
            boolean isTiff = (
                (String) associativeArray.getOrDefault("X-TL-CONTENT-TYPE", "")
                ).equalsIgnoreCase("image/tiff");
            boolean isPageCountUnknown = (
                (String) associativeArray.getOrDefault("X-TL-PAGE-COUNT", "TL_PAGE_COUNT_UNKNOWN")
                ).equals("TL_PAGE_COUNT_UNKNOWN");

            if (isTiff && isPageCountUnknown) {
                countPageInTiff(doc);
            }
        }
        LOG.debug("finished " + this.getClass().getSimpleName() + " on doc " +doc.getDocName());
        return doc;
    }

    public void countPageInTiff(Document doc) throws IOException {
        // Use identify to count the number of pages in tiff

        File tempFile = File.createTempFile(doc.getDocName(), ".tiff");
        FileUtils.writeByteArrayToFile(tempFile, doc.getBinaryContent());

        String[] cmd = { getImageMagickIdentifyProg(), "-ping", "-format",
                         "%n\\n", tempFile.getAbsolutePath()};

        try {
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT", "TL_PAGE_COUNT_UNKNOWN_2");
            String output = externalProcessHandler(cmd);
            output = output.split("\\n")[0];
            doc.getAssociativeArray().put("X-TL-PAGE-COUNT", output);
        }
        finally {
            tempFile.delete();
        }
    }

    private String externalProcessHandler(String[] cmd) throws IOException {
        String stdout = "";
        Process process = new ProcessBuilder(cmd).start();
        IOUtils.closeQuietly(process.getOutputStream());
        InputStream processInputStream = process.getInputStream();
        FutureTask<Integer> waitTask = new FutureTask<>(process::waitFor);
        Thread waitThread = new Thread(waitTask);
        waitThread.start();
        try {
            waitTask.get(5, TimeUnit.SECONDS);
            stdout = IOUtils.toString(processInputStream, "UTF-8");
        } catch (InterruptedException e) {
            waitThread.interrupt();
            process.destroy();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            // should not be thrown
            LOG.error(e.getMessage());
        } catch (TimeoutException e) {
            waitThread.interrupt();
            process.destroy();
            LOG.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(processInputStream);
        }
        return stdout;
    }

    private String getImageMagickIdentifyProg() {
        // TODO: Windows equivalent?
        return System.getProperty("os.name").startsWith("Windows") ? "" : "identify";
    }

}
