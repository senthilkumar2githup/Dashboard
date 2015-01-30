package org.hpccsystems.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;

public class Constants {

	public static final String EXCEPTION = "EXCEPTION - {}";
	
	//Events
	public static final String ON_DELTE_DASHBOARD = "onDeleteDashboard";
	public static final String ON_DRAW_CHART = "onDrawChart";
	public static final String ON_RUN_COMPOSITION = "onRunComposition";
	
	public static final String HIPIE_RAW_DATASET = "RawDataset";
	public static final String USER_CREDENTIAL = "userCredential";
    public static final String ON_ADD_DASHBOARD = "onAddDashboard";
    public static final String DASHBOARD = "dashboard";
    public static final String UNABLE_TO_FETCH_DATA = "Unable to fetch Hpcc data";
    public static final String HPCC_CONNECTION = "hpccConnection";
    public static final String MEASURE = "measure";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String FILTER = "filter";
    public static final String DOT=".";
    public static final String COMMA=" , ";
    public static final String SUCCESS="Success";
    public static final String FAIL="Fail";
    // Notification Types
    public static final String ERROR_NOTIFICATION = "error";
    // Notification positions
    public static final String POSITION_CENTER = "middle_center";
    
    public static final String WIDGET_CONFIG = "widgetWrapper";
    public static final String ON_SELECT = "onSelect";
    public static final String HPCC_ID = "hpccID";
    public static final String LABEL = "label";
    public static final String WEIGHT = "weight";
 
    public static final Map<String, ChartConfiguration> CHART_CONFIGURATIONS = new LinkedHashMap<String, ChartConfiguration>(){
        private static final long serialVersionUID = 1L;
        {
            put(HipieChartNames.PIE.getChartName(), new ChartConfiguration(ChartTypes.PIE, "Pie Chart", "assets/img/charts/pie.png", "widget/pie.zul" ,HipieChartNames.PIE.getChartName()));
          //put(HipieChartNames.DONUT.getChartName(), new ChartConfiguration(ChartTypes.DONUT, "Donut Chart", "assets/img/charts/donut.png", "widget/pie.zul" , HipieChartNames.DONUT.getChartName()));
            put(HipieChartNames.LINE.getChartName(), new ChartConfiguration(ChartTypes.LINE, "Line Chart", "assets/img/charts/line.png", "widget/xyChart.zul", HipieChartNames.LINE.getChartName()));
            put(HipieChartNames.BAR.getChartName(), new ChartConfiguration(ChartTypes.BAR, "Bar Chart", "assets/img/charts/bar.png", "widget/xyChart.zul",HipieChartNames.BAR.getChartName()));
            //Keep chart type as 'BAR' for column chart as per Hipie requirement
            put(HipieChartNames.COLUMN.getChartName(), new ChartConfiguration(ChartTypes.BAR, "Column Chart", "assets/img/charts/column.png", "widget/xyChart.zul",HipieChartNames.COLUMN.getChartName()));
            put(HipieChartNames.US_MAP.getChartName(), new ChartConfiguration(ChartTypes.US_MAP, "US_Map", "assets/img/charts/geo.png", "widget/usMap.zul",HipieChartNames.US_MAP.getChartName()));
            put(HipieChartNames.TABLE.getChartName(), new ChartConfiguration(ChartTypes.TABLE, "Table Widget", "assets/img/charts/table.png", "widget/table.zul", HipieChartNames.TABLE.getChartName()));
          //put(HipieChartNames.STEP.getChartName(), new ChartConfiguration(ChartTypes.STEP, "Step Chart", "assets/img/charts/step.png", "widget/xyChart.zul", HipieChartNames.STEP.getChartName()));
          //put(HipieChartNames.SCATTER.getChartName(), new ChartConfiguration(ChartTypes.SCATTER, "Scatter Chart", "assets/img/charts/scatter.png", "widget/xyChart.zul", HipieChartNames.SCATTER.getChartName()));
          //put(HipieChartNames.AREA.getChartName(), new ChartConfiguration(ChartTypes.AREA, "Area Chart", "assets/img/charts/area.png", "widget/xyChart.zul",HipieChartNames.AREA.getChartName()));
        }
    };
    public static final String ACTIVE_DASHBOARD = "ActiveDashboard";
    
    public enum AGGREGATION {
        SUM ("SUM"),
        COUNT ("COUNT"),
        MIN ("MIN"),
        MAX ("MAX"),
        AVG ("AVG"),
        NONE ("NONE");

        private final String name; 
        
        private AGGREGATION(String s) {
            name = s;
        }
        public String toString(){
           return name;
        }

    }
    
    public enum FLOW {
        NEW("NEW"),
        EDIT ("EDIT"),
        DELETE ("DELETE");  
        
        private final String name; 
        
        private FLOW(String s) {
            name = s;
        }
        
        public String toString(){
            return name;
         }
    }
    
    public enum ChartTypes {

        PIE("PIE"), DONUT("DONUT"), LINE("LINE"), BAR("BAR"), COLUMN("COLUMN"), US_MAP(
                "CHORO"), TABLE("TABLE"), STEP("STEP"), SCATTER("SCATTER"), AREA("AREA");
        
        private final String chartCode; 
        
        private ChartTypes(String code) {
            chartCode = code;
        }
        public String toString(){
            return chartCode;
         }
    }
    
    //HIPIE Constants
    public class HIPIE {
        public static final String _CHARTTYPE = "_charttype";
    }
    
    // Style class names
    public static final String STYLE_POPUP = "popup";

    public static final String URL = "url";

    public static final String TARGET = "target";

    public static final String ON_UPDATE_COMPOSITION = "onUpdateComposition";

    
    
}

