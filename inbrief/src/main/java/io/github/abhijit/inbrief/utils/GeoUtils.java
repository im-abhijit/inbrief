package io.github.abhijit.inbrief.utils;

import java.util.concurrent.ThreadLocalRandom;

public class GeoUtils {

    private static final double MIN_LAT = 12.8;
    private static final double MAX_LAT = 28.8;
    private static final double MIN_LON = 72.7;
    private static final double MAX_LON = 77.5;

    private static final double START_LAT = 6.5;
    private static final double END_LAT = 37.0;
    private static final double START_LON = 68.0;
    private static final double END_LON = 97.5;

    private static final double GRID_STEP = 0.5;
    private static final double GRID_RADIUS = 50;

    public static double randomLat() {
        return ThreadLocalRandom.current().nextDouble(MIN_LAT, MAX_LAT);
    }

    public static double randomLon() {
        return ThreadLocalRandom.current().nextDouble(MIN_LON, MAX_LON);
    }

    public static double getStartLat() {
        return START_LAT;
    }
    public static double getEndLat() {
        return END_LAT;
    }
    public static double getStartLon() {
        return START_LON;
    }
    public static double getEndLon() {
        return END_LON;
    }
    public static double getGridStep() {
        return GRID_STEP;
    }
    public static double getGridRadius() {
        return GRID_RADIUS;
    }

}
