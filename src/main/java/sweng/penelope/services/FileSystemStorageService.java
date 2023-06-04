package sweng.penelope.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

import sweng.penelope.entities.Bird;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.BirdRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.xml.BirdXML;
import sweng.penelope.xml.CampusXML;
import sweng.penelope.xml.CampusesListXML;
import sweng.penelope.xml.CommonXML;
import sweng.penelope.xml.UsersListXML;
import sweng.penelope.xml.XMLConfiguration;

/**
 * <code>FileSystemStorageService</code> implements {@link StorageService} for
 * file
 * system and database operations.
 */
@Service
public class FileSystemStorageService implements StorageService {

    @Value("${penelope.storage.base-folder}")
    private String baseString;

    @Value("${penelope.storage.keys-folder}")
    private String keysBaseString;

    @Autowired
    private BirdRepository birdRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private DataManagerRepository dataManagerRepository;

    private void createDir(Path path) throws IOException {
        if (!Files.exists(path))
            Files.createDirectories(path);
    }

    @Override
    public void init() {
        Path keysBasePath = Paths.get(keysBaseString);
        Path basePath = Paths.get(baseString);
        Path videoPath = basePath.resolve("video");
        Path audioPath = basePath.resolve("audio");
        Path imagePath = basePath.resolve("image");
        try {
            createDir(videoPath);
            createDir(audioPath);
            createDir(imagePath);
            createDir(keysBasePath);
        } catch (IOException ioException) {
            throw new StorageException("Could not create base directories structure", ioException);
        }
    }

    @Override
    public boolean store(String type, String campusId, MultipartFile file, String fileName) {
        Path destinationRoot = Paths.get(baseString, type, campusId);
        Path destinationPath = destinationRoot.resolve(fileName);
        try {
            createDir(destinationRoot);
            file.transferTo(destinationPath);
            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean remove(String fileUrl) {
        Path storeRoot = Paths.get(baseString);
        Path filePath = storeRoot.resolve(fileUrl);
        try {
            Files.delete(filePath);

            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean storeProcessedImage(String fileName, String campusId, BufferedImage image) {
        Path destinationRoot = Paths.get(baseString, "image", campusId);
        File outFile = destinationRoot.resolve(fileName).toFile();
        try {
            createDir(destinationRoot);
            ImageIO.write(image, "png", outFile);
            return true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
    }

    @Override
    public Path load(String type, String campusId, String fileName) {
        return Paths.get(baseString, type, campusId, fileName);
    }

    @Override
    public Resource loadAsResource(String type, String campusId, String filename) {
        Path filePath = load(type, campusId, filename);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable())
                return resource;
        } catch (MalformedURLException malformedURLException) {
            throw new StorageException("Invalid file name", malformedURLException);
        }
        return null;
    }

    /**
     * Generates a {@link BirdXML}.
     * 
     * @param id        The {@link Bird} id.
     * @param serverUrl The server url, with protocol and port.
     * @return
     */
    private BirdXML getBird(Long id, String serverUrl) {
        Optional<Bird> requestBird = birdRepository.findById(id);

        return requestBird.map(bird -> {
            XMLConfiguration xmlConfiguration = new XMLConfiguration(bird.getAuthor(), bird.getName(), id);
            xmlConfiguration.setServerUrl(serverUrl);

            BirdXML birdXML = new BirdXML(xmlConfiguration);

            String aboutMe = HtmlUtils.htmlEscape(bird.getAboutMe());
            String diet = HtmlUtils.htmlEscape(bird.getDiet());
            String location = HtmlUtils.htmlEscape(bird.getLocation());

            birdXML.addHeroSlide(bird.getSoundURL(), bird.getHeroImageURL());
            birdXML.addAboutMe(bird.getAboutMeVideoURL(), aboutMe);
            birdXML.addDiet(bird.getDietImageURL(), diet);
            birdXML.addLocation(bird.getLocationImageURL(), location);

            return birdXML;
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private CampusXML getCampus(Long id, String serverUrl) {
        CampusXML campusXML = null;

        Optional<Campus> requestCampus = campusRepository.findById(id);
        if (requestCampus.isPresent()) {
            Campus campus = requestCampus.get();
            XMLConfiguration xmlConfiguration = new XMLConfiguration(campus.getAuthor(), campus.getName(), id);
            xmlConfiguration.setServerUrl(serverUrl);
            campusXML = new CampusXML(xmlConfiguration);

            Iterator<Bird> birdsIterator = campus.getBirds().iterator();
            while (birdsIterator.hasNext()) {
                Bird bird = birdsIterator.next();

                campusXML.addBird(bird.getName(), bird.getAboutMe(), bird.getId(), bird.getListImageURL());
            }
        }

        return campusXML;
    }

    private CampusesListXML getCampusesList() {
        XMLConfiguration xmlConfiguration = new XMLConfiguration("The Penelope Team", "Campuses list", -1L);
        CampusesListXML campusesListXML = new CampusesListXML(xmlConfiguration);
        Iterator<Campus> campusIterator = campusRepository.findAll().iterator();

        while (campusIterator.hasNext()) {
            Campus campus = campusIterator.next();
            campusesListXML.addCampus(campus.getName(), campus.getId());
        }

        return campusesListXML;
    }

    private UsersListXML getUsersList() {
        XMLConfiguration xmlConfiguration = new XMLConfiguration("The Penelope Team", "Users list", -1L);
        UsersListXML usersListXML = new UsersListXML(xmlConfiguration);

        dataManagerRepository.findAll().forEach(dataManager -> {
            Set<Campus> campuses;

            if (dataManager.isSysadmin())
                campuses = null;
            else
                campuses = dataManager.getCampuses();

            usersListXML.addUser(dataManager.getUsername(), campuses);
        });

        return usersListXML;
    }

    @Override
    public Resource loadAsResourceFromDB(String type, Long id, String serverUrl) {
        CommonXML xml = null;
        if (type.equals("campus"))
            xml = getCampus(id, serverUrl);
        else if (type.equals("bird"))
            xml = getBird(id, serverUrl);
        else if (type.equals("usersList"))
            xml = getUsersList();
        else
            xml = getCampusesList();

        if (xml != null) {
            byte[] bytesArray = xml.getBytes();
            if (bytesArray != null) {
                return new ByteArrayResource(bytesArray);
            }
        }
        return null;
    }
}
