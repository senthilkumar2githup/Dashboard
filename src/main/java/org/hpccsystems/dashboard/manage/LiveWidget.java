package org.hpccsystems.dashboard.manage;

public class LiveWidget {
    
    private String widgetName;
    private String widgetLabel;
    
    private String attributeColumn;
    private String attributeContractField;
    
    public String getWidgetName() {
        return widgetName;
    }
    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }
    public String getAttributeColumn() {
        return attributeColumn;
    }
    public void setAttributeColumn(String attributeColumn) {
        this.attributeColumn = attributeColumn;
    }
    public String getAttributeContractField() {
        return attributeContractField;
    }
    public void setAttributeContractField(String attributeContractField) {
        this.attributeContractField = attributeContractField;
    }
    public String getWidgetLabel() {
        return widgetLabel;
    }
    public void setWidgetLabel(String widgetLabel) {
        this.widgetLabel = widgetLabel;
    }
}
