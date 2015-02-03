package org.hpccsystems.dashboard.exception;

public class CompositionException extends Exception {
    String error = null;
    public CompositionException(String error, Exception e) {
        super(error, e);
        this.error = error;
     }
    public String getError(){
        return this.error;
    }
}
