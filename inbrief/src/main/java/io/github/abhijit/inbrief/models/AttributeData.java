package io.github.abhijit.inbrief.models;


import java.util.Map;

public class AttributeData {
    private double globalWeight;
    private Map<String, Double> values;


    public AttributeData() {}

    public AttributeData(double globalWeight, Map<String, Double> values) {
        this.globalWeight = globalWeight;
        this.values = values;
    }

    public double getGlobalWeight() {
        return globalWeight;
    }
    public void setGlobalWeight(double globalWeight) {
        this.globalWeight = globalWeight;
    }

    public Map<String, Double> getValues() {
        return values;
    }
    public void setValues(Map<String, Double> values) {
        this.values = values;
    }
}
