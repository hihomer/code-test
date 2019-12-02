package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.service.DBConnector;

public class DBMigration {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        createDatabaseIfNeeded(vertx);
    }

    private static void createDatabaseIfNeeded(Vertx vertx) {
        DBConnector connector = new DBConnector(vertx);
        connector.query("CREATE TABLE IF NOT EXISTS service (name VARCHAR(128) NOT NULL, url VARCHAR(128) NOT NULL, added_on VARCHAR(20) NOT NULL, status VARCHAR(7) NOT NULL)")
                 .setHandler(done -> {
                     if (done.succeeded()) {
                         System.out.println("completed db migrations");
                     } else {
                         done.cause().printStackTrace();
                     }
                     vertx.close(shutdown -> System.exit(0));
                 });
    }
}
