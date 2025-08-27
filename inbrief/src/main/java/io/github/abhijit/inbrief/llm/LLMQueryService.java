package io.github.abhijit.inbrief.llm;

import io.github.abhijit.inbrief.models.QueryData;
import io.github.abhijit.inbrief.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class LLMQueryService {
    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";

    private final String apiKey;
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(LLMQueryService.class);

    public LLMQueryService(@Value("${gemini.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Calls Gemini API to process user query into structured QueryData.
     */
    public QueryData getQueryData(String rawQuery) {
        String prompt =
                "You are an assistant that extracts structured information from free-text user queries "
                        + "for a news search application. The goal is to help the system decide how to query the database "
                        + "for the most relevant articles.\n\n"

                        + "From the user query, extract the following fields:\n"
                        + "1. intent → one or more of: [\"category\", \"source\", \"search\", \"nearby\", \"score\"].\n"
                        + "   - category → if the query refers to a general domain/topic (e.g., technology, sports, politics).\n"
                        + "   - source → if the query specifies a news source (e.g., Reuters, New York Times).\n"
                        + "   - search → if the query requires keyword search in article titles/descriptions.\n"
                        + "   - nearby → if the query specifies a location to find nearby news.\n"
                        + "   - score → if the query suggests filtering by relevance score or importance.\n\n"

                        + "2. categories → a list of objects. Each object must contain:\n"
                        + "   - globalWeight (a number between 0 and 1)\n"
                        + "   - values (a JSON object mapping category string → weight)\n\n"

                        + "3. keywords → a list of objects. Each object must contain:\n"
                        + "   - globalWeight (a number between 0 and 1)\n"
                        + "   - values (a JSON object mapping keyword string → weight)\n\n"

                        + "4. location → a list of objects. Each object must contain:\n"
                        + "   - globalWeight (a number between 0 and 1)\n"
                        + "   - values (a JSON object mapping location string → weight)\n\n"

                        + "5. source → a list of objects. Each object must contain:\n"
                        + "   - globalWeight (a number between 0 and 1)\n"
                        + "   - values (a JSON object mapping source name → weight)\n\n"

                        + "Important rules:\n"
                        + "- The sum of all globalWeight values across categories, keywords, location, and source must equal 1.0.\n"
                        + "- Within each group, the sum of weights in 'values' must equal 1.0.\n"
                        + "- If a field is not relevant to the query, set its list to an empty array [].\n\n"

                        + "Return only valid JSON in this exact format:\n"
                        + "{\n"
                        + "  \"intent\": [\"...\"],\n"
                        + "  \"categories\": [\n"
                        + "    { \"globalWeight\": 0.3, \"values\": { \"technology\": 1.0 } }\n"
                        + "  ],\n"
                        + "  \"keywords\": [\n"
                        + "    { \"globalWeight\": 0.4, \"values\": { \"databases\": 0.7, \"AI\": 0.3 } }\n"
                        + "  ],\n"
                        + "  \"location\": [\n"
                        + "    { \"globalWeight\": 0.2, \"values\": { \"New York\": 1.0 } }\n"
                        + "  ],\n"
                        + "  \"source\": [\n"
                        + "    { \"globalWeight\": 0.1, \"values\": { \"Reuters\": 1.0 } }\n"
                        + "  ]\n"
                        + "}\n\n"

                        + "Query: \"" + rawQuery + "\"";

        String response = generateContent(prompt);
        return GeminiResponseParser.parseResponse(response);
    }

    /**
     * Calls Gemini API to generate a short summary of a news article.
     */
    public String getSummary(String title, String desc) {
        String prompt =
                "You are a helpful assistant that writes verbose but concise summaries for news articles. " +
                        "Use both the title and description to generate the summary. " +
                        "The summary should:\n" +
                        "- Be clear, factual, and descriptive.\n" +
                        "- Provide enough detail so the reader understands the article.\n" +
                        "- Stay under 50 words.\n\n" +

                        "Title: " + title + "\n" +
                        "Description: " + desc + "\n\n" +
                        "Write a verbose summary (maximum 80 words):";
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Summary generation sleep interrupted: {}", e.toString());
        }
        return GeminiResponseParser.parseSummary(generateContent(prompt));
    }

    /**
     * Reusable Gemini API caller.
     */
    private String generateContent(String prompt) {
        try {
            String requestBody = "{ \"contents\": [ { \"parts\": [ { \"text\": \""
                    + prompt.replace("\"", "\\\"")
                    + "\" } ] } ] }";

            String endpoint = String.format(ENDPOINT_TEMPLATE, apiKey);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else if (response.statusCode() >= 500) {
                LOGGER.error("LLM upstream unavailable. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new ServiceUnavailableException("LLM upstream unavailable: " + response.statusCode());
            } else {
                LOGGER.error("LLM request failed. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new ServiceUnavailableException("LLM request failed: " + response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.error("LLM request error: {}", e.toString());
            throw new ServiceUnavailableException("LLM request error: " + e.getMessage());
        }
    }
}
