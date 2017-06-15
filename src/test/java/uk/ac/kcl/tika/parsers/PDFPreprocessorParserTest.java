/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.kcl.tika.parsers;


import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.external.ExternalParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import uk.ac.kcl.tika.config.ImageMagickConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static uk.ac.kcl.tika.parsers.PDFPreprocessorParser.getImageMagickProg;

/**
 *
 * @author rich
 */
public class PDFPreprocessorParserTest {

    TikaConfig config;

    @Before
    public void initConfig() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("tika-config.xml");
        try {
            config = new TikaConfig(is);
        } catch (TikaException | IOException | SAXException ex) {
            Logger.getLogger(PDFPreprocessorParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean canRun() {
        ImageMagickConfig config = new ImageMagickConfig();
        PDFPreprocessorParserTest imOCRTest = new PDFPreprocessorParserTest();
        return imOCRTest.canRun(config);
    }

    private boolean canRun(ImageMagickConfig config) {
        String[] checkCmd = {config.getTesseractPath() + getImageMagickProg()};
        return ExternalParser.check(checkCmd);
    }

    /*
     Check that if IM is not found, the PDFPreprocessorParser claims to not support
     any file types. So, the standard image parser is called instead.
     */
    @Test
    public void offersNoTypesIfNotFound() throws Exception {
        PDFPreprocessorParser parser = new PDFPreprocessorParser();
        DefaultParser defaultParser = new DefaultParser();
        MediaType pdf = MediaType.application("pdf");

        // With an invalid path, will offer no types
        ImageMagickConfig invalidConfig = new ImageMagickConfig();
        invalidConfig.setImageMagickPath("/made/up/path");

        ParseContext parseContext = new ParseContext();
        parseContext.set(ImageMagickConfig.class, invalidConfig);

        // No types offered
        assertEquals(0, parser.getSupportedTypes(parseContext).size());

        // And DefaultParser won't use us
        assertEquals(PDFParser.class, defaultParser.getParsers(parseContext).get(pdf).getClass());
    }

    /*
     If Tesseract is found, test we retrieve the proper number of supporting Parsers.
     */
    @Test
    public void offersTypesIfFound() throws Exception {
        PDFPreprocessorParser parser = new PDFPreprocessorParser();
        //DefaultParser defaultParser = new DefaultParser();

        ParseContext parseContext = new ParseContext();
        MediaType pdf = MediaType.application("pdf");

        // Assuming that Tesseract is on the path, we should find 5 Parsers that support PNG.
        assumeTrue(canRun());

        assertEquals(1, parser.getSupportedTypes(parseContext).size());
        assertTrue(parser.getSupportedTypes(parseContext).contains(pdf));

        // DefaultParser will not select the PDFPreprocessorParser, unless configured in tika config
        //assertEquals(PDFPreprocessorParser.class, defaultParser.getParsers(parseContext).get(pdf).getClass());
    }
    
    @Ignore
    @Test
    public void testParseRequiringOCR() throws Exception {
        System.out.println("parse");
        InputStream stream = getClass().getClassLoader().getResourceAsStream("tika/testdocs/pdf_ocr_test.pdf");
        AutoDetectParser parser = new AutoDetectParser(config);
        //PDFPreprocessorParser parser = new PDFPreprocessorParser();
        BodyContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();
        parser.parse(stream, body, metadata);
        String parsedString = body.toString();
        // From first page
        assertTrue(parsedString.contains("Father or mother"));
        // From second (last) page
        assertTrue(parsedString.contains("how you have determined who is the Nearest"));
    }

    @Ignore
    @Test
    public void testMassiveOCRDoc() throws Exception {
        System.out.println("testMassiveOCRDoc");
        InputStream stream = getClass().getClassLoader().getResourceAsStream("long_OCR_doc.pdf");
        AutoDetectParser parser = new AutoDetectParser(config);
        //PDFPreprocessorParser parser = new PDFPreprocessorParser();
        BodyContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();
        parser.parse(stream, body, metadata);
        assertTrue(body.toString().contains("Saliva-derived genomic DNA samples were genotyped using"));
    }

    @Test
    public void testEncryptedPDFDoc() throws Exception {
        System.out.println("testEncryptedPDFDoc");
        InputStream stream = getClass().getClassLoader().getResourceAsStream("pdf_encrypted_test.pdf");
        AutoDetectParser parser = new AutoDetectParser(config);
        //PDFPreprocessorParser parser = new PDFPreprocessorParser();
        BodyContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try {
            parser.parse(stream, body, metadata);
        } catch (Exception ex) {
            //donowt
        }
        assertFalse(body.toString().contains("PDF Encrypted"));
    }
    
    @Test
    public void testEncryptedWordDoc() throws Exception {
        System.out.println("testEncryptedWordDoc");
        InputStream stream = getClass().getClassLoader().getResourceAsStream("encryptedWordDocx.docx");
        AutoDetectParser parser = new AutoDetectParser(config);
        //PDFPreprocessorParser parser = new PDFPreprocessorParser();
        BodyContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try {
            parser.parse(stream, body, metadata);
        } catch (Exception ex) {
            //donowt
        }
        assertFalse(body.toString().contains("Word doc Encrypted"));
    }    

    @Test
    public void testParseRequiringNotRequiringOCR() throws Exception {
        System.out.println("parse");
        InputStream stream = getClass().getClassLoader().getResourceAsStream("tika/testdocs/pdf_nonOCR_test.pdf");
        AutoDetectParser parser = new AutoDetectParser(config);
        //AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler body = new BodyContentHandler();
        Metadata metadata = new Metadata();
        try {
            parser.parse(stream, body, metadata);
        } finally {
            stream.close();
        }
        assertTrue(body.toString().contains("An Example Paper"));
    }

    /**
     * Test of parse method, of class PDFPreprocessorParser.
     */
}
