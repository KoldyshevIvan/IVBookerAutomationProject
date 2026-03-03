package tests;

import com.fasterxml.jackson.annotation.JsonInclude;
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

public class PatchBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private int bookingId;

    @BeforeEach
    public void setup() throws JsonProcessingException {
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

        // Выполняем запрос на создание newBooking через APIClient
        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект newBooking
        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);

        // Запоминаем id созданного бронирования
        bookingId = createdBooking.getBookingid();
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testPatchBooking() throws JsonProcessingException {
        // Подготавливаем объект с частичными данными для обновления
        NewBooking patchData = new NewBooking();
        patchData.setTotalprice(9999);
        patchData.setAdditionalneeds("Alcohol");

        // Настраиваем ObjectMapper для игнорирования null-полей при сериализации
        ObjectMapper patchMapper = new ObjectMapper();
        patchMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String patchBody = patchMapper.writeValueAsString(patchData);

        // Выполняем запрос на частичное обновление newBooking через APIClient
        Response patchResponse = apiClient.partlyUpdateBooking(bookingId, patchBody);
        assertThat(patchResponse.getStatusCode()).isEqualTo(200);

        // Получаем объект обновленного бронирования по id
        Response getPatchResponse = apiClient.getBookingById(bookingId);
        assertThat(getPatchResponse.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в объект getUpdatedBooking
        String getPatchResponseBody = getPatchResponse.asString();
        NewBooking patchedBooking = objectMapper.readValue(getPatchResponseBody, NewBooking.class);

        // Проверяем, что изменённые поля обновились, а остальные остались прежними
        assertThat(patchedBooking).isNotNull();
        assertEquals(patchedBooking.getTotalprice(), patchData.getTotalprice());
        assertEquals(patchedBooking.getAdditionalneeds(), patchData.getAdditionalneeds());
        assertEquals(patchedBooking.getFirstname(), createdBooking.getBooking().getFirstname());
        assertEquals(patchedBooking.getLastname(), createdBooking.getBooking().getLastname());
        assertEquals(patchedBooking.getBookingdates().getCheckin(), createdBooking.getBooking().getBookingdates().getCheckin());
        assertEquals(patchedBooking.getDepositpaid(), createdBooking.getBooking().getDepositpaid());
        assertEquals(patchedBooking.getBookingdates().getCheckout(), createdBooking.getBooking().getBookingdates().getCheckout());
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
