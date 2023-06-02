package sweng.penelope.repositories;

import org.springframework.data.repository.CrudRepository;

import sweng.penelope.entities.DataManager;


public interface DataManagerRepository extends CrudRepository<DataManager, String>{
    DataManager findByUsername(String username);
}
