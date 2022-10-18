package com.example.productsapi.services;

import com.example.productsapi.dtos.CreateProductDto;
import com.example.productsapi.dtos.ProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    ProductDto create(CreateProductDto dto);
    ProductDto update(UpdateProductDto dto);
    void delete(long productId);
    Optional<ProductDto> getById(long productId);
    List<ProductDto> getAll();
}
