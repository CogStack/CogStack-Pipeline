package uk.ac.kcl.itemHandlers;


import org.apache.commons.io.FileUtils;
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
import java.util.List;
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
            default:
                break;
            }
        }
    }

    private void handlePdf(Document doc) throws IOException{
        FileUtils.writeByteArrayToFile(
            new File(outputPath + File.separator + doc.getDocName() + ".pdf"),
            doc.getBinaryContent()
            );
    }

}
