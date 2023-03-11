package sweng.penelope.xml;

import org.dom4j.Element;

/**
 * <code>CampusXML</code> handles campus xml creation.
 */
public class CampusXML extends CommonXML {

    /**
     * <code>CampusXML</code> constructor.
     * 
     * @param xmlConfiguration {@link XMLConfiguration} with the required xml info.
     */
    public CampusXML(XMLConfiguration xmlConfiguration) {
        super(xmlConfiguration);
    }

    /**
     * Clips the provided bird description to 50 characters.
     * 
     * @param description The text description
     * @return The clipped text description.
     */
    private String formatDescription(String description) {
        // Display max 50 chars
        String formattedDescription = description.substring(0, Math.min(description.length(), 50));

        if (description.length() > 50)
            formattedDescription += "...";

        return formattedDescription;
    }

    /**
     * Adds a bird to the campus xml.
     * 
     * @param name        The bird's name.
     * @param description The bird's description (about me section).
     * @param id          The bird's id.
     * @param imageURL    The bird's list image url.
     */
    public void addBird(String name, String description, Long id, String imageURL) {
        Element duckSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                .addAttribute(HEIGHT, "200")
                .addAttribute("title", Long.toString(id));

        // Title
        duckSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_TITLE_SM)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "560") // (480 + 40 (image)) + 40
                .addAttribute(Y_COORDINATE, "30").addAttribute(WIDTH, MATCH_PARENT).addAttribute(HEIGHT, WRAP_CONTENT)
                .addText(name);

        // Description
        duckSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_BODY)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "560").addAttribute(WIDTH, "1300")
                .addAttribute(HEIGHT, WRAP_CONTENT)
                .addAttribute(Y_COORDINATE, "68") // 28 (FONT_SIZE_TITLE_SM) + 2*20 (FONT_SIZE_BODY)
                .addText(formatDescription(description));

        // Image
        duckSlide.addElement("image").addAttribute("url", imageURL).addAttribute(WIDTH, "480") // 480 = 100 * (1920/200)
                                                                                               // * (100/200)
                .addAttribute(HEIGHT, "100").addAttribute(X_COORDINATE, "40")
                .addAttribute(Y_COORDINATE, "40");

        incrementNumSlides();
    }
}
