# Products API
Simple REST API of a crud of products made with the purpose of teach me basic unit and integration test.

# Intro 
In this project implements a ProductService who contains two repos first is ProductRepository(JPA) and the second is ProductCacheRepository(Redis Cache).
Every time when a product is created this product should be put in cache same for update



## Product Service
```java
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductCacheRepository cacheRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductCacheRepository cacheRepository) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
    }


    @Override
    public ProductDto create(CreateProductDto dto) {

        var entity = new ProductEntity();

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());

        var result = this.repository.saveAndFlush(entity);

        putProductToCache(result);

        return new ProductDto(
                result.getId(),
                result.getName(),
                result.getPrice());
    }

    @Override
    public ProductDto update(UpdateProductDto dto) {

        var opt = repository.findById(dto.getId());

        if (opt.isEmpty())
            throw new RecordNotFoundException("Product with the Id " + dto.getId() + " was not found");

        var entity = opt.get();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        var result = repository.save(entity);
        putProductToCache(result);
        return new ProductDto(result.getId(), result.getName(), result.getPrice());
    }

    @Override
    public void delete(long productId) {
        if (!repository.existsById(productId))
            throw new RecordNotFoundException("Product with the Id " + productId + " was not found");
        cacheRepository.deleteById(productId);
        repository.deleteById(productId);
    }

    @Override
    public Optional<ProductDto> getById(long productId) {

        if (cacheRepository.existsById(productId)) {
            var entity = cacheRepository.findById(productId);
            return entity.map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()));
        }

        var entity = repository.findById(productId);

        if (entity.isPresent()) {
            putProductToCache(entity.get());
            return entity.map((p) -> new ProductDto(p.getId(), p.getName(), p.getPrice()));
        }

        return Optional.empty();
    }

    @Override
    public List<ProductDto> getAll() {
        return repository.findAll()
                .stream()
                .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
                .collect(Collectors.toList());
    }

    private void putProductToCache(ProductEntity entity) {
        var cacheProduct = new ProductCacheEntity();
        cacheProduct.setId(entity.getId());
        cacheProduct.setName(entity.getName());
        cacheProduct.setPrice(entity.getPrice());
        this.cacheRepository.save(cacheProduct);
    }
}

```

## API Integration Tests
```java
@WebMvcTest
public class ProductsControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProductService service;

    @Test
    public void productsGetByIdWhenProductExistsStatusShouldBeOk() throws Exception {

        long productId = 1;
        var product = new ProductDto(productId, "Xbox Series X", 1000.25d);
        var expectedOptional = Optional.of(product);
        Mockito.when(service.getById(productId)).thenReturn(expectedOptional);

        var request = MockMvcRequestBuilders
                .get("/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("Xbox Series X")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price", Matchers.is(1000.25d)));

    }

    @Test
    public void productsGetByIdWhenProductNotExistsStatusShouldBeNotFound() throws Exception {
        long productId = 1;
        Optional<ProductDto> expectedOptional = Optional.empty();

        Mockito.when(service.getById(productId)).thenReturn(expectedOptional);

        var request = MockMvcRequestBuilders
                .get("/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void productsDeleteWhenProductExistsStatusShouldBeOk() throws Exception {
        long productId = 1;

        Mockito.doAnswer((i) -> null).when(service).delete(productId);

        var request = MockMvcRequestBuilders
                .delete("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void productsDeleteWhenProductNotExistsStatusShouldBeNotFound() throws Exception {

        long productId = 1;
        Mockito.doThrow(RecordNotFoundException.class).when(service).delete(productId);

        var request = MockMvcRequestBuilders
                .delete("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void productsUpdateWhenProductExistsStatusShouldBeOk() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                productId,
                "Xbox One",
                1000.50d);

        var dto = new ProductDto(
                productId,
                "Xbox One",
                1000.50d);

        Mockito.when(service.update(updateProductDto)).thenReturn(dto);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void productsUpdateWhenProductNotExistsStatusShouldBeNotFound() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                productId,
                "Xbox One",
                1000.50d);

        Mockito.when(service.update(Mockito.any(UpdateProductDto.class))).thenThrow(RecordNotFoundException.class);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void productsUpdateWhenOverPostingStatusShouldBeBadRequest() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                2,
                "Xbox One",
                1000.50d);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }

    @Test
    public void productsGetAllStatusShouldBeOk() throws Exception {

        var products = List.of(new ProductDto(1, "Xbox One", 7000d),
                new ProductDto(2, "PlayStation 4", 8000d));

        Mockito.when(service.getAll()).thenReturn(products);

        var request = MockMvcRequestBuilders
                .get("/products/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()));
    }
}
```

## Service Integration Tests
```java
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

```


## Libraries
* JPA
* Redis 
* Test Containers

