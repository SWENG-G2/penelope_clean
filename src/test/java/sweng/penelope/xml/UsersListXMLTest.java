package sweng.penelope.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import sweng.penelope.entities.Campus;

public class UsersListXMLTest {
    private static final String TEST_USER_NAME = "UserTest";
    private static final String PRESENTATION_TITLE = "The test presentation.";
    private static final String PRESENTATION_AUTHOR = "Batman";
    private static final Long ITEM_ID = 69L;
    private static final Long TEST_CAMPUS_ID_1 = 420L;
    private static final Long TEST_CAMPUS_ID_2 = 421L;
    private static final String TEST_CAMPUS_ID_LIST = TEST_CAMPUS_ID_1 + "," + TEST_CAMPUS_ID_2;

    XMLConfiguration xmlConfiguration = new XMLConfiguration(PRESENTATION_AUTHOR, PRESENTATION_TITLE, ITEM_ID);

    @Test
    public void canAddUserToUsersListXml() throws Exception {
        UsersListXML usersListXML = new UsersListXML(xmlConfiguration);
        Set<Campus> campuses = new HashSet<>();

        Campus testCampus1 = new Campus();
        testCampus1.setName("Campus 1");
        testCampus1.setId(TEST_CAMPUS_ID_1);
        Campus testCampus2 = new Campus();
        testCampus2.setName("Campus 2");
        testCampus2.setId(TEST_CAMPUS_ID_2);
        
        campuses.add(testCampus1);
        campuses.add(testCampus2);

        usersListXML.addUser(TEST_USER_NAME, campuses);

        byte[] test_byte = usersListXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);
        Element presentation = document.getRootElement();

        Element slide = presentation.element("slide");
        assertNotEquals(null, slide);
        assertEquals(TEST_USER_NAME, slide.attributeValue("title"));

        Element permissions = slide.element("text");
        assertNotEquals(null, permissions);
        assertEquals(TEST_CAMPUS_ID_LIST, permissions.getText());
    }

    @Test
    public void canAddUserWithoutCampusesToUsersListXml() throws Exception {
        UsersListXML usersListXML = new UsersListXML(xmlConfiguration);

        usersListXML.addUser(TEST_USER_NAME, null);

        byte[] test_byte = usersListXML.getBytes();
        String xmlStr = new String(test_byte, StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xmlStr);
        Element presentation = document.getRootElement();

        Element slide = presentation.element("slide");
        assertNotEquals(null, slide);
        assertEquals(TEST_USER_NAME, slide.attributeValue("title"));

        Element permissions = slide.element("text");
        assertNotEquals(null, permissions);
        assertEquals("All", permissions.getText());
    }
}