package io.github.abhijit.inbrief.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abhijit.inbrief.models.ArticleResult;
import io.github.abhijit.inbrief.models.EventData;
import io.github.abhijit.inbrief.utils.GeoUtils;
import io.github.abhijit.inbrief.utils.ObjectMapperUtil;
import io.github.abhijit.inbrief.utils.RedisKeyUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TrendingService {

    private static final String GEO_KEY = RedisKeyUtil.geoArticlesKey();
    private static final String SCORE_KEY = RedisKeyUtil.scoreArticlesKey();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;
    private final ZSetOperations<String, String> zSetOps;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrendingService.class);

    public TrendingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    // Add article with geo + score
    public void addArticle(String articleId, double lat, double lon, double score) {
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(lon, lat), articleId);
        zSetOps.add(SCORE_KEY, articleId, score);
    }
    public void addScore(String articleId, double score) {
        zSetOps.incrementScore(SCORE_KEY, articleId, score);
    }

    public void addArticleGeo(String articleId, double lat, double lon) {
        redisTemplate.opsForGeo().add(GEO_KEY, new Point(lon, lat), articleId);
    }

    public void setRecencyScore(String articleId, EventData eventData) {
        String eventDataString = StringUtil.EMPTY_STRING;
        try {
             eventDataString = objectMapper.writeValueAsString(eventData);
        }
        catch (Exception e) {
            LOGGER.error("Failed to serialize EventData for article {}. Error: {}", articleId, e.toString());
        }
        redisTemplate.opsForList().leftPush(RedisKeyUtil.eventsListKey(articleId), eventDataString);
    }
    public List<ArticleResult> getTrendingNearby(double userLat, double userLon,
                                                 double radiusKm, int limit) {
        var geoResults = redisTemplate.opsForGeo().radius(
                GEO_KEY, new Circle(new Point(userLon, userLat), new Distance(radiusKm, Metrics.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance());
        if (geoResults == null || geoResults.getContent().isEmpty()) return List.of();
        List<ArticleResult> results = new ArrayList<>();
        for (var gr : geoResults.getContent()) {
            String articleId = gr.getContent().getName();
            Double volumeScore = zSetOps.score(SCORE_KEY, articleId);
            if (volumeScore == null) continue;
            double distanceKm = gr.getDistance().getValue();
            double geoBoost = 1 / (1 + distanceKm);
            List<String> eventJsonList = redisTemplate.opsForList().range(RedisKeyUtil.eventsListKey(articleId), 0, -1);
            double recencyFactor = 0;
            long now = System.currentTimeMillis();
            double lambda = 0.001;
            if (eventJsonList != null) {
                for (String json : eventJsonList) {
                    try {
                        EventData e = objectMapper.readValue(json, EventData.class);
                        recencyFactor += e.weight() * Math.exp(-lambda * (now - e.timestamp()));
                    } catch (Exception ex) {
                        LOGGER.warn("Failed to deserialize EventData. Skipping event. Error: {}. Payload: {}", ex.toString(), json);
                    }
                }
            }
            double finalScore = (volumeScore + recencyFactor) * geoBoost;
            results.add(new ArticleResult(articleId, finalScore, distanceKm));
        }

        return results.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(limit)
                .toList();
    }

    public void saveTrendingForGrid(String gridKey, List<ArticleResult> nearbyArticles) {
        if (nearbyArticles == null || nearbyArticles.isEmpty()) return;
        redisTemplate.delete(gridKey);
        for (ArticleResult article : nearbyArticles) {
            redisTemplate.opsForZSet().add(gridKey, article.id(), article.score());
            String hashKey = "article:" + article.id();
            Map<String, Object> fields = Map.of(
                    "score", article.score(),
                    "distance", article.distanceKm()
            );
            redisTemplate.opsForValue().set(hashKey, Objects.requireNonNull(ObjectMapperUtil.getStringFromObject(fields)));
        }
    }


    public List<ArticleResult> fetchArticlesFromNearByLocation(double lat, double lon, double radiusKm, int limit) {
        lat = snapToGrid(lat, GeoUtils.getStartLat(),GeoUtils.getGridStep());
        lon = snapToGrid(lon,GeoUtils.getStartLon(),GeoUtils.getGridStep());
        String gridKey = RedisKeyUtil.trendingGridKey(lat, lon);
        Set<String> topIds = redisTemplate.opsForZSet().reverseRange(gridKey, 0, limit - 1);
        if (topIds == null || topIds.isEmpty()) return List.of();
        List<ArticleResult> results = new ArrayList<>();
        for (String id : topIds) {
            String data = redisTemplate.opsForValue().get(RedisKeyUtil.articleDataKey(id));
            Map<String, Object> fields = ObjectMapperUtil.getObjectFromString(data, new TypeReference<Map<String, Object>>() {});
            if (data == null || data.isEmpty()) continue;

            double score = 0;
            double distance = 0;

            Object scoreObj = fields.get("score");
            if (scoreObj != null) {
                score = Double.parseDouble(scoreObj.toString());
            }

            Object distanceObj = fields.get("distance");
            if (distanceObj != null) {
                distance = Double.parseDouble(distanceObj.toString());
            }

            results.add(new ArticleResult(id, score, distance));
        }

        return results;
    }

    private double snapToGrid(double value, double min, double step) {
        return min + Math.floor((value - min) / step) * step;
    }

}
