package sweng.penelope.auth;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sweng.penelope.entities.ApiKey;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.ApiKeyRepository;
import sweng.penelope.services.StorageService;

/**
 * <code>ApiKeyAuthenticationManager</code> handles KEY verification.
 */
@Service
public class ApiKeyAuthenticationManager implements AuthenticationManager {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private StorageService storageService;

    /**
     * Verifies the validity of the provided KEY.
     * 
     * @param authentication {@link Authentication} autowired.
     * @param apiKey         The {@link ApiKey} to process.
     * @return {@link Authentication}
     * @throws AuthorizationServiceException
     */
    private Authentication verifyCredentials(Authentication authentication, ApiKey apiKey)
            throws AuthorizationServiceException {
        try {
            // Retrieve private key
            byte[] privateKeyBytes = storageService.loadKey(apiKey.getIdentity());

            if (privateKeyBytes.length == 0)
                throw new IOException("Key is empty");

            PrivateKey privateKey = RSAUtils.regeneratePrivateKey(privateKeyBytes);

            // Decrypt credentials
            String credentials = RSAUtils.decrypt(privateKey, authentication.getCredentials().toString());
            String[] credentialsSplit = credentials.split("=");

            // Compare principal
            if (!authentication.getPrincipal().toString().equals(credentialsSplit[0]))
                throw new BadCredentialsException("Princpal mismatch");

            // Compare timestamp
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/London"));
            ZonedDateTime sentAt = ZonedDateTime.parse(credentialsSplit[1]);
            Duration delta = Duration.between(now, sentAt);
            if (delta.getSeconds() < 60)
                authentication.setAuthenticated(true);
            else
                throw new TimeoutException("Stale request");
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("Bad Credentials");
        }
        return authentication;
    }

    /**
     * Handles authentication for a non-priviledged key.
     * 
     * @param authentication {@link Authentication} autowired.
     * @param apiKey         The {@link ApiKey} to process.
     * @param campusId       The ID of the campus the {@link ApiKey} is claiming
     *                       access to
     * @return {@link Authentication}
     */
    private Authentication handleNonAdminKey(Authentication authentication, ApiKey apiKey, String campusId) {
        if (authentication.getPrincipal().toString().contains("_admin"))
            throw new UnauthorisedException();

        for (Campus campus : apiKey.getCampuses()) {
            // Check user has access to requested campus
            if (campus.getId().toString().equals(campusId)) {
                // Proceed with key verification
                return verifyCredentials(authentication, apiKey);
            }
        }
        throw new UnauthorisedException();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null || authentication.getPrincipal() == null)
            throw new BadCredentialsException("Authentication headers are missing");

        String[] principal = authentication.getPrincipal().toString().split("_");
        if (principal.length > 1) {
            Optional<ApiKey> requestKey = apiKeyRepository.findById(principal[0]);
            
            requestKey.ifPresentOrElse(apiKey -> {
                if (Boolean.FALSE.equals(apiKey.getAdmin())) {
                    handleNonAdminKey(authentication, apiKey, principal[1]);
                } else {
                    verifyCredentials(authentication, apiKey);
                }
            }, () -> {
                throw new UsernameNotFoundException("Could not find apikey");
            });

            return authentication;
        }
        return authentication;
    }

}
