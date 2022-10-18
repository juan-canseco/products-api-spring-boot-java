package com.example.productsapi.controllers;

import com.example.productsapi.dtos.CreateProductDto;
import com.example.productsapi.dtos.ProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import com.example.productsapi.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("products")
@RestController
public class ProductsController {
    private final ProductService service;

    @Autowired
    public ProductsController(ProductService service) {
        this.service = service;
    }

    @GetMapping("{productId}")
    public ResponseEntity<ProductDto> getById(@PathVariable long productId) {
        var opt = service.getById(productId);
        return opt.isEmpty()? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(opt.get());
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody CreateProductDto dto) {
        final var result = service.create(dto);
        return ResponseEntity.ok(result);
    }

    @PutMapping("{productId}")
    public ResponseEntity<ProductDto> update(@PathVariable long productId, @RequestBody UpdateProductDto dto) {
        if (productId != dto.getId())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final var result = service.update(dto);
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("{productId}")
    public ResponseEntity<?> delete(@PathVariable long productId) {
        service.delete(productId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/")
    private ResponseEntity<List<ProductDto>> getAll() {
        final var result = service.getAll();
        return ResponseEntity.ok(result);
    }


}
