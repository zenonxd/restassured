package com.educandoweb.dscommerce.controllers;

import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {


    private Long existingId, nonExistingId, dependantId;

    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setUp() {
        //baseUri pertence ao RestAssured
        baseURI = "http://localhost:8080";

        postProductInstance = new HashMap<>();
        postProductInstance.put("name", "Meu produto");
        postProductInstance.put("description", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit et odit eaque reprehenderit ex maxime delectus dolore labore, quisquam quae tempora natus esse aliquam veniam doloremque quam minima culpa alias maiores commodi. Perferendis enim");
        postProductInstance.put("imgUrl", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postProductInstance.put("price", 50.0F);

        //as categorias também são um conjunto que possuem dois tipos de dados
        //repare no Postman que será inserido uma Lista do mesmo jeito, dessa vez com "id"
        List<Map<String, Object>> categories = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 2);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 3);

        categories.add(category1);
        categories.add(category2);

        postProductInstance.put("categories", categories);
    }

    @Test
    public void findByIdShouldReturnProductWhenIdExists() {
        existingId = 2L;

        given()
                .get("/products/{id}", existingId)
                .then()
                .statusCode(200)
                .assertThat().body("id", is(2))
                //para comparar String, usamos equalTo
                .assertThat().body("name", equalTo("Smart TV"))
                .assertThat().body("imgUrl", equalTo("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/2-big.jpg"))
                .assertThat().body("price", is(2190.0F))
                //para verificarmos arrays
                .assertThat().body("categories.id", hasItems(3, 2))
                .assertThat().body("categories.name", hasItems("Eletrônicos", "Computadores"));
    }

    @Test
    public void findAllShouldReturnAllProductsWhenNameIsEmpty() {

        given()
                .get("/products?page=0")
                .then()
                .statusCode(200)
                .assertThat().body("content.name", hasItems("Macbook Pro", "PC Gamer Tera"));
    }

    @Test
    public void findAllShouldReturnPagedWhenNameIsFilled() {

        given()
        .get("/products?page=0&pageSize=10&name={name}", "Macbook Pro")
                .then()
                .statusCode(200)
                .assertThat().body("content.id[0]", is(3))
                .assertThat().body("content.name[0]", equalTo("Macbook Pro"))
                .assertThat().body("content.price[0]", is(1250.0F));

    }

    @Test
    public void findAllShouldReturnPagedWhenProductPriceGreaterThan2000() {

        given()
                //usamos tamanho 25 para obter todos os produtos
                .get("/products?size=25")
                .then()
                .statusCode(200)
                //chamamos a lista de content (do postman), procurando todos os produtos
                //que possuem preço maior que 2000 e pegamos somente o name deles
                .body("content.findAll { it.price > 2000}.name", hasItems("Smart TV", "PC Gamer Weed"));
    }

    @Test
    public void insertShouldReturnProductCreatedWhenAdminLogged() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        //parei aqui, falta inserir o TokenUtil e continuar


        given()
                //isso serve pra especificar o tipo da informação
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newProduct)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)

                .when()

                .port("/products")
                .then()
                .statusCode(201)
                .body("name", equalTo("Meu produto"))
                .body("price", is(50.0F))
                .body("categories", hasItems(2, 3))

    }
}
