package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {

    private final String baseurl;
    private String token;

    public APIClient() {
        this.baseurl = determineBaseUrl();
    }

    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file: " + configFileName, e);
        }

        return properties.getProperty("baseUrl");
    }

    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseurl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter());
    }

    //GET запрос на эндпоинт /ping
    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) // Используем ENUM для эндпоинта /ping
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    //GET запрос на эндпоинт /booking
    public Response getBooking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath()) // Используем ENUM для эндпоинта /ping
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response getBookingById(int id) {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/" + id) // Используем ENUM для эндпоинта /ping
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    // Метод получения токена
    public void createToken(String username, String password) {
        String requestBody = String.format("{ \"username\": \"%s\", \"password\": \"%s\" }", username, password);

        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post(ApiEndpoints.AUTH.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();

        token = response.jsonPath().getString("token");
    }

    // Фильтр для добавления токена в заголовок Authorization
    private Filter addAuthTokenFilter() {
        return (FilterableRequestSpecification requestSpec, FilterableResponseSpecification responceSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
            }
            return ctx.next(requestSpec, responceSpec);
        };
    }

    // DELETE запрос на эндпоинт /booking
    public Response deleteBooking(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId) // Указываем path parametr для ID
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .response();
    }
}
