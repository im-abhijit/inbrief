package io.github.abhijit.inbrief.service;

import io.github.abhijit.inbrief.llm.LLMQueryService;
import io.github.abhijit.inbrief.models.*;
import io.github.abhijit.inbrief.repository.NewsArticlesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueryOrchestrationService {

    @Autowired
    private NewsRetrievalService newsRetrievalService;

    @Autowired
    private LLMQueryService llmQueryService;

    @Autowired
    private NewsArticlesRepository newsArticlesRepository;

    public CursorPage<NewsArticle> executeQuery(String rawQuery, int limit, String cursor) {
        List<NewsArticle> allResults = new ArrayList<>();
        QueryData queryData = llmQueryService.getQueryData(rawQuery);

        // Category search
        if (queryData.getIntent().contains("category") && queryData.getCategories() != null) {
            for (AttributeData categoryGroup : queryData.getCategories()) {
                for (String category : categoryGroup.getValues().keySet()) {
                    allResults.addAll(
                            newsRetrievalService.fetchByCategory(category, limit, cursor).getItems()
                    );
                }
            }
        }

        // Source search
        if (queryData.getIntent().contains("source") && queryData.getSource() != null) {
            for (AttributeData sourceGroup : queryData.getSource()) {
                for (String source : sourceGroup.getValues().keySet()) {
                    allResults.addAll(
                            newsRetrievalService.fetchBySource(source, limit, cursor).getItems()
                    );
                }
            }
        }

        // Keyword search
        if (queryData.getKeywords() != null && !queryData.getKeywords().isEmpty()) {
            for (AttributeData keywordGroup : queryData.getKeywords()) {
                for (String keyword : keywordGroup.getValues().keySet()) {
                    allResults.addAll(
                            newsRetrievalService.fetchBySearch(keyword, cursor, limit).getItems()
                    );
                }
            }
        }

        // Location (optional: for nearby intent)
        if (queryData.getIntent().contains("nearby") && queryData.getLocation() != null) {
            for (AttributeData locationGroup : queryData.getLocation()) {
                for (String loc : locationGroup.getValues().keySet()) {
                    // You can add nearby search logic here
                    // Example: newsRetrievalService.fetchByLocation(loc, cursor, limit)
                }
            }
        }

        return buildCursorPage(allResults, queryData, limit);
    }

    private double computeRank(NewsArticle article, QueryData queryData) {
        double score = article.getRelevanceScore();

        // Apply weighted contributions from each attribute group
        score += computeAttributeScore(article.getCategory(), queryData.getCategories());
        score += computeAttributeScore(Collections.singletonList(article.getSource()), queryData.getSource());
        score += computeKeywordScore(article, queryData.getKeywords());

        return score;
    }

    private double computeAttributeScore(List<String> articleValues, List<AttributeData> groups) {
        if (groups == null) return 0.0;

        double total = 0.0;
        for (AttributeData group : groups) {
            double globalWeight = group.getGlobalWeight();
            for (String value : articleValues) {
                if (group.getValues().containsKey(value)) {
                    total += globalWeight * group.getValues().get(value);
                }
            }
        }
        return total;
    }

    private double computeKeywordScore(NewsArticle article, List<AttributeData> keywordGroups) {
        if (keywordGroups == null) return 0.0;

        double total = 0.0;
        for (AttributeData group : keywordGroups) {
            double globalWeight = group.getGlobalWeight();
            for (Map.Entry<String, Double> entry : group.getValues().entrySet()) {
                String keyword = entry.getKey();
                double localWeight = entry.getValue();
                if (article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        article.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                    total += globalWeight * localWeight;
                }
            }
        }
        return total;
    }

    private CursorPage<NewsArticle> buildCursorPage(List<NewsArticle> allResults, QueryData queryData, int limit) {
        Map<String, NewsArticle> deduped = new HashMap<>();
        for (NewsArticle article : allResults) {
            deduped.put(article.getId(), article);
        }

        List<NewsArticle> dedupedList = new ArrayList<>(deduped.values());

        dedupedList.sort(Comparator
                .comparingDouble((NewsArticle a) -> computeRank(a, queryData)).reversed()
                .thenComparing(NewsArticle::getPublicationDate, Comparator.reverseOrder())
        );

        List<NewsArticle> paginated = dedupedList.stream()
                .limit(limit)
                .collect(Collectors.toList());

        CursorPage<NewsArticle> page = new CursorPage<>();
        Metadata metadata = new Metadata();
        metadata.setPageSize(paginated.size());
        metadata.setNextCursor(getNextCursor(paginated));
        page.setMetadata(metadata);
        page.setItems(paginated);
        return page;
    }

    private String getNextCursor(List<NewsArticle> articles) {
        if (articles.isEmpty()) return null;
        return articles.get(articles.size() - 1).getPublicationDate().toString();
    }

    public void populateSummary() {
        List<NewsArticle> articles = newsArticlesRepository.getAllArticles();
        int count  =0;
        for(NewsArticle article : articles) {
            //String summary = llmQueryService.getSummary(article.getTitle(), article.getDescription());
            String summary = article.getTitle() + "." +article.getDescription();
            newsArticlesRepository.addSummaryToDoc(article.getId(),summary);
            count++;
            System.out.println(article.getTitle() + " " + count);
        }
    }
}
