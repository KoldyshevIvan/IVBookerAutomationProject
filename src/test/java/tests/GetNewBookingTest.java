package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetNewBookingTest {

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
        NewBooking newBooking = objectMapper.readValue(responseBody, NewBooking.class);

        // Проверяем, что тело ответа не пустое
        assertThat(newBooking).isNotNull();
        // Проверяем все поля для бронирования с id = 1
        assertEquals("John", newBooking.getFirstname(), "Имя не совпадает с ожидаемым");
        assertEquals("Smith", newBooking.getLastname(), "Фамилия не совпадает с ожидаемой");
        assertEquals(111, newBooking.getTotalprice(), "Цена не совпадает с ожидаемой");
        assertEquals(true, newBooking.getDepositpaid(), "Статус депозита не совпадает с ожидаемым");
        assertEquals("2018-01-01", newBooking.getBookingdates().getCheckin(), "Дата заезда не совпадает с ожидаемой");
        assertEquals("2019-01-01", newBooking.getBookingdates().getCheckout(), "Дата выезда не совпадает с ожидаемой");
        assertEquals("Breakfast", newBooking.getAdditionalneeds(), "Пожелания не совпадают с ожидаемыми");
    }
}

