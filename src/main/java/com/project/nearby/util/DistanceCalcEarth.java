/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.project.nearby.util;




import com.project.nearby.util.Shapes.BBox;

import static java.lang.Math.*;

/**
 * @author Peter Karich
 */
public class DistanceCalcEarth implements DistanceCalc {
    /**
     * mean radius of the earth
     */
    public final static double R = 6371000; // m
    /**
     * Radius of the earth at equator
     */
    public final static double R_EQ = 6378137; // m
    /**
     * Circumference of the earth
     */
    public final static double C = 2 * PI * R;
    public final static double KM_MILE = 1.609344;
    public final static double METERS_PER_DEGREE = C / 360.0;
    public static final DistanceCalcEarth DIST_EARTH = new DistanceCalcEarth();

    /**
     * Calculates distance of (from, to) in meter.
     * <p>
     * http://en.wikipedia.org/wiki/Haversine_formula a = sin²(Δlat/2) +
     * cos(lat1).cos(lat2).sin²(Δlong/2) c = 2.atan2(√a, √(1−a)) d = R.c
     */
    @Override
    public double calcDist(double fromLat, double fromLon, double toLat, double toLon) {
        double normedDist = calcNormalizedDist(fromLat, fromLon, toLat, toLon);
        return R * 2 * asin(sqrt(normedDist));
    }

    /**
     * This implements a rather quick solution to calculate 3D distances on earth using euclidean
     * geometry mixed with Haversine formula used for the on earth distance. The haversine formula makes
     * not so much sense as it is only important for large distances where then the rather smallish
     * heights would becomes negligible.
     */
    @Override
    public double calcDist3D(double fromLat, double fromLon, double fromHeight,
                             double toLat, double toLon, double toHeight) {
        double eleDelta = hasElevationDiff(fromHeight, toHeight) ? (toHeight - fromHeight) : 0;
        double len = calcDist(fromLat, fromLon, toLat, toLon);
        return Math.sqrt(eleDelta * eleDelta + len * len);
    }

    @Override
    public double calcDenormalizedDist(double normedDist) {
        return R * 2 * asin(sqrt(normedDist));
    }

    /**
     * Returns the specified length in normalized meter.
     */
    @Override
    public double calcNormalizedDist(double dist) {
        double tmp = sin(dist / 2 / R);
        return tmp * tmp;
    }

    @Override
    public double calcNormalizedDist(double fromLat, double fromLon, double toLat, double toLon) {
        double sinDeltaLat = sin(toRadians(toLat - fromLat) / 2);
        double sinDeltaLon = sin(toRadians(toLon - fromLon) / 2);
        return sinDeltaLat * sinDeltaLat
                + sinDeltaLon * sinDeltaLon * cos(toRadians(fromLat)) * cos(toRadians(toLat));
    }

    /**
     * Circumference of the earth at different latitudes (breitengrad)
     */
    public double calcCircumference(double lat) {
        return 2 * PI * R * cos(toRadians(lat));
    }

    public boolean isDateLineCrossOver(double lon1, double lon2) {
        return abs(lon1 - lon2) > 180.0;
    }

    @Override
    public BBox createBBox(double lat, double lon, double radiusInMeter) {
        if (radiusInMeter <= 0)
            throw new IllegalArgumentException("Distance must not be zero or negative! " + radiusInMeter + " lat,lon:" + lat + "," + lon);

        // length of a circle at specified lat / dist
        double dLon = (360 / (calcCircumference(lat) / radiusInMeter));

        // length of a circle is independent of the longitude
        double dLat = (360 / (DistanceCalcEarth.C / radiusInMeter));

        // Now return bounding box in coordinates
        return new BBox(lon - dLon, lon + dLon, lat - dLat, lat + dLat);
    }

    @Override
    public double calcNormalizedEdgeDistance(double r_lat_deg, double r_lon_deg,
                                             double a_lat_deg, double a_lon_deg,
                                             double b_lat_deg, double b_lon_deg) {
        double shrinkFactor = calcShrinkFactor(a_lat_deg, b_lat_deg);

        double a_lat = a_lat_deg;
        double a_lon = a_lon_deg * shrinkFactor;

        double b_lat = b_lat_deg;
        double b_lon = b_lon_deg * shrinkFactor;

        double r_lat = r_lat_deg;
        double r_lon = r_lon_deg * shrinkFactor;

        double delta_lon = b_lon - a_lon;
        double delta_lat = b_lat - a_lat;

        if (delta_lat == 0)
            // special case: horizontal edge
            return calcNormalizedDist(a_lat_deg, r_lon_deg, r_lat_deg, r_lon_deg);

        if (delta_lon == 0)
            // special case: vertical edge
            return calcNormalizedDist(r_lat_deg, a_lon_deg, r_lat_deg, r_lon_deg);

        double norm = delta_lon * delta_lon + delta_lat * delta_lat;
        double factor = ((r_lon - a_lon) * delta_lon + (r_lat - a_lat) * delta_lat) / norm;

        // x,y is projection of r onto segment a-b
        double c_lon = a_lon + factor * delta_lon;
        double c_lat = a_lat + factor * delta_lat;
        return calcNormalizedDist(c_lat, c_lon / shrinkFactor, r_lat_deg, r_lon_deg);
    }

    @Override
    public double calcNormalizedEdgeDistance3D(double r_lat_deg, double r_lon_deg, double r_ele_m,
                                               double a_lat_deg, double a_lon_deg, double a_ele_m,
                                               double b_lat_deg, double b_lon_deg, double b_ele_m) {
        if (Double.isNaN(r_ele_m) || Double.isNaN(a_ele_m) || Double.isNaN(b_ele_m))
            return calcNormalizedEdgeDistance(r_lat_deg, r_lon_deg, a_lat_deg, a_lon_deg, b_lat_deg, b_lon_deg);

        double shrinkFactor = calcShrinkFactor(a_lat_deg, b_lat_deg);

        double a_lat = a_lat_deg;
        double a_lon = a_lon_deg * shrinkFactor;
        double a_ele = a_ele_m / METERS_PER_DEGREE;

        double b_lat = b_lat_deg;
        double b_lon = b_lon_deg * shrinkFactor;
        double b_ele = b_ele_m / METERS_PER_DEGREE;

        double r_lat = r_lat_deg;
        double r_lon = r_lon_deg * shrinkFactor;
        double r_ele = r_ele_m / METERS_PER_DEGREE;

        double delta_lon = b_lon - a_lon;
        double delta_lat = b_lat - a_lat;
        double delta_ele = b_ele - a_ele;

        double norm = delta_lon * delta_lon + delta_lat * delta_lat + delta_ele * delta_ele;
        double factor = ((r_lon - a_lon) * delta_lon + (r_lat - a_lat) * delta_lat + (r_ele - a_ele) * delta_ele) / norm;
        if (Double.isNaN(factor)) factor = 0;

        // x,y,z is projection of r onto segment a-b
        double c_lon = a_lon + factor * delta_lon;
        double c_lat = a_lat + factor * delta_lat;
        double c_ele_m = (a_ele + factor * delta_ele) * METERS_PER_DEGREE;
        return calcNormalizedDist(c_lat, c_lon / shrinkFactor, r_lat_deg, r_lon_deg) + calcNormalizedDist(r_ele_m - c_ele_m);
    }

    double calcShrinkFactor(double a_lat_deg, double b_lat_deg) {
        return cos(toRadians((a_lat_deg + b_lat_deg) / 2));
    }


    @Override
    public boolean validEdgeDistance(double r_lat_deg, double r_lon_deg,
                                     double a_lat_deg, double a_lon_deg,
                                     double b_lat_deg, double b_lon_deg) {
        double shrinkFactor = calcShrinkFactor(a_lat_deg, b_lat_deg);
        double a_lat = a_lat_deg;
        double a_lon = a_lon_deg * shrinkFactor;

        double b_lat = b_lat_deg;
        double b_lon = b_lon_deg * shrinkFactor;

        double r_lat = r_lat_deg;
        double r_lon = r_lon_deg * shrinkFactor;

        double ar_x = r_lon - a_lon;
        double ar_y = r_lat - a_lat;
        double ab_x = b_lon - a_lon;
        double ab_y = b_lat - a_lat;
        double ab_ar = ar_x * ab_x + ar_y * ab_y;

        double rb_x = b_lon - r_lon;
        double rb_y = b_lat - r_lat;
        double ab_rb = rb_x * ab_x + rb_y * ab_y;

        // calculate the exact degree alpha(ar, ab) and beta(rb,ab) if it is case 1 then both angles are <= 90°
        // double ab_ar_norm = Math.sqrt(ar_x * ar_x + ar_y * ar_y) * Math.sqrt(ab_x * ab_x + ab_y * ab_y);
        // double ab_rb_norm = Math.sqrt(rb_x * rb_x + rb_y * rb_y) * Math.sqrt(ab_x * ab_x + ab_y * ab_y);
        // return Math.acos(ab_ar / ab_ar_norm) <= Math.PI / 2 && Math.acos(ab_rb / ab_rb_norm) <= Math.PI / 2;
        return ab_ar > 0 && ab_rb > 0;
    }

    @Override
    public boolean isCrossBoundary(double lon1, double lon2) {
        return abs(lon1 - lon2) > 300;
    }

    protected boolean hasElevationDiff(double a, double b) {
        return a != b && !Double.isNaN(a) && !Double.isNaN(b);
    }

    @Override
    public String toString() {
        return "EXACT";
    }
}
