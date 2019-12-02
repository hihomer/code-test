package se.kry.codetest.service;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import se.kry.codetest.domain.Service;
import se.kry.codetest.domain.Services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static se.kry.codetest.domain.Service.AvailabilityStatus.UNKNOWN;

public class PolledServicesService {

    private static final String NEW_SERVICE_REQUEST = "INSERT INTO service (name, url, added_on, status) VALUES (?, ?, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'utc'), ?)";
    private static final String DELETE_REQUEST = "DELETE FROM service where name = ?";
    private static final String SELECT_ALL_REQUEST = "SELECT name, url, added_on, status FROM service";

    private final DBConnector dbConnector;

    public PolledServicesService(Vertx vertx) {
        this.dbConnector = new DBConnector(vertx);
    }

    public Future<Services> all() {
        Future<Services> services = Future.future();
        this.dbConnector.query(SELECT_ALL_REQUEST)
                        .setHandler(res -> {
                            if (res.failed()) {
                                services.fail(res.cause());
                                return;
                            }
                            List<Service> serviceList = new ArrayList<>();
                            for (JsonObject service : res.result().getRows()) {
                                serviceList.add(buildService(service));
                            }
                            services.complete(buildServices(serviceList));
                        });
        return services;
    }

    public Future<Services> add(String name, String url) {
        Future<Services> insertion = Future.future();
        Service service = Service.newService(name, url, LocalDateTime.now(), UNKNOWN);
        this.dbConnector.update(NEW_SERVICE_REQUEST, new JsonArray().add(service.getName()).add(service.getUrl()).add(UNKNOWN.name()))
                        .setHandler(res -> {
                            if (res.failed()) {
                                insertion.fail(res.cause());
                                return;
                            }
                            insertion.complete();
                        });
        return insertion.failed() ? insertion : all();
    }

    public Future<Services> remove(String serviceName) {
        Future<Services> deletion = Future.future();
        this.dbConnector.update(DELETE_REQUEST, new JsonArray().add(serviceName))
                        .setHandler(res -> {
                            if (res.failed()) {
                                deletion.fail(res.cause());
                                return;
                            }
                            deletion.complete();
                        });
        return deletion.failed() ? deletion : all();
    }

    public CompositeFuture updateStatuses(List<Service> services) {
        List<Future> result = new ArrayList<>();
        for (Service service : services) {
            result.add(updateService(service));
        }
        return CompositeFuture.all(result);
    }

    private Future<Service> updateService(Service service) {
        Future<Service> update = Future.future();
        this.dbConnector.update("UPDATE service SET status = ? where name = ?", new JsonArray().add(service.getStatus().name()).add(service.getName()))
                        .setHandler(res -> {
                            if (res.failed()) {
                                update.fail(res.cause());
                                return;
                            }
                            update.complete();
                        });
        return update;
    }

    private Services buildServices(List<Service> serviceList) {
        return new Services(serviceList);
    }

    private Service buildService(JsonObject service) {
        final String name = service.getString("name");
        final String url = service.getString("url");
        final LocalDateTime addedOn = dateTimeOnCurrentTimezone(service.getString("added_on"));
        final Service.AvailabilityStatus status = Service.AvailabilityStatus.valueOf(service.getString("status"));
        System.out.printf("%s %s %s %s\n", name, url, addedOn, status);
        return Service.buildService(name, url, addedOn, status);
    }

    private LocalDateTime dateTimeOnCurrentTimezone(String date) {
        return LocalDateTime.from(ISO_DATE_TIME.parse(date))
                            .atZone(ZoneOffset.UTC)
                            .withZoneSameInstant(ZoneId.systemDefault())
                            .toLocalDateTime();
    }
}
