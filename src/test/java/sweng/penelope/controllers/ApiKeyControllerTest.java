package sweng.penelope.controllers;

import static org.mockito.ArgumentMatchers.notNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.ApiKey;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.ApiKeyRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiKeyControllerTest {

    private static final String baseAddress = "/api/apikeys/";
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
    public void cannotInteractWithoutHeaders() throws Exception {
        mockMvc.perform(post(baseAddress + "new")).andExpect(status().isForbidden());
        mockMvc.perform(delete(baseAddress + "remove")).andExpect(status().isForbidden());
        mockMvc.perform(patch(baseAddress + "addCampus")).andExpect(status().isForbidden());
        mockMvc.perform(patch(baseAddress + "removeCampus")).andExpect(status().isForbidden());
    }

    @Test
    public void cannotInteractWithBadCredentials() throws Exception {
        // Get admin auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);
        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setIdentity(USER_IDENTITY);
        apiKeyRepository.save(apiKey);

        // Note: Identity is not priviledged and credentetials mismatch. Admin key and
        // User identity.
        mockMvc.perform(post(baseAddress + "new").header("IDENTITY", USER_IDENTITY).header("KEY", key))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(baseAddress + "remove").header("IDENTITY", USER_IDENTITY).header("KEY", key))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(baseAddress + "addCampus").header("IDENTITY", USER_IDENTITY).header("KEY", key))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch(baseAddress + "removeCampus").header("IDENTITY", USER_IDENTITY).header("KEY", key))
                .andExpect(status().isForbidden());
    }

    @Test
    public void canCreateWithCredentials() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);
        // Mock storing new key - Both parameters notNull as they are backend generated
        Mockito.doReturn(true).when(storageService).storeKey(notNull(), notNull());

        // Get auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);
        // Perform request
        mockMvc.perform(
                post(baseAddress + "new").header("IDENTITY", IDENTITY).header("KEY", key).param("ownerName", "Test")
                        .param("admin", "false"))
                .andExpect(status().isOk());
    }

    @Test
    public void canRemoveWithCredentials() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);
        // Mock deleting key
        Mockito.doReturn(true).when(storageService).removeKey(USER_IDENTITY);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setIdentity(USER_IDENTITY);
        apiKeyRepository.save(apiKey);

        // Get auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);
        // Perform request
        mockMvc.perform(
                delete(baseAddress + "remove").header("IDENTITY", IDENTITY).header("KEY", key).param("identity",
                        USER_IDENTITY))
                .andExpect(status().isOk());
    }

    @Test
    public void canAddCampusPermissionsWithCredentials() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setIdentity(USER_IDENTITY);
        apiKeyRepository.save(apiKey);

        // Insert fake campus
        Campus campus = new Campus();
        campus.setId(1L);
        campus.setAuthor("Ben");
        campus.setName("Dover University");
        campusRepository.save(campus);

        // Get auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);
        // Perform request
        mockMvc.perform(
                patch(baseAddress + "addCampus").header("IDENTITY", IDENTITY).header("KEY", key).param("identity",
                        USER_IDENTITY).param("campusId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    public void canRemoveCampusPermissionsWithCredentials() throws Exception {
        // Mock loading key
        Mockito.doReturn(mockAdminPrivateKey.getEncoded()).when(storageService).loadKey(IDENTITY);

        // Insert fake campus
        Campus campus = new Campus();
        campus.setAuthor("Ben");
        campus.setName("Dover University");
        campus = campusRepository.save(campus);

        // Insert fake user
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(false);
        apiKey.setIdentity(USER_IDENTITY);
        HashSet<Campus> campuses = new HashSet<>();
        campuses.add(campus);
        apiKey.setCampuses(campuses);
        apiKeyRepository.save(apiKey);

        // Get auth key
        String key = AuthUtils.getKeyForIdentity(mockAdminPublicKey, IDENTITY, 0);
        // Perform request
        mockMvc.perform(
                patch(baseAddress + "removeCampus").header("IDENTITY", IDENTITY).header("KEY", key).param("identity",
                        USER_IDENTITY).param("campusId", Long.toString(campus.getId())))
                .andExpect(status().isOk());
    }
}
