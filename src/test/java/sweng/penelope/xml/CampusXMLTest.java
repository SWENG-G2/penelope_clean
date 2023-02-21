package sweng.penelope.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

public class CampusXMLTest {
    private static final String PRESENTATION_TITLE = "The test presentation.";
    private static final String PRESENTATION_AUTHOR = "Batman";
    private static final Long ITEM_ID = 69L;
    private static final String TEST_BIRD_NAME = "Buff bird";
    private static final String TEST_BIRD_DESCRIPTION = "Feed me";
    private static final Long TEST_BIRD_ID = 420L;
    private static final String TEST_BIRD_IMAGE_URL = "ChadBird.png";

    XMLConfiguration xmlConfiguration = new XMLConfiguration(PRESENTATION_AUTHOR, PRESENTATION_TITLE, ITEM_ID);

    @Test
    public void canFormatDescription() {
        String longDescription = "This is a really long description for a bird that is more than 50 characters";
        String correctDescription = "This is a really long description for a bird that ...";
        String actualDescription = new CampusXML(xmlConfiguration).formatDescription(longDescription);

        assertEquals(correctDescription, actualDescription);

    }

    @Test
    public void canAddBirdToCampusXml() throws Exception {
        CampusXML campusXML = new CampusXML(xmlConfiguration);
        campusXML.addBird(TEST_BIRD_NAME, TEST_BIRD_DESCRIPTION, TEST_BIRD_ID, TEST_BIRD_IMAGE_URL);

        byte[] test_byte = campusXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);

        Element presentation = document.getRootElement();
        Element slide = presentation.element("slide");

        assertNotEquals(null, slide);
        assertEquals(Long.toString(TEST_BIRD_ID), slide.attributeValue("title"));

        Element title = slide.element("text");
        assertNotEquals(null, title);
        assertEquals(TEST_BIRD_NAME, title.getText());

        // retrieve children of "slide" that are "text"
        List<Element> elements = slide.elements("text");
        assertEquals(2, elements.size());

        // assign second "text" element to "description"
        Element description = elements.get(1);
        assertNotEquals(null, description);
        assertEquals(campusXML.formatDescription(TEST_BIRD_DESCRIPTION), description.getText());

        Element image = slide.element("image");
        assertNotEquals(null, image);
        assertEquals(TEST_BIRD_IMAGE_URL, image.attributeValue("url"));

    }

}
