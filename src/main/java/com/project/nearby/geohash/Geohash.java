package com.project.nearby.geohash;

public interface Geohash<T> {

    T encodeLatLon(double lat, double lon);
}
