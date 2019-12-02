package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.domain.Service;
import se.kry.codetest.domain.Services;
import se.kry.codetest.service.BackgroundPoller;
import se.kry.codetest.service.PolledServicesService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class ServicesVerticle extends AbstractVerticle {

    private static final int ONE_MINUTE = 1000 * 60;
    private static final int SERVICE_POLLING_DELAY = ONE_MINUTE;

    private PolledServicesService polledServicesService;
    private BackgroundPoller poller;

    @Override
    public void start(Future<Void> startFuture) {
        poller = new BackgroundPoller(vertx);
        polledServicesService = new PolledServicesService(vertx);
        launchServicesPolling();

        createServer(startFuture);
    }

    private void createServer(Future<Void> startFuture) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        setupRoutes(router);

        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void launchServicesPolling() {
        vertx.setPeriodic(SERVICE_POLLING_DELAY, timerId -> pollServices());
    }

    private void pollServices() {
        polledServicesService.all()
                             .setHandler(services -> {
                                 if (services.failed()) {
                                     System.out.println("Unable to get services to poll");
                                     return;
                                 }
                                 poller.pollServices(services.result())
                                       .setHandler(updatedServices ->
                                               polledServicesService.updateStatuses(updatedServices.result().list())
                                                                    .setHandler(ar -> {
                                                                        if (ar.failed()) {
                                                                            System.err.println("Unable to update services status");
                                                                        }
                                                                    }));
                             });
    }

    private void setupRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());

        router.get("/service")
              .handler(this::handleListServices);
        router.post("/service")
              .handler(this::handleAddService)
              .failureHandler(context -> context.response()
                                                .setStatusCode(400)
                                                .end("KO")
              );
        router.delete("/service").handler(this::handleDeleteService);
    }

    private void handleDeleteService(RoutingContext req) {
        JsonObject jsonBody = req.getBodyAsJson();
        this.polledServicesService.remove(jsonBody.getString("name"))
                                  .setHandler(services -> {
                                      String status = services.failed() ? "KO" : "OK";
                                      req.response()
                                         .putHeader("content-type", "text/plain")
                                         .end(status);
                                  });
    }

    private void handleAddService(RoutingContext req) {
        JsonObject jsonBody = req.getBodyAsJson();
        this.polledServicesService.add(jsonBody.getString("name"), jsonBody.getString("url"))
                                  .setHandler(services -> {
                                      String status = services.failed() ? "KO" : "OK";
                                      if (services.failed()) {
                                          System.err.println(services.cause().getMessage());
                                          services.cause().printStackTrace();
                                      }
                                      req.response()
                                         .putHeader("content-type", "text/plain")
                                         .end(status);
                                  });
    }

    private void handleListServices(RoutingContext req) {
        this.polledServicesService.all()
                                  .setHandler(services -> {
                                      if (services.failed()) {
                                          responseServices(req, emptyList());
                                          return;
                                      }
                                      responseServices(req, services);
                                  });
    }

    private void responseServices(RoutingContext req, AsyncResult<Services> services) {
        responseServices(req, buildServicesAsJson(services));
    }

    private List<JsonObject> buildServicesAsJson(AsyncResult<Services> services) {
        List<JsonObject> jsonServices = new ArrayList<>();
        for (Service service : services.result()) {
            jsonServices.add(buildServiceAsJson(service));
        }
        return jsonServices;
    }

    private JsonObject buildServiceAsJson(Service service) {
        return new JsonObject()
                .put("name", service.getName())
                .put("added_on", service.getAddedOn().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("status", service.getStatus().name());
    }

    private void responseServices(RoutingContext req, List<JsonObject> jsonServices) {
        req.response()
           .putHeader("content-type", "application/json")
           .end(new JsonArray(jsonServices).encode());
    }

}



