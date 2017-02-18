package uk.ac.kcl.itemWriters;


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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;


/**
 * Created by rich on 20/04/16.
 */
@Service("jsonFileItemWriter")
@Profile("jsonFileWriter")
public class JSONFileItemWriter implements ItemWriter<Document> {
    private static final Logger LOG = LoggerFactory.getLogger(JSONFileItemWriter.class);

    @Autowired
    Environment env;

    String outputPath;
    @PostConstruct
    public void init()  {
        this.outputPath = env.getProperty("fileOutputDirectory");
    }
    @PreDestroy
    public void destroy(){

    }

    @Override
    public final void write(List<? extends Document> documents) throws Exception {

        for (Document doc : documents) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(
                    new File(outputPath + File.separator + doc.getDocName())))){
                bw.write(doc.getOutputData());

            }
        }
    }


}
