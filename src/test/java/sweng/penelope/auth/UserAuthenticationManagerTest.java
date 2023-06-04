package sweng.penelope.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import sweng.penelope.entities.Campus;
import sweng.penelope.entities.DataManager;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.repositories.DataManagerRepository;

@SpringBootTest
@ActiveProfiles("test")
public class UserAuthenticationManagerTest {
    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "pwd";
    private final String USERNAME = "user";
    private final String PASSWORD = "pwd";
    private final Authentication authentication = Mockito.mock(Authentication.class);
    private final String ADMIN_PRINCIPAL_NO_TIMESTAMP = ADMIN_USERNAME + "=" + ADMIN_PASSWORD + "=";
    private final String USER_PRINCIPAL_NO_TIMESTAMP = USERNAME + "=" + PASSWORD + "=";
    private final String NOW_TIMESTAMP = ZonedDateTime.now(ZoneId.of("Europe/London")).toString();
    private final String ADMIN_CLAIM = "admin";
    private boolean authResult;

    @Autowired
    private DataManagerRepository dataManagerRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAuthenticationManager classUnderTest;

    @BeforeEach
    public void authorisedLatch() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object arg = invocation.getArgument(0);
                if (arg instanceof Boolean) {
                    authResult = (boolean) arg;
                } else
                    fail();
                return null;
            }
        }).when(authentication).setAuthenticated(anyBoolean());
    }

    @Test
    public void rejectsStaleRequest() {
        // Time stamps 10 seconds past stale threshold
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))
                .minusSeconds((long) (UserAuthenticationManager.STALE_TIMEOUT_SECONDS + 10));

        String principal = ADMIN_PRINCIPAL_NO_TIMESTAMP + zonedDateTime.toString();

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getCredentials()).thenReturn(ADMIN_CLAIM);

        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void rejectsPasswordMismatch() {
        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(ADMIN_USERNAME);
        dataManager.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        dataManager.setSysadmin(true);
        dataManagerRepository.save(dataManager);

        String principal = ADMIN_USERNAME + "=" + "wrong_password" + "=" + NOW_TIMESTAMP;

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getCredentials()).thenReturn(ADMIN_CLAIM);

        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void rejectsOnMissingCampus() {
        // Inject campus
        Campus campus = new Campus();
        campus.setName("Some name");
        campus.setAuthor(ADMIN_USERNAME);
        campus = campusRepository.save(campus);

        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(USERNAME);
        dataManager.setPassword(passwordEncoder.encode(PASSWORD));
        dataManager.setSysadmin(false);
        Set<Campus> campuses = new HashSet<Campus>();
        campuses.add(campus);
        dataManager.setCampuses(campuses);
        dataManagerRepository.save(dataManager);

        long badId = campus.getId() + 1;

        when(authentication.getPrincipal()).thenReturn(USER_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(Long.toString(badId));

        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void rejectsOnMMissingRights() {
        // Inject campus
        Campus campus = new Campus();
        campus.setName("Some name");
        campus.setAuthor(ADMIN_USERNAME);
        campus = campusRepository.save(campus);

        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(USERNAME);
        dataManager.setPassword(passwordEncoder.encode(PASSWORD));
        dataManager.setSysadmin(false);
        dataManagerRepository.save(dataManager);

        long campusId = campus.getId();

        when(authentication.getPrincipal()).thenReturn(USER_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(Long.toString(campusId));

        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void rejectsAdminClaimFromNonAdmin() {
        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(USERNAME);
        dataManager.setPassword(passwordEncoder.encode(PASSWORD));
        dataManager.setSysadmin(false);
        dataManagerRepository.save(dataManager);

        String principal = USER_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP;

        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getCredentials()).thenReturn(ADMIN_CLAIM);

        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void rejectsMissingUser() {
        // Inject campus
        Campus campus = new Campus();
        campus.setName("Some name");
        campus.setAuthor(ADMIN_USERNAME);
        campus = campusRepository.save(campus);

        long campusId = campus.getId();

        // Normal user
        when(authentication.getPrincipal()).thenReturn(USER_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(Long.toString(campusId));
        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);

        // Admin
        when(authentication.getPrincipal()).thenReturn(ADMIN_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(ADMIN_CLAIM);
        assertThrows(ResponseStatusException.class, () -> {
            classUnderTest.authenticate(authentication);
        });
        assertFalse(authResult);
    }

    @Test
    public void successForNonAdmin() {
        // Inject campus
        Campus campus = new Campus();
        campus.setName("Some name");
        campus.setAuthor(ADMIN_USERNAME);
        campus = campusRepository.save(campus);

        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(USERNAME);
        dataManager.setPassword(passwordEncoder.encode(PASSWORD));
        dataManager.setSysadmin(false);
        Set<Campus> campuses = new HashSet<Campus>();
        campuses.add(campus);
        dataManager.setCampuses(campuses);
        dataManagerRepository.save(dataManager);

        long campusId = campus.getId();

        when(authentication.getPrincipal()).thenReturn(USER_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(Long.toString(campusId));

        classUnderTest.authenticate(authentication);
        assertTrue(authResult);
    }

    @Test
    public void successForAdmin() {
        // Inject user
        DataManager dataManager = new DataManager();
        dataManager.setUsername(ADMIN_USERNAME);
        dataManager.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        dataManager.setSysadmin(true);
        dataManagerRepository.save(dataManager);

        when(authentication.getPrincipal()).thenReturn(ADMIN_PRINCIPAL_NO_TIMESTAMP + NOW_TIMESTAMP);
        when(authentication.getCredentials()).thenReturn(ADMIN_CLAIM);

        classUnderTest.authenticate(authentication);
        assertTrue(authResult);
    }
}
