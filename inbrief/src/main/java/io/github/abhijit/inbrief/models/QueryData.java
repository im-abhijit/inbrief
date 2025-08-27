package io.github.abhijit.inbrief.models;


import java.util.List;
import java.util.Map;

public class QueryData {
    private List<String> intent;
    private List<AttributeData> categories;
    private List<AttributeData> keywords;
    private List<AttributeData> location;
    private List<AttributeData> source;


    public List<String> getIntent() {
        return intent;
    }

    public void setIntent(List<String> intent) {
        this.intent = intent;
    }

    public List<AttributeData> getCategories() {
        return categories;
    }

    public void setCategories(List<AttributeData> categories) {
        this.categories = categories;
    }

    public List<AttributeData> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<AttributeData> keywords) {
        this.keywords = keywords;
    }

    public List<AttributeData> getLocation() {
        return location;
    }

    public void setLocation(List<AttributeData> location) {
        this.location = location;
    }

    public List<AttributeData> getSource() {
        return source;
    }

    public void setSource(List<AttributeData> source) {
        this.source = source;
    }
}


