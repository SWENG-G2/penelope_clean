package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.ApiKeyRepository;
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
    public void setUpAdminIdentity() {
        ApiKey apiKey = new ApiKey();

        apiKey.setAdmin(true);
        apiKey.setIdentity(IDENTITY);
        apiKey.setOwnerName("The testing admin");

        apiKeyRepository.save(apiKey);

        Campus campus = new Campus();
        campus.setName("test campus");
        campus.setAuthor(IDENTITY);
        campus.setId(5345L);
        testCampusId = campus.getId();

        campusRepository.save(campus);
    }

    @AfterEach
    public void cleanUp() {
        // Clean all
        apiKeyRepository.deleteAll();
        campusRepository.deleteAll();
    }

    @Test
    public void cannotCreateIfCampusNotThere() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 42069);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

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

        MockHttpServletRequestBuilder request = post(formatAddress("new", "42069")).header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void canCreateIfCampusIsThere() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 5345);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

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

        MockHttpServletRequestBuilder request = post(formatAddress("new", Long.toString(testCampusId, 10))).header("IDENTITY", IDENTITY)
                .header("KEY", key).params(parameters);

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }



}
