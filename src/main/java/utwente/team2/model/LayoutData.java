package utwente.team2.model;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LayoutData {

    @XmlAnyElement
    private List<Card> cards;
    private String name;
    private int layoutID;

    public LayoutData(String name, int layoutID) {
        this.name = name;
        this.layoutID = layoutID;
    }

    public LayoutData(List<Card> cards, String name, int layoutID) {
        this.cards = cards;
        this.name = name;
        this.layoutID = layoutID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLayoutID() {
        return layoutID;
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }

    public LayoutData(List<Card> cards) {
        this.cards = cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public LayoutData() {
        cards = new ArrayList<>();
    }
}
