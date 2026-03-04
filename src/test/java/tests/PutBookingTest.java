package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
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

public class PutBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private NewBooking updatedBooking;
    private Response response;
    private Response responseUpdatedBooking;
    private NewBooking getUpdatedBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        step("Создать объект для нового бронирования newBooking", () -> {
                    newBooking = new NewBooking();
                    newBooking.setFirstname("Frosya");
                    newBooking.setLastname("Semenova");
                    newBooking.setTotalprice(444);
                    newBooking.setDepositpaid(true);
                    newBooking.setBookingdates(new NewBooking.Bookingdates("2020-01-01", "2025-02-02"));
                    newBooking.setAdditionalneeds("Breakfast");
                }
        );

        step("Создать объект бронирования updatedBooking", () -> {
                    updatedBooking = new NewBooking();
                    updatedBooking.setFirstname("Frosya");
                    updatedBooking.setLastname("Semenova");
                    updatedBooking.setTotalprice(555);
                    updatedBooking.setDepositpaid(false);
                    updatedBooking.setBookingdates(new NewBooking.Bookingdates("2021-01-01", "2025-02-02"));
                    updatedBooking.setAdditionalneeds("Breakfast");
                }
        );
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testPutBooking() throws JsonProcessingException {

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

        int bookingId = createdBooking.getBookingid();

        step("Отправить JSON на энпоинт /booking на обновление бронирования", () ->
                {
                    String requestUpdatedBookingBody = objectMapper.writeValueAsString(updatedBooking);
                    responseUpdatedBooking = apiClient.updateBooking(bookingId, requestUpdatedBookingBody);
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(responseUpdatedBooking.getStatusCode()).isEqualTo(200));
                    Response responseGetUpdatedBooking = apiClient.getBookingById(bookingId);
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(responseGetUpdatedBooking.getStatusCode()).isEqualTo(200));
                    String responseGetUpdatedBookingBody = responseGetUpdatedBooking.asString();
                    getUpdatedBooking = objectMapper.readValue(responseGetUpdatedBookingBody, NewBooking.class);
                }
        );

        step("Проверить, что все поля объекта getUpdatedBooking, полученного по id, соответствуют полям объекта updatedBooking, на который был обновлен объект newBooking", () -> {
                    assertThat(getUpdatedBooking).isNotNull();
                    assertEquals(getUpdatedBooking.getFirstname(), updatedBooking.getFirstname());
                    assertEquals(getUpdatedBooking.getLastname(), updatedBooking.getLastname());
                    assertEquals(getUpdatedBooking.getTotalprice(), updatedBooking.getTotalprice());
                    assertEquals(getUpdatedBooking.getDepositpaid(), updatedBooking.getDepositpaid());
                    assertEquals(getUpdatedBooking.getBookingdates().getCheckin(), updatedBooking.getBookingdates().getCheckin());
                    assertEquals(getUpdatedBooking.getBookingdates().getCheckout(), updatedBooking.getBookingdates().getCheckout());
                    assertEquals(getUpdatedBooking.getAdditionalneeds(), updatedBooking.getAdditionalneeds());
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
