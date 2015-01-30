package org.hpccsystems.dashboard.entity.widget;

import org.hpccsystems.dashboard.Constants.ChartTypes;


public class ChartConfiguration {
    private ChartTypes type;
    private String name;
    private String staticImage;
    private String editLayout;
    private String hipieChartName;
    
    public ChartConfiguration(ChartTypes type, String name, String image,
            String layout, String hipieChartName) {
        this.setType(type);
        this.name = name;
        this.staticImage = image;
        this.editLayout = layout;
        this.hipieChartName = hipieChartName;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStaticImage() {
        return staticImage;
    }
    public void setStaticImage(String staticImage) {
        this.staticImage = staticImage;
    }
    public String getEditLayout() {
        return editLayout;
    }
    public void setEditLayout(String editLayout) {
        this.editLayout = editLayout;
    }

    public ChartTypes getType() {
        return type;
    }

    public void setType(ChartTypes type) {
        this.type = type;
    }

    public String getHipieChartName() {
        return hipieChartName;
    }

    public void setHipieChartName(String hipieChartName) {
        this.hipieChartName = hipieChartName;
    }
}
