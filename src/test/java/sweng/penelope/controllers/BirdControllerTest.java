package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.ApiKey;
import sweng.penelope.entities.Bird;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.ApiKeyRepository;
import sweng.penelope.repositories.BirdRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BirdControllerTest {

    private static final String baseAddress = "/api/birds/%s/";
    private static final String IDENTITY = "admin";
    private static final String USER_IDENTITY = "user";
    private static PublicKey mockAdminPublicKey;
    private static PrivateKey mockAdminPrivateKey;
    private Long testCampusId;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private CampusRepository campusRepository;

    @Autowired
    private BirdRepository birdRepository;

    @MockBean
    private StorageService storageService;

    private static String formatAddress(String endPoint, String campusId) {
        return String.format(Locale.getDefault(), baseAddress, campusId) + endPoint;
    }

    @BeforeAll
    public static void setUpKeys() throws NoSuchAlgorithmException {
        KeyPair keys = RSAUtils.generateKeys();
        mockAdminPrivateKey = keys.getPrivate();
        mockAdminPublicKey = keys.getPublic();
    }

    @BeforeEach
    public void setUpAdminAndCampus() {
        ApiKey apiKey = new ApiKey();

        apiKey.setAdmin(true);
        apiKey.setIdentity(IDENTITY);
        apiKey.setOwnerName("The testing admin");

        apiKeyRepository.save(apiKey);

        Campus campus = new Campus();
        campus.setName("test campus");
        campus.setAuthor(IDENTITY);
        campusRepository.save(campus);
        
        testCampusId = campus.getId();
    }
    
    @AfterEach
    public void cleanUp() {
        // Clean all
        apiKeyRepository.deleteAll();
        campusRepository.deleteAll();
        birdRepository.deleteAll();
    }

    @Test
    public void cannotCreateIfCampusNotThere() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 42069);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

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
                .header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void cannotCreateIfNoAccessToCampus() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, Math.toIntExact(testCampusId));

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setIdentity(USER_IDENTITY);
        apiKey.setOwnerName("testing user");
        apiKeyRepository.save(apiKey);

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

        // Identity is not privileged; Credentials mismatch.
        MockHttpServletRequestBuilder request = post(formatAddress("new", Long.toString(testCampusId, 10)))
                .header("IDENTITY", USER_IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

    }

    @Test
    public void cannotCreateIfParametersMissing() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, Math.toIntExact(testCampusId));

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("name", null);
        parameters.add("listImageURL", "");
        parameters.add("heroImageURL", "");
        parameters.add("soundURL", "");
        parameters.add("aboutMe", "");
        parameters.add("aboutMeVideoURL", "");
        parameters.add("location", "");
        parameters.add("locationImageURL", "");
        parameters.add("diet", "");
        parameters.add("dietImageURL", "");

        MockHttpServletRequestBuilder request = post(formatAddress("new", Long.toString(testCampusId, 10)))
                .header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }

    @Test
    public void canCreateIfEverythingValid() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, Math.toIntExact(testCampusId));

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

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

        MockHttpServletRequestBuilder request = post(formatAddress("new", Long.toString(testCampusId, 10)))
                .header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void canRemoveBirdWithCredentials() throws Exception{
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Mock deleting key
        Mockito.doReturn(true).when(storageService).removeKey(IDENTITY);

        // Insert fake campus
        Campus temp_campus = new Campus();
        temp_campus.setName("test campus");
        temp_campus.setAuthor(IDENTITY);
        campusRepository.save(temp_campus);

        testCampusId = temp_campus.getId();

        // Insert fake bird
        Bird bird = new Bird();
        bird.setAuthor(IDENTITY);
        bird.setCampus(temp_campus);
        birdRepository.save(bird);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("id", Long.toString(bird.getId(), 10));

        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, Math.toIntExact(testCampusId));

        MockHttpServletRequestBuilder request = delete(formatAddress("remove", Long.toString(testCampusId, 10)))
                .header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void canEditBirdWithCredentials() throws Exception{
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake campus
        Campus temp_campus = new Campus();
        temp_campus.setName("test campus");
        temp_campus.setAuthor(IDENTITY);
        campusRepository.save(temp_campus);

        testCampusId = temp_campus.getId();

        // Insert fake bird
        Bird bird = new Bird();
        bird.setAuthor(IDENTITY);
        bird.setCampus(temp_campus);
        bird.setName("Boris");
        bird = birdRepository.save(bird);

        // Bird parameters
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("id", Long.toString(bird.getId(), 10));
        parameters.set("name", "test name");

        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, Math.toIntExact(testCampusId));

        MockHttpServletRequestBuilder request = patch(formatAddress("edit", Long.toString(testCampusId, 10)))
                .header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isOk());
                
        // Callback function
        // If mbird present, do first lambda {}
        // else mbird is not present, do second lambda {}
        birdRepository.findById(bird.getId()).ifPresentOrElse(mbird -> {assertEquals("test name", mbird.getName());}, () -> {fail();});

    }

}
