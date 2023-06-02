package sweng.penelope.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

public class CampusesListXMLTest {
    private static final String TEST_CAMPUS_NAME = "Arizzona";
    private static final Long TEST_CAMPUS_ID = 420L;
    private static final String TEST_LINE_THICKNESS = "2";
    private static final String PRESENTATION_TITLE = "The test presentation.";
    private static final String PRESENTATION_AUTHOR = "Batman";
    private static final Long ITEM_ID = 69L;

    XMLConfiguration xmlConfiguration = new XMLConfiguration(PRESENTATION_AUTHOR, PRESENTATION_TITLE, ITEM_ID);

    @Test
    public void canAddCampusToCampusListXml() throws Exception {
        CampusesListXML campusesListXML = new CampusesListXML(xmlConfiguration);

        campusesListXML.addCampus(TEST_CAMPUS_NAME, TEST_CAMPUS_ID);

        byte[] test_byte = campusesListXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);
        Element presentation = document.getRootElement();

        Element slide = presentation.element("slide");
        assertNotEquals(null, slide);
        assertEquals(Long.toString(TEST_CAMPUS_ID), slide.attributeValue("title"));

        Element name = slide.element("text");
        assertNotEquals(null, name);
        assertEquals(TEST_CAMPUS_NAME, name.getText());

        Element line = slide.element("line");
        assertNotEquals(null, line);
        assertEquals(TEST_LINE_THICKNESS, line.attributeValue("thickness"));

    }

}
