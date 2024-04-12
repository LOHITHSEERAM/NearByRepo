package com.project.nearby.geohash;

import com.project.nearby.util.Shapes.BBox;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class GeoHashAlgo implements Geohash<String> {

    static Integer precision = 6;
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    @Autowired
    @Qualifier("bBox")
    BBox bBox;

    public String encodeLatLon(double lat, double lon) {

        if (Double.isNaN(lat) || Double.isNaN(lon)) {
            throw new IllegalArgumentException("Invalid geohash");
        }

        if (precision == null) {
            for (int p = 1; p <= 12; p++) {
                String hash = encodeLatLon(lat, lon);
                Map<String, Double> posn = decode(hash);
                if (posn.get("lat").equals(lat) && posn.get("lon").equals(lon)) {
                    return hash;
                }
            }
            precision = 12; // set to maximum
        }

        int idx = 0;
        int bit = 0;
        boolean evenBit = true;
        StringBuilder geohash = new StringBuilder();

        double latMin = bBox.minLat, latMax = bBox.maxLat;
        double lonMin = bBox.minLon, lonMax = bBox.maxLon;

        while (geohash.length() < precision) {
            if (evenBit) {
                double lonMid = (lonMin + lonMax) / 2;
                if (lon >= lonMid) {
                    idx = idx * 2 + 1;
                    lonMin = lonMid;
                } else {
                    idx = idx * 2;
                    lonMax = lonMid;
                }
            } else {
                double latMid = (latMin + latMax) / 2;
                if (lat >= latMid) {
                    idx = idx * 2 + 1;
                    latMin = latMid;
                } else {
                    idx = idx * 2;
                    latMax = latMid;
                }
            }
            evenBit = !evenBit;

            if (++bit == 5) {
                geohash.append(BASE32.charAt(idx));
                bit = 0;
                idx = 0;
            }
        }

        return geohash.toString();
    }

    public Map<String, Double> decode(String geohash) {
        Map<String, Map<String, Double>> bounds = bounds(geohash); // <-- call to bounds method
        // now just determine the centre of the cell...

        double latMin = bounds.get("sw").get("lat");
        double lonMin = bounds.get("sw").get("lon");
        double latMax = bounds.get("ne").get("lat");
        double lonMax = bounds.get("ne").get("lon");

        // cell centre
        double lat = (latMin + latMax) / 2;
        double lon = (lonMin + lonMax) / 2;

        // round to close to center without excessive precision: ⌊2-log10(Δ°)⌋ decimal places
        int precisionLat = (int) Math.floor(2 - Math.log(latMax - latMin) / Math.log(10));
        int precisionLon = (int) Math.floor(2 - Math.log(lonMax - lonMin) / Math.log(10));

        lat = Double.parseDouble(String.format("%." + precisionLat + "f", lat));
        lon = Double.parseDouble(String.format("%." + precisionLon + "f", lon));

        Map<String, Double> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lon", lon);

        return result;
    }

    public Map<String, Map<String, Double>> bounds(String geohash) {
        if (geohash.length() == 0) {
            throw new IllegalArgumentException("Invalid geohash");
        }

        geohash = geohash.toLowerCase();

        boolean evenBit = true;
        double latMin = bBox.minLat, latMax = bBox.maxLat;
        double lonMin = bBox.minLon, lonMax = bBox.maxLon;

        for (int i = 0; i < geohash.length(); i++) {
            char chr = geohash.charAt(i);
            int idx = BASE32.indexOf(chr);
            if (idx == -1) {
                throw new IllegalArgumentException("Invalid geohash");
            }

            for (int n = 4; n >= 0; n--) {
                int bitN = idx >> n & 1;
                if (evenBit) {
                    double lonMid = (lonMin + lonMax) / 2;
                    if (bitN == 1) {
                        lonMin = lonMid;
                    } else {
                        lonMax = lonMid;
                    }
                } else {
                    double latMid = (latMin + latMax) / 2;
                    if (bitN == 1) {
                        latMin = latMid;
                    } else {
                        latMax = latMid;
                    }
                }
                evenBit = !evenBit;
            }
        }

        Map<String, Map<String, Double>> bounds = new HashMap<>();
        Map<String, Double> sw = new HashMap<>();
        sw.put("lat", latMin);
        sw.put("lon", lonMin);

        Map<String, Double> ne = new HashMap<>();
        ne.put("lat", latMax);
        ne.put("lon", lonMax);

        bounds.put("sw", sw);
        bounds.put("ne", ne);

        return bounds;
    }

    public String adjacent(String geohash, String direction) {
        geohash = geohash.toLowerCase();
        direction = direction.toLowerCase();

        if (geohash.length() == 0 || !direction.matches("[nsew]")) {
            throw new IllegalArgumentException("Invalid geohash or direction");
        }

        Map<String, String[]> neighbour = new HashMap<>();
        neighbour.put("n", new String[]{"p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx"});
        neighbour.put("s", new String[]{"14365h7k9dcfesgujnmqp0r2twvyx8zb", "238967debc01fg45kmstqrwxuvhjyznp"});
        neighbour.put("e", new String[]{"bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy"});
        neighbour.put("w", new String[]{"238967debc01fg45kmstqrwxuvhjyznp", "14365h7k9dcfesgujnmqp0r2twvyx8zb"});

        Map<String, String[]> border = new HashMap<>();
        border.put("n", new String[]{"prxz", "bcfguvyz"});
        border.put("s", new String[]{"028b", "0145hjnp"});
        border.put("e", new String[]{"bcfguvyz", "prxz"});
        border.put("w", new String[]{"0145hjnp", "028b"});

        String lastCh = String.valueOf(geohash.charAt(geohash.length() - 1));
        String parent = geohash.substring(0, geohash.length() - 1);

        int type = geohash.length() % 2;

        if (border.get(direction)[type].contains(lastCh) && !parent.isEmpty()) {
            parent = adjacent(parent, direction);
        }

        return parent + BASE32.charAt(neighbour.get(direction)[type].indexOf(lastCh));
    }

    public Map<String, String> neighbours(String geohash) {
        geohash = geohash.toLowerCase();

        Map<String, String> result = new HashMap<>();
        result.put("n", adjacent(geohash, "n"));
        result.put("ne", adjacent(adjacent(geohash, "n"), "e"));
        result.put("e", adjacent(geohash, "e"));
        result.put("se", adjacent(adjacent(geohash, "s"), "e"));
        result.put("s", adjacent(geohash, "s"));
        result.put("sw", adjacent(adjacent(geohash, "s"), "w"));
        result.put("w", adjacent(geohash, "w"));
        result.put("nw", adjacent(adjacent(geohash, "n"), "w"));

        return result;
    }
}