package ru.muravin.marketplaceshowcase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MarketplaceShowcaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketplaceShowcaseApplication.class, args);
	}

}
