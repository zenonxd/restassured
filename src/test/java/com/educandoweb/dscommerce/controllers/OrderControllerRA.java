package com.educandoweb.dscommerce.controllers;

import com.educandoweb.dscommerce.TokenUtilRA;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.baseURI;

public class OrderControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String clientToken, adminToken, invalidToken;
    private Long existingId, nonExistingId, dependantId;

    @BeforeEach
    void setUp() throws Exception {
        baseURI = "http://localhost:8080";

        clientUsername = "maria@gmail.com";
        clientPassword = "123456";
        clientToken = TokenUtilRA.obtainAccessToken(clientUsername, clientPassword);

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";
        adminToken = TokenUtilRA.obtainAccessToken(adminUsername, adminPassword);

        invalidToken = adminToken + "xpto"; // invalid token
    }

    @Test
    public void findByIdShouldReturnOrderWhenLoggedAsAdmin() {

        existingId = 1L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingId)
                .then()
                .statusCode(200)
                .assertThat().body("id", equalTo(1))
                .assertThat().body("moment", equalTo("2022-07-25T13:00:00Z"))
                .assertThat().body("status", equalTo("PAID"))
                .assertThat().body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
    }

    @Test
    public void findByIdShouldReturnOrderWhenLoggedAsClientAndOrderBelongsToClient() {

        existingId = 1L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingId)
                .then()
                .statusCode(200)
                .assertThat().body("client.name", equalTo("Maria Brown"))
                .assertThat().body("id", equalTo(1))
                .assertThat().body("moment", equalTo("2022-07-25T13:00:00Z"))
                .assertThat().body("status", equalTo("PAID"))
                .assertThat().body("items.name", hasItems("The Lord of the Rings", "Macbook Pro"));
    }

    @Test
    public void findByIdShouldReturn403WhenLoggedAsClientAndOrderBelongsToOtherClient() {

        existingId = 2L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingId)
                .then()
                .statusCode(403);
    }

    @Test
    public void findByIdShouldReturn404WhenOrderDoesntExistLoggedAsAdmin() {

        nonExistingId = 999L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturn404WhenOrderDoesntExistLoggedAsClient() {

        nonExistingId = 999L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", nonExistingId)
                .then()
                .statusCode(404);
    }

    @Test
    public void findByIdShouldReturn401WhenInvalidToken() {

        existingId = 1L;

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .accept(ContentType.JSON)
                .when()
                .get("/orders/{id}", existingId)
                .then()
                .statusCode(401);
    }
}
