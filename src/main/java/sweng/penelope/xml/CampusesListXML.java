package sweng.penelope.xml;

import org.dom4j.Element;

/**
 * <code>CampusesListXML</code> handles campuses list xml creation.
 */
public class CampusesListXML extends CommonXML {

    /**
     * <code>CampusesListXML</code> constructor.
     * 
     * @param xmlConfiguration {@link XMLConfiguration} with the required xml info.
     */
    public CampusesListXML(XMLConfiguration xmlConfiguration) {
        super(xmlConfiguration);
    }

    /**
     * Adds a campus to the campuses list xml.
     * 
     * @param name The campus' name.
     * @param id   The campus's id.
     */
    public void addCampus(String name, Long id) {
        Element campusSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                .addAttribute(HEIGHT, "120")
                .addAttribute("title", Long.toString(id));

        // Campus Name
        campusSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_TITLE)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "100")
                .addAttribute(Y_COORDINATE, "45").addAttribute(WIDTH, MATCH_PARENT).addAttribute(HEIGHT, WRAP_CONTENT)
                .addText(name);

        // Bottom border
        campusSlide.addElement("line").addAttribute("thickness", "5")
                .addAttribute(FROM_X, "100").addAttribute(FROM_Y, "120").addAttribute(TO_X, "1820")
                .addAttribute(TO_Y, "120")
                .addAttribute(COLOUR, DARK_GRAY);

        incrementNumSlides();
    }
}
