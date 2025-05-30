package ru.muravin.marketplaceshowcase;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
				.withDatabaseName("mydatabase")
				.withUsername("myuser")
				.withPassword("secret")
				.withExposedPorts(5432);
	}
	@Bean
	@ServiceConnection
	RedisContainer redisContainer() {
		return new RedisContainer(DockerImageName.parse("redis:6.2.1"));
	}
}
