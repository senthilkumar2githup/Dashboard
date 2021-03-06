package org.hpccsystems.dashboard.entity;

import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.common.Constants;

public class Portlet {

    private String name;
    private Integer id;
    private Integer chartType;
    private String widgetState;
    private Integer column;

    private String chartDataXML;
    private String chartDataJSON;

    private ChartData chartData;
    private boolean isSinglePortlet;

    public boolean getIsSinglePortlet() {
		return isSinglePortlet;
	}

	public void setIsSinglePortlet(boolean isSinglePortlet) {
		this.isSinglePortlet = isSinglePortlet;
	}

	public final Integer getColumn() {
        return column;
    }

    public final void setColumn(final Integer column) {
        this.column = column;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String title) {
        this.name = title;
    }

    public final Integer getId() {
        return id;
    }

    public final void setId(final Integer id) {
        this.id = id;
    }

    public final Integer getChartType() {
        return chartType;
    }

    public final void setChartType(final Integer chartType) {
        this.chartType = chartType;
    }

    public final String getChartDataJSON() {
        return chartDataJSON;
    }

    public final void setChartDataJSON(final String chartDataJSON) {
        this.chartDataJSON = chartDataJSON;
    }

    public String getWidgetState() {
        return widgetState;
    }

    public void setWidgetState(String widgetState) {
        this.widgetState = widgetState;
    }

    public String getChartDataXML() {
        return chartDataXML;
    }

    public void setChartDataXML(String chartData) {
        this.chartDataXML = chartData;
    }

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }
    
    public boolean isLive() {
		return Constants.STATE_LIVE_CHART.equals(getWidgetState());
	}

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append("Portlet [name=").append(name).append(", id=").append(id)
                .append(", chartType=").append(chartType)
                .append(", widgetState=").append(widgetState)
                .append(", column=").append(column).append(", chartDataXML=")
                .append(chartDataXML).append(", chartDataJSON=")
                .append(chartDataJSON).append(", chartData=").append(chartData)
                .append(", isSinglePortlet=").append(isSinglePortlet)
                .append("]");
        return buffer.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portlet other = (Portlet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
    
    

}
