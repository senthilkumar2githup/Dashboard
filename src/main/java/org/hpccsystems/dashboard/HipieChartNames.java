package org.hpccsystems.dashboard;

public enum HipieChartNames {


    PIE("C3_PIE"), DONUT("C3_DONUT"), LINE("C3_LINE"), BAR("C3_BAR"), COLUMN(
            "C3_COLUMN"), US_MAP("CHORO"), TABLE("TABLE"), STEP("C3_STEP"), SCATTER(
            "C3_SCATTER"), AREA("C3_AREA");

    private String chartName;

    public String getChartName() {
        return chartName;
    }

    HipieChartNames(String chartName) {
        this.chartName = chartName;
    }

}
