package sweng.penelope.xml;

import java.util.Optional;
import java.util.Set;

import org.dom4j.Element;
import org.springframework.lang.Nullable;

import sweng.penelope.entities.Campus;

public class UsersListXML extends CommonXML {

    /**
     * <code>UsersListXML</code> constructor.
     * 
     * @param xmlConfiguration {@link XMLConfiguration} with the required xml info.
     */
    public UsersListXML(XMLConfiguration xmlConfiguration) {
        super(xmlConfiguration);
    }

    /**
     * Adds a user to the users list xml.
     * 
     * @param username The user' username.
     * @param campuses A set of campuses the user has access to.
     */
    public void addUser(String username, @Nullable Set<Campus> campuses) {
        Element userSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                .addAttribute(HEIGHT, "120")
                .addAttribute("title", username);

        // Campuses permissions
        String campusesList;
        if (campuses != null) {
            StringBuilder campusesSB = new StringBuilder();

            campuses.forEach(campus -> campusesSB.append(String.format("%d,", campus.getId())));

            campusesList = Optional.ofNullable(campusesSB.toString())
                    .filter(s -> s.length() > 0)
                    .map(s -> s.substring(0, s.length() - 1)).orElse("");
        } else
            campusesList = "All";

        userSlide.addElement("text").addAttribute(FONT_NAME, FONT).addAttribute(FONT_SIZE, FONT_SIZE_TITLE)
                .addAttribute(COLOUR, BLACK).addAttribute(X_COORDINATE, "100")
                .addAttribute(Y_COORDINATE, "45").addAttribute(WIDTH, MATCH_PARENT).addAttribute(HEIGHT, WRAP_CONTENT)
                .addText(campusesList);

        incrementNumSlides();
    }
}
