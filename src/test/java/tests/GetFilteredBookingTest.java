package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetFilteredBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking;
    private CreatedBooking createdBooking;
    private Response response;
    private Response getFilteredResponse;
    private List<Booking> filteredBookings;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @CsvSource({
            "Jana, Doe, 101, true, 2020-02-02, 2020-03-03, Bar",
            "Aleksandr, Shalchinov, 102, true, 2021-21-21, 2022-22-22, Pub"
    })
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testGetFilteredBooking
            (String firstname, String lastname, Integer totalprice, Boolean depositpaid, String checkin, String checkout, String additionalneeds) throws JsonProcessingException {

        step("Создать объекты для нового бронирования", () ->
                {
                    newBooking = new NewBooking();
                    newBooking.setFirstname(firstname);
                    newBooking.setLastname(lastname);
                    newBooking.setTotalprice(totalprice);
                    newBooking.setDepositpaid(depositpaid);
                    newBooking.setBookingdates(new NewBooking.Bookingdates(checkin, checkout));
                    newBooking.setAdditionalneeds(additionalneeds);
                }
        );

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

        step("Получить список всех бронирований по фильтрам firstname и lastname", () ->
                {
                    getFilteredResponse = apiClient.getFilteredBooking(firstname, lastname);
                    step("Проверить, что статус-код ответа == 200", () ->
                            assertThat(getFilteredResponse.getStatusCode()).isEqualTo(200));
                    String responseFilteredBody = getFilteredResponse.asString();
                    filteredBookings = objectMapper.readValue(responseFilteredBody, new TypeReference<List<Booking>>() {
                    });
                    step("Проверить, что список, с полученными id не пустой", () ->
                            assertThat(filteredBookings).isNotEmpty());
                }
        );

        step("Получить все объекты бронирования из списка filteredBookings", () ->
                {
                    for (Booking filteredBooking : filteredBookings) {
                        Response filteredBookingResponse = apiClient.getBookingById(filteredBooking.getBookingid());
                        String filteredBookingResponseBody = filteredBookingResponse.asString();
                        NewBooking newFilteredBooking = objectMapper.readValue(filteredBookingResponseBody, NewBooking.class);

                        step("Проверить, что Имя и Фамилия каждого объекта filteredBooking соответствует фильтру", () ->
                                {
                                    assertEquals(newFilteredBooking.getFirstname(), createdBooking.getBooking().getFirstname());
                                    assertEquals(newFilteredBooking.getLastname(), createdBooking.getBooking().getLastname());
                                }
                        );
                    }
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
