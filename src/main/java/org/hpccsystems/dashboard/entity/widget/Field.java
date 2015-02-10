package org.hpccsystems.dashboard.entity.widget;

import java.util.Optional;


public class Field {

	private String column;
	private String dataType;
	
	public Field() {
	}
	
	public Field(String column, String dataType) {
	    this.column = column;
	    this.dataType = dataType;
    }
	
    public Field(Field field) {
        this.column = field.column;
        this.dataType = field.dataType;
    }

    public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public boolean isNumeric() {
	    Optional<String> optionalDatatype = Optional.ofNullable(dataType);
	    if(optionalDatatype.isPresent()) {
	        String lowerCaseType = optionalDatatype.get().trim().toLowerCase();
	        if(lowerCaseType.contains("integer") || lowerCaseType.contains("real") 
	                || lowerCaseType.contains("decimal") || lowerCaseType.contains("unsigned")) {
	            return true;
	        }
	    }
	    return false;
	}
   
    /* 
     *Returns true if two field has same name and same datatype
     *and same aggregate function in case of numeric type
     */
    @Override
    public boolean equals(Object o) {
        final Field thisField = (Field) o;
        //Checking numeric fields
        if (thisField.isNumeric()) {
            if (this.column.equals(thisField.column)
                    && ((Measure) this).getAggregation().equals(
                            ((Measure) thisField).getAggregation())) {
                return true;
            }
        } else if (this.column.equals(thisField.column)
                && this.dataType.equalsIgnoreCase(thisField.dataType)) {
            return true;
        }
        return false;
    }
    
    @Override    
    public int hashCode(){
        int hash = 3;
        hash = 53 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Field [column=" + column + ", dataType=" + dataType + "]";
    }

    
}
