package ru.muravin.marketplaceshowcase.configurations;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;

public class PostgreSQLContainer {
    @Container
    @ServiceConnection
    static final org.testcontainers.containers.PostgreSQLContainer<?> postgreSQLContainer
            = new org.testcontainers.containers.PostgreSQLContainer<>()
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
            .withExposedPorts(3306);
}
