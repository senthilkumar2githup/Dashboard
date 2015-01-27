package org.hpccsystems.dashboard.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.entity.widget.charts.Table;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.entity.widget.charts.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HipieUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HipieUtil.class);
    private static final String AND = "AND";
    private static final String UNSIGNED ="unsigned";
    private static final String STRING ="string";
    private static final String PERCENATGE ="%";
    private static final String SQUARE_OPEN ="[";
    private static final String SQUARE_CLOSE ="]";
    private static final String LESS_THAN ="<=";
    private static final String GREATER_THAN =">=";
    private static final String IN ="IN";
    private static final String QUOTE = "'";
    private static final String COMMA =",";
    private static final String SPACE = " ";
    
    public static Widget getVisualElementWidget(ContractInstance contractInstance,String chartName,Dashboard dashboard) throws Exception{
       
       Contract contract = contractInstance.getContract();
       
       VisualElement visualElement = getVisualElement(contract,chartName);
       
       LOGGER.debug("logical file -->"+contractInstance.getPrecursors().get(visualElement.getBasis().getBase()).getProperty("LogicalFilename"));
       Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;

       ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
       Widget widget = null;
       
       if (chartConfig.getType() == ChartTypes.PIE.getChartCode()
               || chartConfig.getType() == ChartTypes.DONUT.getChartCode()) {
           widget = new Pie();
           ( (Pie) widget).setWeight(createMeasre(visualElement.getOption(VisualElement.WEIGHT),contractInstance));
           ( (Pie) widget).setLabel(createAttribute(visualElement.getOption(VisualElement.LABEL),contractInstance));
           
       } else if (chartConfig.getType() == ChartTypes.BAR.getChartCode()
               || chartConfig.getType() == ChartTypes.COLUMN.getChartCode() ) {
           widget = new XYChart();
         //TODO: once the issue #21 closed iterate over the element option and prepare the measure list.
           List<Measure> measures=new ArrayList<Measure>();
           measures.add(createMeasre(visualElement.getOption(VisualElement.WEIGHT),contractInstance));
           ( (XYChart) widget).setMeasure(measures);
           
           LOGGER.debug("visualElement --->{}",visualElement.getOptions());
           LOGGER.debug("visualElement --->{}",visualElement.getOptionValues());
           
           ( (XYChart) widget).setAttribute(createAttribute(visualElement.getOption(VisualElement.LABEL),contractInstance));
           
       } else if(ChartTypes.LINE.getChartCode() == chartConfig.getType()){
           widget = new XYChart();
           List<Measure> measures=new ArrayList<Measure>();
           measures.add(createMeasre(visualElement.getOption(VisualElement.Y),contractInstance));
           ( (XYChart) widget).setMeasure(measures);
           
           ( (XYChart) widget).setAttribute(createAttribute(visualElement.getOption(VisualElement.X),contractInstance));
           
       } else if (chartConfig.getType() == ChartTypes.US_MAP.getChartCode()) {
           widget = new USMap();
           ( (USMap) widget).setMeasure(createMeasre(visualElement.getOption(VisualElement.WEIGHT),contractInstance));
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
       widget.setLogicalFile(hookedRawdataset.getProperty("LogicalFilename").substring(1));
       
       LOGGER.debug("file --->"+hookedRawdataset.getProperty("LogicalFilename").substring(1));
       LOGGER.debug("Title -->"+visualElement.getOption(VisualElement.TITLE).getParams().get(0).getName());
       LOGGER.debug("Ri -->"+visualElement.getBasisQualifier().toString());
       LOGGER.debug("filter -->"+visualElement.getBasisFilter());
       List<Filter> filters = getFilters(visualElement) ;
      
       LOGGER.debug("widget -->"+widget);
       
       return widget;
    }



    private static List<Filter> getFilters(VisualElement visualElement) {
        String hipieFilter = visualElement.getBasisFilter();
        List<String> filterStr = Arrays.asList(StringUtils.splitByWholeSeparator(hipieFilter, AND));
        List<Filter> filters = new ArrayList<Filter>();
        
       
        filterStr.stream().forEach(filterLabel->{
            Filter filter = null;
            LOGGER.debug("filterLabel -->{}",filterLabel);
            if(filterLabel.contains(LESS_THAN)){
                LOGGER.debug("Max-->");
                String[] strArray = StringUtils.splitByWholeSeparator(filterLabel, SPACE);
                BigDecimal maxValue = new BigDecimal(strArray[strArray.length-1].trim());
                filter = new NumericFilter();
                String columnName = strArray[0];
                columnName = StringUtils.removeStart(columnName, PERCENATGE);
                columnName = StringUtils.removeEnd(columnName, PERCENATGE);
                filter.setColumn(columnName);
                filter.setDataType(UNSIGNED);
                filter.setColumn(strArray[0]);
                ((NumericFilter)filter).setMaxValue(maxValue);
                
            }else if(filterLabel.contains(GREATER_THAN)){
                LOGGER.debug("Min-->");
                String[] strArray = StringUtils.splitByWholeSeparator(filterLabel, SPACE);
                BigDecimal minValue = new BigDecimal(strArray[strArray.length-1].trim());
                ((NumericFilter)filter).setMinValue(minValue);
                filters.add(filter);
            }else if(filterLabel.contains(IN) && filterLabel.contains(SQUARE_OPEN) && filterLabel.contains(SQUARE_CLOSE)){
                LOGGER.debug("str-->");
                filter = new StringFilter();
                String[] strArray = StringUtils.splitByWholeSeparator(filterLabel, " ");
                String columnName = strArray[0];
                columnName = StringUtils.removeStart(columnName, PERCENATGE);
                columnName = StringUtils.removeEnd(columnName,PERCENATGE);
                filter.setColumn(columnName);
                filter.setDataType(STRING);
                ((StringFilter)filter).setValues(getStrFilterValues(strArray[strArray.length-1].trim(),filter));
                filters.add(filter);
            }
            LOGGER.debug("filter-->{}",filter);
        });
        return filters;
    }


    private static List<String> getStrFilterValues(String valueStr,Filter filter) {
        List<String> values = new ArrayList<String>();
       
        valueStr = StringUtils.removeStart(valueStr, SQUARE_OPEN);
        valueStr = StringUtils.removeEnd(valueStr, SQUARE_CLOSE);
        
        List<String> valueList = Arrays.asList(StringUtils.splitByWholeSeparator(valueStr, COMMA));
        valueList.stream().forEach(vlaue ->{
            vlaue = vlaue.trim();
            vlaue = StringUtils.removeStart(vlaue, QUOTE);
            vlaue = StringUtils.removeEnd(vlaue,  QUOTE);
            values.add(vlaue);
        });
       
        LOGGER.debug("values -->{}",values);
        return values;
    }



    private static List<Field> createTableFields(VisualElement visualElement,
            ContractInstance contractInstance,Dashboard dashboard) throws Exception {
        String filename = contractInstance.getPrecursors()
                .get(visualElement.getBasis().getBase())
                .getProperty("LogicalFilename").substring(1);
        RecordInstance recordInstance = dashboard.getHpccConnection().getDatasetFields(filename, null);
        String structure=recordInstance.toString();
        String[] filecolumns = structure.split(","), dataType = null;
        HashMap<String, String> columnMap = new HashMap<String, String>();
        for (String property : filecolumns) {
            dataType = property.split(" ");
            columnMap.put(dataType[1], dataType[0]);
        }
        List<Field> tableColumns=new ArrayList<Field>();
        ElementOption option = visualElement.getOption(VisualElement.VALUE);
        option.getParams()
                .stream()
                .forEach(
                        fieldInstance -> {
                            Field tableField = new Field();
                            tableField.setColumn(contractInstance
                                    .getProperty(fieldInstance.getName()));
                            if (fieldInstance.getType() != null) {
                                tableField.setDataType("unsigned");
                                Measure measure = new Measure(tableField);
                                measure.setAggregation(AGGREGATION
                                        .valueOf(fieldInstance.getType()));
                                measure.setDisplayName(fieldInstance
                                        .getFieldLabel());
                                tableColumns.add(measure);
                            } else {
                                if ("STRING".equalsIgnoreCase(columnMap
                                        .get(tableField.getColumn()))) {
                                    tableField.setDataType("string");
                                    Attribute attribute = new Attribute(
                                            tableField);
                                    attribute.setDisplayName(fieldInstance
                                            .getFieldLabel());
                                    tableColumns.add(attribute);
                                } else {
                                    tableField.setDataType("unsigned");
                                    Measure measure = new Measure(tableField);
                                    measure.setAggregation(AGGREGATION.NONE);
                                    measure.setDisplayName(fieldInstance
                                            .getFieldLabel());
                                    tableColumns.add(measure);
                                }
                            }
                        });
        LOGGER.debug("tableColumns --->{}", tableColumns);
        return tableColumns;
    }

    
public static VisualElement getVisualElement(Contract contract ,String chartName) {

    VisualElement visualization = contract.getVisualElements().iterator().next();
    VisualElement visualElement = (VisualElement)visualization.getChildElement(chartName);
    return visualElement;
}

    private static Attribute createAttribute(ElementOption option,
            ContractInstance contractInstance) {

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
        attribute.setDisplayName(fieldInstance.getFieldLabel());

        return attribute;
    }

    private static Measure createMeasre(ElementOption option,
            ContractInstance contractInstance) {

        FieldInstance fieldInstance = option.getParams().get(0);

        LOGGER.debug("field -->" + fieldInstance.getCanonicalName());
        LOGGER.debug("field -->" + fieldInstance.getName());
        LOGGER.debug("field -->" + fieldInstance.getType());

        // getting actual column name like 'productline' from instance property as
        //contractInstance.getProperty("Measure_piechart2")
        Field measureField = new Field();
        measureField.setColumn(contractInstance.getProperty(fieldInstance.getName()));
        measureField.setDataType("unsigned");
        
        Measure measure = new Measure(measureField);
        if (fieldInstance.getType() != null) {
            measure.setAggregation(AGGREGATION.valueOf(fieldInstance.getType()));
        } else {
            measure.setAggregation(AGGREGATION.NONE);
        }

        measure.setDisplayName(fieldInstance.getFieldLabel());

        return measure;
    }

    public static boolean checkOutputExists(Contract contract, String output,
            String chartName) {

        VisualElement visualization = contract.getVisualElements().iterator().next();

        List<VisualElement> elementWithsameoutput = new ArrayList<VisualElement>(
                visualization
                        .getChildElements()
                        .stream()
                        .filter(visualElement -> !chartName
                                .equals(visualElement.getName()))
                        .map(visualElement -> (VisualElement) visualElement)
                        .filter(visualElement -> output.equals(visualElement
                                .getBasis().getName()))
                        .collect(Collectors.toList()));

        LOGGER.debug("ElementWithsameoutput -->{}", elementWithsameoutput);

        if (elementWithsameoutput.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static void deleteInputOutputAndVisualElement(
            ContractInstance contractInstance, VisualElement visualElement) {
        Contract contract = contractInstance.getContract();      
        
        String output = visualElement.getBasis().getName();
        String input = visualElement.getBasis().getBase();
        
        LOGGER.debug("Output -->"+output);
        LOGGER.debug("Input -->"+input);
        
        //Removing output element
       Element outElement =  contract.getOutputElements().stream().filter(element -> output.equals(element.getName())).findFirst().get();
       contract.getOutputElements().remove(outElement);
       
       //Removing input element
       Element inputElement =  contract.getInputElements().stream().filter(element -> input.equals(element.getName())).findFirst().get();
       contract.getInputElements().remove(inputElement);
       
       //Removing Visual element
        VisualElement visualization = contract.getVisualElements().iterator().next();
        visualization.getChildElements().remove(visualElement);
        
       Map<String,ElementOption> weightLableElement = getWeightLabelElementOption(visualElement);
       
       List<String> labelWeightNames = getLabelWeightNames(weightLableElement,visualElement);
       
     //Removing instance properties
       labelWeightNames.stream().forEach(name -> contractInstance.getProps().remove(name));
       
       LOGGER.debug(" contractInstance.getProps()---> {}", contractInstance.getProps());
       LOGGER.debug("contract -->{}",contract);
    }

    public static Map<String, ElementOption> getWeightLabelElementOption(
            VisualElement visualElement) {
        
        Map<String,ElementOption> weightLableElement = new HashMap<String, ElementOption>();
        
        ElementOption label = null;
        ElementOption weight = null;
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
        if (ChartTypes.PIE.getChartCode() == chartConfig.getType()
                || ChartTypes.DONUT.getChartCode() == chartConfig.getType()
                || ChartTypes.BAR.getChartCode() == chartConfig.getType()
                || ChartTypes.COLUMN.getChartCode() == chartConfig.getType()) {

            label = visualElement.getOption(VisualElement.LABEL);
            weight = visualElement.getOption(VisualElement.WEIGHT);

        } else if (ChartTypes.US_MAP.getChartCode() == chartConfig.getType()) {
            label = visualElement.getOption(VisualElement.STATE);
            weight = visualElement.getOption(VisualElement.WEIGHT);

        } else if (ChartTypes.LINE.getChartCode().equals(chartConfig.getType())) {
            label = visualElement.getOption(VisualElement.X);
            weight = visualElement.getOption(VisualElement.Y);

        }else if(ChartTypes.TABLE.getChartCode() == chartConfig.getType()){
            //TODO:need to check table edit/delete, it will return array of labels/values
            label = visualElement.getOption(VisualElement.LABEL);
            weight = visualElement.getOption(VisualElement.VALUE);
        }
        weightLableElement.put(Constants.LABEL, label);
        weightLableElement.put(Constants.WEIGHT, weight);
        
        return weightLableElement;
    }

    public static void removeFieldsAndVisualElement(
            ContractInstance contractInstance, VisualElement visualElement) {
        
        Contract contract = contractInstance.getContract();
        //Removing Visual element
        VisualElement visualization = contract.getVisualElements().iterator().next();
        visualization.getChildElements().remove(visualElement);
        
        Map<String,ElementOption> weightLableElement = getWeightLabelElementOption(visualElement);
        List<String> labelWeightNames = getLabelWeightNames(weightLableElement, visualElement);
        
        //Removing instance properties
        labelWeightNames.stream().forEach(name -> contractInstance.getProps().remove(name));
       
        LOGGER.debug("contractInstance.getProps()---> {}", contractInstance.getProps());
        
        
        String input = visualElement.getBasis().getBase();
        Element inputElement =  contract.getInputElements().stream().filter(element -> input.equals(element.getName())).findFirst().get();
        
        //Removing input fields
        List<Element> fieldsToRemove = new ArrayList<Element>(
                inputElement.getChildElements().stream().filter(element ->labelWeightNames.contains(element.getName()))
                .collect(Collectors.toList()));  
        
        inputElement.getChildElements().removeAll(fieldsToRemove);
        LOGGER.debug("After removing fields -->{}",inputElement.getChildElements());
        
        LOGGER.debug("contract -->{}",contract);
    }

    public static void removeWeightAndLabel(VisualElement visualElement) {
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
        
        if (ChartTypes.PIE.getChartCode() == chartConfig.getType()
                || ChartTypes.DONUT.getChartCode() == chartConfig.getType()
                || ChartTypes.BAR.getChartCode() == chartConfig.getType()
                || ChartTypes.COLUMN.getChartCode() == chartConfig.getType()) {

            visualElement.getOptions().remove(VisualElement.LABEL);
            visualElement.getOptions().remove(VisualElement.WEIGHT);

        } else if (ChartTypes.US_MAP.getChartCode() == chartConfig.getType()) {
            visualElement.getOptions().remove(VisualElement.STATE);
            visualElement.getOptions().remove(VisualElement.WEIGHT);

        } else if (ChartTypes.LINE.getChartCode().equals(chartConfig.getType())) {
            visualElement.getOptions().remove(VisualElement.X);
            visualElement.getOptions().remove(VisualElement.Y);

        }else if(ChartTypes.TABLE.getChartCode() == chartConfig.getType()){
            visualElement.getOptions().remove(VisualElement.LABEL);
            visualElement.getOptions().remove(VisualElement.VALUE);
        }
       
    }
    
    public static List<String> getLabelWeightNames(Map<String, ElementOption> weightLableElement,
            VisualElement visualElement) {
        
        List<String> labelWeightNames = new ArrayList<String>();
        
        ElementOption label = weightLableElement.get(Constants.LABEL);
        ElementOption weight = weightLableElement.get(Constants.WEIGHT);
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
        
        if(ChartTypes.TABLE.getChartCode() == chartConfig.getType()){
            weight.getParams().stream().forEach(fieldInstance ->{
                labelWeightNames.add(fieldInstance.getName());
            });
        }else{
            FieldInstance labelFieldInstance = label.getParams().get(0);
            FieldInstance weightFieldInstance = weight.getParams().get(0);
            labelWeightNames.add(labelFieldInstance.getName());
            labelWeightNames.add(weightFieldInstance.getName());
        }
        return labelWeightNames;
    }

}
