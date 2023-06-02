package sweng.penelope.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import sweng.penelope.entities.Campus;
import sweng.penelope.entities.Bird;

/**
 * <code>BirdRepository</code> is a {@link CrudRepository} which handles {@link Bird}
 */
public interface BirdRepository extends CrudRepository<Bird, Long> {
    List<Bird> findByCampus(Campus campus);
}
