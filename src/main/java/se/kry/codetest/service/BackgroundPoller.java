package se.kry.codetest.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import se.kry.codetest.domain.Service;
import se.kry.codetest.domain.Service.AvailabilityStatus;
import se.kry.codetest.domain.Services;

import java.util.ArrayList;
import java.util.List;

import static se.kry.codetest.domain.Service.AvailabilityStatus.FAIL;
import static se.kry.codetest.domain.Service.AvailabilityStatus.OK;

public class BackgroundPoller {

    public static final long SERVICE_REQUEST_TIMEOUT = 5000L;
    private final WebClient client;

    public BackgroundPoller(Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public CompositeFuture pollServices(Services services) {
        final List<Future> result = new ArrayList<>();

        System.out.println("Will poll services");

        for (Service service : services) {
            System.out.println("Will poll service : " + service.getName() + " on " + service.getUrl());

            result.add(Future.future(promise -> checkServiceAndUpdateStatus(service, promise)));
        }
        return CompositeFuture.all(result);
    }

    private void checkServiceAndUpdateStatus(Service service, Future<Object> promise) {
        this.client
                .getAbs(service.getUrl())
                .timeout(SERVICE_REQUEST_TIMEOUT)
                .send(ar -> {
                    AvailabilityStatus status = computeServiceStatus(ar);
                    service.updateStatus(status);
                    promise.complete(service);
                });
    }

    private AvailabilityStatus computeServiceStatus(AsyncResult<HttpResponse<Buffer>> ar) {
        AvailabilityStatus status;
        if (ar.succeeded()) {
            HttpResponse<Buffer> httpResponse = ar.result();
            status = computeServiceStatus(httpResponse);
        } else {
            status = FAIL;
        }
        return status;
    }

    private AvailabilityStatus computeServiceStatus(HttpResponse<Buffer> httpResponse) {
        return acceptableResponse(httpResponse) ? OK : FAIL;
    }

    private boolean acceptableResponse(HttpResponse<Buffer> response) {
        return response.statusCode() >= 200 && response.statusCode() < 400;
    }
}
