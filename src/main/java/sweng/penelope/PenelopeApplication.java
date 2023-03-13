package sweng.penelope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PenelopeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PenelopeApplication.class, args);
	}

}
