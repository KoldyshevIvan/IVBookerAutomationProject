package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetFilteredBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking;
    private CreatedBooking createdBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @CsvSource({
            "Jane, Doe, 101, true, 2020-02-02, 2020-03-03, Bar",
            "Aleksandr, Shalchinov, 102, true, 2021-21-21, 2022-22-22, Pub"
            //"Josh, Allen, 103, false, 2023-21-21, 2023-22-22, Pub"
    })
    public void testGetFilteredBooking
            (String firstname, String lastname, Integer totalprice, Boolean depositpaid, String checkin, String checkout, String additionalneeds) throws JsonProcessingException {
        newBooking = new NewBooking();
        newBooking.setFirstname(firstname);
        newBooking.setLastname(lastname);
        newBooking.setTotalprice(totalprice);
        newBooking.setDepositpaid(depositpaid);
        newBooking.setBookingdates(new NewBooking.Bookingdates(checkin, checkout));
        newBooking.setAdditionalneeds(additionalneeds);

        // Выполняем запрос к эндпоинту  /booking  через APIClient
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект createdBooking
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        // Выполняем запрос к эндпоинту  /booking  через APIClient, используя фильтр по firstname и lastname
        Response getFilteredResponse = apiClient.getFilteredBooking(firstname, lastname);
        assertThat(getFilteredResponse.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект filteredBooking
        String responseFilteredBody = getFilteredResponse.asString();
        List<Booking> filteredBookings = objectMapper.readValue(responseFilteredBody, new TypeReference<List<Booking>>() {
        });
        assertThat(filteredBookings).isNotEmpty();

        for (Booking filteredBooking : filteredBookings) {
            // Получаем объект бронирования по id
            Response filteredBookingResponse = apiClient.getBookingById(filteredBooking.getBookingid());

            // Десериализуем тело ответа в объект NewBooking
            String filteredBookingResponseBody = filteredBookingResponse.asString();
            NewBooking newFilteredBooking = objectMapper.readValue(filteredBookingResponseBody, NewBooking.class);

            // Проверяем, что Имя и Фамилия каждого объекта filteredBooking соответствует фильтру
            assertEquals(newFilteredBooking.getFirstname(), createdBooking.getBooking().getFirstname());
            assertEquals(newFilteredBooking.getLastname(), createdBooking.getBooking().getLastname());
        }
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
