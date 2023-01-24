package sweng.penelope.services;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class StorageServiceTestConfiguration {
    @Bean
    @Primary
    public StorageService storageService() {
        return Mockito.mock(StorageService.class);
    }
}
