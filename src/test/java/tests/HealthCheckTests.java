package tests;

import core.clients.APIClient;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class HealthCheckTests {
    private APIClient apiClient;

    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
    }

    //Тест на метод ping()
    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testPing(){
        Response response = apiClient.ping();
        assertThat(response.getStatusCode()).isEqualTo(201);
    }
}
