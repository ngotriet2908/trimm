package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Individual implements Card {
    private String cardTypeName;
    private double minimum;
    private double maximum;
    private double average;
    private String name;
    private  String meaning;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public Individual(double minimum, double maximum, double average, String name, String meaning) {
        cardTypeName = "individual";
        this.minimum = minimum;
        this.maximum = maximum;
        this.average = average;
        this.name = name;
        this.meaning = meaning;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public Individual(double minimum, double maximum, double average) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.average = average;
    }
}
