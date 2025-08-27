package io.github.abhijit.inbrief.service;

import io.github.abhijit.inbrief.models.ArticleResult;
import io.github.abhijit.inbrief.models.CursorPage;
import io.github.abhijit.inbrief.models.Metadata;
import io.github.abhijit.inbrief.models.NewsArticle;
import io.github.abhijit.inbrief.repository.NewsArticlesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class NewsRetrievalService {

    @Autowired
    private NewsArticlesRepository newsArticlesRepository;

    @Autowired
    private TrendingService trendingService;


    public CursorPage<NewsArticle> fetchByCategory(String category, int limit, String cursor)  {
        List<NewsArticle> articles = newsArticlesRepository.findByCategory(category, cursor, limit);
        return buildCursorPage(articles,getNextCursorForNonDuplicateFields(articles));
    }

    public CursorPage<NewsArticle> fetchBySource(String source, int limit, String cursor)  {
        List<NewsArticle> articles = newsArticlesRepository.findBySource(source, cursor, limit);
        return buildCursorPage(articles,getNextCursorForNonDuplicateFields(articles));
    }

    public CursorPage<NewsArticle> fetchByRelevanceScore(double threshold, int limit, String cursor) throws Exception {
        List<NewsArticle> articles = newsArticlesRepository.findByRelevanceScore(threshold, cursor, limit);
        return buildCursorPage(articles,getNextCursorForDuplicateFields(articles));
    }

    public CursorPage<NewsArticle> fetchBySearch(String keyword, String cursor, int limit){
        List<NewsArticle> articles = newsArticlesRepository.findBySearch(keyword, cursor, limit);
        return buildCursorPage(articles,getNextCursorForNonDuplicateFields(articles));
    }

    public CursorPage<NewsArticle> fetchByLocation(double lat, double lon, double radiusKm, int limit, String cursor){
        List<NewsArticle> articles = newsArticlesRepository.findByLocation(lat, lon,radiusKm,cursor, limit);
        return buildCursorPage(articles,getNextCursorForNonDuplicateFields(articles));

    }

    public List<String> findAllArticlesIds(){
        return newsArticlesRepository.findAllArticlesIds();
    }

    public NewsArticle findArticleById(String id){
        return newsArticlesRepository.fetchArticleById(id);
    }

    public NewsArticle findRandomArticle(){
        return newsArticlesRepository.getRandomArticle();
    }

    public CursorPage<NewsArticle> findTrendingArticlesByLocation(double lat, double lon, double radiusKm,int limit){
        List<ArticleResult> trendingNearby = trendingService.getTrendingNearby(lat,lon,radiusKm,limit);
        List<NewsArticle> articles = new ArrayList<>();
        for(ArticleResult article : trendingNearby){
            NewsArticle newsArticle = findArticleById(article.id());
            newsArticle.setDistance(article.distanceKm());
            newsArticle.setTrendingScore(article.score());
            articles.add(newsArticle);
        }
        return buildCursorPage(articles,getNextCursorForNonDuplicateFields(articles));
    }

    private CursorPage<NewsArticle> buildCursorPage(List<NewsArticle> articles, String cursor) {
        CursorPage<NewsArticle> cursorPage = new CursorPage<>();
        cursorPage.setItems(articles);
        Metadata metadata = new Metadata();
        metadata.setNextCursor(cursor);
        metadata.setPageSize(articles.size());
        metadata.setExecutedQuery(!articles.isEmpty() ?articles.getFirst().getQuery():null);
        cursorPage.setMetadata(metadata);
        return cursorPage;
    }

    private String getNextCursorForDuplicateFields(List<NewsArticle> articles) {
        if(Objects.nonNull(articles) && !articles.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            NewsArticle lastArticle = articles.getLast();
            sb.append(lastArticle.getRelevanceScore()).append("_").append(lastArticle.getId());
            return sb.toString();
        }
        return null;
    }

    private String getNextCursorForNonDuplicateFields(List<NewsArticle> articles) {
        if(Objects.nonNull(articles) && !articles.isEmpty()){
            NewsArticle lastArticle = articles.getLast();
            return lastArticle.getPublicationDate();
        }
        return null;
    }

}
