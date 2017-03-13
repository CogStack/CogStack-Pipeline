package uk.ac.kcl.itemProcessors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.pdmodel.interactive.form.PDXFAResource;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.ac.kcl.model.Document;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.ws.commons.util.NamespaceContextImpl;

@Profile("pdfbox")
@Service("PdfBoxItemProcessor")
public class PdfBoxItemProcessor extends TLItemProcessor implements ItemProcessor<Document, Document> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PdfBoxItemProcessor.class);

    @Autowired
    Environment env;

    private Map<String, String> xmlNamespaces;
    private Map<String, String> fieldsMapping;

    @PostConstruct
    public void init() {
      xmlNamespaces = new HashMap<String, String>();
      fieldsMapping = new HashMap<String, String>();
      // LOG.info("namespaces:{}", env.getProperty("pdfbox.namespaces"));
      // LOG.info("namespaces:{}", env.getProperty("pdfbox.mapping"));
      List<String> namespaces = Arrays.asList(env.getProperty("pdfbox.namespaces").split(";"));
      for (String namespace : namespaces) {
        String[] ns = namespace.split(",");
        // LOG.info("ns:{}", ns);
        if (ns.length == 2) {
          xmlNamespaces.put(ns[0], ns[1]);
        }
      }

      List<String> fields = Arrays.asList(env.getProperty("pdfbox.mapping").split(";"));
      for (String field : fields) {
        String[] fieldKV = field.split(",");
        // LOG.info("field:{}", fieldKV);
        if (fieldKV.length == 2) {
          fieldsMapping.put(fieldKV[0], fieldKV[1]);
        }
      }
      // LOG.info("xmlNamespaces:{}", xmlNamespaces);
      // LOG.info("fieldsMapping:{}", fieldsMapping);
    }


    @Override
    public Document process(final Document doc) throws Exception {
        // TODO: Because of this, we implicitly assume tika is used - should we avoid it?
        String contentType = (String) doc.getAssociativeArray().getOrDefault("X-TL-CONTENT-TYPE", "TL_CONTENT_TYPE_UNKNOWN");
        if (!contentType.equals("application/pdf")) {
          LOG.info("Skipping {} on non-pdf doc: {}",
                   this.getClass().getSimpleName(), doc.getDocName());
          return doc;
        }
        LOG.debug("starting " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        long startTime = System.currentTimeMillis();
        PDDocument pdf = null;
        try {
          pdf = PDDocument.load(doc.getBinaryContent());
          if (pdf != null) {
            PDDocumentCatalog docCatalog = pdf.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();
            if (acroForm != null) {
              Map<String, Object> map = new HashMap<String, Object>();
              PDXFAResource	xfa = acroForm.getXFA();
              org.w3c.dom.Document formDoc = xfa.getDocument();

              NamespaceContextImpl nsContext = new NamespaceContextImpl();
              for (Map.Entry<String, String> entry: xmlNamespaces.entrySet()) {
                nsContext.startPrefixMapping(entry.getKey(), entry.getValue());
              }
              XPath xpath = XPathFactory.newInstance().newXPath();
              xpath.setNamespaceContext(nsContext);

              for (Map.Entry<String, String> entry: fieldsMapping.entrySet()) {
                // TODO: This XPath is making the assumption that the field name is unique in this form because the expression matches node in any depth, without specifying the full path.
                String expression = "//" + entry.getValue() + "/text()";
                String nodeValue = (String) xpath.evaluate(expression, formDoc, XPathConstants.STRING);
                doc.getAssociativeArray().put(entry.getKey(), nodeValue);
                // LOG.info("Adding {} with value {} to {}", entry.getKey(), nodeValue, entry.getValue());
              }
            }
          }
        } catch (XPathExpressionException e) {
          LOG.info("Caught XPathExpressionException: {}", e);
        } finally {
          if (pdf != null) {
            pdf.close();
          }
        }

        long endTime = System.currentTimeMillis();
        LOG.debug("{};Time:{} ms",
                 this.getClass().getSimpleName(),
                 endTime - startTime);
        LOG.debug("finished " + this.getClass().getSimpleName() +" on doc " +doc.getDocName());
        return doc;
    }
}
