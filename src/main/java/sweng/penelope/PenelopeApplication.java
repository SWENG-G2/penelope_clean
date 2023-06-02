package sweng.penelope;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;

import sweng.penelope.auth.RSAUtils;
import sweng.penelope.entities.DataManager;
import sweng.penelope.repositories.DataManagerRepository;
import sweng.penelope.services.StorageService;

@SpringBootApplication
@EnableCaching
public class PenelopeApplication {
	@Value("${penelope.adminUsername:}")
	private String adminDefaultUsername;

	@Value("${penelope.adminPassword:}")
	private String adminDefaultPassword;

	public static void main(String[] args) {
		SpringApplication.run(PenelopeApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		// Initialise storage service
		return args -> storageService.init();
	}

	@Bean
	KeyPair serverKeyPair() throws NoSuchAlgorithmException {
		return RSAUtils.generateKeys();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(BCryptVersion.$2A, 10);
	}

	@Bean
	@ConditionalOnProperty(name = "penelope.inject-admin", havingValue = "true")
	CommandLineRunner injectAdmin(DataManagerRepository dataManagerRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			Optional<DataManager> existingAdmin = dataManagerRepository.findById(adminDefaultUsername);

			if (existingAdmin.isEmpty()) {
				DataManager admin = new DataManager();
				admin.setUsername(adminDefaultUsername);
				admin.setPassword(passwordEncoder.encode(adminDefaultPassword));
				admin.setSysadmin(true);

				dataManagerRepository.save(admin);
			}
		};
	}
}
