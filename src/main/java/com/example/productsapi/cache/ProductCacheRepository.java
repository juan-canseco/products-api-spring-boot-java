package com.example.productsapi.cache;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCacheRepository extends CrudRepository<ProductCacheEntity, Long> { }
