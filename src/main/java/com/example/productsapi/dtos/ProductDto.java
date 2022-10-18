package com.example.productsapi.dtos;

import lombok.*;

@EqualsAndHashCode
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private long id;
    private String name;
    private Double price;
}
