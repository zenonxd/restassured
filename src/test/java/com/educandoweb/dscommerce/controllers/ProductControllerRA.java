package com.educandoweb.dscommerce.controllers;

import com.educandoweb.dscommerce.TokenUtilRA;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ProductControllerRA {


    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingId, nonExistingId, dependantId;

    private Map<String, Object> postProductInstance;

    @BeforeEach
    public void setUp() throws Exception {
        //baseUri pertence ao RestAssured
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        clientToken = TokenUtilRA.obtainAccessToken(clientUsername, clientPassword);

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";
        adminToken = TokenUtilRA.obtainAccessToken(adminUsername, adminPassword);

        invalidToken = adminToken + "xpto"; // invalid token

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

        String productAsString = newProduct.toString();


        given()
                //isso serve para especificar o tipo da informação
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/products")
        .then()
                .statusCode(201)
                .body("name", equalTo("Meu produto"))
                .body("price", is(50.0F))
                .body("categories.id", hasItems(2, 3));

    }

    @Test
    public void insertShouldReturn422AndCustomMessageWhenLoggedAdminAndNameInvalid() {
        //colocando propriedade name invalida
        postProductInstance.put("name", "aa");
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Nome precisa ter de 3 a 80 caracteres"));

    }

    @Test
    public void insertShouldReturn422AndCustomMessageWhenAdminLoggedAndDescriptionInvalid() {
        //colocando propriedade description invalida
        postProductInstance.put("description", "aa");
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Descrição precisa ter no mínimo 10 caracteres"));
    }

    @Test
    public void insertShouldReturn422AndCustomMessageWhenAdminLoggedAndPriceNegative() {
        //colocando propriedade price negativa
        postProductInstance.put("price", -50.F);
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturn422AndCustomMessageWhenAdminLoggedAndPriceIsZero() {
        //colocando propriedade price zerada
        postProductInstance.put("price", -0.0F);
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("O preço deve ser positivo"));
    }

    @Test
    public void insertShouldReturn422AndCustomMessageWhenAdminLoggedAndNoCategories() {
        //colocando propriedade de categorias zerada
        postProductInstance.put("categories", null);
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();

        given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(422)
                .body("errors.message[0]", equalTo("Deve ter pelo menos uma categoria"));
    }

    @Test
    public void insertShouldReturn403WhenLoggedAsClient() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();


        given()
                //isso serve para especificar o tipo da informação
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(403);
    }

    @Test
    public void insertShouldReturn401WhenInvalidToken() {
        JSONObject newProduct = new JSONObject(postProductInstance);

        String productAsString = newProduct.toString();


        given()
                //isso serve para especificar o tipo da informação
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .body(productAsString)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/products")
                .then()
                .statusCode(401);
    }

    @Test
    public void deleteShouldReturnNoContentWhenLoggedAsAdmin() {
        existingId = 5L;

        given()
                //isso serve para especificar o tipo da informação
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", existingId)
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteShouldReturn404WhenLoggedAsAdmin() {
        nonExistingId = 288L;

        given()
                //isso serve para especificar o tipo da informação
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", nonExistingId)
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteShouldReturn400DependentIdWhenLoggedAsAdmin() {
        dependantId = 1L;

        given()
                //isso serve para especificar o tipo da informação
                .header("Authorization", "Bearer " + adminToken)

                .when()
                .delete("/products/{id}", dependantId)
                .then()
                .statusCode(400);
    }

    @Test
    public void deleteShouldReturn403WhenLoggedAsclient() {
        existingId = 5L;

        given()
                //isso serve para especificar o tipo da informação
                .header("Authorization", "Bearer " + clientToken)

                .when()
                .delete("/products/{id}", existingId)
                .then()
                .statusCode(403);
    }

    @Test
    public void deleteShouldReturn401WhenInvalidToken() {
        existingId = 5L;

        given()
                //isso serve para especificar o tipo da informação
                .header("Authorization", "Bearer " + invalidToken)

                .when()
                .delete("/products/{id}", existingId)
                .then()
                .statusCode(401);
    }


}
