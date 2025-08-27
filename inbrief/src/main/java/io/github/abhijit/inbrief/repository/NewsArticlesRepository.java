package io.github.abhijit.inbrief.repository;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import io.github.abhijit.inbrief.models.NewsArticle;
import io.github.abhijit.inbrief.utils.DateParser;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Sorts.orderBy;

@Component
public class NewsArticlesRepository {

    private final MongoCollection<Document> newsArticlesCollection;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    public NewsArticlesRepository(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("inbrief");
        this.newsArticlesCollection = database.getCollection("news_articles");
    }

    public List<NewsArticle> findByCategory(String category, String cursor, int limit)  {

        Bson filter = and(
                eq("category", category),
                lt("publication_date", DateParser.parseDate(cursor))
        );

        String executedQuery = filter.toString();
        List<NewsArticle> results = new ArrayList<>();
        newsArticlesCollection.find(filter)
                .limit(limit)
                .forEach(doc -> results.add(NewsArticle.fromDocument(doc,executedQuery)));

        return results;
    }

    public List<NewsArticle> findBySource(String source, String cursor, int limit)  {
        Bson filter = and(
                eq("source_name", source),
                lt("publication_date", DateParser.parseDate(cursor))
        );

        List<NewsArticle> results = new ArrayList<>();
        newsArticlesCollection.find(filter)
                .limit(limit)
                .forEach(doc -> results.add(NewsArticle.fromDocument(doc,filter.toString())));

        return results;
    }

    public List<NewsArticle> findByRelevanceScore(double threshold, String cursor, int limit)  {
        Double lastScore = null;
        String lastId = null;

        if (cursor != null && cursor.contains("_")) {
            String[] parts = cursor.split("_");
            lastScore = Double.parseDouble(parts[0]);
            lastId = parts[1];
        }

        // Base filter
        Bson baseFilter = gt("relevance_score", threshold);

        // Cursor filter
        Bson cursorFilter = null;
        if (lastScore != null && lastId != null) {
            cursorFilter = or(
                    lt("relevance_score", lastScore),
                    and(eq("relevance_score", lastScore), lt("id", lastId))
            );
        }

        Bson finalFilter = cursorFilter != null ? and(baseFilter, cursorFilter) : baseFilter;

        // Sort by score DESC, id DESC
        Bson sort = orderBy(descending("relevance_score"), descending("id"));

        List<NewsArticle> results = new ArrayList<>();
        newsArticlesCollection.find(finalFilter)
                .sort(sort)
                .limit(limit)
                .forEach(doc -> results.add(NewsArticle.fromDocument(doc,finalFilter.toString())));

        return results;
    }

    public List<NewsArticle> findBySearch(String keyword, String cursor, int limit) {
        // Text search filter
        Bson textFilter = text(keyword);

        // Cursor handling (pagination by publication_date)
        Bson cursorFilter = cursor != null
                ? lt("publication_date", DateParser.parseDate(cursor))
                : new Document();

        // Combine filters
        Bson finalFilter = and(textFilter, cursorFilter);

        // Projection: add MongoDB’s textScore
        Bson projection = Projections.fields(
                Projections.metaTextScore("score"),
                Projections.include("_id", "id", "title", "description", "url",
                        "source", "publication_date", "category",
                        "relevance_score", "latitude", "longitude", "llmSummary")
        );

        // Sort by textScore (higher is more relevant)
        Bson sort = Sorts.metaTextScore("score");

        List<NewsArticle> results = new ArrayList<>();
        newsArticlesCollection.find(finalFilter)
                .projection(Projections.fields(projection, Projections.include("*")))
                .sort(sort)
                .limit(limit)
                .forEach(doc -> {
                    NewsArticle article = NewsArticle.fromDocument(doc,finalFilter.toString());
                    article.setTextScore(doc.getDouble("score")); // Capture MongoDB’s text relevance
                    results.add(article);
                });

        return results;
    }



    public List<NewsArticle> findByLocation(double lat, double lon, double radiusKm, String cursor, int limit) {

        double radiusMeters = radiusKm * 1000;

        Bson geoFilter = nearSphere(
                "location_point",
                new Point(new Position(lon, lat)),
                radiusMeters,
                null
        );

        // For pagination
//        Bson dateFilter = cursor != null ? Filters.lt("publication_date", LocalDateTime.parse(cursor)) : new Document();
//        Bson finalFilter = Filters.and(geoFilter, dateFilter);

        List<NewsArticle> results = new ArrayList<>();
        newsArticlesCollection.find(geoFilter)
                .sort(Sorts.descending("publication_date"))
                .limit(limit)
                .forEach(doc -> results.add(NewsArticle.fromDocument(doc,geoFilter.toString())));

        return results;
    }
    public List<String> findAllArticlesIds() {
        List<String> articles = new ArrayList<>();
        newsArticlesCollection.find()
                .projection(Projections.include("_id")) // only fetch IDs
                .forEach(doc -> articles.add(doc.getObjectId("_id").toHexString()));
        return articles;
    }

    public NewsArticle fetchArticleById(String id) {
        Document doc = newsArticlesCollection.find(eq("id", id)).first();
        return doc != null ? NewsArticle.fromDocument(doc,"") : null;
    }

    public NewsArticle getRandomArticle() {
        List<Document> result = newsArticlesCollection.aggregate(
                List.of(Aggregates.sample(1))
        ).into(new ArrayList<>());

        if (result.isEmpty()) {
            return null;
        }
        return NewsArticle.fromDocument(result.get(0),"");
    }

    public List<NewsArticle> getAllArticles() {
        List<NewsArticle> articles = new ArrayList<>();
        newsArticlesCollection.find().forEach(doc->articles.add(NewsArticle.fromDocument(doc,"")));
        return articles;
    }

    public void addSummaryToDoc(String docId, String summaryText) {
        Bson filter = eq("id", docId);   // select the doc you want
        Bson update = Updates.set("llm_summary", summaryText); // add new field

        newsArticlesCollection.updateOne(filter, update);
    }

}
