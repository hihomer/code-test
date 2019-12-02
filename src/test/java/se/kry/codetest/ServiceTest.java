package se.kry.codetest;

import org.junit.jupiter.api.Test;
import se.kry.codetest.domain.Service;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static se.kry.codetest.domain.Service.AvailabilityStatus.UNKNOWN;
import static se.kry.codetest.domain.Service.newService;

public class ServiceTest {

    public static final String NAME = "name";
    public static final String URL = "http://www.kry.se";
    public static final LocalDateTime ADDED_ON = LocalDateTime.of(2019, 11, 30, 17, 11);

    @Test
    public void service_creation_test() {
        final String name = "name";
        final String url = "http://ww.kry.se";
        Service service = newService(name, url, ADDED_ON, UNKNOWN);

        assertEquals(service.getName(), name);
        assertEquals(service.getUrl(), url);
    }

    @Test
    public void service_creation_invalid_url_test() {
        final String name = "name";
        final String url = "invalidurl";
        assertThrows(IllegalArgumentException.class, () -> newService(name, url, ADDED_ON, UNKNOWN));
    }

    @Test
    public void service_equals_test() {
        Service service1 = newService(NAME, URL, ADDED_ON, UNKNOWN);
        Service service2 = newService(NAME, URL, ADDED_ON, UNKNOWN);

        assertEquals(service1, service2);
    }

    @Test
    public void service_not_equals_on_name_test() {
        final String name1 = "name1";
        final String name2 = "name2";
        Service service1 = newService(name1, URL, ADDED_ON, UNKNOWN);
        Service service2 = newService(name2, URL, ADDED_ON, UNKNOWN);

        assertNotEquals(service1, service2);
    }

}
