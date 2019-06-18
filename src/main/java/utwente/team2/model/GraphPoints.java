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

    public void reprocessSpeed(BigDecimal stepDistance) {

        System.out.println(stepDistance);
        List<BigDecimal> timeStamp = new ArrayList<>();
        for(int i = 0; i < getLeft().size() - 1; i++) {
            timeStamp.add((getLeft().get(i + 1).subtract(getLeft().get(i))
                    .divide(stepDistance.multiply(BigDecimal.valueOf(getStep_no().get(i + 1) - getStep_no().get(i))), BigDecimal.ROUND_UP))
                    .setScale(3, BigDecimal.ROUND_UP));
        }
        setLeft(timeStamp);
        step_no.remove(0);

        System.out.println(step_no.size() + " " + getLeft().size());

    }

}