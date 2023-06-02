package sweng.penelope.controllers;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;
import sweng.penelope.entities.Bird;
import sweng.penelope.entities.Campus;
import sweng.penelope.repositories.BirdRepository;
import sweng.penelope.repositories.CampusRepository;

/**
 * <code>BirdController</code> handles all Bird endpoints.
 */
@Controller
@RequestMapping(path = "/api/birds")
@Api(tags = "Bird operations")
@ApiImplicitParams({
        @ApiImplicitParam(paramType = "header", name = "IDENTITY", required = true, dataType = "java.lang.String"),
        @ApiImplicitParam(paramType = "header", name = "KEY", required = true, dataType = "java.lang.String")
})
@Validated
public class BirdController {
    @Autowired
    private BirdRepository birdRepository;
    @Autowired
    private CampusRepository campusRepository;
    @Autowired
    private CacheManager cacheManager;

    /**
     * Creates a new Bird belonging to the relevant campus.
     * 
     * @param name             The bird's name
     * @param listImageURL     URL to the image displayed in the birds list
     * @param heroImageURL     URL to the main bird image
     * @param soundURL         URL to the bird's sound
     * @param aboutMe          About the bird text information
     * @param aboutMeVideoURL  URL to the bird's video
     * @param location         Bird location text information
     * @param locationImageURL URL to the bird's location image
     * @param diet             Text information about the bird's diet
     * @param dietImageURL     URL to the bird's diet image
     * @param campusId         ID of the campus the bird belongs to
     * @param authentication   {@link Authentication} autowired
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Creates a new Bird belonging to the relevant campus.")
    @PostMapping(path = "{campusId}/new")
    public ResponseEntity<String> newDuck(
            @ApiParam(value = "The bird's name") @RequestParam @NotBlank @Size(max = 20) String name,
            @ApiParam(value = "URL to the image displayed in the birds list") @RequestParam @NotBlank String listImageURL,
            @ApiParam(value = "URL to the main bird image") @RequestParam @NotBlank String heroImageURL,
            @ApiParam(value = "URL to the bird's sound") @RequestParam @NotBlank String soundURL,
            @ApiParam(value = "About the bird text information") @RequestParam @NotBlank String aboutMe,
            @ApiParam(value = "URL to the bird's video") @RequestParam @NotBlank String aboutMeVideoURL,
            @ApiParam(value = "Bird location text information") @RequestParam @NotBlank String location,
            @ApiParam(value = "URL to the bird's location image") @RequestParam @NotBlank String locationImageURL,
            @ApiParam(value = "Text information about the bird's diet") @RequestParam @NotBlank String diet,
            @ApiParam(value = "URL to the bird's diet image") @RequestParam @NotBlank String dietImageURL,
            @ApiParam(value = "ID of the campus the bird belongs to") @PathVariable Long campusId,
            @ApiIgnore Authentication authentication) {

        Optional<Campus> campusRequest = campusRepository.findById(campusId);

        return campusRequest.map(campus -> {
            String author = ControllerUtils.getAuthorName(authentication);

            Bird bird = new Bird(name, listImageURL, heroImageURL, soundURL, aboutMe, aboutMeVideoURL, location,
                    locationImageURL,
                    diet, dietImageURL, campus, author);

            birdRepository.save(bird);

            CacheUtils.evictCache(cacheManager, CacheUtils.CAMPUSES, campusId);

            return ResponseEntity.ok().body(String.format("Bird \"%s\" created with id %d%n", name, bird.getId()));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Changes bird information
     * 
     * @param id               The bird's id
     * @param name             The bird's name - Optional
     * @param heroImageURL     URL to the main bird image - Optional
     * @param soundURL         URL to the bird's sound - Optional
     * @param aboutMe          About the bird text information - Optional
     * @param aboutMeVideoURL  URL to the bird's video - Optional
     * @param location         Bird location text information - Optional
     * @param locationImageURL URL to the bird's location image - Optional
     * @param diet             Text information about the bird's diet - Optional
     * @param dietImageURL     URL to the bird's diet image - Optional
     * @param campusId         ID of the campus the bird belongs to
     * @param authentication   {@link Authentication} autowired
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Changes bird information")
    @PatchMapping(path = "{campusId}/edit")
    public ResponseEntity<String> updateDuck(
            @ApiParam(value = "The bird's id") @RequestParam Long id,
            @ApiParam(value = "The bird's name") @RequestParam(required = false) Optional<String> name,
            @ApiParam(value = "URL to the main bird image") @RequestParam(required = false) Optional<String> heroImageURL,
            @ApiParam(value = "URL to the bird's sound") @RequestParam(required = false) Optional<String> soundURL,
            @ApiParam(value = "About the bird text information") @RequestParam(required = false) Optional<String> aboutMe,
            @ApiParam(value = "URL to the bird's video") @RequestParam(required = false) Optional<String> aboutMeVideoURL,
            @ApiParam(value = "Bird location text information") @RequestParam(required = false) Optional<String> location,
            @ApiParam(value = "URL to the bird's location image") @RequestParam(required = false) Optional<String> locationImageURL,
            @ApiParam(value = "Text information about the bird's diet") @RequestParam(required = false) Optional<String> diet,
            @ApiParam(value = "URL to the bird's diet image") @RequestParam(required = false) Optional<String> dietImageURL,
            @ApiParam(value = "ID of the campus the bird belongs to") @PathVariable Long campusId,
            @ApiIgnore Authentication authentication) {

        Optional<Bird> requestBird = birdRepository.findById(id);
        if (requestBird.isPresent()) {
            Bird bird = requestBird.get();
            String author = ControllerUtils.getAuthorName(authentication);
            Long previousCampus = bird.getCampus().getId();

            // This is 7yo writing python code quality. Look into
            // https://www.baeldung.com/spring-data-partial-update#1-mapping-strategy
            if (name.isPresent())
                bird.setName(name.get());
            if (heroImageURL.isPresent())
                bird.setHeroImageURL(heroImageURL.get());
            if (soundURL.isPresent())
                bird.setSoundURL(soundURL.get());
            if (aboutMe.isPresent())
                bird.setAboutMe(aboutMe.get());
            if (aboutMeVideoURL.isPresent())
                bird.setAboutMeVideoURL(aboutMeVideoURL.get());
            if (location.isPresent())
                bird.setLocation(location.get());
            if (locationImageURL.isPresent())
                bird.setLocationImageURL(locationImageURL.get());
            if (diet.isPresent())
                bird.setDiet(diet.get());
            if (dietImageURL.isPresent())
                bird.setDietImageURL(dietImageURL.get());

            String currentAuthors = bird.getAuthor();
            if (!currentAuthors.contains(author))
                currentAuthors += ", " + author;

            bird.setAuthor(currentAuthors);

            birdRepository.save(bird);

            Long currentCampus = bird.getCampus().getId();
            if (!currentCampus.equals(previousCampus))
                CacheUtils.evictCache(cacheManager, CacheUtils.CAMPUSES, currentCampus);

            CacheUtils.evictCache(cacheManager, CacheUtils.CAMPUSES, previousCampus);

            CacheUtils.evictCache(cacheManager, CacheUtils.BIRDS, bird.getId());

            return ResponseEntity.ok().body(String.format("Bird \"%s\" updated%n", bird.getName()));
        } else
            return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a bird
     * 
     * @param id       The bird's id
     * @param campusId ID of the campus the bird belongs to
     * @return {@link ResponseEntity}
     */
    @ApiOperation("Deletes a bird")
    @DeleteMapping(path = "{campusId}/remove")
    public ResponseEntity<String> removeDuck(@ApiParam(value = "The bird's id") @RequestParam Long id,
            @ApiParam(value = "ID of the campus the bird belongs to") @PathVariable Long campusId) {
        Optional<Bird> requestDuck = birdRepository.findById(id);

        return requestDuck.map(duck -> {
            CacheUtils.evictCache(cacheManager, CacheUtils.CAMPUSES, duck.getCampus().getId());
            CacheUtils.evictCache(cacheManager, CacheUtils.BIRDS, duck.getId());

            birdRepository.delete(duck);

            return ResponseEntity.ok().body(String.format("Bird %d removed from database.%n", id));
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
