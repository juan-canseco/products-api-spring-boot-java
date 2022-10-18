package com.example.productsapi.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "products")
public class ProductEntity {
    public ProductEntity(){}
    @Id
    @GeneratedValue
    private long id;
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    @Column(name = "price", nullable = false)
    private Double price;
}
