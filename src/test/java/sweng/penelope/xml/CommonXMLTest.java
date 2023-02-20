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
    public void canGetSlideAmountInString() { 
        assertEquals("0", commonXML.numSlidesString());
        commonXML.numSlides++;
        
        assertEquals("1", commonXML.numSlidesString());

    }
    
}