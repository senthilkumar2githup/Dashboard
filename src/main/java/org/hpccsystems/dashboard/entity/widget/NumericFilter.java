package org.hpccsystems.dashboard.entity.widget;

import java.math.BigDecimal;

import org.hpccsystems.dashboard.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumericFilter extends Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NumericFilter.class);
    
    private BigDecimal minValue;
    private BigDecimal maxValue;
    
    public NumericFilter() {
    }
    
    public NumericFilter(Field field) {
        super(field);
    }
    
    public BigDecimal getMinValue() {
        return minValue;
    }
    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }
    public BigDecimal getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
    @Override
    public String generateFilterSQL(String fileName) {
        if(!this.hasValues())
            return "";
        StringBuilder numFilterSql=new StringBuilder();
        numFilterSql.append(fileName)
            .append(Constants.DOT)
            .append(this.getColumn())
            .append(" <= ")
            .append(maxValue)
            .append(" AND ")
            .append(fileName)
            .append(Constants.DOT)
            .append(this.getColumn())
            .append(" >= ")
            .append(minValue);
        
        return numFilterSql.toString();
    }

    @Override
    public boolean hasValues() {
        return minValue != null || maxValue != null;
    }

    @Override
    public String getHipieFilterQuery(int index, String chartName) {
        
        StringBuilder sql = new StringBuilder();
        sql.append("%");
        sql.append(getFilterName(index,chartName));
        sql.append("% ");
        sql.append(" <= ")
            .append(maxValue)
            .append(" AND ");
        sql.append("%");
        sql.append(getFilterName(index,chartName));
        sql.append("% ");
        sql.append(" >= ")
        .append(minValue);
        
        LOGGER.debug("Hipie filter query -->"+sql);
        
        return sql.toString();
    }

    @Override
    public String toString() {
        return "NumericFilter [minValue=" + minValue + ", maxValue=" + maxValue
                + "]";
    }
    
    
}
