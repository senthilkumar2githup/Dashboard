package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.manage.Interactivity;
import org.hpccsystems.dashboard.manage.LiveWidget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget,String userId) throws Exception;
    
    CompositionInstance runComposition(Dashboard dashboard,String userId) throws Exception;
    
    String getWorkunitId(Dashboard dashboard,String userId) throws Exception ;

    void addCompositionChart(Dashboard dashboard, Widget widget,String userId) throws Exception;
    
    void editCompositionChart(Dashboard dashboard, Widget widget,String userId) throws Exception;
    
    void deleteCompositionChart(Dashboard dashboard, String userId, String chartName) throws Exception;
    
    List<LiveWidget> extractLiveWidgets(Dashboard dashboard, String user);
    
    List<Interactivity> extractInteractivities(Dashboard dashboard, String user);
    
    void addInteractivity(Dashboard dashboard, String user, Interactivity interactivity);
    
    void removeInteractivity(Dashboard dashboard, String user, Interactivity interactivity);
}
