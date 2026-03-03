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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PutBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private NewBooking updatedBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        // Создаем объект Booking с необходимыми данными
        newBooking = new NewBooking();
        newBooking.setFirstname("Frosya");
        newBooking.setLastname("Semenova");
        newBooking.setTotalprice(444);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new NewBooking.Bookingdates("2020-01-01", "2025-02-02"));
        newBooking.setAdditionalneeds("Breakfast");

        // Создаем объект updatedBooking, на который будем обновлять newBooking
        updatedBooking = new NewBooking();
        updatedBooking.setFirstname("Frosya");
        updatedBooking.setLastname("Semenova");
        updatedBooking.setTotalprice(555);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(new NewBooking.Bookingdates("2021-01-01", "2025-02-02"));
        updatedBooking.setAdditionalneeds("Breakfast");
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testPutBooking() throws JsonProcessingException {

        // Выполняем запрос на создание newBooking через APIClient
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект newBooking
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        // Запоминаем id созданного бронирования
        int bookingId = createdBooking.getBookingid();

        // Выполняем запрос на обновление newBooking на updatedBooking через APIClient
        String requestUpdatedBookingBody = objectMapper.writeValueAsString(updatedBooking);
        Response responseUpdatedBooking = apiClient.updateBooking(bookingId, requestUpdatedBookingBody);
        assertThat(responseUpdatedBooking.getStatusCode()).isEqualTo(200);

        // Получаем объект обновленного бронирования по id
        Response responseGetUpdatedBooking = apiClient.getBookingById(bookingId);
        assertThat(responseGetUpdatedBooking.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект getUpdatedBooking
        String responseGetUpdatedBookingBody = responseGetUpdatedBooking.asString();
        NewBooking getUpdatedBooking = objectMapper.readValue(responseGetUpdatedBookingBody, NewBooking.class);

        // Проверяем, что объект getUpdatedBooking, полученный по id, соответствует объекту updatedBooking, на который мы обновили объект newBooking
        assertThat(getUpdatedBooking).isNotNull();
        assertEquals(getUpdatedBooking.getFirstname(), updatedBooking.getFirstname());
        assertEquals(getUpdatedBooking.getLastname(), updatedBooking.getLastname());
        assertEquals(getUpdatedBooking.getTotalprice(), updatedBooking.getTotalprice());
        assertEquals(getUpdatedBooking.getDepositpaid(), updatedBooking.getDepositpaid());
        assertEquals(getUpdatedBooking.getBookingdates().getCheckin(), updatedBooking.getBookingdates().getCheckin());
        assertEquals(getUpdatedBooking.getBookingdates().getCheckout(), updatedBooking.getBookingdates().getCheckout());
        assertEquals(getUpdatedBooking.getAdditionalneeds(), updatedBooking.getAdditionalneeds());
    }

    @AfterEach
    public void tearDown() {
        // Удаляем созданное бронирование
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        // Проверяем, что бронирование успешно удалено
        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}
