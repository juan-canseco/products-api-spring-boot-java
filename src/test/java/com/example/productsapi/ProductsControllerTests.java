package com.example.productsapi;

import com.example.productsapi.dtos.ProductDto;
import com.example.productsapi.dtos.UpdateProductDto;
import com.example.productsapi.exceptions.RecordNotFoundException;
import com.example.productsapi.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.List;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ProductsControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ProductService service;

    @Test
    public void productsGetByIdWhenProductExistsStatusShouldBeOk() throws Exception {

        long productId = 1;
        var product = new ProductDto(productId, "Xbox Series X", 1000.25d);
        var expectedOptional = Optional.of(product);
        Mockito.when(service.getById(productId)).thenReturn(expectedOptional);

        var request = MockMvcRequestBuilders
                .get("/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("Xbox Series X")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price", Matchers.is(1000.25d)));

    }

    @Test
    public void productsGetByIdWhenProductNotExistsStatusShouldBeNotFound() throws Exception {
        long productId = 1;
        Optional<ProductDto> expectedOptional = Optional.empty();

        Mockito.when(service.getById(productId)).thenReturn(expectedOptional);

        var request = MockMvcRequestBuilders
                .get("/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void productsDeleteWhenProductExistsStatusShouldBeOk() throws Exception {
        long productId = 1;

        Mockito.doAnswer((i) -> null).when(service).delete(productId);

        var request = MockMvcRequestBuilders
                .delete("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void productsDeleteWhenProductNotExistsStatusShouldBeNotFound() throws Exception {

        long productId = 1;
        Mockito.doThrow(RecordNotFoundException.class).when(service).delete(productId);

        var request = MockMvcRequestBuilders
                .delete("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void productsUpdateWhenProductExistsStatusShouldBeOk() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                productId,
                "Xbox One",
                1000.50d);

        var dto = new ProductDto(
                productId,
                "Xbox One",
                1000.50d);

        Mockito.when(service.update(updateProductDto)).thenReturn(dto);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    public void productsUpdateWhenProductNotExistsStatusShouldBeNotFound() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                productId,
                "Xbox One",
                1000.50d);

        Mockito.when(service.update(Mockito.any(UpdateProductDto.class))).thenThrow(RecordNotFoundException.class);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    public void productsUpdateWhenOverPostingStatusShouldBeBadRequest() throws Exception {

        long productId = 1;

        var updateProductDto =  new UpdateProductDto(
                2,
                "Xbox One",
                1000.50d);

        var request = MockMvcRequestBuilders
                .put("/products/" + productId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateProductDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }

    @Test
    public void productsGetAllStatusShouldBeOk() throws Exception {

        var products = List.of(new ProductDto(1, "Xbox One", 7000d),
                new ProductDto(2, "PlayStation 4", 8000d));

        Mockito.when(service.getAll()).thenReturn(products);

        var request = MockMvcRequestBuilders
                .get("/products/")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.notNullValue()));
    }
}
