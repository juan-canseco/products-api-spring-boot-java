package com.example.productsapi.repositories;

import com.example.productsapi.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository  extends JpaRepository<ProductEntity, Long> { }
