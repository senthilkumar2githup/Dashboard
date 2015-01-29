package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zul.ListModelList;

public class Table extends Widget{

    private List<Field> tableColumns;

	@Override
	public String generateSQL() {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");

		tableColumns.forEach(column -> {
			if (column.isNumeric()) {
				Measure measure = (Measure) column;
				if (measure.getAggregation() != AGGREGATION.NONE) {
					sql.append(measure.getAggregation()).append("(")
							.append(getLogicalFile()).append(Constants.DOT)
							.append(measure.getColumn()).append(")");
				} else {
					sql.append(getLogicalFile()).append(Constants.DOT)
							.append(measure.getColumn());
				}
			} else {
				sql.append(getLogicalFile()).append(Constants.DOT)
						.append(column.getColumn());
			}
			sql.append(Constants.COMMA);
		});
		StringBuilder query = new StringBuilder(sql.toString().trim());
		query.deleteCharAt(query.length()-1);
		query.append(" FROM ").append(getLogicalFile());
		if((this.getFilters()!=null)&&(!this.getFilters().isEmpty())){
			query.append(" WHERE ");
			getFilterQuery(query);
       }
		return query.toString();
	}

    
    public void addColumn(Field column) {
		if(tableColumns == null) {
			tableColumns = new ArrayList<>();
		}
		tableColumns.add(column);
	}
    
    public void removeColumn(Field column) {
		tableColumns.remove(column);
	}
    

    public List<Field> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Field> tableColumns) {
        this.tableColumns = tableColumns;
    }
    
	@Override
	public boolean isConfigured() {
		 return tableColumns!= null && !tableColumns.isEmpty();
	}

	@Override
	public List<String> getColumns() {
		 List<String> columnList=new ArrayList<String>();
		 tableColumns.forEach(field -> {
			 columnList.add(field.getColumn());
		 });
	     return columnList;
	}

	@Override
	public List<String> getSQLColumns() {
		List<String> sqlColumnList = new ArrayList<String>();
		 int listSize=1;
		 for (Field column : tableColumns) {
			 if (column.isNumeric()) {
					Measure measure = (Measure) column;
					if (measure.getAggregation() != AGGREGATION.NONE) {
						sqlColumnList.add(measure.getAggregation().toString()
								+ "out" + listSize );
					} else {
						sqlColumnList.add(measure.getDisplayName());
					}
					 listSize++;
				} else {
					Attribute attribute = new Attribute(column);
					sqlColumnList.add(attribute.getDisplayName());
				} 
			}
		
		return sqlColumnList;
	}

	
	@Override
	public VisualElement generateVisualElement() {

        VisualElement visualElement = new VisualElement();
        // TODO:Need to set chart type using Hipie's 'Element' class
        visualElement.setType(this.getChartConfiguration().getType());
        visualElement.addCustomOption(ElementOption.CreateElementOption(Constants.HIPIE._CHARTTYPE,
                new FieldInstance(null, this.getChartConfiguration()
                        .getHipieChartName())));
        visualElement.setName(DashboardUtil.removeSpaceSplChar(this.getName()));

        generateVisualOption(visualElement);
        
        // Setting Title for chart
        visualElement.addOption(ElementOption.CreateElementOption(VisualElement.TITLE,
                new FieldInstance(null, this.getTitle())));
        
        return visualElement;
	}

	@Override
	public List<InputElement> generateInputElement() {
		  List<InputElement> inputs = new ListModelList<InputElement>();
		  tableColumns.forEach(column -> {
		  InputElement columnInput = new InputElement();
		  columnInput.setName(createInputName(column));
		  columnInput.addOption(ElementOption.CreateElementOption(Element.LABEL,new FieldInstance(null,column.getColumn())));
		  columnInput.setType(InputElement.TYPE_FIELD);
		  inputs.add(columnInput);
		});
		  
		  if(this.getFilters() != null){
	            this.getFilters().forEach(filter->{
	                 InputElement filterElement = new InputElement();
	                 filterElement.setName(filter.getFilterName(filter,
	                         getFilters().indexOf(filter), this.getName()));
	                 filterElement.addOption(ElementOption.CreateElementOption(Element.LABEL,
	                         new FieldInstance(null,filter.getColumn())));
	                 filterElement.setType(InputElement.TYPE_FIELD);
	                 inputs.add(filterElement);
	            });
	        }
		  return inputs;
	}

	@Override
	public Map<String, String> getInstanceProperties() {
		
		 Map<String, String> fieldNames = new HashMap<String, String>();
		 tableColumns.forEach(field -> {
			 fieldNames.put(createInputName(field),field.getColumn() );
		 });
		 
		 if(this.getFilters() != null){
	            this.getFilters().forEach(filter->{
	                fieldNames.put(
	                        filter.getFilterName(filter,
	                                getFilters().indexOf(filter), this.getName()),
	                        filter.getColumn());
	             });
	            
	        }
	        return fieldNames;
	    }
	/**
     * generates Name as 'Column1_chartName[ie: getName()]'
     * @return String
     */
    public String createInputName(Field field) {
        StringBuilder builder = new StringBuilder();
        builder.append("Column")
                .append(getTableColumns().indexOf(field) + 1).append("_")
                .append(this.getName());
        return builder.toString();
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


    @Override
    public void editVisualElement(VisualElement visualElement) {
        visualElement.setBasisQualifier(null);
        generateVisualOption(visualElement);
    }

    private void generateVisualOption(VisualElement visualElement) {
        
        RecordInstance ri = new RecordInstance();
        visualElement.setBasisQualifier(ri);

        visualElement.setBasisFilter(null);
        if(this.getFilters() != null && !this.getFilters().isEmpty()){
            visualElement.setBasisFilter(getHipieFilterQuery());
        }

        String[] labelArray = new String[tableColumns.size()];
        String[] valueArray = new String[tableColumns.size()];
        List<String> labellist = new ArrayList<String>();
        List<String> valueList = new ArrayList<String>();

        // Columns settings
        tableColumns.forEach(column -> {
            if (column.isNumeric()) {
                //For Numeric field
                Measure measure = (Measure) column;
                //creates label as 'SUM(buyprice)'
                labellist.add((!AGGREGATION.NONE.equals(measure.getAggregation())) ? (measure
                        .getAggregation().toString()
                        + "("
                        + measure.getDisplayName() + ")") : measure
                        .getDisplayName());
                ri.add(new FieldInstance(
                        (!AGGREGATION.NONE.equals(measure.getAggregation())) ? measure
                                .getAggregation().toString() : null,
                        createInputName(measure)));
                //creates Value as 'SUM(Column1_Tablewidget)'
                valueList.add((!AGGREGATION.NONE.equals(measure.getAggregation())) ? (measure
                        .getAggregation().toString()
                        + "("
                        + createInputName(column) + ")") : createInputName(column));
            } else {
                //For String field
                //creates label as 'buyprice'
                Attribute attribute = new Attribute(column);
                labellist.add(attribute.getDisplayName());
                ri.add(new FieldInstance(null, createInputName(attribute)));
                //creates value as Column2_Tablewidget
                valueList.add(createInputName(column));
            }
        });
        
        visualElement.addOption(ElementOption.CreateElementOption(VisualElement.LABEL, labellist.toArray(labelArray)));
        visualElement.addOption(ElementOption.CreateElementOption(VisualElement.VALUE, valueList.toArray(valueArray)));
        
    }
}
