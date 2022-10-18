package com.example.productsapi.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateProductDto {
    private String name;
    private Double price;
}
