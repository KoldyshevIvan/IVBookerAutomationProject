package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
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

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class GetAllBookingTest {
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
                    newBooking.setFirstname("Vovan");
                    newBooking.setLastname("Petrov");
                    newBooking.setTotalprice(222);
                    newBooking.setDepositpaid(false);
                    newBooking.setBookingdates(new NewBooking.Bookingdates("2025-02-01", "2026-02-02"));
                    newBooking.setAdditionalneeds("Breakfast");
                }
        );
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testGetAllBooking() {

        step("Отправить JSON на энпоинт /booking и создать новое бронирование", () ->
                {
                    String requestBody = objectMapper.writeValueAsString(newBooking);
                    Response response = apiClient.createBooking(requestBody);
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(response.getStatusCode()).isEqualTo(200));
                    String responseBody = response.asString();
                    createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);
                }
        );

        step("Получить список id всех бронирований", () ->
                {
                    Response listBookingResponse = apiClient.getBooking();
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(listBookingResponse.getStatusCode()).isEqualTo(200));

                    String responseListBookingBody = listBookingResponse.getBody().asString();
                    List<Booking> bookings = objectMapper.readValue(responseListBookingBody, new TypeReference<List<Booking>>() {
                    });
                    step("Проверить, что список не пустой", () ->
                            assertThat(bookings).isNotEmpty()
                    );
                    step("Проверить, что все id больше 0", () ->
                            {
                                for (Booking booking : bookings) {
                                    assertThat(booking.getBookingid()).isGreaterThan(0);
                                }
                            }
                    );
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
