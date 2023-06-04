package sweng.penelope.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.MediaType;
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
        private static final String IMAGE_NAME = "Test.png";
        private static final String VIDEO_NAME = "Test.mp4";
        private static final String AUDIO_NAME = "Test.mp3";
        private static final String VIDEO_MIME_TYPE = "video/mp4";
        private static final String AUDIO_MIME_TYPE = "audio/mp3";

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
                String fileName = IMAGE_NAME;
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
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
                String fileName = "../test.png";
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void noImageProcessFileUpload() throws Exception {
                String type = "image";
                String fileName = IMAGE_NAME;
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
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

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE, fileBA);

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

        @Test
        public void videoFileUpload() throws Exception {
                String type = "video";
                String fileName = VIDEO_NAME;
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, VIDEO_MIME_TYPE,
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
        public void audioFileUpload() throws Exception {
                String type = "audio";
                String fileName = AUDIO_NAME;
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, AUDIO_MIME_TYPE,
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
        public void processIgnoredIfNotImage() throws Exception {
                String type = "audio";
                String fileName = AUDIO_NAME;
                boolean process = true;

                MockMultipartFile file = new MockMultipartFile("file", fileName, AUDIO_MIME_TYPE,
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
                                                String.format("%s/%s/%s", type, testCampusID, fileNameNoExtension))))
                                .andExpect(content().string(not(containsString("_processed.png"))));

                verify(storageService, times(1)).store(eq(type), eq(testCampusID), eq(file), anyString());
        }

        @Test
        public void handlesFailedStore() throws Exception {
                String type = "audio";
                String fileName = AUDIO_NAME;
                boolean process = true;

                MockMultipartFile file = new MockMultipartFile("file", fileName, AUDIO_MIME_TYPE,
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
                                .andExpect(status().isInternalServerError());

                verify(storageService, times(1)).store(eq(type), eq(testCampusID), eq(file), anyString());
        }

        @Test
        public void handlesFailedStoreProcessedImage() throws Exception {
                String type = "image";
                String fileName = IMAGE_NAME;
                boolean process = true;

                String testFileName = "classpath:duckTest.png";
                File testFile = defaultResourceLoader.getResource(testFileName).getFile();
                BufferedImage testBufferedImage = ImageIO.read(testFile);
                // byte[] fileBA = Files.readAllBytes(testFile.toPath());

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
                                "test image content".getBytes());

                MockedStatic<ImageIO> imageIo = Mockito.mockStatic(ImageIO.class);
                imageIo.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(testBufferedImage);

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                when(storageService.storeProcessedImage(anyString(), anyString(), any(BufferedImage.class)))
                                .thenReturn(false);

                mockMvc.perform(request)
                                .andExpect(status().isInternalServerError());

                verify(storageService, times(1)).storeProcessedImage(anyString(), anyString(),
                                any(BufferedImage.class));
        }

        @Test
        public void handlesEmptyFileName() throws Exception {
                String type = "image";
                String fileName = "";
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void handlesNullFileName() throws Exception {
                String type = "image";
                String fileName = null;
                boolean process = false;

                MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.IMAGE_PNG_VALUE,
                                "test image content".getBytes());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                .multipart(formatAddress("new", testCampusID))
                                .file(file)
                                .header(credentialsHeader.toLowerCase(), ENCRYPTED_CREDENTIALS)
                                .param("type", type)
                                .param("process", String.valueOf(process))
                                .secure(true);

                mockMvc.perform(request)
                                .andExpect(status().isBadRequest());
        }

}
