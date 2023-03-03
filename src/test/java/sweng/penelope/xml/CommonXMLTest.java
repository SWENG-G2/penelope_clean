package sweng.penelope.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

public class CommonXMLTest {
    private static final String PRESENTATION_TITLE = "The test presentation.";
    private static final String PRESENTATION_AUTHOR = "Batman";
    private static final Long ITEM_ID = 69L;

    private XMLConfiguration xmlConfiguration = new XMLConfiguration(PRESENTATION_AUTHOR, PRESENTATION_TITLE, ITEM_ID);
    private CommonXML commonXML = new CommonXML(xmlConfiguration);

    @Test
    public void canCreateDocument() {
        commonXML.createDocument();
        Document document = commonXML.document;

        assertNotEquals(null, document);
        assertEquals("presentation", document.getRootElement().getName());
        assertEquals("urn:SWENG", document.getRootElement().getNamespace().getText());

        Element info = document.getRootElement().element("info");

        assertNotEquals(null, info);
        assertEquals("title", info.element("title").getName());
        assertEquals(xmlConfiguration.getTitle(), info.elementText("title"));

        assertEquals("author", info.element("author").getName());
        assertEquals(xmlConfiguration.getAuthor(), info.elementText("author"));

        assertEquals("date", info.element("date").getName());
        assertNotEquals(null, info.elementText("date"));

        assertEquals("numSlides", info.element("numSlides").getName());
        assertEquals(commonXML.numSlidesString(), info.elementText("numSlides"));

    }

    @Test
    public void canGetBytes() throws Exception {
        commonXML.createDocument();

        byte[] test_byte = commonXML.getBytes();

        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);

        Element root = document.getRootElement();
        assertEquals("presentation", root.getName());

    }

    @Test
    public void canIncrementNumOfSlides() {
        commonXML.createDocument();

        assertEquals("0", commonXML.numSlidesString());

        commonXML.incrementNumSlides();
        assertEquals("1", commonXML.numSlidesString());

        commonXML.incrementNumSlides();
        assertEquals("2", commonXML.numSlidesString());

    }

}