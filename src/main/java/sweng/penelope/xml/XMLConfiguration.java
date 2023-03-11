package sweng.penelope.xml;

import lombok.Getter;
import lombok.Setter;

/**
 * <code>XMLConfiguration</code> is a data class for presentation basic info.
 */
@Getter
@Setter
public class XMLConfiguration {
    private String author;
    private String title;
    private Long itemId;

    /**
     * <code>XMLConfiguration</code> constructor.
     * 
     * @param author The presentation author.
     * @param title  The presentation title.
     * @param itemId The ID of the resource associated with the presentation.
     */
    public XMLConfiguration(String author, String title, Long itemId) {
        this.author = author;
        this.title = title;
        this.itemId = itemId;
    }
}
