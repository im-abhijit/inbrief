package io.github.abhijit.inbrief.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.abhijit.inbrief.utils.DateParser;
import lombok.Data;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsArticle {
    private String id;
    private String title;
    private String description;
    private String url;
    private String source;
    private String publicationDate;
    private List<String> category;
    private Double relevanceScore;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private Double trendingScore;
    private String llmSummary;
    private Double textScore;
    @JsonIgnore
    private String query;


    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public Double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getTrendingScore() {
        return trendingScore;
    }

    public void setTrendingScore(Double trendingScore) {
        this.trendingScore = trendingScore;
    }

    public String getLlmSummary() {
        return llmSummary;
    }

    public void setLlmSummary(String llmSummary) {
        this.llmSummary = llmSummary;
    }

    public Double getTextScore() {
        return textScore;
    }

    public void setTextScore(Double textScore) {
        this.textScore = textScore;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public static NewsArticle fromDocument(Document doc, String query) {
        NewsArticle article = new NewsArticle();
        article.setId(doc.getString("id"));
        article.setTitle(doc.getString("title"));
        article.setDescription(doc.getString("description"));
        article.setUrl(doc.getString("url"));
        article.setSource(doc.getString("source_name"));
        article.setRelevanceScore(doc.getDouble("relevance_score"));
        article.setLatitude(doc.getDouble("latitude"));
        article.setLongitude(doc.getDouble("longitude"));
        article.setCategory(doc.getList("category", String.class));
        article.setLlmSummary(doc.getString("llm_summary"));
        if (doc.getDate("publication_date") != null) {
            article.setPublicationDate(DateParser.parseDate(doc.getDate("publication_date")));
        }
        article.setQuery(query);
        return article;
    }
}
