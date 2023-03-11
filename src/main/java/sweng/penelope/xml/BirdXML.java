package sweng.penelope.xml;

import org.dom4j.Element;

/**
 * <code>BirdXML</code> handles bird xml creation.
 */
public class BirdXML extends CommonXML {
        private static final String HERO_SLIDE_HEIGHT = "485";
        private static final String HERO_IMAGE_CIRCLE_RADIUS = "175";
        private static final String HERO_IMAGE_WIDTH = "1700";
        private static final String TEXT_WIDTH = "1880";

        // Attributes
        private static final String URL = "url";

        private String birdName;

        /**
         * <code>BirdXML</code> constructor.
         * 
         * @param xmlConfiguration {@link XMLConfiguration} with the required xml info.
         */
        public BirdXML(XMLConfiguration xmlConfiguration) {
                super(xmlConfiguration);

                birdName = xmlConfiguration.getTitle();
        }

        /**
         * Adds the hero slide to the bird xml.
         * 
         * @param audioURL URL to the bird sound resource.
         * @param imageURL URL to the bird hero image resource.
         */
        public void addHeroSlide(String audioURL, String imageURL) {
                // Create slide
                Element heroSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                                .addAttribute(HEIGHT, HERO_SLIDE_HEIGHT)
                                .addAttribute("title", "heroSlide");
                // Add Title container
                heroSlide.addElement("rectangle").addAttribute(WIDTH, SLIDE_WIDTH).addAttribute(HEIGHT, "100")
                                .addAttribute(X_COORDINATE, "0").addAttribute(Y_COORDINATE, "0")
                                .addAttribute(COLOUR, "#E89266FF"); // Hero title container colour
                // Title text
                heroSlide.addElement("text").addAttribute(X_COORDINATE, "20").addAttribute(Y_COORDINATE, "25")
                                .addAttribute(COLOUR, BLACK).addAttribute(FONT_NAME, FONT)
                                .addAttribute(FONT_SIZE, FONT_SIZE_TITLE_MD).addAttribute(WIDTH, MATCH_PARENT)
                                .addAttribute(HEIGHT, WRAP_CONTENT)
                                .addText(birdName);
                // Title audio
                heroSlide.addElement("audio").addAttribute(URL, audioURL).addAttribute("loop", "false")
                                .addAttribute(X_COORDINATE, END_OF_PARENT)
                                .addAttribute(Y_COORDINATE, "0");

                // Image
                heroSlide.addElement("image").addAttribute(URL, imageURL).addAttribute(WIDTH, HERO_IMAGE_WIDTH)
                                .addAttribute(HEIGHT, "360")
                                .addAttribute(X_COORDINATE, CENTER_IN_PARENT)
                                .addAttribute(Y_COORDINATE, "115");

                // Image background shape
                heroSlide.addElement("circle").addAttribute("radius", HERO_IMAGE_CIRCLE_RADIUS)
                                .addAttribute(X_COORDINATE, CENTER_IN_PARENT)
                                .addAttribute(Y_COORDINATE, PAD_CLIENT_SIDE + "120")
                                .addAttribute(COLOUR, TRANSPARENT)
                                .addAttribute(BORDER_WIDTH, "15")
                                .addAttribute(BORDER_COLOUR, DARK_GRAY);
        }

        /**
         * Add the "about me" slide to the bird xml.
         * 
         * @param aboutMeVideoURL URL to the bird video.
         * @param aboutMe         Section text content.
         */
        public void addAboutMe(String aboutMeVideoURL, String aboutMe) {
                // Create slide
                Element aboutMeSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT_CLIENT_SIDE)
                                .addAttribute("title", "About me");

                // Add video
                aboutMeSlide.addElement("video").addAttribute(X_COORDINATE, CENTER_IN_PARENT)
                                .addAttribute(Y_COORDINATE, "0").addAttribute(WIDTH, "1820") // Slide Width - 100
                                .addAttribute(HEIGHT, "250").addAttribute("loop", "false")
                                .addAttribute(URL, aboutMeVideoURL);

                // Add description
                aboutMeSlide.addElement("text").addAttribute(X_COORDINATE, "20").addAttribute(Y_COORDINATE, "250")
                                .addAttribute(COLOUR, BLACK).addAttribute(FONT_NAME, FONT)
                                .addAttribute(FONT_SIZE, FONT_SIZE_BODY).addAttribute(WIDTH, TEXT_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT)
                                .addText(aboutMe);
        }

        /**
         * Adds the "diet" slide to the bird xml.
         * 
         * @param dietImageURL URL to the bird diet image.
         * @param diet         Section text content.
         */
        public void addDiet(String dietImageURL, String diet) {
                // Create slide
                Element dietSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT_CLIENT_SIDE)
                                .addAttribute("title", "Diet");

                // Image
                dietSlide.addElement("image").addAttribute(URL, dietImageURL).addAttribute(WIDTH, HERO_IMAGE_WIDTH)
                                .addAttribute(HEIGHT, "200")
                                .addAttribute(X_COORDINATE, CENTER_IN_PARENT)
                                .addAttribute(Y_COORDINATE, "0");

                // Add diet
                dietSlide.addElement("text").addAttribute(X_COORDINATE, "20").addAttribute(Y_COORDINATE, "210")
                                .addAttribute(COLOUR, BLACK).addAttribute(FONT_NAME, FONT)
                                .addAttribute(FONT_SIZE, FONT_SIZE_BODY).addAttribute(WIDTH, TEXT_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT)
                                .addText(diet);
        }

        /**
         * Adds the "location" slide to the bird xml.
         * 
         * @param locationImageURL URL to the bird location image.
         * @param location         Section text content.
         */
        public void addLocation(String locationImageURL, String location) {
                // Create slide
                Element dietSlide = presentation.addElement("slide").addAttribute(WIDTH, SLIDE_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT_CLIENT_SIDE)
                                .addAttribute("title", "Location");

                // Image
                dietSlide.addElement("image").addAttribute(URL, locationImageURL).addAttribute(WIDTH, HERO_IMAGE_WIDTH)
                                .addAttribute(HEIGHT, "200")
                                .addAttribute(X_COORDINATE, CENTER_IN_PARENT)
                                .addAttribute(Y_COORDINATE, "0");

                // Add diet
                dietSlide.addElement("text").addAttribute(X_COORDINATE, "20").addAttribute(Y_COORDINATE, "210")
                                .addAttribute(COLOUR, BLACK).addAttribute(FONT_NAME, FONT)
                                .addAttribute(FONT_SIZE, FONT_SIZE_BODY).addAttribute(WIDTH, TEXT_WIDTH)
                                .addAttribute(HEIGHT, WRAP_CONTENT)
                                .addText(location);
        }
}