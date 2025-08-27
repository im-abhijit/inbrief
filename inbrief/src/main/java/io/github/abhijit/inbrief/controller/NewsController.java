package io.github.abhijit.inbrief.controller;

import io.github.abhijit.inbrief.api.ApiResponse;
import io.github.abhijit.inbrief.api.ResultCode;
import io.github.abhijit.inbrief.exception.BadRequestException;
import io.github.abhijit.inbrief.models.CursorPage;
import io.github.abhijit.inbrief.models.NewsArticle;
import io.github.abhijit.inbrief.service.NewsRetrievalService;
import io.github.abhijit.inbrief.service.QueryOrchestrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/news")
public class NewsController {

    private final NewsRetrievalService newsRetrievalService;

    private final QueryOrchestrationService queryOrchestrationService;

    @Autowired
    public NewsController(NewsRetrievalService newsRetrievalService, QueryOrchestrationService queryOrchestrationService) {
        this.newsRetrievalService = newsRetrievalService;
        this.queryOrchestrationService = queryOrchestrationService;
    }



    @GetMapping("/category")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> getByCategory(
            @RequestParam String name,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String cursor) throws Exception{
        if (name == null || name.isBlank()) throw new BadRequestException("Category name must not be blank");
        CursorPage<NewsArticle> page = newsRetrievalService.fetchByCategory(name, limit, cursor);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Fetched by category", page));
    }

    @GetMapping("/source")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> getBySource(
            @RequestParam String name,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor) throws Exception{
        if (name == null || name.isBlank()) throw new BadRequestException("Source name must not be blank");
        CursorPage<NewsArticle> page = newsRetrievalService.fetchBySource(name, limit, cursor);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Fetched by source", page));
    }

    @GetMapping("/score")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> getByRelevanceScore(
            @RequestParam double score,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String cursor) throws Exception{
        CursorPage<NewsArticle> page = newsRetrievalService.fetchByRelevanceScore(score, limit, cursor);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Fetched by relevance score", page));
    }

    @GetMapping("/query")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> queryNews(@RequestParam String query,
                                             @RequestParam(required = false, defaultValue = "10") int limit,
                                             @RequestParam(required = false) String cursor) {
        if (query == null || query.isBlank()) throw new BadRequestException("Query must not be blank");
        CursorPage<NewsArticle> page = queryOrchestrationService.executeQuery(query, limit, cursor);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Query executed", page));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> findBySearch(@RequestParam String query,
                                                                          @RequestParam(required = false, defaultValue = "10") int limit,
                                                                          @RequestParam(required = false) String cursor) {
        if (query == null || query.isBlank()) throw new BadRequestException("Query must not be blank");
        CursorPage<NewsArticle> page = newsRetrievalService.fetchBySearch(query, cursor, limit);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Query executed", page));
    }

    @GetMapping("/location")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> getByLocation(@RequestParam double lat,
                                                                 @RequestParam double lon,
                                                                 @RequestParam(required = false, defaultValue = "10") int limit,
                                                                 @RequestParam(required = false) String cursor,
                                                                 @RequestParam double radiusKm){
        CursorPage<NewsArticle> page = newsRetrievalService.fetchByLocation(lat,lon,radiusKm,limit,cursor);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Fetched by location", page));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<CursorPage<NewsArticle>>> trendingNearMe(@RequestParam double lat,
                                                            @RequestParam double lon,
                                                            @RequestParam(required = false,defaultValue = "50") double radiusKm,
                                                            @RequestParam(required = false,defaultValue = "10") int limit ){
        CursorPage<NewsArticle> page = newsRetrievalService.findTrendingArticlesByLocation(lat,lon,radiusKm,limit);
        if (page.getItems() == null || page.getItems().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(ResultCode.NO_MORE_ARTICLES, "No more articles", page));
        }
        return ResponseEntity.ok(ApiResponse.success(ResultCode.SUCCESS, "Fetched by location", page));
    }

    @GetMapping("/populate")
    public ResponseEntity<String> populateSummary(){
        queryOrchestrationService.populateSummary();
        return ResponseEntity.ok("Populated");
    }

}
