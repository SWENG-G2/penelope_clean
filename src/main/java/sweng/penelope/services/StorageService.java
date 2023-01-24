package sweng.penelope.services;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
	void init();

	boolean store(String type, String campusId, MultipartFile file);

	boolean storeProcessedImage(String fileName, String campusId, BufferedImage image);

	boolean storeKey(PrivateKey privateKey, String identity);

	boolean removeKey(String identity);

	byte[] loadKey(String identity);

	Stream<Path> loadAll();

	Path load(String fileHome, String campusId, String filename);

	Resource loadAsResource(String fileHome, String campusId, String fileName);

	Resource loadAsResourceFromDB(String type, Long id);

	void deleteAll();
}
