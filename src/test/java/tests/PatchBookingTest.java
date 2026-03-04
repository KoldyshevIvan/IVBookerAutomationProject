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

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private int bookingId;
    private Response response;
    private NewBooking patchData;
    private String patchBody;
    private NewBooking patchedBooking;

    @BeforeEach
    public void setup() throws JsonProcessingException {
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
        bookingId = createdBooking.getBookingid();
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Ivan Koldyshev")
    public void testPatchBooking() throws JsonProcessingException {

        step("Подготовить данные для частичного обновления бронирования", () ->
                {
                    patchData = new NewBooking();
                    patchData.setTotalprice(9999);
                    patchData.setAdditionalneeds("Alcohol");
                }
        );

        step("Настроить ObjectMapper для игнорирования null-полей при сериализации", () ->
                {
                    ObjectMapper patchMapper = new ObjectMapper();
                    patchMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    patchBody = patchMapper.writeValueAsString(patchData);
                }
        );

        step("Отправить запрос на частичное обновление бронирования", () ->
                {
                    Response patchResponse = apiClient.partlyUpdateBooking(bookingId, patchBody);
                    step("Проверить, что статус-код ответа на частичное обновление бронирования == 200", () ->
                            assertThat(patchResponse.getStatusCode()).isEqualTo(200));
                }
        );

        step("Отправить запрос на частичное обновление бронирования", () ->
                {
                    Response getPatchResponse = apiClient.getBookingById(bookingId);
                    step("Проверить, что статус-код ответа на запрос бронирования по id == 200", () ->
                            assertThat(getPatchResponse.getStatusCode()).isEqualTo(200));
                    String getPatchResponseBody = getPatchResponse.asString();
                    patchedBooking = objectMapper.readValue(getPatchResponseBody, NewBooking.class);
                }
        );

        step("Проверить, что измененные поля обновились, а остальные остались прежними", () ->
                {
                    assertThat(patchedBooking).isNotNull();
                    assertEquals(patchedBooking.getTotalprice(), patchData.getTotalprice());
                    assertEquals(patchedBooking.getAdditionalneeds(), patchData.getAdditionalneeds());
                    assertEquals(patchedBooking.getFirstname(), createdBooking.getBooking().getFirstname());
                    assertEquals(patchedBooking.getLastname(), createdBooking.getBooking().getLastname());
                    assertEquals(patchedBooking.getBookingdates().getCheckin(), createdBooking.getBooking().getBookingdates().getCheckin());
                    assertEquals(patchedBooking.getDepositpaid(), createdBooking.getBooking().getDepositpaid());
                    assertEquals(patchedBooking.getBookingdates().getCheckout(), createdBooking.getBooking().getBookingdates().getCheckout());
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
