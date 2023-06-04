package sweng.penelope.auth;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.security.KeyPair;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class UserFilterTest {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private KeyPair keyPair;

    @Value("${penelope.api-credentialsHeader}")
    private String credentialsHeader;

    @Test
    public void principalIsNullForBadHeader() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setMethod("POST");

        // Bad header value
        mockHttpServletRequest.addHeader(credentialsHeader, "Chocolate chocolate chip");
        UserFilter classUnderTest = new UserFilter(authenticationManager, keyPair, credentialsHeader);
        Object principal = classUnderTest.getPreAuthenticatedPrincipal(mockHttpServletRequest);

        assertNull(principal);

        // Missing header
        mockHttpServletRequest.removeHeader(credentialsHeader);
        principal = classUnderTest.getPreAuthenticatedPrincipal(mockHttpServletRequest);

        assertNull(principal);
    }
}
