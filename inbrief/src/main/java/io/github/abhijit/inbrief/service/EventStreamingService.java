package io.github.abhijit.inbrief.service;


import io.github.abhijit.inbrief.models.ArticleResult;
import io.github.abhijit.inbrief.models.EventData;
import io.github.abhijit.inbrief.models.NewsArticle;
import io.github.abhijit.inbrief.utils.GeoUtils;
import io.github.abhijit.inbrief.utils.RedisKeyUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;

@Component
public class EventStreamingService {

    private final TrendingService trendingService;
    private final NewsRetrievalService newsRetrievalService;
    private final Random random = new Random();

    public EventStreamingService(TrendingService trendingService, NewsRetrievalService newsRetrievalService) {
        this.trendingService = trendingService;
        this.newsRetrievalService = newsRetrievalService;
    }

    @Scheduled(fixedRate = 100, initialDelay = 1000)
    public void emitEvent() {

        // 1️⃣ Random location
        double lat = GeoUtils.randomLat();
        double lon = GeoUtils.randomLon();

        // 2️⃣ Random article
        NewsArticle article = newsRetrievalService.findRandomArticle();
        if (article == null) return;
        String articleId = article.getId();

        // 3️⃣ Random event type
        EventType eventType = EventType.randomEvent();
        double weight = eventType.getWeight();

        // 4️⃣ Timestamp for recency
        long now = System.currentTimeMillis();

        // 5️⃣ Push event into Redis list for this article
        trendingService.setRecencyScore(article.getId(), new EventData(eventType.name(), weight, now));

        // 6️⃣ Update cumulative ZSET score (volume + event type)
        trendingService.addScore(articleId, weight);

        // 7️⃣ Add/update geo location
        trendingService.addArticleGeo(articleId, lat, lon);

        System.out.printf("[%s] Event: %s on article %s at (%.5f, %.5f) → weight %.2f%n",
                Instant.now(), eventType, articleId, lat, lon, weight);
    }

    @Scheduled(fixedRate = 5000)
    public void updateTrendingScores() {
        for (double lat = GeoUtils.getStartLat(); lat <= GeoUtils.getEndLat(); lat += GeoUtils.getGridStep()) {
            for (double lon = GeoUtils.getStartLon(); lon <= GeoUtils.getEndLon(); lon += GeoUtils.getGridStep()) {
                String gridKey = "trending:" + lat + ":" + lon;
                List<ArticleResult> nearbyArticles = trendingService.getTrendingNearby(lat, lon, GeoUtils.getGridRadius(), 100);
                if (nearbyArticles.isEmpty()) continue;
                trendingService.saveTrendingForGrid(gridKey,nearbyArticles);
            }
        }
    }
    public enum EventType {
        VIEW(1.0),
        CLICK(2.0),
        SHARE(3.0);

        private final double weight;
        private static final EventType[] VALUES = values();
        private static final Random RANDOM = new Random();

        EventType(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }

        // Returns a random enum value
        public static EventType randomEvent() {
            return VALUES[RANDOM.nextInt(VALUES.length)];
        }
    }

}
