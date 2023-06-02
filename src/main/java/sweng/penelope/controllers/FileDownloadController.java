package sweng.penelope.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import sweng.penelope.services.StorageService;

/**
 * <code>FileDownloadController</code> handles all download endpoints.
 */
@Controller
@Api(tags = "File download operations")
public class FileDownloadController {
    @Autowired
    private StorageService storageService;

    /**
     * Generates a response from the provided resource.
     * 
     * @param resource  The {@link Resource} to display.
     * @param mediaType The {@link MediaType} of the resource.
     * @return {@link ResponseEntity}
     */
    private ResponseEntity<Resource> provideResponse(Resource resource, MediaType mediaType) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // inline = display as response body
                .body(resource);
    }

    /**
     * Generates a response from the provided xml resource.
     * 
     * @param resource The {@link Resource} to display
     * @return {@link ResponseEntity}
     */
    private ResponseEntity<Resource> provideXMLResponse(Resource resource) {
        if (resource != null) {
            return provideResponse(resource, MediaType.APPLICATION_XML);
        }

        return ResponseEntity.internalServerError().body(null);
    }

    /**
     * Generates a response from the provided asset resource (image, video, audio).
     * 
     * @param resource The {@link Resource} to display
     * @return {@link ResponseEntity}
     */
    private ResponseEntity<Resource> provideAssetResponse(Resource resource) {
        if (resource != null) {
            String contentType;
            try {
                // Determine what type of asset it is
                contentType = Files.probeContentType(Paths.get(resource.getURI()));
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(null);
            }
            // Infer correct media type
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return provideResponse(resource, mediaType);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns the xml containing information about the desired bird.
     * 
     * @param birdId The ID of the desired bird.
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/bird/{birdId}")
    @Cacheable(CacheUtils.BIRDS)
    @ApiOperation("Returns the xml containing information about the desired bird.")
    public ResponseEntity<Resource> serveBirdXML(@ApiParam("The ID of the desired bird.") @PathVariable Long birdId) {
        Resource resource = storageService.loadAsResourceFromDB("bird", birdId);

        return provideXMLResponse(resource);
    }

    /**
     * Returns the xml containing information about the desired campus.
     * 
     * @param campusId The ID of the desired campus.
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/campus/{campusId}")
    @Cacheable(CacheUtils.CAMPUSES)
    @ApiOperation("Returns the xml containing information about the desired campus.")
    public ResponseEntity<Resource> serveCampusXML(
            @ApiParam("The ID of the desired campus.") @PathVariable Long campusId) {
        Resource resource = storageService.loadAsResourceFromDB("campus", campusId);

        return provideXMLResponse(resource);
    }

    /**
     * Returns the xml containing a list of available campuses.
     *
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/campus/list")
    @Cacheable(CacheUtils.CAMPUSES_LIST)
    @ApiOperation("Returns the xml containing a list of available campuses.")
    public ResponseEntity<Resource> serveCampusesListXML() {
        Resource resource = storageService.loadAsResourceFromDB("campusList", null);

        return provideXMLResponse(resource);
    }

    /**
     * Returns the desired asset.
     * 
     * @param type     The asset type (image, video, audio).
     * @param campusId The ID of the campus the resource belongs to.
     * @param fileName The asset file name.
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/{type}/{campusId}/{fileName}")
    @Cacheable(value = CacheUtils.ASSETS, key = "#fileName")
    @ApiOperation("Returns the desired asset.")
    public ResponseEntity<Resource> serveAsset(
            @ApiParam(value = "The asset type", allowableValues = "image, video, audio") @PathVariable String type,
            @ApiParam("The ID of the campus the resource belongs to.") @PathVariable String campusId,
            @ApiParam("The asset file name.") @PathVariable String fileName) {
        Resource resource = storageService.loadAsResource(type, campusId, fileName);

        return provideAssetResponse(resource);
    }
}
