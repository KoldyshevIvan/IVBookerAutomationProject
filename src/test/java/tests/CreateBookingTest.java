package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private Response response;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        step("Создать объект для нового бронирования", () -> {
                    newBooking = new NewBooking();
                    newBooking.setFirstname("Ivan");
                    newBooking.setLastname("Ivanov");
                    newBooking.setTotalprice(111);
                    newBooking.setDepositpaid(true);
                    newBooking.setBookingdates(new NewBooking.Bookingdates("2026-01-01", "2026-02-02"));
                    newBooking.setAdditionalneeds("Breakfast");
                }
        );
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testCreateBooking() {

        step("Отправить JSON на энпоинт /booking и создать новое бронирование", () ->
                {
                    String requestBody = objectMapper.writeValueAsString(newBooking);
                    response = apiClient.createBooking(requestBody);
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(response.getStatusCode()).isEqualTo(200)
                    );
                    String responseBody = response.asString();
                    createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);
                }
        );

        step("Проверить параметры созданного бронирования", () -> {
                    assertThat(createdBooking).isNotNull();
                    assertEquals(createdBooking.getBooking().getFirstname(), newBooking.getFirstname());
                    assertEquals(createdBooking.getBooking().getLastname(), newBooking.getLastname());
                    assertEquals(createdBooking.getBooking().getTotalprice(), newBooking.getTotalprice());
                    assertEquals(createdBooking.getBooking().getDepositpaid(), newBooking.getDepositpaid());
                    assertEquals(createdBooking.getBooking().getBookingdates().getCheckin(), newBooking.getBookingdates().getCheckin());
                    assertEquals(createdBooking.getBooking().getBookingdates().getCheckout(), newBooking.getBookingdates().getCheckout());
                    assertEquals(createdBooking.getBooking().getAdditionalneeds(), newBooking.getAdditionalneeds());
                }
        );
    }

    @AfterEach
    public void tearDown() {

        step("Удалить созданное бронирование", () -> {
                    apiClient.createToken("admin", "password123");
                    apiClient.deleteBooking(createdBooking.getBookingid());

                    step("Проверить, что статус-код ответа == 404", () ->
                            assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404));
                }
        );
    }
}
