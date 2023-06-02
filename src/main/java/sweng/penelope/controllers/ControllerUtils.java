package sweng.penelope.controllers;

import org.springframework.security.core.Authentication;

public class ControllerUtils {
    private ControllerUtils() {
        throw new IllegalStateException("ControllerUtils is a utility class.");
    }

    /**
     * Retrieves author's human friendly name.
     * 
     * @param authentication {@link Authentication} autowired.
     * @return {@link String} the author's email.
     */
    public static final String getAuthorName(Authentication authentication) {
        String email = authentication.getPrincipal().toString().split("=")[0];

        return email;
    }
}
