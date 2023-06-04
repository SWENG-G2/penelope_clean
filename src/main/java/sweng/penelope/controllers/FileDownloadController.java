package sweng.penelope.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;
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

        return ResponseEntity.notFound().build();
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
                // Infer correct media type
                MediaType mediaType = MediaType.parseMediaType(contentType);
                return provideResponse(resource, mediaType);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(null);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Returns the xml containing information about the desired bird.
     * 
     * @param birdId The ID of the desired bird.
     * @return {@link ResponseEntity}
     */

    /**
     * Returns the xml containing information about the desired bird.
     * 
     * @param birdId  The ID of the desired bird.
     * @param request The {@link HttpServletRequest} request.
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/bird/{birdId}")
    @Cacheable(value = CacheUtils.BIRDS, key = "#birdId")
    @ApiOperation("Returns the xml containing information about the desired bird.")
    public ResponseEntity<Resource> serveBirdXML(@ApiParam("The ID of the desired bird.") @PathVariable Long birdId,
            @ApiIgnore HttpServletRequest request) {
        String serverUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();

        Resource resource = storageService.loadAsResourceFromDB("bird", birdId, serverUrl);

        return provideXMLResponse(resource);
    }

    /**
     * Returns the xml containing information about the desired campus.
     * 
     * @param campusId The ID of the desired campus.
     * @param request  The {@link HttpServletRequest} request.
     * @return {@link ResponseEntity}
     */
    @GetMapping(path = "/campus/{campusId}")
    @Cacheable(value = CacheUtils.CAMPUSES, key = "#campusId")
    @ApiOperation("Returns the xml containing information about the desired campus.")
    public ResponseEntity<Resource> serveCampusXML(
            @ApiParam("The ID of the desired campus.") @PathVariable Long campusId,
            @ApiIgnore HttpServletRequest request) {
        String serverUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        Resource resource = storageService.loadAsResourceFromDB("campus", campusId, serverUrl);

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
        Resource resource = storageService.loadAsResourceFromDB("campusList", null, null);

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
    @Cacheable(value = CacheUtils.ASSETS, key = "#type.concat('/').concat(#campusId).concat('/').concat(#fileName)")
    @ApiOperation("Returns the desired asset.")
    public ResponseEntity<Resource> serveAsset(
            @ApiParam(value = "The asset type", allowableValues = "image, video, audio") @PathVariable String type,
            @ApiParam("The ID of the campus the resource belongs to.") @PathVariable String campusId,
            @ApiParam("The asset file name.") @PathVariable String fileName) {
        Resource resource = storageService.loadAsResource(type, campusId, fileName);

        return provideAssetResponse(resource);
    }
}
