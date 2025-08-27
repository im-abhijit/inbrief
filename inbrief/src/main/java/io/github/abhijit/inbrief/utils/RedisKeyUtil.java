package io.github.abhijit.inbrief.utils;

public final class RedisKeyUtil {

    private static final String GEO_ARTICLES = "geo:articles";
    private static final String SCORE_ARTICLES = "score:articles";
    private static final String EVENTS_PREFIX = "events:";
    private static final String TRENDING_PREFIX = "trending:";
    private static final String ARTICLE_PREFIX = "article:";

    private RedisKeyUtil() {}

    public static String geoArticlesKey() {
        return GEO_ARTICLES;
    }

    public static String scoreArticlesKey() {
        return SCORE_ARTICLES;
    }

    public static String eventsListKey(String articleId) {
        return EVENTS_PREFIX + articleId;
    }

    public static String trendingGridKey(double lat, double lon) {
        return TRENDING_PREFIX + lat + ":" + lon;
    }

    public static String articleDataKey(String articleId) {
        return ARTICLE_PREFIX + articleId;
    }
}

