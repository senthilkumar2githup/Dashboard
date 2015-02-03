package org.hpccsystems.dashboard.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.exception.WSSQLException;


public interface WSSQLService {
    List<String> getDistinctValues(Field field, HPCCConnection connection, String fileName, List<Filter> filters,int wssqlPort) throws HpccConnectionException,XMLStreamException, WSSQLException;
    Map<String, BigDecimal> getMinMax(Field field, HPCCConnection connection, String fileName, List<Filter> filters,int wssqlPort) throws WSSQLException, XMLStreamException, HpccConnectionException, RemoteException;
    ChartdataJSON getChartdata(Widget widget, HPCCConnection connection,int wssqlPort) throws WSSQLException;
}
