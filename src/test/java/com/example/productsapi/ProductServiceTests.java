package com.example.productsapi;

import com.example.productsapi.cache.ProductCacheEntity;
import com.example.productsapi.cache.ProductCacheRepository;
import com.example.productsapi.dtos.CreateProductDto;
import com.example.productsapi.dtos.ProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import com.example.productsapi.entities.ProductEntity;
import com.example.productsapi.exceptions.RecordNotFoundException;
import com.example.productsapi.repositories.ProductRepository;
import com.example.productsapi.services.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository repository;
    @Mock
    private ProductCacheRepository cacheRepository;
    @InjectMocks
    private ProductServiceImpl service;

    @Test
    public void productServiceWhenCreateShouldReturnDto() {
        var createDto = new CreateProductDto("Playstation 4", 500.99d);
        var expectedDto = new ProductDto(1, "Playstation 4",500.99d);
        var expectedEntity = new ProductEntity(1, "Playstation 4",500.99d);
        var cacheEntity = new ProductCacheEntity();
        cacheEntity.setId(1);
        cacheEntity.setName("Playstation 4");
        cacheEntity.setPrice(500.99d);
        when(repository.saveAndFlush(any(ProductEntity.class))).thenReturn(expectedEntity);
        when(cacheRepository.save(any(ProductCacheEntity.class))).thenReturn(any(ProductCacheEntity.class));
        var result = service.create(createDto);
        assertEquals(expectedDto, result);
        verify(cacheRepository).save(any(ProductCacheEntity.class));
    }

    @Test
    public void productServiceUpdateWhenProductNotExistsShouldThrowException() {
        var dto = new UpdateProductDto(1L, "Playstation 4", 499.99d);
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RecordNotFoundException.class, () -> service.update(dto));
    }

    @Test
    public void productServiceUpdateWhenProductExistsShouldReturnDto() {
        var updateDto = new UpdateProductDto(1L, "Playstation 4 Pro", 599.99d);
        var expectedDto = new ProductDto(1L, "Playstation 4 Pro", 599.99d);
        var entity = new ProductEntity(1L, "Playstation 4", 499.99d);
        var result = new ProductEntity(1L, "Playstation 4 Pro", 599.99d);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(result);
        when(cacheRepository.save(any(ProductCacheEntity.class))).thenReturn(any(ProductCacheEntity.class));
        var resultDto = service.update(updateDto);
        assertEquals(expectedDto, resultDto);
        verify(cacheRepository).save(any(ProductCacheEntity.class));
    }

    @Test
    public void productServiceDeleteWhenProductNotExistsShouldThrowException() {
        var productId = 1L;
        when(repository.existsById(productId)).thenReturn(false);
        assertThrows(RecordNotFoundException.class, () -> service.delete(productId));
    }

    @Test
    public void productServiceDeleteWhenProductExistsShouldDelete() {
        var productId = 1L;
        when(repository.existsById(productId)).thenReturn(true);
        doNothing().when(repository).deleteById(productId);
        doNothing().when(cacheRepository).deleteById(productId);
        service.delete(productId);
        verify(repository, times(1)).deleteById(productId);
        verify(cacheRepository, times(1)).deleteById(productId);
    }

    @Test
    public void productServiceGetByIdWhenProductIsInCacheShouldReturnOptionalOfDto() {
        var productId = 1L;
        var productCache = new ProductCacheEntity();
        productCache.setId(productId);
        productCache.setName("Playstation 4");
        productCache.setPrice(399.99d);
        var cacheOpt = Optional.of(productCache);
        var expectedOpt = Optional.of(new ProductDto(1L,"Playstation 4", 399.99d));
        when(cacheRepository.existsById(productId)).thenReturn(true);
        when(cacheRepository.findById(productId)).thenReturn(cacheOpt);
        var resultOpt = service.getById(productId);
        assertEquals(expectedOpt, resultOpt);
    }

    @Test
    public void productServiceGetByIdWhenProductIsNotInCacheButExistsShouldPutInCacheAndReturnOptionalOfDto() {
        var productId = 1L;
        var entity = Optional.of(new ProductEntity(1L, "Playstation 4", 399.99d));
        var expectedOpt = Optional.of(new ProductDto(1L, "Playstation 4", 399.99d));
        when(cacheRepository.existsById(productId)).thenReturn(false);
        when(repository.findById(productId)).thenReturn(entity);
        when(cacheRepository.save(any(ProductCacheEntity.class))).thenReturn(any(ProductCacheEntity.class));
        var resultOpt = service.getById(productId);
        assertEquals(expectedOpt, resultOpt);
        verify(cacheRepository).save(any(ProductCacheEntity.class));
    }

    @Test
    public void productServiceGetByIdWhenProductIsNotInCacheAndNotExistsShouldReturnEmptyOptional() {
        var productId = 1L;
        when(cacheRepository.existsById(productId)).thenReturn(false);
        when(repository.findById(productId)).thenReturn(Optional.empty());
        var result = service.getById(productId);
        assertTrue(result.isEmpty());
    }

}
