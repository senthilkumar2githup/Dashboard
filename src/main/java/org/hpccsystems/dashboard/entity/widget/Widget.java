package org.hpccsystems.dashboard.entity.widget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.entity.widget.charts.Table;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.entity.widget.charts.XYChart;
import org.hpccsystems.dashboard.util.HipieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Widget {
    private static final String SPACE = " ";
    private static final String UNSIGNED ="unsigned";
    private static final String STRING ="string";
    private static final String PERCENATGE ="%";
    private static final String SQUARE_OPEN ="[";
    private static final String SQUARE_CLOSE ="]";
    private static final String LESS_THAN ="<=";
    private static final String GREATER_THAN =">=";
    private static final String QUOTE = "'";
    private static final String COMMA =",";
    private static final String IN ="IN";
    private static final String AND = "AND";
    private static final String LOGICAL_FILE_NAME = "LogicalFilename";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Widget.class);
    
    private String name;
    private String logicalFile;
    private List<Filter> filters;
    private String title;
    private ChartConfiguration chartConfiguration;  

	public abstract boolean isConfigured();

	public abstract List<String> getColumns();

	public abstract List<String> getSQLColumns();

	public abstract String generateSQL();

	public abstract VisualElement generateVisualElement();

	public abstract List<InputElement> generateInputElement();

	public abstract Map<String, String> getInstanceProperties();
	
	public abstract void editVisualElement(VisualElement visualElement);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public String getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(String logicalFile) {
		this.logicalFile = logicalFile;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ChartConfiguration getChartConfiguration() {
		return chartConfiguration;
	}

	public void setChartConfiguration(ChartConfiguration chartConfiguration) {
		this.chartConfiguration = chartConfiguration;
	}

    public void addFilter(Filter filter) {
        if(filters == null) {
            filters = new ArrayList<Filter>();
        }
        filters.add(filter);
    }
    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }
    
    public String getFilterQuery(StringBuilder sql){
        Iterator<Filter> filters=this.getFilters().iterator();
        while(filters.hasNext()){
            sql.append(filters.next().generateFilterSQL(getLogicalFile()));
            if(filters.hasNext()){
                sql.append(" AND ");
            }
        }   
       return sql.toString();
   }
    
    public String getHipieFilterQuery(){
        StringBuilder query = new StringBuilder();
        ListIterator<Filter> filters= this.getFilters().listIterator();
        Filter filter = null;
        while(filters.hasNext()){
            filter = filters.next();           
            query.append(filter.getHipieFilterQuery(filter,filters.nextIndex()-1, this.getName()));
            if(filters.hasNext()){
                query.append(" AND ");
            }
        }   
        
       return query.toString();
    }

    @Override
    public String toString() {
        return "Widget [name=" + name + ", logicalFile=" + logicalFile
                + ", filters=" + filters + ", title=" + title
                + ", chartConfiguration=" + chartConfiguration + "]";
    }

    public void removeInput(InputElement inputElement,List<String> labelWeightNames) {
       List<Element> inputs = inputElement.getChildElements();
       labelWeightNames.stream().forEach(fieldName -> {
           inputs.remove(inputElement.getChildElement(fieldName));
       });
      
    }

    public void removeInstanceProperty(LinkedHashMap<String, String[]> props,List<String> labelWeightNames) {
        labelWeightNames.stream().forEach(fieldName -> {
            props.remove(fieldName);
        });
    }
    
    public static Widget create(ContractInstance contractInstance,String chartName,Dashboard dashboard) {
        Contract contract = contractInstance.getContract();
        
        VisualElement visualElement = HipieUtil.getVisualElement(contract,chartName);
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        String _chartType = visualElement.getCustomOption(Constants.HIPIE._CHARTTYPE).getParam(0);
        LOGGER.debug("_chartType -->{}",_chartType);
        ChartConfiguration chartConfig = chartTypes.get(_chartType);
        Widget widget = null;
        
        if (chartConfig.getType() == ChartTypes.PIE.getChartCode()
                || chartConfig.getType() == ChartTypes.DONUT.getChartCode()) {
            widget = new Pie();
            ( (Pie) widget).setWeight(createMeasures(visualElement.getOption(VisualElement.WEIGHT),contractInstance).get(0));
            ( (Pie) widget).setLabel(createAttribute(visualElement.getOption(VisualElement.LABEL),contractInstance));
            
        } else if (chartConfig.getType() == ChartTypes.BAR.getChartCode()
                || chartConfig.getType() == ChartTypes.COLUMN.getChartCode() ) {
            widget = new XYChart();
            ElementOption weight = visualElement.getOption(VisualElement.WEIGHT);
            //Chart has single measure
            if(weight != null){
                ((XYChart) widget).setMeasure(createMeasures(visualElement.getOption(VisualElement.WEIGHT),contractInstance));
                ((XYChart) widget).setAttribute(createAttribute(visualElement.getOption(VisualElement.LABEL),contractInstance));
            }else{//Chart has multiple measures
                ( (XYChart) widget).setMeasure(createMeasures(visualElement.getOption(VisualElement.Y),contractInstance));
                ( (XYChart) widget).setAttribute(createAttribute(visualElement.getOption(VisualElement.X),contractInstance));
            }
            
        } else if(ChartTypes.LINE.getChartCode() == chartConfig.getType()){
            widget = new XYChart();
            ( (XYChart) widget).setMeasure(createMeasures(visualElement.getOption(VisualElement.Y),contractInstance));
            ( (XYChart) widget).setAttribute(createAttribute(visualElement.getOption(VisualElement.X),contractInstance));
            
        } else if (chartConfig.getType() == ChartTypes.US_MAP.getChartCode()) {
            widget = new USMap();
            ( (USMap) widget).setMeasure(createMeasures(visualElement.getOption(VisualElement.WEIGHT),contractInstance).get(0));
            ( (USMap) widget).setState(createAttribute(visualElement.getOption(VisualElement.STATE),contractInstance));
        }else if (chartConfig.getType() == ChartTypes.TABLE.getChartCode()) {
            widget = new Table();
            ( (Table) widget).setTableColumns(createTableFields(visualElement,contractInstance,dashboard));
        }
        widget.setName(visualElement.getName());
        widget.setChartConfiguration(chartConfig);
        widget.setTitle(visualElement.getOption(VisualElement.TITLE)
                 .getParams().get(0).getName());
        //getting used logical file
        ContractInstance hookedRawdataset = contractInstance.getPrecursors().get(visualElement.getBasis().getBase());
        widget.setLogicalFile(hookedRawdataset.getProperty(LOGICAL_FILE_NAME).substring(1));
        
        LOGGER.debug("file --->"+hookedRawdataset.getProperty(LOGICAL_FILE_NAME).substring(1));
        LOGGER.debug("Title -->"+visualElement.getOption(VisualElement.TITLE).getParams().get(0).getName());
        LOGGER.debug("Ri -->"+visualElement.getBasisQualifier().toString());
        LOGGER.debug("filter -->"+visualElement.getBasisFilter());
        //Recreating filters with applied values
        //getting filters
        if(visualElement.getBasisFilter() != null && !visualElement.getBasisFilter().isEmpty()){
            widget.setFilters(getFilters(contractInstance,visualElement)) ;
        }
       
       
        LOGGER.debug("widget -->"+widget);
        
        return widget;
    };
    
    private static List<Measure> createMeasures(ElementOption option, ContractInstance contractInstance) {
        
        //TODO:Need to validate for multiple measures
        List<Measure> measures=new ArrayList<Measure>();
        option.getParams().stream().forEach(fieldInstance ->{
            LOGGER.debug("field -->" + fieldInstance.getCanonicalName());
            LOGGER.debug("field -->" + fieldInstance.getName());
            LOGGER.debug("field -->" + fieldInstance.getType());

            // getting actual column name like 'productline' from instance property as
            //contractInstance.getProperty("Measure_piechart2")
            Field measureField = new Field();
            measureField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
            measureField.setDataType(UNSIGNED);
            
            Measure measure = new Measure(measureField);
            if (fieldInstance.getType() != null) {
                measure.setAggregation(AGGREGATION.valueOf(fieldInstance.getType()));
            } else {
                measure.setAggregation(AGGREGATION.NONE);
            }

            measure.setDisplayName(contractInstance.getProperty(fieldInstance.getName()));
            measures.add(measure);
        });

        return measures;
    }
    
    private static Attribute createAttribute(ElementOption option, ContractInstance contractInstance) {

        //All the charts can have only one Attribute
        FieldInstance fieldInstance = option.getParams().get(0);

        LOGGER.debug("field -->" + fieldInstance.getCanonicalName());
        LOGGER.debug("field -->" + fieldInstance.getName());
        LOGGER.debug("field -->" + fieldInstance.getType());
        
        // getting actual column name like 'productline' from instance property as
        //contractInstance.getProperty("Attribute_piechart2")
        Field attributeField = new Field();
        attributeField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
        attributeField.setDataType("string");

        Attribute attribute = new Attribute(attributeField);
        attribute.setDisplayName(contractInstance.getProperty(fieldInstance.getName()));

        return attribute;
    }
    
    private static List<Filter> getFilters(ContractInstance contractInstance, VisualElement visualElement) {
        
        String hipieFilter = visualElement.getBasisFilter();
        List<String> filterStr = Arrays.asList(StringUtils.splitByWholeSeparator(hipieFilter, AND));
        List<Filter> filters = new ArrayList<Filter>();
        
        Filter filter = null;
            for(String filterLabel :filterStr ){
                if(filterLabel.contains(LESS_THAN)){
                    String[] strArray = filterLabel.trim().split(SPACE);
                    BigDecimal maxValue = new BigDecimal(strArray[strArray.length-1].trim());
                    filter = new NumericFilter();
                    String columnName = strArray[0].trim();
                    columnName = StringUtils.removeStart(columnName, PERCENATGE);
                    columnName = StringUtils.removeEnd(columnName, PERCENATGE);
                    filter.setColumn(contractInstance.getProperty(columnName));
                    filter.setDataType(UNSIGNED);
                    ((NumericFilter)filter).setMaxValue(maxValue);
                    
                }else if(filterLabel.contains(GREATER_THAN)){
                    String[] strArray = filterLabel.trim().split(SPACE);
                    BigDecimal minValue = new BigDecimal(strArray[strArray.length-1].trim());
                    ((NumericFilter)filter).setMinValue(minValue);
                    filters.add(filter);
                }else if(filterLabel.contains(IN) && filterLabel.contains(SQUARE_OPEN) && filterLabel.contains(SQUARE_CLOSE)){
                    filter = new StringFilter();
                    filterLabel = filterLabel.trim();
                    LOGGER.debug("filterLabel -->{}",filterLabel);
                    LOGGER.debug("column label -->{}", StringUtils.substringBefore(filterLabel, IN).trim());
                    LOGGER.debug("Value label -->{}", StringUtils.substringAfter(filterLabel, IN).trim());
                    String columnName =  StringUtils.substringBefore(filterLabel, IN).trim();
                    columnName = StringUtils.removeStart(columnName, PERCENATGE);
                    columnName = StringUtils.removeEnd(columnName,PERCENATGE);
                    filter.setColumn(contractInstance.getProperty(columnName));
                    filter.setDataType(STRING);
                    ((StringFilter)filter).setValues(getStrFilterValues(StringUtils.substringAfter(filterLabel, IN).trim(),filter));
                    filters.add(filter);
                }
            }
            LOGGER.debug("filters-->{}",filters);
        return filters;
    }
    
    private static List<Field> createTableFields(VisualElement visualElement,
            ContractInstance contractInstance,Dashboard dashboard) {
        List<Field> tableColumns=new ArrayList<Field>();
        ElementOption option = visualElement.getOption(VisualElement.VALUE);
        option.getParams()
                .stream()
                .forEach(fieldInstance -> {
                            Field tableField = new Field();
                            tableField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
                            if (fieldInstance.getType() != null) {
                                tableField.setDataType(UNSIGNED);
                                Measure measure = new Measure(tableField);
                                measure.setAggregation(AGGREGATION.valueOf(fieldInstance.getType()));
                                measure.setDisplayName(fieldInstance.getFieldLabel());
                                tableColumns.add(measure);
                            } else {
                                tableField.setDataType(STRING);
                                Attribute attribute = new Attribute(tableField);
                                attribute.setDisplayName(fieldInstance.getFieldLabel());
                                tableColumns.add(attribute);
                            }
                        });
        return tableColumns;
    }
    
    private static List<String> getStrFilterValues(String valueStr,Filter filter) {
        List<String> values = new ArrayList<String>();
       
        valueStr = StringUtils.removeStart(valueStr, SQUARE_OPEN);
        valueStr = StringUtils.removeEnd(valueStr, SQUARE_CLOSE);
        LOGGER.debug("valueStr -->{}",valueStr);
        List<String> valueList = Arrays.asList(valueStr.split(COMMA));
        LOGGER.debug("valueList -->{}",valueList);
        valueList.stream().forEach(vlaue ->{
            vlaue = vlaue.trim();
            vlaue = StringUtils.removeStart(vlaue, QUOTE);
            vlaue = StringUtils.removeEnd(vlaue,  QUOTE);
            values.add(vlaue);
        });
       
        LOGGER.debug("values -->{}",values);
        return values;
    }
}

