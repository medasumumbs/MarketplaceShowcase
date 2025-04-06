package ru.muravin.marketplaceshowcase;

import org.springframework.boot.SpringApplication;

public class TestMarketplaceShowcaseApplication {

	public static void main(String[] args) {
		SpringApplication.from(MarketplaceShowcaseApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
