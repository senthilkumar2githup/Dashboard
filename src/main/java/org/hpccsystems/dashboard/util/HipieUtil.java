package org.hpccsystems.dashboard.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HipieUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HipieUtil.class);
    private static final String AND = "AND";
    private static final String PERCENATGE ="%";
    private static final String SQUARE_OPEN ="[";
    private static final String SQUARE_CLOSE ="]";
    private static final String LESS_THAN ="<=";
    private static final String IN ="IN";
    private static final String SPACE = " ";
    
    public static VisualElement getVisualElement(Contract contract ,String chartName) {
    
        VisualElement visualization = contract.getVisualElements().iterator().next();
        VisualElement visualElement = (VisualElement)visualization.getChildElement(chartName);
        return visualElement;
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
        
        List<String> labelWeightNames = getWeightLabelFilterNames(visualElement);
       
       
     //Removing instance properties
       labelWeightNames.stream().forEach(name -> contractInstance.getProps().remove(name));
       
       LOGGER.debug(" contractInstance.getProps()---> {}", contractInstance.getProps());
       LOGGER.debug("contract -->{}",contract);
    }

    public static  List<String> getWeightLabelFilterNames(
            VisualElement visualElement) {
        
        ElementOption label = null;
        ElementOption weight = null;
        List<String> labelWeightNames = new ArrayList<String>();
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
        if (ChartTypes.PIE.getChartCode() == chartConfig.getType()
                || ChartTypes.DONUT.getChartCode() == chartConfig.getType()) {
            label = visualElement.getOption(VisualElement.LABEL);
            weight = visualElement.getOption(VisualElement.WEIGHT);

        } else if (ChartTypes.US_MAP.getChartCode() == chartConfig.getType()) {
            label = visualElement.getOption(VisualElement.STATE);
            weight = visualElement.getOption(VisualElement.WEIGHT);

        } else if (ChartTypes.LINE.getChartCode().equals(chartConfig.getType())) {
            label = visualElement.getOption(VisualElement.X);
            weight = visualElement.getOption(VisualElement.Y);

        }else if(ChartTypes.TABLE.getChartCode() == chartConfig.getType()){
            label = visualElement.getOption(VisualElement.LABEL);
            weight = visualElement.getOption(VisualElement.VALUE);
            
        }else if(ChartTypes.BAR.getChartCode() == chartConfig.getType()
                || ChartTypes.COLUMN.getChartCode() == chartConfig.getType()){
            //chart has single measure
            label = visualElement.getOption(VisualElement.LABEL);
            weight = visualElement.getOption(VisualElement.WEIGHT);
            //chart has multiple measures
            if(weight == null){
                label = visualElement.getOption(VisualElement.X);
                weight = visualElement.getOption(VisualElement.Y);
            }
        }
        
        //getting Attribute/Measure field names
        weight.getParams().stream().forEach(fieldInstance ->{
            labelWeightNames.add(fieldInstance.getName());
        });
        if(ChartTypes.TABLE.getChartCode() != chartConfig.getType()){
            label.getParams().stream().forEach(fieldInstance ->{
                labelWeightNames.add(fieldInstance.getName());
            });
        }
        //getting filter field names
        if(visualElement.getBasisFilter() != null && !visualElement.getBasisFilter().isEmpty()){
            labelWeightNames.addAll(getFilterNames(visualElement.getBasisFilter()));
        }
       
        return labelWeightNames;
        
    }

    /**
     * Gets the filter name from DUD file like 'Filter1_filterchart'
     * @param hipieFilterStr
     * @return 
     */
    private static List<String> getFilterNames(
            String hipieFilterStr) {
        List<String> filterStr = Arrays.asList(StringUtils.splitByWholeSeparator(hipieFilterStr, AND));
        List<String> filterNames = new ArrayList<String>();
        
        filterStr.stream().forEach(filterLabel ->{
            String fieldName = null;
            if(filterLabel.contains(LESS_THAN)){
                String[] strArray = filterLabel.split(SPACE);
                fieldName = strArray[0].trim();
                fieldName = StringUtils.removeStart(fieldName, PERCENATGE);
                fieldName = StringUtils.removeEnd(fieldName, PERCENATGE);      
                filterNames.add(fieldName);
            }else if(filterLabel.contains(IN) && filterLabel.contains(SQUARE_OPEN) && filterLabel.contains(SQUARE_CLOSE)){
                String[] strArray = filterLabel.trim().split(SPACE);                
                fieldName = strArray[0].trim();
                fieldName = StringUtils.removeStart(fieldName, PERCENATGE);
                fieldName = StringUtils.removeEnd(fieldName,PERCENATGE);
                filterNames.add(fieldName);
            }
        
        });
        LOGGER.debug("filterNames -->{}",filterNames);
        return filterNames;
    }



    public static void removeFieldsAndVisualElement(
            ContractInstance contractInstance, VisualElement visualElement) {
        
        Contract contract = contractInstance.getContract();
        //Removing Visual element
        VisualElement visualization = contract.getVisualElements().iterator().next();
        visualization.getChildElements().remove(visualElement);
        
        List<String> labelWeightFilterNames = getWeightLabelFilterNames(visualElement);
        
        //Removing instance properties
        labelWeightFilterNames.stream().forEach(name -> contractInstance.getProps().remove(name));
       
        LOGGER.debug("contractInstance.getProps()---> {}", contractInstance.getProps());
        
        
        String input = visualElement.getBasis().getBase();
        Element inputElement =  contract.getInputElements().stream().filter(element -> input.equals(element.getName())).findFirst().get();
        
        //Removing input fields
        List<Element> fieldsToRemove = new ArrayList<Element>(
                inputElement.getChildElements().stream().filter(element ->labelWeightFilterNames.contains(element.getName()))
                .collect(Collectors.toList()));  
        
        inputElement.getChildElements().removeAll(fieldsToRemove);
        LOGGER.debug("After removing fields -->{}",inputElement.getChildElements());
        
        LOGGER.debug("contract -->{}",contract);
    }

    public static void removeWeightAndLabel(VisualElement visualElement) {
        
        Map<String, ChartConfiguration> chartTypes = Constants.CHART_CONFIGURATIONS;
        ChartConfiguration chartConfig = chartTypes.get(visualElement.getType());
        
        if (ChartTypes.PIE.getChartCode() == chartConfig.getType()
                || ChartTypes.DONUT.getChartCode() == chartConfig.getType()) {

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
            
        }else if(ChartTypes.BAR.getChartCode() == chartConfig.getType()
                || ChartTypes.COLUMN.getChartCode() == chartConfig.getType()){
            //chart has single measure
            ElementOption weight = visualElement.getOption(VisualElement.WEIGHT);
            //chart has multiple measures
            if(weight == null){
                visualElement.getOptions().remove(VisualElement.X);
                visualElement.getOptions().remove(VisualElement.Y);
            }else{ //chart has single measure
                visualElement.getOptions().remove(VisualElement.LABEL);
                visualElement.getOptions().remove(VisualElement.WEIGHT);
            }
        }
    }
    
   
}
