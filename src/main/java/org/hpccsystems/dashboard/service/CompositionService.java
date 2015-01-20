package org.hpccsystems.dashboard.service;

import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;

public interface CompositionService {
    
    void createComposition(Dashboard dashboard, Widget widget,String userId) throws Exception;
    
    CompositionInstance runComposition(Dashboard dashboard,String userId) throws Exception;
    
    String getWorkunitId(Dashboard dashboard,String userId) throws Exception ;

    void addCompositionChart(Dashboard dashboard, Widget widget,String userId);
    
    void editCompositionChart(Dashboard dashboard, Widget widget,String userId) throws Exception;
    
    void deleteCompositionChart(Dashboard dashboard, String userId, String chartName) throws Exception;
}
