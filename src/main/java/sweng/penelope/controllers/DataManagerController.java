package sweng.penelope.controllers;

import java.security.KeyPair;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;
import springfox.documentation.annotations.ApiIgnore;
import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.Campus;
import sweng.penelope.entities.DataManager;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.services.StorageService;

/**
 * <code>DataManagerController</code> handles all DataManager (user) endpoints.
 */
@Controller
@RequestMapping(path = "/api/users")
@Api(tags = "DataManager operations")
@ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "Credentials", value = "Authentication credentials. Format: <code>username=password=timestamp</code>. RSA encoded with server's public key.", required = true, dataType = "java.lang.String")
})
public class DataManagerController {
    @Autowired
    private DataManagerRepository dataManagerRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private StorageService storageService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KeyPair serverKeyPair;

    @Value("${penelope.validation-valid}")
    private String validHeader;

    @Value("${penelope.validation-admin}")
    private String adminHeader;

    @Value("${penelope.api-credentialsHeader}")
    private String credentialsHeader;

    @Value("${penelope.api-campusesHeader}")
    private String campusesHeader;

    @Value("${penelope.campusesAll}")
    private String campusesAll;

    /**
     * DataManager creation endpoint.
     * 
     * @param username Human friendly name of the DataManager's owner (i.e. email).
     * @param password DataManager's password.
     * @param sysadmin Whether the new user should have sysadmin priviledges.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("DataManager creation endpoint.")
    @PostMapping(path = "/new")
    public ResponseEntity<String> createNewUser(
            @ApiParam(value = "Human friendly name of the DataManager's owner (i.e. email).") @RequestParam String username,
            @ApiParam(value = "DataManager's password.") @RequestParam String password,
            @ApiParam("Whether the new user should have sysadmin priviledges.") @RequestParam(required = false) Boolean sysadmin) {
        DataManager user = new DataManager();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        if (sysadmin != null)
            user.setSysadmin(sysadmin.booleanValue());

        dataManagerRepository.save(user);

        return ResponseEntity.ok().body("User created");
    }

    /**
     * DataManager removal endpoint.
     * 
     * @param username DataManager's username to remove.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("DataManager removal endpoint.")
    @DeleteMapping(path = "/remove")
    public ResponseEntity<String> removeUser(
            @ApiParam(value = "DataManager's username to remove.") @RequestParam String username) {
        return dataManagerRepository.findById(username).map(user -> {
            dataManagerRepository.delete(user);

            return ResponseEntity.ok().body("User deleted");
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Grants permissions to access resources under a certain campus to a
     * DataManager.
     * 
     * @param username The DataManager's username.
     * @param campusID The id of the campus the resources belong to.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Grants permissions to access resources under a certain campus to a DataManager.")
    @PatchMapping(path = "/addCampus")
    public ResponseEntity<String> addCampusRight(
            @ApiParam(value = "The DataManager's username.") @RequestParam String username,
            @ApiParam("The id of the campus the resources belong to.") @RequestParam Long campusID) {
        return dataManagerRepository.findById(username).map(user -> {
            return campusRepository.findById(campusID).map(campus -> {
                Set<Campus> campuses = user.getCampuses();
                campuses.add(campus);

                user.setCampuses(campuses);
                dataManagerRepository.save(user);

                return ResponseEntity.ok().body("Rights granted to user");
            }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Removes permissions to access resources under a certain campus from a
     * DataManager.
     * 
     * @param username The DataManager's username.
     * @param campusID The id of the campus the resources belong to.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Removes permissions to access resources under a certain campus from a DataManager.")
    @PatchMapping(path = "/removeCampus")
    public ResponseEntity<String> removeCampusRight(
            @ApiParam(value = "The DataManager's username.") @RequestParam String username,
            @ApiParam("The id of the campus the resources belong to.") @RequestParam Long campusID) {
        return dataManagerRepository.findById(username).map(user -> {
            return campusRepository.findById(campusID).map(campus -> {
                Set<Campus> campuses = user.getCampuses();

                if (!campuses.contains(campus))
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND);

                campuses.remove(campus);

                user.setCampuses(campuses);
                dataManagerRepository.save(user);

                return ResponseEntity.ok().body("Rights removed from user");
            }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Verifies a user's credentials and provides their level of permission.
     * 
     * @param headers Authentication credentials. Format:
     *                <code>username=password=timestamp</code>. RSA encoded with
     *                server's public key.
     * @return An empty {@link ResponseEntity} with <code>Valid: boolean</code> and
     *         <code>Admin: boolean</code> headers.
     */
    @ApiOperation("Verifies a user's credentials and provides their level of permission.")
    @ApiResponse(code = 200, message = "OK", responseHeaders = {
            @ResponseHeader(name = "Admin", description = "Boolean, either admin credentials or not", response = Boolean.class),
            @ResponseHeader(name = "Valid", description = "Boolean, either valid credentials or not", response = Boolean.class),
            @ResponseHeader(name = "Campuses", description = "A string array of campuses ids (e.g. 1,2,3,4). -1 for admin users. Can be an empty String.", response = String.class) })
    @PostMapping(path = "/validate")
    public ResponseEntity<Void> validateUser(
            @ApiIgnore @RequestHeader Map<String, String> headers) {
        String credentials = headers.get(credentialsHeader.toLowerCase());

        String[] decryptedCredentials;
        // Default headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(validHeader, "false");
        httpHeaders.set(adminHeader, "false");

        try {
            // Decrypt credentials
            decryptedCredentials = RSAUtils.decrypt(serverKeyPair.getPrivate(), credentials).split("=");

            String username = decryptedCredentials[0];
            String password = decryptedCredentials[1];
            String timestamp = decryptedCredentials[2];
            // Compare timestamp
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/London"));
            ZonedDateTime sentAt = ZonedDateTime.parse(timestamp);
            Duration delta = Duration.between(sentAt, now);
            if (delta.getSeconds() < 60) {
                dataManagerRepository.findById(username).ifPresent(dataManager -> {
                    // Verify credentials validity
                    if (passwordEncoder.matches(password, dataManager.getPassword())) {
                        httpHeaders.set(validHeader, "true");
                        // Verify admin permissions
                        if (dataManager.isSysadmin()) {
                            httpHeaders.set(adminHeader, "true");
                            httpHeaders.set(campusesHeader, campusesAll);
                        } else { // Campus permissions
                            Set<Campus> campuses = dataManager.getCampuses();
                            StringBuilder campusesHeaderSB = new StringBuilder();

                            campuses.forEach(campus -> campusesHeaderSB.append(String.format("%d,", campus.getId())));

                            String campusesHeaderContent = Optional.ofNullable(campusesHeaderSB.toString())
                                    .filter(s -> s.length() > 0)
                                    .map(s -> s.substring(0, s.length() - 1)).orElse("");

                            httpHeaders.set(campusesHeader, campusesHeaderContent);
                        }
                    }
                });
            }
        } catch (Exception exception) {
            // Realistically not much we can do here.
            // Log the exception and move on
            exception.printStackTrace();
        }

        return ResponseEntity.noContent().headers(httpHeaders).build();
    }

    /**
     * Provides a list of all users and their campuses permissions.
     * 
     * Note: This is not in the FileDownloadController because
     * that controller handles publicly available endpoints.
     * This endpoint needs admin access, claimed by making a request to (most)
     * <code>/api/users</code> endpoint and verified by the
     * UserAuthenticationManager.
     * Yes this is reduntant logic, /shrugs.
     * 
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Provides a list of all users and their campuses permissions")
    @GetMapping("/list")
    public ResponseEntity<Resource> serveUsersXML() {
        Resource resource = storageService.loadAsResourceFromDB("usersList", null);

        if (resource != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline") // inline = display as response body
                    .body(resource);
        } else
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
