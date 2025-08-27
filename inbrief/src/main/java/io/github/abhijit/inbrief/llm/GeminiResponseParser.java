package io.github.abhijit.inbrief.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abhijit.inbrief.models.QueryData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeminiResponseParser {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiResponseParser.class);

    public static QueryData parseResponse(String geminiJson)  {
        try {
            // Step 1: Parse the outer Gemini response
            GeminiResponse response = mapper.readValue(geminiJson, GeminiResponse.class);

            String textBlock = response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();

            // Step 2: Clean up the inner JSON (remove ```json ... ```)
            String cleanedJson = textBlock
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Step 3: Parse into QueryData
            return mapper.readValue(cleanedJson, QueryData.class);
        }
        catch (Exception e) {
            LOGGER.error("Failed to parse Gemini response to QueryData. Error: {}. Raw: {}", e.toString(), geminiJson);
            throw new RuntimeException("Unable to parse LLM response", e);
        }
    }

    public static String parseSummary(String geminiJson)  {
        try{
            GeminiResponse response = mapper.readValue(geminiJson, GeminiResponse.class);

            String textBlock = response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();
            return textBlock;

        }
        catch (Exception e) {
            LOGGER.error("Failed to parse Gemini summary response. Error: {}. Raw: {}", e.toString(), geminiJson);
        }
        return null;
    }
}
