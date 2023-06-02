package sweng.penelope.repositories;

import org.springframework.data.repository.CrudRepository;

import sweng.penelope.entities.Campus;

/**
 * <code>CampusRepository</code> is a {@link CrudRepository} which handles {@link Campus}
 */
public interface CampusRepository extends CrudRepository<Campus, Long> {
    
}
