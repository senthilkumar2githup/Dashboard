package org.hpccsystems.dashboard.chart.entity;

import java.util.List;

public class XYGroup {
    private List<String> xColumnNames;
    private List<String> yColumnNames;
    
    public List<String> getyColumnNames() {
        return yColumnNames;
    }
    public void setyColumnNames(List<String> yColumnNames) {
        this.yColumnNames = yColumnNames;
    }
    public List<String> getxColumnNames() {
        return xColumnNames;
    }
    public void setxColumnNames(List<String> xColumnNames) {
        this.xColumnNames = xColumnNames;
    }
}
