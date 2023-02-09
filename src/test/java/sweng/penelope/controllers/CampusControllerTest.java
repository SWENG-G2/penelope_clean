package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
public class CampusControllerTest {
    
    private static final String baseAddress = "/api/birds/%s/";
    private static final String IDENTITY = "admin";
    private static final String USER_IDENTITY = "user";
    private static PublicKey mockAdminPublicKey;
    private static PrivateKey mockAdminPrivateKey;

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
    }

    @AfterEach
    public void cleanUp() {
        // Clean all
        apiKeyRepository.deleteAll();
        campusRepository.deleteAll();
    }

    @Test
    public void cannotCreateIfNotAdmin() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 1);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setOwnerName("testing user");
        apiKey.setIdentity(USER_IDENTITY);

        apiKeyRepository.save(apiKey);

        // Identity is not privileged; Credentials mismatch.
        MockHttpServletRequestBuilder request = post(formatAddress("new", String.valueOf(1)))
        .header("IDENTITY", USER_IDENTITY)
        .header("KEY", key)
        .param("name", "test campus");

        // CLIENT ERROR 400, NOT 200 if IDENTITY = admin ???
        mockMvc.perform(request)
                .andExpectAll(status().isForbidden());

    }

    @Test
    public void cannotCreateIfInvalidParameters() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 1);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        MockHttpServletRequestBuilder request = post(formatAddress("new", String.valueOf(1)))
        .header("IDENTITY", IDENTITY)
        .header("KEY", key)
        .param("name", "");

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());

    }










}