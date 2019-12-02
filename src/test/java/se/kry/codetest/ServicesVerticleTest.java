package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
@Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
public class ServicesVerticleTest {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new ServicesVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("1 Add  service")
    void t1_add_service(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                 .post(8080, "::1", "/service")
                 .sendJsonObject(
                         new JsonObject().put("name", "KRY").put("url", "http://www.kry.se"),
                         response -> testContext.verify(() -> checkUpdateResponse(testContext, response.result(), 200, "OK")));
    }

    @Test
    @DisplayName("2 Add  service with invalid url")
    void t2_add_service_with_invalid_url(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                 .post(8080, "::1", "/service")
                 .sendJsonObject(
                         new JsonObject().put("name", "KRY").put("url", "kry"),
                         response -> testContext.verify(() -> checkUpdateResponse(testContext, response.result(), 400, "KO")));
    }

    @Test
    @DisplayName("3 Request services list")
    void t3_list_services(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                 .get(8080, "::1", "/service")
                 .send(response -> testContext.verify(() -> {
                     final HttpResponse<Buffer> httpResponse = response.result();
                     checkStatusCode(200, httpResponse);
                     JsonArray services = httpResponse.bodyAsJsonArray();
                     assertEquals(1, services.size());
                     final JsonObject jsonObject = services.getJsonObject(0);
                     assertEquals("KRY", jsonObject.getString("name"));
                     testContext.completeNow();
                 }));
    }

    @Test
    @DisplayName("4 Remove  service")
    void t4_remove_service(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                 .delete(8080, "::1", "/service")
                 .sendJsonObject(
                         new JsonObject().put("name", "KRY"),
                         response -> testContext.verify(() -> checkUpdateResponse(testContext, response.result(), 200, "OK")));
    }

    private void checkUpdateResponse(VertxTestContext testContext, HttpResponse<Buffer> response, int expectedStatus, String ok) {
        checkStatusCode(expectedStatus, response);
        String creationResponse = response.bodyAsString();
        assertEquals(ok, creationResponse);
        testContext.completeNow();
    }

    private void checkStatusCode(int expectedStatus, HttpResponse<Buffer> httpResponse) {
        assertEquals(expectedStatus, httpResponse.statusCode());
    }

}
