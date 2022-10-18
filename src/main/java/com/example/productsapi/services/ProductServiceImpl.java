package com.example.productsapi.services;

import com.example.productsapi.cache.ProductCacheEntity;
import com.example.productsapi.cache.ProductCacheRepository;
import com.example.productsapi.dtos.CreateProductDto;
import com.example.productsapi.dtos.ProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import com.example.productsapi.entities.ProductEntity;
import com.example.productsapi.exceptions.RecordNotFoundException;
import com.example.productsapi.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
