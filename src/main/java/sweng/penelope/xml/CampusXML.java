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
    public String formatDescription(String description) {
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
                .addAttribute(HEIGHT, PAD_CLIENT_SIDE + "5")
                .addAttribute("title", Long.toString(id));

        // Title
        duckSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_TITLE_SM)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "520") // 480 + 40
                .addAttribute(Y_COORDINATE, "0").addAttribute(WIDTH, MATCH_PARENT).addAttribute(HEIGHT, WRAP_CONTENT)
                .addText(name);

        // Description
        duckSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_BODY)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "520").addAttribute(WIDTH, "1400")
                .addAttribute(HEIGHT, WRAP_CONTENT)
                .addAttribute(Y_COORDINATE, "30")
                .addText(formatDescription(description));

        // Image
        duckSlide.addElement("image").addAttribute("url", formatResourceUrl(imageURL)).addAttribute(WIDTH, "480") // 1920 / 4
                .addAttribute(HEIGHT, MATCH_WIDTH_CLIENT_SIDE).addAttribute(X_COORDINATE, "0")
                .addAttribute(Y_COORDINATE, "0");

        incrementNumSlides();
    }
}
