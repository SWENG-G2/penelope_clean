package sweng.penelope.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.DataManager;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CampusControllerTest {

        private static final String baseAddress = "/api/campus/%s/";
        private static final String USERNAME = "admin";
        private static final String PASSWORD = "qwerty123456";
        private static final String TIMESTAMP = ZonedDateTime.now(ZoneId.of("Europe/London")).toString();

        private String ENCODED_PASSWORD, CREDENTIALS, ENCRYPTED_CREDENTIALS;
        private String testCampusID;
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

        @Value("${penelope.api-credentialsHeader}")
        private String credentialsHeader;

        private String formatAddress(String value) {
                return String.format(Locale.getDefault(), baseAddress, value);
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
        }

        @AfterEach
        public void cleanUp() {
                // Clean all
                dataManagerRepository.deleteAll();
                campusRepository.deleteAll();
        }

        @Test
        public void cannotCreateIfNotAdmin() throws Exception {
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

                MockHttpServletRequestBuilder request = post(formatAddress("new"))
                                .header(credentialsHeader.toLowerCase(), testEncryptedCredentials)
                                .param("name", "test campus").secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isForbidden());
        }

        @Test
        public void cannotCreateIfParametersInvalid() throws Exception {
                MockHttpServletRequestBuilder request = post(formatAddress("new"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS).secure(true);
                // Simulate a campus without a name
                // .param("name", "")

                mockMvc.perform(request)
                                .andExpect(status().is4xxClientError());
        }

        @Test
        public void canCreateIfEverythingValid() throws Exception {
                MockHttpServletRequestBuilder request = post(formatAddress("new"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("name", "test campus").secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isOk());
        }

        @Test
        public void cannotDeleteIfWrongID() throws Exception {
                // Insert fake campus
                Campus temp_campus = new Campus();
                temp_campus.setName("test campus");
                temp_campus.setAuthor(USERNAME);
                temp_campus = campusRepository.save(temp_campus);

                testCampusID = temp_campus.getId().toString();

                MockHttpServletRequestBuilder request = delete(formatAddress("remove"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("id", "42069").secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isNotFound());
        }

        @Test
        public void canDeleteIfCorrectID() throws Exception {
                // Insert fake campus
                Campus temp_campus = new Campus();
                temp_campus.setName("test campus");
                temp_campus.setAuthor(USERNAME);
                temp_campus = campusRepository.save(temp_campus);

                testCampusID = temp_campus.getId().toString();

                MockHttpServletRequestBuilder request = delete(formatAddress("remove"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("id", testCampusID).secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isOk());
        }

        @Test
        public void canEditAsAdmin() throws Exception {
                // Insert fake campus
                Campus testCampus = new Campus();
                testCampus.setName("test campus");
                testCampus.setAuthor(USERNAME);
                testCampus = campusRepository.save(testCampus);

                testCampusID = testCampus.getId().toString();

                String newName = "New name";

                MockHttpServletRequestBuilder request = patch(formatAddress("edit"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("id", testCampusID)
                                .param("newName", newName).secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isOk());

                campusRepository.findById(testCampus.getId()).ifPresentOrElse((campus) -> {
                        assertEquals(newName, campus.getName());
                }, () -> {
                        fail();
                });

        }

        @Test
        public void cannotEditIfNotAdmin() throws Exception {
                // Insert fake campus
                Campus testCampus = new Campus();
                testCampus.setName("test campus");
                testCampus.setAuthor(USERNAME);
                testCampus = campusRepository.save(testCampus);

                testCampusID = testCampus.getId().toString();

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

                String newName = "New name";

                MockHttpServletRequestBuilder request = patch(formatAddress("edit"))
                                .header(credentialsHeader.toLowerCase(), testEncryptedCredentials)
                                .param("id", testCampusID)
                                .param("newName", newName).secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isForbidden());
        }

        @Test
        public void cannotEditMissingCampus() throws Exception {
                String newName = "New name";

                MockHttpServletRequestBuilder request = patch(formatAddress("edit"))
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("id", "0")
                                .param("newName", newName).secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isNotFound());
        }

        @Test
        public void authorUpdates() throws Exception {
                // Insert second admin
                String username = "admin2";
                String password = "pwd";
                String credentials = username + "=" + password + "=" + TIMESTAMP;
                String encryptedCredentials = RSAUtils.encrypt(keyPair.getPublic(), credentials);
                String encodedPassword = passwordEncoder.encode(password);

                DataManager mDataManager = new DataManager();
                mDataManager.setUsername(username);
                mDataManager.setPassword(encodedPassword);
                mDataManager.setSysadmin(true);
                dataManagerRepository.save(mDataManager);

                // Insert fake campus
                Campus testCampus = new Campus();
                testCampus.setName("test campus");
                testCampus.setAuthor(USERNAME);
                testCampus = campusRepository.save(testCampus);

                testCampusID = testCampus.getId().toString();

                String newName = "New name";

                MockHttpServletRequestBuilder request = patch(formatAddress("edit"))
                                .header(credentialsHeader.toLowerCase(), encryptedCredentials)
                                .param("id", testCampusID)
                                .param("newName", newName).secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isOk());

                campusRepository.findById(testCampus.getId()).ifPresentOrElse((campus) -> {
                        String author = campus.getAuthor();
                        assertTrue(author.contains(username));
                        assertTrue(author.contains(USERNAME));
                }, () -> {
                        fail();
                });
        }

}
