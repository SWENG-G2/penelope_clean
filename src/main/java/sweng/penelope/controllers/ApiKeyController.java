package sweng.penelope.controllers;

import java.nio.file.FileSystemException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.ApiKey;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.ApiKeyRepository;
import sweng.penelope.repositories.CampusRepository;
import sweng.penelope.services.StorageService;

/**
 * <code>ApiKeyController</code> handles all APIKeys endpoints.
 */
@Controller
@RequestMapping(path = "/api/apikeys")
@Api(tags = "ApiKey operations")
@ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "IDENTITY", required = true, dataType = "java.lang.String"),
        @ApiImplicitParam(paramType = "header", name = "KEY", required = true, dataType = "java.lang.String")
})
public class ApiKeyController {
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private StorageService storageService;

    private static final String[] CHARS = "abcdefghijklmnoprstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
    private static final int IDENTITY_LENGTH = 10;

    /**
     * Generates a secure random {@link String}.
     * 
     * @param length       The desired length for the string.
     * @param secureRandom A {@link SecureRandom} instance.
     * @return The random {@link String}.
     */
    private String generateString(int length, SecureRandom secureRandom) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(CHARS[secureRandom.nextInt(CHARS.length)]);
        }

        return builder.toString();
    }

    /**
     * ApiKey creation endpoint.
     * 
     * @param admin     Whether the new key should have admin priviledges.
     * @param ownerName Human friendly name of the key's owner.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Creates a new ApiKey")
    @PostMapping(path = "/new")
    public ResponseEntity<String> createNewApiKey(
            @ApiParam(value = "Whether the new key should have admin priviledges.") @RequestParam Boolean admin,
            @ApiParam(value = "Human friendly name of the key's owner.") @RequestParam String ownerName) {
        ApiKey apiKey = new ApiKey();
        apiKey.setAdmin(admin);
        apiKey.setOwnerName(ownerName);

        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            String identity;

            // Make sure identity is unique
            while (true) {
                identity = generateString(IDENTITY_LENGTH, secureRandom);

                if (!apiKeyRepository.findById(identity).isPresent())
                    break;
            }

            KeyPair keyPair = RSAUtils.generateKeys();

            // Store key
            if (!storageService.storeKey(keyPair.getPrivate(), identity))
                throw new FileSystemException("Could not store key");

            PublicKey publicKey = keyPair.getPublic();
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            apiKey.setIdentity(identity);

            apiKeyRepository.save(apiKey);

            // Reply with identity:publick key
            return ResponseEntity.ok().body(String.format("%s:%s%n", identity, publicKeyBase64));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ApiKey removal endpoint.
     * 
     * @param targetIdentity Identity corresponding to the ApiKey to remove
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Removes an existing ApiKey")
    @DeleteMapping(path = "/remove")
    public ResponseEntity<String> removeApiKey(
            @ApiParam(value = "Identity corresponding to the ApiKey to remove.") @RequestParam String targetIdentity) {
        Optional<ApiKey> requestedKeyToBeDeleted = apiKeyRepository.findById(targetIdentity);

        return requestedKeyToBeDeleted.map(keyToBeDeleted -> {
            if (storageService.removeKey(keyToBeDeleted.getIdentity())) {
                apiKeyRepository.delete(keyToBeDeleted);

                return ResponseEntity.ok().body(String.format("Key %s deleted.%n", targetIdentity));
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Grants permissions to access resources under a certain campus to an ApiKey.
     * 
     * @param campusId       The id of the campus the resources belong to.
     * @param targetIdentity The ApiKey's identity.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Grants permissions to access resources under a certain campus to an ApiKey.")
    @PatchMapping(path = "/addCampus")
    public ResponseEntity<String> addCampusToKey(
            @ApiParam(value = "The id of the campus the resources belong to") @RequestParam Long campusId,
            @ApiParam(value = "The ApiKey's identity") @RequestParam String targetIdentity) {
        Optional<ApiKey> requestKey = apiKeyRepository.findById(targetIdentity);
        Optional<Campus> requestCampus = campusRepository.findById(campusId);

        return requestKey.map(apiKey -> requestCampus.map(campus -> {
            Set<Campus> campusesSet = apiKey.getCampuses();
            campusesSet.add(campus);

            apiKey.setCampuses(campusesSet);

            apiKeyRepository.save(apiKey);

            return ResponseEntity.ok().body(String.format("Campus %d added to key %s.%n", campusId, targetIdentity));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Removes permissions to access resources under a certain campus from an
     * ApiKey.
     * 
     * @param campusId       The id of the campus the resources belong to.
     * @param targetIdentity The ApiKey's identity.
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Removes permissions to access resources under a certain campus from an ApiKey.")
    @PatchMapping(path = "/removeCampus")
    public ResponseEntity<String> removeCampusFromKey(
            @ApiParam(value = "The id of the campus to remove permissions to.") @RequestParam Long campusId,
            @ApiParam(value = "The ApiKey's identity") @RequestParam String targetIdentity) {
        Optional<ApiKey> requestKey = apiKeyRepository.findById(targetIdentity);
        Optional<Campus> requestCampus = campusRepository.findById(campusId);

        return requestKey.map(apiKey -> requestCampus.map(campus -> {
            Set<Campus> campusesSet = apiKey.getCampuses();

            if (campusesSet.contains(campus)) {
                campusesSet.remove(campus);

                apiKey.setCampuses(campusesSet);

                apiKeyRepository.save(apiKey);

                return ResponseEntity.ok()
                        .body(String.format("Campus %d removed from key %s.%n", campusId, targetIdentity));
            }

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
