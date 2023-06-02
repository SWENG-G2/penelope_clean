package sweng.penelope.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.server.ResponseStatusException;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.Bird;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.BirdRepository;
import sweng.penelope.repositories.CampusRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileSystemStorageServiceTest {
    private static Path keysBasePath;
    private static Path basePath;
    private static Path videoPath;
    private static Path audioPath;
    private static Path imagePath;

    private static final String IMAGE_NAME = "Test.png";
    private static final String VIDEO_NAME = "Test.mp4";
    private static final String AUDIO_NAME = "Test.mp3";
    private static final String VIDEO_MIME_TYPE = "video/mp4";
    private static final String AUDIO_MIME_TYPE = "audio/mp3";
    private static final int BUFFERED_IMAGE_SIZE = 400;
    private static final String IDENTITY = "Elon Musk";

    private static final String BIRD_MOCK_DATA = "A";

    @Value("${penelope.storage.base-folder}")
    private String baseString;

    @Value("${penelope.storage.keys-folder}")
    private String keysBaseString;

    @Autowired
    private FileSystemStorageService classUnderTest;

    @Autowired
    private BirdRepository birdRepository;

    @Autowired
    private CampusRepository campusRepository;

    @BeforeEach
    public void init() {
        keysBasePath = Paths.get(keysBaseString);
        basePath = Paths.get(baseString);
        videoPath = basePath.resolve("video");
        audioPath = basePath.resolve("audio");
        imagePath = basePath.resolve("image");
        try {
            classUnderTest.init();
        } catch (StorageException se) {
            fail(se.getMessage());
        }
    }

    @AfterEach
    public void cleanUp() throws IOException {
        FileSystemUtils.deleteRecursively(basePath);
        FileSystemUtils.deleteRecursively(keysBasePath);

        birdRepository.deleteAll();
        campusRepository.deleteAll();
    }

    @Test
    public void initCanFailWithBadPath() {
        // Try to write to root fs
        ReflectionTestUtils.setField(classUnderTest, "baseString", "/");
        ReflectionTestUtils.setField(classUnderTest, "keysBaseString", "/");

        assertThrows(StorageException.class, () -> {
            classUnderTest.init();
        });

        // Restore fields
        ReflectionTestUtils.setField(classUnderTest, "baseString", baseString);
        ReflectionTestUtils.setField(classUnderTest, "keysBaseString", keysBaseString);
    }

    @Test
    public void canInitialiseFs() {
        assertTrue(Files.exists(keysBasePath));
        assertTrue(Files.exists(videoPath));
        assertTrue(Files.exists(audioPath));
        assertTrue(Files.exists(imagePath));
    }

    @Test
    public void attemptsToRecreateFS() {
        // Folders already exist
        assertTrue(Files.exists(keysBasePath));
        assertTrue(Files.exists(videoPath));
        assertTrue(Files.exists(audioPath));
        assertTrue(Files.exists(imagePath));

        classUnderTest.init();

        // Folders still exist after init
        assertTrue(Files.exists(keysBasePath));
        assertTrue(Files.exists(videoPath));
        assertTrue(Files.exists(audioPath));
        assertTrue(Files.exists(imagePath));
    }

    @Test
    public void canStoreFile() throws Exception {
        MockMultipartFile mpf = new MockMultipartFile("file", IMAGE_NAME, MediaType.IMAGE_PNG_VALUE,
                "content".getBytes());
        boolean canStoreImage = classUnderTest.store("image", "1", mpf);

        mpf = new MockMultipartFile("file", VIDEO_NAME, VIDEO_MIME_TYPE, "content".getBytes());
        boolean canStoreVideo = classUnderTest.store("video", "1", mpf);

        mpf = new MockMultipartFile("file", AUDIO_NAME, AUDIO_MIME_TYPE, "content".getBytes());
        boolean canStoreAudio = classUnderTest.store("audio", "1", mpf);

        assertTrue(canStoreImage);
        assertTrue(canStoreVideo);
        assertTrue(canStoreAudio);
    }

    @Test
    public void cannotStoreBadFile() throws Exception {
        // Original file name is empty
        MockMultipartFile mpf = new MockMultipartFile("file", "".getBytes());

        boolean failsToStore = classUnderTest.store("image", "1", mpf);

        assertFalse(failsToStore);
    }

    @Test
    public void canStoreProcessedImage() {
        BufferedImage bi = new BufferedImage(BUFFERED_IMAGE_SIZE, BUFFERED_IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

        boolean canWriteImage = classUnderTest.storeProcessedImage(IMAGE_NAME, "1", bi);

        assertTrue(canWriteImage);
    }

    @Test
    public void cannotStoreBadProcessedImage() {
        BufferedImage bi = new BufferedImage(BUFFERED_IMAGE_SIZE, BUFFERED_IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

        // Bad file name
        boolean canWriteImage = classUnderTest.storeProcessedImage("..", "1", bi);

        assertFalse(canWriteImage);
    }

    @Test
    public void canLoadPath() {
        Path expected = Paths.get(baseString, "image", "1", IMAGE_NAME);
        Path actual = classUnderTest.load("image", "1", IMAGE_NAME);

        assertEquals(expected, actual);
    }

    @Test
    public void canLoadExistingResource() throws IOException {
        // Create test file
        Path testPath = imagePath.resolve("1");
        if (!Files.exists(testPath))
            Files.createDirectories(testPath);

        Path testDestinationPath = testPath.resolve(IMAGE_NAME);
        Files.write(testDestinationPath, "test".getBytes());

        Resource r = classUnderTest.loadAsResource("image", "1", IMAGE_NAME);

        assertEquals(IMAGE_NAME, r.getFilename());

        // Cleanup
        FileSystemUtils.deleteRecursively(testPath);
    }

    @Test
    public void cannotLoadMissingResource() {
        Resource r = classUnderTest.loadAsResource("image", "1", IMAGE_NAME);

        assertNull(r);
    }

    @Test
    public void canLoadBird() {
        // Create db entries
        Campus campus = new Campus();
        campus.setName(BIRD_MOCK_DATA);
        campus.setAuthor(BIRD_MOCK_DATA);
        campus.setBirds(new HashSet<Bird>());
        campus = campusRepository.save(campus);

        Bird bird = new Bird();
        bird.setAuthor(BIRD_MOCK_DATA);
        bird.setName(BIRD_MOCK_DATA);
        bird.setListImageURL(BIRD_MOCK_DATA);
        bird.setSoundURL(BIRD_MOCK_DATA);
        bird.setAboutMe(BIRD_MOCK_DATA);
        bird.setAboutMeVideoURL(BIRD_MOCK_DATA);
        bird.setLocation(BIRD_MOCK_DATA);
        bird.setLocationImageURL(BIRD_MOCK_DATA);
        bird.setDiet(BIRD_MOCK_DATA);
        bird.setDietImageURL(BIRD_MOCK_DATA);
        bird.setCampus(campus);
        bird = birdRepository.save(bird);

        Resource birdResource = classUnderTest.loadAsResourceFromDB("bird", bird.getId());

        assertNotNull(birdResource);
    }

    @Test
    public void cannotLoadMissinBird() {
        assertThrows(ResponseStatusException.class, () -> {
            // Arbitrary ID
            classUnderTest.loadAsResourceFromDB("bird", 200L);
        });
    }

    @Test
    @Transactional
    public void canLoadCampusWithBird() {
        // Create db entries
        Campus campus = new Campus();
        campus.setName(BIRD_MOCK_DATA);
        campus.setAuthor(BIRD_MOCK_DATA);
        campus.setBirds(new HashSet<Bird>());
        campus = campusRepository.save(campus);

        Bird bird = new Bird();
        bird.setAuthor(BIRD_MOCK_DATA);
        bird.setName(BIRD_MOCK_DATA);
        bird.setListImageURL(BIRD_MOCK_DATA);
        bird.setSoundURL(BIRD_MOCK_DATA);
        bird.setAboutMe(BIRD_MOCK_DATA);
        bird.setAboutMeVideoURL(BIRD_MOCK_DATA);
        bird.setLocation(BIRD_MOCK_DATA);
        bird.setLocationImageURL(BIRD_MOCK_DATA);
        bird.setDiet(BIRD_MOCK_DATA);
        bird.setDietImageURL(BIRD_MOCK_DATA);
        bird.setCampus(campus);
        bird = birdRepository.save(bird);

        // Add bird to campus
        HashSet<Bird> birds = new HashSet<>();
        birds.add(bird);
        campus.setBirds(birds);
        campus = campusRepository.save(campus);

        Resource campusResource = classUnderTest.loadAsResourceFromDB("campus", campus.getId());

        assertNotNull(campusResource);
    }

    @Test
    @Transactional
    public void canLoadCampusNoBird() {
        // Create db entries
        Campus campus = new Campus();
        campus.setName(BIRD_MOCK_DATA);
        campus.setAuthor(BIRD_MOCK_DATA);
        campus.setBirds(new HashSet<Bird>());
        campus = campusRepository.save(campus);

        Resource campusResource = classUnderTest.loadAsResourceFromDB("campus", campus.getId());

        assertNotNull(campusResource);
    }

    @Test
    public void cannotLoadMissinCampus() {
        // Arbitrary ID
        assertNull(classUnderTest.loadAsResourceFromDB("campus", 200L));
    }

    @Test
    public void canLoadCampusList() {
        // Create db entries
        Campus campus = new Campus();
        campus.setName(BIRD_MOCK_DATA);
        campus.setAuthor(BIRD_MOCK_DATA);
        campus.setBirds(new HashSet<Bird>());
        campus = campusRepository.save(campus);

        Resource campusesListResource = classUnderTest.loadAsResourceFromDB("campusList", null);

        assertNotNull(campusesListResource);
    }
}
