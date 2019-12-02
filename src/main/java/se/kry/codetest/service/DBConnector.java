package se.kry.codetest.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;

public class DBConnector {

    private static final String REQUEST_DELIMITER = ";";

    private final SQLClient client;

    public DBConnector(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:poller.db")
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);

        client = JDBCClient.createShared(vertx, config);
    }

    public Future<ResultSet> query(String query) {
        return query(query, new JsonArray());
    }

    public Future<ResultSet> query(String query, JsonArray params) {
        if (query == null || query.isEmpty()) {
            return Future.failedFuture("Query is null or empty");
        }
        query = validatedQuery(query);

        Future<ResultSet> queryResultFuture = Future.future();

        client.queryWithParams(query, params, result -> {
            if (result.failed()) {
                queryResultFuture.fail(result.cause());
            } else {
                queryResultFuture.complete(result.result());
            }
        });
        return queryResultFuture;
    }

    public Future<UpdateResult> update(String query, JsonArray params) {
        if (query == null || query.isEmpty()) {
            return Future.failedFuture("Query is null or empty");
        }

        query = validatedQuery(query);

        Future<UpdateResult> updateResultFuture = Future.future();

        client.updateWithParams(query, params, result -> {
            if (result.failed()) {
                updateResultFuture.fail(result.cause());
            } else {
                updateResultFuture.complete(result.result());
            }
        });
        return updateResultFuture;
    }

    private String validatedQuery(String query) {
        if (!query.endsWith(REQUEST_DELIMITER)) {
            query = query + REQUEST_DELIMITER;
        }
        return query;
    }

}
