package utwente.team2.model;

import java.util.ArrayList;
import java.util.List;

public class FavoriteOptions {
    private List<LayoutData> layoutDataList;
    private String username;

    public FavoriteOptions(List<LayoutData> layoutDataList) {
        this.layoutDataList = layoutDataList;
    }

    public FavoriteOptions(List<LayoutData> layoutDataList, String username) {
        this.layoutDataList = layoutDataList;
        this.username = username;
    }

    public FavoriteOptions() {
        this.layoutDataList = new ArrayList<>();
    }

    public List<LayoutData> getLayoutDataList() {
        return layoutDataList;
    }

    public void setLayoutDataList(List<LayoutData> layoutDataList) {
        this.layoutDataList = layoutDataList;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
