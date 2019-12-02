package se.kry.codetest.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Services implements Iterable<Service> {

    private final List<Service> services;

    public Services(List<Service> services) {
        this.services = new ArrayList<>(services);
    }

    @Override
    public Iterator<Service> iterator() {
        return this.services.iterator();
    }
}
