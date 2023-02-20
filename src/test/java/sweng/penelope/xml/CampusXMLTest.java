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
    
}