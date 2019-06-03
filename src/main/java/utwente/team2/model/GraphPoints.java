package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GraphPoints implements Card {
    private String cardTypeName;

    private String name;

    public String getName() {
        return name;
    }

    public List<BigDecimal> getLeft() {
        return left;
    }

    public List<BigDecimal> getRight() {
        return right;
    }

    public List<Integer> getStep_no() {
        return step_no;
    }

    private List<BigDecimal> left;
    private List<BigDecimal> right;
    private List<Integer> step_no;

    public GraphPoints(String name) {
        cardTypeName = "graph";
        this.name = name;
        left = new ArrayList<>();
        right = new ArrayList<>();
        step_no = new ArrayList<>();
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeft(List<BigDecimal> left) {
        this.left = left;
    }

    public void setRight(List<BigDecimal> right) {
        this.right = right;
    }

    public void setStep_no(List<Integer> step_no) {
        this.step_no = step_no;
    }
}