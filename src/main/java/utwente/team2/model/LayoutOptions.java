package utwente.team2.model;


import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class LayoutOptions {
    private List<LayoutData> layoutDataList;
    private int currentLayout;

    public LayoutOptions(List<LayoutData> layoutDataList, int currentLayout) {
        this.layoutDataList = layoutDataList;
        this.currentLayout = currentLayout;
    }

    public LayoutOptions() {
        this.layoutDataList = new ArrayList<>();
    }

    public List<LayoutData> getLayoutDataList() {
        return layoutDataList;
    }

    public void setLayoutDataList(List<LayoutData> layoutDataList) {
        this.layoutDataList = layoutDataList;
    }

    public int getCurrentLayout() {
        return currentLayout;
    }

    public void setCurrentLayout(int currentLayout) {
        this.currentLayout = currentLayout;
    }
}
