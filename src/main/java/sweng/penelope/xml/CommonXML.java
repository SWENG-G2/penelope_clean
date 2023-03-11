package sweng.penelope.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * <code>CommonXML</code> is a class that provides and handles common aspects to
 * all xml classes.
 */
public class CommonXML {
    protected Document document;
    protected Element presentation;
    protected Element info;
    protected int numSlides = 0;

    // XML Attributes
    protected static final String Y_COORDINATE = "yCoordinate";
    protected static final String X_COORDINATE = "xCoordinate";
    protected static final String COLOUR = "colour";
    protected static final String WIDTH = "width";
    protected static final String HEIGHT = "height";
    protected static final String FONT_NAME = "fontName";
    protected static final String FONT_SIZE = "fontSize";
    protected static final String FROM_X = "fromX";
    protected static final String FROM_Y = "fromY";
    protected static final String TO_X = "toX";
    protected static final String TO_Y = "toY";
    protected static final String BORDER_WIDTH = "borderWidth";
    protected static final String BORDER_COLOUR = "borderColour";

    // Values
    protected static final String SLIDE_WIDTH = "1920";
    protected static final String DARK_GRAY = "#8A8178FF";
    protected static final String LIGHT_GRAY = "#DFEBEBFF";
    protected static final String BLACK = "#000000FF";
    protected static final String TRANSPARENT = "#00000000";
    protected static final String FONT_SIZE_TITLE = "32";
    protected static final String FONT_SIZE_TITLE_MD = "28";
    protected static final String FONT_SIZE_TITLE_SM = "22";
    protected static final String FONT_SIZE_BODY = "18";
    protected static final String FONT = "mono";
    protected static final String MATCH_WIDTH_CLIENT_SIDE = "-1";
    protected static final String MATCH_X_CLIENT_SIDE = "-1";
    protected static final String PAD_CLIENT_SIDE = "-";
    protected static final String WRAP_CONTENT_CLIENT_SIDE = "-1";
    protected static final String CENTER_IN_PARENT = "-2";
    protected static final String END_OF_PARENT = "-3";
    protected static final String MATCH_PARENT = "-4";
    protected static final String WRAP_CONTENT = "-5";

    private XMLConfiguration xmlConfiguration;

    /**
     * <code>CommonXML constructor</code>
     * 
     * @param xmlConfiguration {@link XMLConfiguration} with the required xml info.
     */
    protected CommonXML(XMLConfiguration xmlConfiguration) {
        this.xmlConfiguration = xmlConfiguration;

        createDocument();
    }

    /**
     * Converts the number of slides counter to a string.
     * 
     * @return String representation of the counter value.
     */
    protected String numSlidesString() {
        return Integer.toString(numSlides);
    }

    /**
     * Creates the xml document via Dom4J utilities.
     */
    private void createDocument() {
        document = DocumentHelper.createDocument();
        // Add namespace
        presentation = document.addElement("presentation", "urn:SWENG").addNamespace("SWENG",
                "https://raw.githubusercontent.com/SWENG-G2/xml_standard/proposal-1/standard.xsd");

        // Presentation info
        info = presentation.addElement("info");

        // Title
        info.addElement("title").addText(xmlConfiguration.getTitle());
        // Author
        info.addElement("author").addText(xmlConfiguration.getAuthor());
        // Date
        info.addElement("date").addText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        // numSlides
        info.addElement("numSlides").addText(numSlidesString());
    }

    /**
     * Converts the current document to a byte array.
     * 
     * @return byte array representation of the document's content.
     */
    public byte[] getBytes() {
        // Human-friendly formatted xml.
        OutputFormat format = OutputFormat.createPrettyPrint();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLWriter xmlWriter;
        try {
            xmlWriter = new XMLWriter(byteArrayOutputStream, format);
            xmlWriter.write(document);
            xmlWriter.close();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Increments the number of slides counter.
     */
    protected void incrementNumSlides() {
        numSlides++;

        info.element("numSlides").setText(numSlidesString());
    }
}
