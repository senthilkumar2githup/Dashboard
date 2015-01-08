package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.AGGREGATION;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class Table extends Widget{

    private List<Field> tableColumns;
    
    public void addColumn(Field column) {
		if(tableColumns == null) {
			tableColumns = new ArrayList<>();
		}
		tableColumns.add(column);
	}
    
    public void removeColumn(Field column) {
		tableColumns.remove(column);
	}
    
	@Override
	public boolean isConfigured() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> getColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSQLColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateSQL() {
        StringBuilder sql=new StringBuilder();
        sql.append("SELECT ");
        
        tableColumns.forEach(column -> {
            if(column.isNumeric()) {
                Measure measure = (Measure) column;
                if(measure.getAggregation() !=null && 
                		measure.getAggregation() != AGGREGATION.NONE){
                	sql.append(measure.getAggregation())
	                	.append("(")
	                	.append(getLogicalFile())
	                	.append(Constants.DOT)
	                	.append(measure.getColumn())
	                	.append(")");
                } else {
                	sql.append(getLogicalFile())
	                    .append(Constants.DOT)
	                    .append(measure.getColumn())
	                    .append(Constants.COMMA);
                }
            } else {
                sql.append(getLogicalFile())
                    .append(Constants.DOT)
                    .append(column.getColumn())
                    .append(Constants.COMMA);
            }
        });
        
        sql.append(" FROM ")
            .append(getLogicalFile());

        return sql.toString();
	}

	@Override
	public VisualElement generateVisualElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<InputElement> generateInputElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getInstanceProperties() {
		// TODO Auto-generated method stub
		return null;
	}

    public List<Field> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<Field> tableColumns) {
        this.tableColumns = tableColumns;
    }
}