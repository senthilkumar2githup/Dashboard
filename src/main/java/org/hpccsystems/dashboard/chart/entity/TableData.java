package org.hpccsystems.dashboard.chart.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TableData extends ChartData {

    private List<Attribute> attributes;
    private boolean enableChangeIndicators;
    
    public TableData() {
    }
    
    public TableData(ChartData chartData) {
        this.setFields(chartData.getFields());
        this.setFiles(chartData.getFiles());
        this.setFilters(chartData.getFilters());
        this.setHpccConnection(chartData.getHpccConnection());
        this.setInputParams(chartData.getInputParams());
        this.setIsFiltered(chartData.getIsFiltered());
        this.setIsQuery(chartData.getIsQuery());
        this.setJoins(chartData.getJoins());
    }

    @XmlElement
    public List<Attribute> getAttributes() {
        if (attributes == null) {
            setAttributes(new ArrayList<Attribute>());
        }
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("TableData [attributes=").append(attributes).append("]");
        return buffer.toString();
    }

    @XmlElement
    public boolean getEnableChangeIndicators() {
        return enableChangeIndicators;
    }

    public void setEnableChangeIndicators(boolean enableChangeIndicators) {
        this.enableChangeIndicators = enableChangeIndicators;
    }
}
