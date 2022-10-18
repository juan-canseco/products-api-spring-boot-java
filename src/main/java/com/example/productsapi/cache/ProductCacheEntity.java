package com.example.productsapi.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;

@RedisHash("Product")
@Getter
@Setter
@RequiredArgsConstructor
public class ProductCacheEntity implements Serializable {
    @Id
    private long id;
    private String name;
    private Double price;
}
