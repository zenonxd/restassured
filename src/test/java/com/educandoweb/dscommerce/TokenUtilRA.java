package com.educandoweb.dscommerce;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class TokenUtilRA {

    public static String obtainAccessToken(String username, String password) throws Exception {
        Response response = authRequest(username, password);

        JsonPath jsonBody = response.jsonPath();

        return jsonBody.getString("access_token");
    }


    private static Response authRequest(String username, String password) throws Exception {
        return given()
                .auth()
                .preemptive()
                .basic("myclientid", "myclientsecret")
                //contentType extamente o que estamos passando na requisição de login
                //do Postman (no body)
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post("/oauth2/token");

    }
}
