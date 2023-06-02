package sweng.penelope.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
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
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.Campus;
import sweng.penelope.entities.DataManager;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.services.StorageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileUploadControllerTest {

        private static final String baseAddress = "/api/file/%s/";
        private static final String USERNAME = "admin";
        private static final String PASSWORD = "qwerty123456";
        private static final String TIMESTAMP = ZonedDateTime.now(ZoneId.of("Europe/London")).toString();

        private final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader();

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
                campus = campusRepository.save(campus);

                testCampusID = campus.getId().toString();

                when(storageService.store(anyString(), anyString(), any(), anyString())).thenReturn(true);
        }

        @AfterEach
        public void cleanUp() {
                // Clean all
                dataManagerRepository.deleteAll();
                campusRepository.deleteAll();
        }

        @Test
        public void incorrectFileTypeUpload() throws Exception {
                String type = "incorrect type";
                String fileName = "test.png";
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png",
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                when(storageService.store(eq(type), eq(testCampusID), eq(file), anyString())).thenReturn(false);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void tooManyFileNameDots() throws Exception {
                String type = "image";
                String fileName = "test..png";
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png",
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                when(storageService.store(eq(type), eq(testCampusID), eq(file), anyString())).thenReturn(false);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void noImageProcessFileUpload() throws Exception {
                String type = "image";
                String fileName = "test.png";
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png",
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                when(storageService.store(eq(type), eq(testCampusID), eq(file), anyString())).thenReturn(true);

                String fileNameNoExtension = fileName.split("\\.")[0];
                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString(
                                                String.format("%s/%s/%s", type, testCampusID, fileNameNoExtension))));

                verify(storageService, times(1)).store(eq(type), eq(testCampusID), eq(file), anyString());
        }

        @Test
        public void imageProcessFileUpload() throws Exception {
                String type = "image";
                String fileName = "classpath:duckTest.png";
                boolean process = true;

                File testFile = defaultResourceLoader.getResource(fileName).getFile();
                byte[] fileBA = Files.readAllBytes(testFile.toPath());

                MockMultipartFile file = new MockMultipartFile("file", fileName, "image/png", fileBA);

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                when(storageService.store(eq(type), eq(testCampusID), eq(file), anyString())).thenReturn(true);

                when(storageService.storeProcessedImage(anyString(), eq(testCampusID),
                                any(BufferedImage.class)))
                                .thenReturn(true);

                String fileNameNoExtension = fileName.split("\\.")[0];
                mockMvc.perform(request)
                                .andExpect(status().isOk())
                                .andExpect(content()
                                                .string(containsString("_processed.png")))
                                .andExpect(content().string(containsString(
                                                String.format("%s/%s/%s", type, testCampusID, fileNameNoExtension))));

                verify(storageService, times(1)).storeProcessedImage(anyString(), eq(testCampusID),
                                any(BufferedImage.class));
        }

}
