package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class PieChart {

    private List<Integer> count;
    private List<String> labels;
    private String cardTypeName;
    private String name;

    public PieChart(List<String> tmp, String name) {

        this.name = name;
        cardTypeName = "pie";

        labels = new ArrayList<>();
        count = new ArrayList<>();


        for (int i = 0; i < tmp.size(); i++) {

            int x = -1;
            for (int j = 0; j < labels.size(); j++) {
                if (labels.get(j).equals(tmp.get(i))) {
                    x = j;
                    count.set(x, count.get(x) + 1);
                }
            }
            if (x == -1) {
                labels.add(tmp.get(i));
                count.add(1);
            }
        }

    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getCount() {
        return count;
    }

    public void setCount(List<Integer> count) {
        this.count = count;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
