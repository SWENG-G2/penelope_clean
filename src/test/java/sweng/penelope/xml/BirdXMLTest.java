package sweng.penelope.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

class BirdXMLTest {
    private static final String PRESENTATION_TITLE = "The test presentation.";
    private static final String PRESENTATION_AUTHOR = "Calypso";
    private static final String TEST_CAMPUS_NAME = "FortKnox";
    private static final String TEST_IMAGE_URL = "ChadBird.png";
    private static final String TEST_AUDIO_URL = "Bruh.mp3";
    private static final String TEST_ABOUT_ME = "I love gym";
    private static final String TEST_ABOUT_ME_VIDEO_URL = "AboutMeVideoLink.url";
    private static final String TEST_DIET = "I love protein";
    private static final String TEST_DIET_IMAGE_URL = "DietImageLink.url";
    private static final String TEST_LOCATION =  "Basement";
    private static final String TEST_LOCATION_IMAGE_URL = "MyLocationLink.url";
    private static final Long ITEM_ID = 69L;

    private static final XMLConfiguration xmlConfiguration = new XMLConfiguration(PRESENTATION_AUTHOR, PRESENTATION_TITLE, ITEM_ID);

    private BirdXML slideDuckXML() {
        BirdXML birdXML = null;
        try {
            birdXML = new BirdXML(xmlConfiguration);
            birdXML.addHeroSlide("audioURL", "imageURL");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return birdXML;
    }

    @Test
    void canCreateXML() {
        BirdXML birdXML = slideDuckXML();

        assertNotEquals(null, birdXML);
    }

    @Test
    public void heroSlideIsCorrect() throws Exception {
        BirdXML birdXML = new BirdXML(xmlConfiguration);
        birdXML.addHeroSlide(TEST_AUDIO_URL, TEST_IMAGE_URL);

        byte[] test_byte = birdXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);

        Element presentation = document.getRootElement();
        Element slide = presentation.element("slide");
        Element image = slide.element("image");
        Element audio = slide.element("audio");

        if (slide == null || image == null || audio == null) {
            fail("Parameters are null");
        }
        assertEquals("heroSlide", slide.attributeValue("title"));
        assertEquals(TEST_IMAGE_URL, image.attributeValue("url"));
        assertEquals(TEST_AUDIO_URL, audio.attributeValue("url"));

    }

    @Test
    public void aboutMeIsCorrect() throws Exception {
        BirdXML birdXML = new BirdXML(xmlConfiguration);
        birdXML.addAboutMe(TEST_ABOUT_ME_VIDEO_URL, TEST_ABOUT_ME);

        byte[] test_byte = birdXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);

        Element presentation = document.getRootElement();
        Element slide = presentation.element("slide");
        Element video = slide.element("video");
        Element text = slide.element("text");

        if (slide == null || video == null || text == null) {
            fail("Parameters are null");
        }

        assertEquals("About me", slide.attributeValue("title"));
        assertEquals("AboutMeVideoLink.url", video.attributeValue("url"));
        assertEquals("I love gym", text.getText());

    }

}