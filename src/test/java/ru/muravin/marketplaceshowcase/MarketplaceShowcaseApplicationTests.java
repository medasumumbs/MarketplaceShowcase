package ru.muravin.marketplaceshowcase;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.muravin.marketplaceshowcase.configurations.PostgreSQLContainer;

@Import(PostgreSQLContainer.class)
@SpringBootTest
class MarketplaceShowcaseApplicationTests {

	@Test
	void contextLoads() {
	}

}
