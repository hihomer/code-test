package se.kry.codetest;

import org.junit.jupiter.api.Test;
import se.kry.codetest.domain.Service;
import se.kry.codetest.domain.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class ServicesTest {

    @Test
    public void empty_services_creation() {
        Services services = new Services(emptyList());

        assertIterableEquals(services, emptyList());
    }

    @Test
    public void services_creation() {
        List<Service> serviceList = new ArrayList<>();
        serviceList.add(Service.newService("KRY", "http://www.kry.se", LocalDateTime.now(), Service.AvailabilityStatus.UNKNOWN));
        serviceList.add(Service.newService("GOOGLE", "http://www.google.fr", LocalDateTime.now(), Service.AvailabilityStatus.UNKNOWN));

        Services services = new Services(serviceList);

        assertIterableEquals(services, serviceList);
    }
}
