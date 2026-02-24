package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");
    }

    @Test
    public void testDeleteBooking() throws JsonProcessingException {
        // Получаем список всех бронирований
        Response response = apiClient.getBooking();
        assertThat(response.getStatusCode()).isEqualTo(200);
        String responseBody = response.getBody().asString();
        List<Booking> bookings = objectMapper.readValue(responseBody, new TypeReference<List<Booking>>() {
        });
        assertThat(bookings).isNotEmpty();
        // Получаем id первого элемента
        int firstBookingId = bookings.getFirst().getBookingid();

        // Удаляем первый элемент из списка всех бронирований
        Response deleteBookingResponse = apiClient.deleteBooking(firstBookingId);
        assertThat(deleteBookingResponse.getStatusCode()).isEqualTo(201);

        // Получаем список бронирований после удаления первого элемента
        Response afterDeleteResponse = apiClient.getBooking();
        String afterDeleteResponseBody = afterDeleteResponse.getBody().asString();
        List<Booking> bookingsAfterDelete = objectMapper.readValue(afterDeleteResponseBody, new TypeReference<List<Booking>>() {
        });

        // Проверяем, что удалённого ID больше нет в списке
        assertThat(bookingsAfterDelete)
                .extracting(Booking::getBookingid)
                .doesNotContain(firstBookingId);
    }
}
