package org.hpccsystems.dashboard.exception;

public class WSSQLException extends Exception {
    String error = null;
    public WSSQLException(String error, Exception e) {
        super(error, e);
        this.error = error;
     }
    public String getError(){
        return this.error;
    }
}
