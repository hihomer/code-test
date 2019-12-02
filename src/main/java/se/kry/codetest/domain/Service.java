package se.kry.codetest.domain;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

public class Service {

    public enum AvailabilityStatus {
        OK,
        FAIL,
        UNKNOWN
    }

    private final String name;
    private final String url;
    private final LocalDateTime addedOn;
    private AvailabilityStatus status;

    private Service(String name, String url, LocalDateTime addedOn, AvailabilityStatus status) {
        this.name = name;
        this.url = url;
        this.addedOn = addedOn;
        this.status = status;
    }

    public static Service newService(String name, String url, LocalDateTime addedOn, AvailabilityStatus status) throws IllegalArgumentException {
        checkUrlValidity(url);
        return buildService(name, url, addedOn, status);
    }

    public static Service buildService(String name, String url, LocalDateTime addedOn, AvailabilityStatus status) {
        return new Service(name, url, addedOn, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return name.equals(service.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void updateStatus(AvailabilityStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public AvailabilityStatus getStatus() {
        return this.status;
    }

    private static void checkUrlValidity(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Provided URL is invalid");
        }
    }
}
