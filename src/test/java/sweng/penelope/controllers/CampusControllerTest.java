package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

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
    
    private static final String baseAddress = "/api/campus/";
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
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setOwnerName("testing user");
        apiKey.setIdentity(USER_IDENTITY);

        apiKeyRepository.save(apiKey);

        // Identity is not privileged; Credentials mismatch.
        MockHttpServletRequestBuilder request = post(baseAddress + "new")
        .header("IDENTITY", USER_IDENTITY)
        .header("KEY", key)
        .param("name", "test campus");

        mockMvc.perform(request)
                .andExpectAll(status().isForbidden());

    }

    // name /= "", needs fixing
    // @Test
    // public void cannotCreateIfParametersInvalid() throws Exception {
    //     // Get admin auth key
    //     String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);

    //     // Mock loading key
    //     Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

    //     MockHttpServletRequestBuilder request = post(baseAddress + "new")
    //     .header("IDENTITY", IDENTITY)
    //     .header("KEY", key)
    //     .param("name", "");

    //     mockMvc.perform(request)
    //             .andExpect(status().is4xxClientError());

    // }

    @Test
    public void canCreateIfEverythingValid() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);

        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        MockHttpServletRequestBuilder request = post(baseAddress + "new")
        .header("IDENTITY", IDENTITY)
        .header("KEY", key)
        .param("name", "test campus");

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void cannotDeleteIfWrongId() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Mock deleting key
        Mockito.doReturn(true).when(storageService).removeKey(IDENTITY);
        
        // Insert fake campus
        Campus campus = new Campus();
        campus.setName("test campus");
        campus.setAuthor(IDENTITY);
        campusRepository.save(campus);
        
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);

        MockHttpServletRequestBuilder request = delete(baseAddress + "remove")
        .header("IDENTITY", IDENTITY)
        .header("KEY", key)
        .param("id", "42069");

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void canDeleteIfCorrectId() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Mock deleting key
        Mockito.doReturn(true).when(storageService).removeKey(IDENTITY);
        
        // Insert fake campus
        Campus campus = new Campus();
        campus.setName("test campus");
        campus.setAuthor(IDENTITY);
        campusRepository.save(campus);
        
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);

        MockHttpServletRequestBuilder request = delete(baseAddress + "remove")
        .header("IDENTITY", IDENTITY)
        .header("KEY", key)
        .param("id", Long.toString(campus.getId()));

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

}
