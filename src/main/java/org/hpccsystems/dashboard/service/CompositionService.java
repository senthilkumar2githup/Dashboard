package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.CompositionException;
import org.hpccsystems.dashboard.manage.Interactivity;
import org.hpccsystems.dashboard.manage.LiveWidget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget,String userId) throws CompositionException;
    
    CompositionInstance runComposition(Dashboard dashboard,String userId) throws CompositionException;
    
    String getWorkunitId(Dashboard dashboard,String userId) throws CompositionException ;

    void addCompositionChart(Dashboard dashboard, Widget widget,String userId) throws CompositionException;
    
    void editCompositionChart(Dashboard dashboard, Widget widget,String userId) throws CompositionException;
    
    void deleteCompositionChart(Dashboard dashboard, String userId, String chartName) throws CompositionException;
    
    List<LiveWidget> extractLiveWidgets(Dashboard dashboard, String user);
    
    List<Interactivity> extractInteractivities(Dashboard dashboard, String user);
    
    void addInteractivity(Dashboard dashboard, String user, Interactivity interactivity);
    
    void removeInteractivity(Dashboard dashboard, String user, Interactivity interactivity);
}
