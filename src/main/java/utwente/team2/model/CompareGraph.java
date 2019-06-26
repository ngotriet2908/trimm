package utwente.team2.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;

@XmlRootElement
public class CompareGraph {
    private String name;
    private List<BigDecimal> stepData;

    public CompareGraph(String name, List<BigDecimal> stepData) {
        this.name = name;
        this.stepData = stepData;
    }

    public CompareGraph() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BigDecimal> getStepData() {
        return stepData;
    }

    public void setStepData(List<BigDecimal> stepData) {
        this.stepData = stepData;
    }
}
