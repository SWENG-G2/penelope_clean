package sweng.penelope.auth;

import java.security.KeyPair;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class UserFilter extends AbstractPreAuthenticatedProcessingFilter {
    private final String header;
    private final KeyPair serverKeyPair;

    public UserFilter(AuthenticationManager authenticationManager, KeyPair serverKeyPair, String header) {
        super.setAuthenticationManager(authenticationManager);
        this.serverKeyPair = serverKeyPair;

        this.header = header;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String authHeader = request.getHeader(header);

        try {
            return RSAUtils
                    .decrypt(serverKeyPair.getPrivate(), authHeader);
        } catch (Exception e) {
            if (authHeader != null)
                e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String claim = "";

        if (requestURI.contains("birds") || requestURI.contains("file")) {
            // Always /api/{campusId}/birds/*
            // So by splitting campusId is always at index 3 (index 0 is empty)
            claim = requestURI.split("/")[3];
        } else if (requestURI.contains("campus") || requestURI.contains("users")) {
            claim = "admin";
        }

        return claim;
    }

}
