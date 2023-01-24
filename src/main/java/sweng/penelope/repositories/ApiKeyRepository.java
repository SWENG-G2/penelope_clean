package sweng.penelope.repositories;

import org.springframework.data.repository.CrudRepository;

import sweng.penelope.entities.ApiKey;

public interface ApiKeyRepository extends CrudRepository<ApiKey, String> {
    
}
