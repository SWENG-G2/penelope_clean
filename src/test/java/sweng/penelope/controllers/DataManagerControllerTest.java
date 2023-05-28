package sweng.penelope.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.http.HttpHeaders;
import java.security.KeyPair;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.Campus;
import sweng.penelope.entities.DataManager;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DataManagerControllerTest {
    private static final String baseAddress = "/api/users/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "qwerty123456";
    private static final String TIMESTAMP = ZonedDateTime.now(ZoneId.of("Europe/London")).toString();
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

    @MockBean
    private StorageService storageService;

    @BeforeEach
    public void setUpCredentials() throws Exception {
        this.CREDENTIALS = USERNAME + "=" + PASSWORD + "=" + TIMESTAMP;
        this.ENCRYPTED_CREDENTIALS = RSAUtils.encrypt(keyPair.getPublic(), CREDENTIALS);
        this.ENCODED_PASSWORD = passwordEncoder.encode(PASSWORD);

        this.dataManager = new DataManager();
        this.dataManager.setUsername(USERNAME);
        this.dataManager.setPassword(ENCODED_PASSWORD);
        this.dataManager.setSysadmin(true);
        dataManagerRepository.save(dataManager);
    }

    @AfterEach
    public void cleanUp() {
        dataManagerRepository.deleteAll();
        campusRepository.deleteAll();
    }

    @Test
    public void canCreateNewUser() throws Exception {
        mockMvc.perform(post(baseAddress + "new")
                .header("Credentials", ENCRYPTED_CREDENTIALS)
                .param("username", "Heisenberg")
                .param("password", "Password123")
                .param("sysadmin", "false"))
                .andExpect(status().isOk());

        assertNotNull(dataManager);
        assertEquals(USERNAME, dataManager.getUsername());
        assertEquals(ENCODED_PASSWORD, dataManager.getPassword());
        assertTrue(dataManager.isSysadmin());
    }

    @Test
    public void canRemoveUser() throws Exception {
        String testUsername = "User123";
        String testPassword = passwordEncoder.encode("Password123");
        DataManager testUser = new DataManager();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setSysadmin(false);
        testUser = dataManagerRepository.save(testUser);

        mockMvc.perform(delete(baseAddress + "remove")
                .header("Credentials", ENCRYPTED_CREDENTIALS)
                .param("username", testUser.getUsername()))
                .andExpect(status().isOk());

        assertTrue(dataManagerRepository.findById(testUser.getUsername()).isEmpty());
    }

    @Test
    public void canAddCampusRights() throws Exception {
        String testUsername = "User123";
        String testPassword = passwordEncoder.encode("Password123");
        DataManager testUser = new DataManager();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setSysadmin(false);
        testUser = dataManagerRepository.save(testUser);
        
        Campus campus = new Campus();
        campus = campusRepository.save(campus);

        mockMvc.perform(patch(baseAddress + "addCampus")
                .header("Credentials", ENCRYPTED_CREDENTIALS)
                .param("username", testUser.getUsername())
                .param("campusID", campus.getId().toString()))
                .andExpect(status().isOk());

    }

    @Test
    public void canRemoveCampusRights() throws Exception {
        String testUsername = "User123";
        String testPassword = passwordEncoder.encode("Password123");
        DataManager testUser = new DataManager();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setSysadmin(false);
        testUser = dataManagerRepository.save(testUser);

        Campus campus = new Campus();
        campus.setName("Hogwarts");
        campus.setAuthor("Snape");
        campus = campusRepository.save(campus);

        Set<Campus> campuses = new HashSet<>();
        campuses.add(campus);
        testUser.setCampuses(campuses);
        testUser = dataManagerRepository.save(testUser);

        mockMvc.perform(patch(baseAddress + "removeCampus")
                .header("Credentials", ENCRYPTED_CREDENTIALS)
                .param("username", testUsername)
                .param("campusID", campus.getId().toString()))
                .andExpect(status().isOk());

        DataManager updatedUser = dataManagerRepository.findById(testUsername).orElseThrow();
        assertFalse(updatedUser.getCampuses().contains(campus));
    }

    @Test
    public void canValidateUser() throws Exception {
        String testUsername = "User123";
        String testPassword = passwordEncoder.encode("Password123");
        DataManager testUser = new DataManager();
        testUser.setUsername(testUsername);
        testUser.setPassword(testPassword);
        testUser.setSysadmin(false);
        testUser = dataManagerRepository.save(testUser);

        Campus campus = new Campus();
        campus.setName("Hogwarts");
        campus.setAuthor("Snape");
        campus = campusRepository.save(campus);

        Set<Campus> campuses = new HashSet<>();
        campuses.add(campus);
        testUser.setCampuses(campuses);
        testUser = dataManagerRepository.save(testUser);

        String testCredentials = testUsername + "=" + "Password123" + "=" + TIMESTAMP;
        String testEncryptedCredentials = RSAUtils.encrypt(keyPair.getPublic(), testCredentials);

        MvcResult result = mockMvc.perform(post(baseAddress + "validate")
                .header("Credentials", testEncryptedCredentials))
                .andExpect(status().isNoContent())
                .andReturn();


        MockHttpServletResponse response = result.getResponse();
        assertEquals("false", response.getHeader("Admin"));
        assertEquals("true", response.getHeader("Valid"));

    }



}