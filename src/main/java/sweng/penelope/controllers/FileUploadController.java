package sweng.penelope.controllers;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import sweng.penelope.services.StorageService;

/**
 * <code>FileUploadController</code> handles all upload endpoints.
 */
@Controller
@RequestMapping(path = "/api/file")
@Api(tags = "File upload operations")
@ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "Credentials", value = "Authentication credentials. Format: <code>username=password=timestamp</code>. RSA encoded with server's public key.", required = true, dataType = "java.lang.String")
})
@Validated
public class FileUploadController {
    @Autowired
    private StorageService storageService;
    @Autowired
    private CacheManager cacheManager;

    private static final int RANDOM_STRING_LOWER_BOUND = 0;
    private static final int RANDOM_STRING_UPPER_BOUND = 9;
    private static final int RANDOM_STRING_LENGTH = 10;
    private final Random random = new Random();

    /**
     * Transforms an input image into a rounded png.
     * 
     * @param file     The input image.
     * @param campusId The campus id the resource belongs to.
     * @param fileName The file name.
     * @return {@link ResponseEntity}
     */
    private ResponseEntity<String> processImage(MultipartFile file, String campusId, String fileName) {
        try {
            // Load input file
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            // To get a circle, we want to use the short side of the image as a measure to
            // crop.
            int size = Math.min(width, height);

            // ARGB for transparency
            BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2d = outputImage.createGraphics();

            // Draw circle image
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2d.fillOval(0, 0, size, size);
            graphics2d.setComposite(AlphaComposite.SrcIn);
            graphics2d.drawImage(bufferedImage, 0, 0, null);

            String processedFileName = fileName.split("\\.")[0] + "_processed.png";

            if (storageService.storeProcessedImage(processedFileName, campusId, outputImage))
                return ResponseEntity.ok(String.format("image/%s/%s", campusId, processedFileName));
        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body("Could not process image file");
        }
        return ResponseEntity.ok("null");
    }

    /**
     * Handles file uploading operations.
     * 
     * @param file     The uploaded file
     * @param type     The file type (image, video, audio)
     * @param process  Whether the file (image only) should be made into a round png
     * @param campusId The ID of the campus the resource belongs to
     * @return {@link ResponseEntity}
     */
    @PostMapping(path = "{campusId}/new")
    @ApiOperation("Stores the uploaded file")
    public ResponseEntity<String> handleFileUpload(
            @ApiParam("The file to upload") @RequestPart @NotNull MultipartFile file,
            @ApiParam(value = "The file type", allowableValues = "image, audio, video") @RequestParam @NotNull String type,
            @ApiParam("Whether the file (image only) should be made into a round png") @RequestParam(required = false) boolean process,
            @ApiParam("The ID of the campus the resource belongs to") @PathVariable @NotNull Long campusId) {
        if ((type.equals("audio") || type.equals("video") || type.equals("image"))) {
            String originalfileName = file.getOriginalFilename();
            if (originalfileName != null && !originalfileName.contains("..") && originalfileName.length() > 0) {
                // Random string to ensure files are unique
                String randomString = random.ints(RANDOM_STRING_LOWER_BOUND, RANDOM_STRING_UPPER_BOUND + 1)
                        .limit(RANDOM_STRING_LENGTH)
                        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                        .toString();

                // Get stripped file name
                String fileName = Paths.get(originalfileName).getFileName().toString();

                if (StringUtils.countOccurrencesOf(fileName, ".") > 1)
                    return ResponseEntity.badRequest().body("File name cannot contain dots");

                String[] splitFileName = fileName.split("\\.");
                fileName = splitFileName[0] + "-" + randomString + "." + splitFileName[1];
                CacheUtils.evictCache(cacheManager, CacheUtils.ASSETS, fileName);

                if (type.equals("image") && process) // Make round PNG
                    return processImage(file, campusId.toString(), fileName);
                else if (storageService.store(type, campusId.toString(), file, fileName)) // Store
                    return ResponseEntity.ok().body(String.format("%s/%s/%s", type, campusId.toString(), fileName));
                else // Something went wrong
                    return ResponseEntity.internalServerError().body("Could not store file.");
            }
            return ResponseEntity.badRequest().body("File name cannot contain \"..\" and cannot be null");
        }

        return ResponseEntity.badRequest().body("File type is not supported");
    }
}
