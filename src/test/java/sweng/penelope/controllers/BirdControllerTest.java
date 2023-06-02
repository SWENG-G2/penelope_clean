package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.DataManager;
import sweng.penelope.entities.Bird;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.repositories.BirdRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BirdControllerTest {
    
    private static final String baseAddress = "/api/birds/%s/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "qwerty123456";
    private static final String TIMESTAMP = ZonedDateTime.now(ZoneId.of("Europe/London")).toString();

    private static final String BAD_NAME = "123456789123456789123";
    private String testCampusID;

    private String ENCODED_PASSWORD, CREDENTIALS, ENCRYPTED_CREDENTIALS;
    private DataManager dataManager;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataManagerRepository dataManagerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private BirdRepository birdRepository;

    @MockBean
    private StorageService storageService;

    @Value("${penelope.api-credentialsHeader}")
    private String credentialsHeader;

    private static String formatAddress(String endPoint, String campusId) {
        return String.format(Locale.getDefault(), baseAddress, campusId) + endPoint;
    }

    @BeforeEach
    public void setUpAdminCredentials() throws Exception {
        this.CREDENTIALS = USERNAME + "=" + PASSWORD + "=" + TIMESTAMP;
        this.ENCRYPTED_CREDENTIALS = RSAUtils.encrypt(keyPair.getPublic(), CREDENTIALS);
        this.ENCODED_PASSWORD = passwordEncoder.encode(PASSWORD);

        this.dataManager = new DataManager();
        this.dataManager.setUsername(USERNAME);
        this.dataManager.setPassword(ENCODED_PASSWORD);
        this.dataManager.setSysadmin(true);
        dataManagerRepository.save(dataManager);

        Campus campus = new Campus();
        campus.setName("test campus");
        campus.setAuthor(USERNAME);
        campusRepository.save(campus);

        testCampusID = campus.getId().toString();
    }

    @AfterEach
    public void cleanUp() {
        // Clean all
        dataManagerRepository.deleteAll();
        campusRepository.deleteAll();
        birdRepository.deleteAll();
    }

    @Test
    public void cannotCreateIfCampusNotThere() throws Exception {
        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", "A");
        parameters.add("listImageURL", "A");
        parameters.add("heroImageURL", "A");
        parameters.add("soundURL", "A");
        parameters.add("aboutMe", "A");
        parameters.add("aboutMeVideoURL", "A");
        parameters.add("location", "A");
        parameters.add("locationImageURL", "A");
        parameters.add("diet", "A");
        parameters.add("dietImageURL", "A");

        MockHttpServletRequestBuilder request = post(formatAddress("new", "42069"))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters)
                .secure(true);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void cannotCreateIfNoAccessToCampus() throws Exception {
        // Insert a user
        String testUsername = "User123";
        String testPassword = passwordEncoder.encode("Password123");
        DataManager testUser = new DataManager();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setSysadmin(false);
        testUser = dataManagerRepository.save(testUser);

        String testCredentials = testUsername + "=" + "Password123" + "=" + TIMESTAMP;
        String testEncryptedCredentials = RSAUtils.encrypt(keyPair.getPublic(), testCredentials);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", "A");
        parameters.add("listImageURL", "A");
        parameters.add("heroImageURL", "A");
        parameters.add("soundURL", "A");
        parameters.add("aboutMe", "A");
        parameters.add("aboutMeVideoURL", "A");
        parameters.add("location", "A");
        parameters.add("locationImageURL", "A");
        parameters.add("diet", "A");
        parameters.add("dietImageURL", "A");

        MockHttpServletRequestBuilder request = post(formatAddress("new", testCampusID))
                .header(credentialsHeader.toLowerCase(), testEncryptedCredentials)
                .params(parameters).secure(true);
        
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void cannotCreateIfParametersMissing() throws Exception {
        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", "");
        parameters.add("listImageURL", "");
        parameters.add("heroImageURL", "");
        parameters.add("soundURL", "");
        parameters.add("aboutMe", "");
        parameters.add("aboutMeVideoURL", "");
        parameters.add("location", "");
        parameters.add("locationImageURL", "");
        parameters.add("diet", "");

        // Comment to simulate missing parameter
        //parameters.add("dietImageURL", "");

        MockHttpServletRequestBuilder request = post(formatAddress("new", testCampusID))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters).secure(true);

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void cannotCreateIfNameTooLong() throws Exception {
        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", BAD_NAME);
        parameters.add("listImageURL", "A");
        parameters.add("heroImageURL", "A");
        parameters.add("soundURL", "A");
        parameters.add("aboutMe", "A");
        parameters.add("aboutMeVideoURL", "A");
        parameters.add("location", "A");
        parameters.add("locationImageURL", "A");
        parameters.add("diet", "A");
        parameters.add("dietImageURL", "A");

        MockHttpServletRequestBuilder request = post(formatAddress("new", testCampusID))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters).secure(true);

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void canCreateIfEverythingValid() throws Exception {
        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", "A");
        parameters.add("listImageURL", "A");
        parameters.add("heroImageURL", "A");
        parameters.add("soundURL", "A");
        parameters.add("aboutMe", "A");
        parameters.add("aboutMeVideoURL", "A");
        parameters.add("location", "A");
        parameters.add("locationImageURL", "A");
        parameters.add("diet", "A");
        parameters.add("dietImageURL", "A");

        MockHttpServletRequestBuilder request = post(formatAddress("new", testCampusID))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters).secure(true);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void canRemoveBirdWithCredentials() throws Exception {
        // Insert fake campus
        Campus temp_campus = new Campus();
        temp_campus.setName("test campus");
        temp_campus.setAuthor(USERNAME);
        temp_campus = campusRepository.save(temp_campus);

        testCampusID = temp_campus.getId().toString();

        // Insert fake bird
        Bird bird = new Bird();
        bird.setAuthor(USERNAME);
        bird.setCampus(temp_campus);
        bird = birdRepository.save(bird);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("id", bird.getId().toString());

        MockHttpServletRequestBuilder request = delete(formatAddress("remove", testCampusID))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters).secure(true);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void canEditBirdWithCredentials() throws Exception {
        // Insert fake campus
        Campus temp_campus = new Campus();
        temp_campus.setName("test campus");
        temp_campus.setAuthor(USERNAME);
        temp_campus = campusRepository.save(temp_campus);

        testCampusID = temp_campus.getId().toString();

        // Insert fake bird
        Bird bird = new Bird();
        bird.setAuthor(USERNAME);
        bird.setCampus(temp_campus);
        bird.setName("Boris");
        bird = birdRepository.save(bird);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("id", bird.getId().toString());
        parameters.set("name", "test name");

        MockHttpServletRequestBuilder request = patch(formatAddress("edit", testCampusID))
                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                .params(parameters).secure(true);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        // Callback function
        // If mbird present, do first lambda {}
        // else mbird is not present, do second lambda {}
        birdRepository.findById(bird.getId()).ifPresentOrElse(mbird -> {
                assertEquals("test name", mbird.getName());
        }, () -> {
                fail();
        });
    }
}
