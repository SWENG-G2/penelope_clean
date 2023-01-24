package sweng.penelope.repositories;

import org.springframework.data.repository.CrudRepository;

import sweng.penelope.entities.Campus;

public interface CampusRepository extends CrudRepository<Campus, Long> {
    
}
