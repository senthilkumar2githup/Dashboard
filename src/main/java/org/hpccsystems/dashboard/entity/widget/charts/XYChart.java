package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zul.ListModelList;

public class XYChart extends Widget{

    private Attribute attribute;
    private List<Measure> measures;
    private Attribute groupAttribute;
        
    @Override
    public List<String> getColumns() {
        List<String> columnList=new ArrayList<String>();
        columnList.add(attribute.getDisplayName());
        measures.stream().forEach(measure->{
            columnList.add(measure.getDisplayName());
        });        
        return columnList;
    }

    @Override
    public String generateSQL() {
        StringBuilder sql=new StringBuilder();
        sql.append("SELECT ")
        .append(getLogicalFile())
        .append(Constants.DOT)
        .append(attribute.getColumn())
        .append(Constants.COMMA);
        measures.stream().forEach(everyMeasure->{
            if(everyMeasure.getAggregation()!=null && everyMeasure.getAggregation()!= AGGREGATION.NONE){
            sql.append(everyMeasure.getAggregation())
            .append("(")
            .append(getLogicalFile())
            .append(Constants.DOT)
            .append(everyMeasure.getColumn())
            .append(")");
            } else {
                sql.append(getLogicalFile())
                .append(Constants.DOT)
                .append(everyMeasure.getColumn());                
            }
            if(measures.indexOf(everyMeasure)!=measures.size()-1)
            sql.append(Constants.COMMA);
        });
        sql.append(" FROM ")
        .append(getLogicalFile());
        
        if((this.getFilters()!=null)&&(!this.getFilters().isEmpty())){
            Iterator<Filter> filters=this.getFilters().iterator();            
            Filter localFilter;
            boolean firstTime=true;                        
            while(filters.hasNext()){
                localFilter = filters.next();
                if(firstTime&&localFilter.hasValues()){
                    sql.append(" WHERE ");
                    sql.append(localFilter.generateFilterSQL(getLogicalFile()));
                    firstTime=false;
                    }else if(localFilter.hasValues()){
                        sql.append(" AND ");
                        sql.append(localFilter.generateFilterSQL(getLogicalFile()));
                    }                
            }            
        }
        sql.append(" GROUP BY ").append(getLogicalFile()).append(Constants.DOT)
        .append(attribute.getColumn());
        return sql.toString();
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void setMeasure(List<Measure> measures) {
        this.measures = measures;
    }
    
    public void addMeasure(Measure measure){
        if(this.measures!=null){
            this.measures.add(measure);
    }else{
            this.measures=new ArrayList<Measure>();
            this.measures.add(measure);
        }
    }
    
    public void removeMeasure(Measure measure){
        this.measures.remove(measure);
    }

    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

    @Override
    public VisualElement generateVisualElement() {
        
        VisualElement visualElement = new VisualElement();
        
        visualElement.setType(this.getChartConfiguration().getType());
        
        visualElement.addCustomOption(ElementOption.CreateElementOption(Constants.HIPIE._CHARTTYPE,
                new FieldInstance(null, this.getChartConfiguration()
                        .getHipieChartName())));

        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));

        generateVisualOption(visualElement);
        
        // Setting Tittle for chart
        visualElement.addOption(ElementOption.CreateElementOption(VisualElement.TITLE,
                new FieldInstance(null, this.getTitle())));

        return visualElement;
    }

    @Override
    public Map<String, String> getInstanceProperties() {
        Map<String, String> fieldNames = new HashMap<String, String>();
        fieldNames.put(getPluginAttribute(), this.getAttribute().getColumn());
        getMeasures().stream().forEach(
                measure -> {
                    fieldNames.put(this.getPluginMeasure(measure),
                            measure.getColumn());
                });
        return fieldNames;
    }

    @Override
    public List<InputElement> generateInputElement() {
        List<InputElement> inputs = new ListModelList<InputElement>();
        
        InputElement attributeInput = new InputElement();
        attributeInput.setName(getPluginAttribute());
        attributeInput.addOption(ElementOption.CreateElementOption(Element.LABEL,
                new FieldInstance(null, getAttribute().getColumn())));
        attributeInput.setType(InputElement.TYPE_FIELD);
        inputs.add(attributeInput);
        
        getMeasures().listIterator().forEachRemaining(measure -> {
                InputElement measureInput = new InputElement();
                measureInput.setName(getPluginMeasure(measure));
                measureInput.addOption(ElementOption.CreateElementOption(Element.LABEL,
                        new FieldInstance(null, measure.getColumn())));
                measureInput.setType(InputElement.TYPE_FIELD);
                inputs.add(measureInput);
            });

        return inputs;
    }

    @Override
    public boolean isConfigured() {
        return (this.getAttribute()!=null)&&(this.getMeasures()!=null)&&(!this.getMeasures().isEmpty());
    }

    @Override
    public List<String> getSQLColumns() {
        List<String> sqlColumnList=new ArrayList<String>();
        int listSize=0;
        sqlColumnList.add(attribute.getDisplayName());
        Iterator<Measure> measureIterator=measures.iterator();
        while(measureIterator.hasNext()){
            Measure measure=measureIterator.next();            
        if((measure.getAggregation()!=null)&&(measure.getAggregation()!=AGGREGATION.NONE)){
            sqlColumnList.add(measure.getAggregation().toString()+"out"+listSize);
        }else{
            sqlColumnList.add(measure.getDisplayName());
        }   
        listSize++;
        }
        return sqlColumnList;
    }
    
    
    /**
     * generates Name as 'Attribute_chartName(ie: getName())'
     * @return String
     */
    public String getPluginAttribute() {
        StringBuilder builder = new StringBuilder();
        builder.append("Attribute").append("_").append(this.getName());
        return builder.toString();
    }

    /**
     * generates Name as 'Measure1_chartName(ie: getName())'
     * @return String
     */
    public String getPluginMeasure(Measure measure) {
        StringBuilder measureName = new StringBuilder();
        measureName.append("Measure")
                .append(getMeasures().indexOf(measure) + 1).append("_")
                .append(this.getName());
        return measureName.toString();
    }

    @Override
    public void editVisualElement(VisualElement visualElement) {
        visualElement.setBasisQualifier(null);
        generateVisualOption(visualElement);
    }

    private void generateVisualOption(VisualElement visualElement) {
        
        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

       
        // Attribute settings
        ri.add(new FieldInstance(null, getPluginAttribute()));
        
        if(ChartTypes.LINE.getChartCode().equals(this.getChartConfiguration().getType())
                || this.getMeasures().size() > 1){
           
            ListIterator<Measure> listItr =  getMeasures().listIterator();
            Measure measure = null;
                while(listItr.hasNext()){
                    measure = listItr.next();
                    StringBuilder meaureLabel = new StringBuilder();
                    meaureLabel.append(getPluginMeasure(measure));
                    ri.add(new FieldInstance(
                            (!AGGREGATION.NONE.equals(measure.getAggregation()) ) ? measure
                                    .getAggregation().toString() : null,getPluginMeasure(measure) ));
                    
                        if(listItr.nextIndex() > 1){
                            // Measures settings
                            ElementOption yElement = visualElement.getOption(VisualElement.Y);
                            yElement.addParam(new FieldInstance((!AGGREGATION.NONE.equals(measure.getAggregation()) ) ? measure
                                            .getAggregation().toString() : null,meaureLabel.toString() ));
                            // Attribute settings
                            ElementOption xElement = visualElement.getOption(VisualElement.X);
                            xElement.addParam(new FieldInstance(null, getPluginAttribute()));
                           
                        }else{
                            // Measures settings
                            visualElement.addOption(ElementOption.CreateElementOption(VisualElement.Y,
                                    new FieldInstance((!AGGREGATION.NONE.equals(measure.getAggregation()) ) ? measure
                                            .getAggregation().toString() : null,meaureLabel.toString() )));
                            // Attribute settings
                            visualElement.addOption(ElementOption.CreateElementOption(VisualElement.X,
                                    new FieldInstance(null, getPluginAttribute())));
                        }
                        
                }
                
        }else{
            Measure measure = this.getMeasures().get(0);
            ri.add(new FieldInstance(
                    (!AGGREGATION.NONE.equals(measure.getAggregation()) ) ? measure
                            .getAggregation().toString() : null,getPluginMeasure(measure) ));
            StringBuilder meaureLabel = new StringBuilder();
            meaureLabel.append(getPluginMeasure(measure));
            // Measures settings
            visualElement.addOption(ElementOption.CreateElementOption(VisualElement.WEIGHT,
                    new FieldInstance((!AGGREGATION.NONE.equals(measure.getAggregation()) ) ? measure
                            .getAggregation().toString() : null,meaureLabel.toString() )));
            // Attribute settings
            visualElement.addOption(ElementOption.CreateElementOption(VisualElement.LABEL,
                    new FieldInstance(null, getPluginAttribute())));
        }
        
    }


}
