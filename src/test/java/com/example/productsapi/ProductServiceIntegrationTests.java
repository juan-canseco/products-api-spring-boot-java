package com.example.productsapi;

import com.example.productsapi.cache.ProductCacheRepository;
import com.example.productsapi.dtos.CreateProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import com.example.productsapi.repositories.ProductRepository;
import com.example.productsapi.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductServiceIntegrationTests {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductCacheRepository cacheRepository;

    @Autowired
    private ProductService service;

    @BeforeEach
    public void cleanBefore() {
        repository.deleteAll();
        cacheRepository.deleteAll();
    }

    @Test
    public void productServiceWhenCreateShouldPutProductInCache() {
        var expectedProductId = 2L;
        var createDto = new CreateProductDto("Xbox One S", 399.99d);
        var resultDto = service.create(createDto);
        var recentlyCreatedProduct = repository.findById(expectedProductId);
        var recentlyCachedProduct = cacheRepository.findById(expectedProductId);
        assertEquals(expectedProductId, resultDto.getId());
        assertTrue(recentlyCachedProduct.isPresent());
        assertTrue(recentlyCreatedProduct.isPresent());
    }


    @Test
    public void productServiceWhenUpdateShouldPutProductInCache() {
        var expectedProductId = 1L;
        var createDto = new CreateProductDto("Xbox One S", 399.99d);
        service.create(createDto);
        var updateDto = new UpdateProductDto(expectedProductId, "Xbox One S", 399.99d);
        var result = service.update(updateDto);
        assertNotNull(result);
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getPrice(), result.getPrice());
    }

    @Container
    static MySQLContainer database = new MySQLContainer("mysql:latest")
            .withDatabaseName("products_db")
            .withPassword("admin1234");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.datasource.url", database::getJdbcUrl);
        propertyRegistry.add("spring.datasource.password", database::getPassword);
        propertyRegistry.add("spring.datasource.username", database::getUsername);
        propertyRegistry.add("spring.redis.host", redis::getHost);
        propertyRegistry.add("spring.redis.port", redis::getFirstMappedPort);

    }
}
