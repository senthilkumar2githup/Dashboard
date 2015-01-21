package org.hpccsystems.dashboard.manage;

import java.util.Set;

public class Interactivity {
    private LiveWidget sourceWidget;
    private Set<LiveWidget> affectedWidgets;
    
    public LiveWidget getSourceWidget() {
        return sourceWidget;
    }
    public void setSourceWidget(LiveWidget sourceWidget) {
        this.sourceWidget = sourceWidget;
    }
    public Set<LiveWidget> getAffectedWidgets() {
        return affectedWidgets;
    }
    public void setAffectedWidgets(Set<LiveWidget> affectedWidgets) {
        this.affectedWidgets = affectedWidgets;
    }
}
