package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.ExistingBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetExistingBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBooking() throws Exception {
        Response response = apiClient.getBookingById(73);
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        ExistingBooking existingBooking = objectMapper.readValue(responseBody, ExistingBooking.class);

        // Проверяем, что тело ответа не пустое
        assertThat(existingBooking).isNotNull();
        // Проверяем все поля для бронирования с id = 1
        assertEquals("John", existingBooking.getFirstname(), "Имя не совпадает с ожидаемым");
        assertEquals("Smith", existingBooking.getLastname(), "Фамилия не совпадает с ожидаемой");
        assertEquals(111, existingBooking.getTotalprice(), "Цена не совпадает с ожидаемой");
        assertEquals(true, existingBooking.getDepositpaid(), "Статус депозита не совпадает с ожидаемым");
        assertEquals("2018-01-01", existingBooking.getBookingdates().getCheckin(), "Дата заезда не совпадает с ожидаемой");
        assertEquals("2019-01-01", existingBooking.getBookingdates().getCheckout(), "Дата выезда не совпадает с ожидаемой");
        assertEquals("Breakfast", existingBooking.getAdditionalneeds(), "Пожелания не совпадают с ожидаемыми");
    }
}

