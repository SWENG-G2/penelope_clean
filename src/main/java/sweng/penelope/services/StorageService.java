package sweng.penelope.services;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * <code>StorageService</code> interface that determines the required storage
 * methods.
 */
public interface StorageService {
	/**
	 * Sets up the storage destination.
	 */
	void init();

	/**
	 * Stores a {@link MultipartFile}.
	 * 
	 * @param type     The file type (image, video, audio).
	 * @param campusId The ID of the campus the resource belongs to.
	 * @param file     The file to save.
	 * @return True for success, false for failure.
	 */
	boolean store(String type, String campusId, MultipartFile file);

	/**
	 * Stores an image as a round PNG.
	 * 
	 * @param fileName The image file name.
	 * @param campusId The ID of the campus the resource belongs to.
	 * @param image    The image to save.
	 * @return True for success, false for failure.
	 */
	boolean storeProcessedImage(String fileName, String campusId, BufferedImage image);

	/**
	 * Resolves the path to the required asset.
	 * 
	 * @param type     The file type (image, video, audio).
	 * @param campusId The ID of the campus the resource belongs to.
	 * @param fileName The file name.
	 * @return A {@link Path} object containing the resolved path, without checking
	 *         for file existance.
	 */
	Path load(String type, String campusId, String fileName);

	/**
	 * Retrieves the desired asset from storage as a {@link Resource}.
	 * 
	 * @param type     The file type (image, video, audio).
	 * @param campusId The ID of the campus the resource belongs to.
	 * @param fileName The file name.
	 * @return {@link Resource} representation of the asset.
	 */
	Resource loadAsResource(String type, String campusId, String fileName);

	/**
	 * Retrieves the desired resource from database as a {@link Resource}.
	 * 
	 * @param type The resource type (bird, campus, campusList).
	 * @param id   The resource ID.
	 * @return {@link Resource} representation of the resource.
	 */
	Resource loadAsResourceFromDB(String type, Long id);
}
