package org.hpccsystems.dashboard.services;

import java.sql.SQLException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.springframework.dao.DataAccessException;
  

/**
 * Service class,has abstract methods for Widget related services
 *
 */
public interface WidgetService {
    
    /**
     * Retrieves Widget list from DB
     * 
     * @param dashboardId
     * @return
     *     A list of Portlet objects corresponding to the provided Dashboard Id
     * @throws SQLException
     */
    List<Portlet> retriveWidgetDetails(Integer dashboardId) throws DataAccessException;    
    
    
    /**
     * @param dashboardId
     * @param portlet
     * @param sequence
     * @return 
     *     portlet Id
     * @throws DataAccessException
     * @throws EncryptDecryptException 
     * @throws Exception 
     */
    Integer addWidget(Integer dashboardId, Portlet portlet, Integer sequence)throws JAXBException, DataAccessException, EncryptDecryptException;
    
    /**
     * @param dashboardId
     * @param portlets
     * @param widgetSequence
     */
    void addWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws DataAccessException;    
    
    /**
     * Deletes widget from DB
     * @param portletId
     * @throws Exception
     */
    void deleteWidget(Integer portletId)throws DataAccessException;
    
    /**
     * Service call to update Widget sequence alone as a batch service
     * @param dashboard
     * @throws Exception
     */
    void updateWidgetSequence(Dashboard dashboard)throws DataAccessException;

     /**
     * Converts associated ChartData object in portlet in to XML and Updates portlet to DB  
     * 
     * @param portlet
     * @throws EncryptDecryptException 
     * @throws Exception
     */
    void updateWidget(Portlet portlet)throws DataAccessException ,JAXBException, EncryptDecryptException;

    /**
     * Service to update chart title
     * @param portlet
     */
    void updateWidgetTitle(Portlet portlet)throws DataAccessException;
    
    /**
     * Encrypts the password and updates the DB
     * 
     * @param dashboards
     * @param hpccConnection
     * @param password
     * @return
     * @throws EncryptDecryptException 
     */
    int updateHpccPassword(List<Dashboard> dashboards, HpccConnection hpccConnection, String password) throws EncryptDecryptException;
}
