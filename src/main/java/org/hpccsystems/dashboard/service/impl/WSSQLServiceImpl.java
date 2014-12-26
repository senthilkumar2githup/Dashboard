package org.hpccsystems.dashboard.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ws_sql.ws.hpccsystems.ExecuteSQLRequest;
import ws_sql.ws.hpccsystems.Ws_sqlLocator;
import ws_sql.ws.hpccsystems.Ws_sqlServiceSoap;

import com.mysql.jdbc.Field;

public class WSSQLServiceImpl implements WSSQLService{
	  private static final Logger LOGGER =LoggerFactory
	            .getLogger(WSSQLServiceImpl.class);
    private static final String SELECT = "select ";
    private static final String WHERE = "where";
    private static final String WHERE_WITH_SPACES = " where ";
    
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    
    private static final String UNAUTHORIZED = "Unauthorized";  

    private static final String DFU_ENDPOINT = "/WsDfu?ver_=1.2";
	
	private String executeSQL(HPCCConnection hpccConnection, String sql)
			throws Exception {

		String resultString = null;
		Ws_sqlLocator locator = new Ws_sqlLocator();
		StringBuilder endpoint = new StringBuilder();
		if (hpccConnection.isHttps) {
			endpoint.append(HTTPS);
		} else {
			endpoint.append(HTTP);
		}
		// TO DO add the IP and  port number in HIPIE  and then implement
		
		endpoint.append("https://216.105.19.2:18009"); 
		endpoint.append("/ws_sql?ver_=1");

		locator.setWs_sqlServiceSoapAddress(endpoint.toString());
		locator.setWs_sqlServiceSoap_userName(hpccConnection.getUserName());
		locator.setWs_sqlServiceSoap_password(hpccConnection.getPwd());

		ExecuteSQLRequest req = new ExecuteSQLRequest();
		req.setSqlText(sql);
		req.setTargetCluster("thor");
		req.setIncludeResults(true);

		Ws_sqlServiceSoap soap = locator.getws_sqlServiceSoap();
		resultString = soap.executeSQL(req).getResult();

		if (resultString != null && resultString.length() > 0) {
			resultString = StringUtils.substringBetween(resultString,
					"<Dataset", "</Dataset>");
			resultString = "<Dataset" + resultString + "</Dataset>";
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result String: Afetr removing schema tag "
					+ resultString);
		}

		return resultString;

	}

	/* Sample response xml
	 * 
	 * <Dataset name='WsSQLResult'> <Row><productcode>S10_1678</productcode><quantityinstock>7933</quantityinstock><buyprice>48.81</buyprice></Row></Dataset>
	 * 
	 */
	
    @SuppressWarnings("unused")
	private static ChartdataJSON parseChartdataResponse(List<String> columns, String responseXML){
    	ChartdataJSON dataJSON=null;
    	if (responseXML != null && responseXML.length() > 0) {
    		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    		try {
				XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(responseXML));
				List <List<Object>> dataList=new ArrayList<List<Object>>();List<Object> dataRowList=null;
				while(xmlEventReader.hasNext()){
					XMLEvent xmlEvent = xmlEventReader.nextEvent();
					if (xmlEvent.isStartElement()){
	                       StartElement startElement = xmlEvent.asStartElement();
	                       if(startElement.getName().getLocalPart().equals("Row")){
	                    	   dataRowList=new ArrayList<Object>();
	                       }
	                       else if(!startElement.getName().getLocalPart().equals("Dataset")){
	                           xmlEvent = xmlEventReader.nextEvent();
	                           if(xmlEvent.isCharacters()){
	                        	   dataRowList.add(xmlEvent.asCharacters().getData());
	 	                           }else{
	 	                        	  dataRowList.add("0");
	 	                           }
	                          
	                       }
	                }
					if(xmlEvent.isEndElement()){
                       EndElement endElement = xmlEvent.asEndElement();
                       if(endElement.getName().getLocalPart().equals("Row")){
                    	   dataList.add(dataRowList);
                       }
	               }
				}
				dataJSON=new ChartdataJSON();
				dataJSON.setColumns(columns);
				dataJSON.setData(dataList);
				LOGGER.info("data list {}",dataList);
			} catch (XMLStreamException e) {
				LOGGER.error(Constants.EXCEPTION, e);
			}
    	}
        return dataJSON;
    }
    
    @Override
    public List<String> getDistinctValues(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception  {
    	 List<String> dataList = null;
         try {
             final StringBuilder queryTxt = new StringBuilder(SELECT);
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field);
             queryTxt.append(" from ");
             queryTxt.append(fileName);

             if (!filters.isEmpty()) {            	 
                queryTxt.append(constructWhereClause(filters ,fileName));
             }

             queryTxt.append(" group by ");
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field);

             if (LOGGER.isDebugEnabled()) {
            	 LOGGER.debug("Query for Distinct values -> " + queryTxt.toString());
             }

             final String resultString = executeSQL(connection, queryTxt.toString());
             if (resultString != null && resultString.length() > 0) {
            	 XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            	 XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(resultString));
 				 dataList=new ArrayList<String>();
 				while(xmlEventReader.hasNext()){
 					XMLEvent xmlEvent = xmlEventReader.nextEvent();
 					if (xmlEvent.isStartElement()){
 	                       StartElement startElement = xmlEvent.asStartElement();
 	                       if(startElement.getName().getLocalPart().equals("Row") || startElement.getName().getLocalPart().equals("Dataset")){
 	                    	   continue;
 	                       }
 	                       else {
 	                           xmlEvent = xmlEventReader.nextEvent();
 	                           if(xmlEvent.isCharacters()){
 	                        	  dataList.add(xmlEvent.asCharacters().getData());
 	                           }else{
 	                        	  dataList.add("");
 	                           }
 	                          
 	                       }
 	                }
 				}
            	 
             }else{
            	 throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
             }
                 
         }  catch (RemoteException e) {
             if (e.getMessage().contains(UNAUTHORIZED)) {
                 throw new HpccConnectionException("401 Unauthorized");
             }
             LOGGER.error(Constants.EXCEPTION, e);
             throw e;
         } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
        	 LOGGER.error(Constants.EXCEPTION, ex);
             throw ex;
         }
         return dataList;
    }

    @Override
    public Map<String, Number> getMinMax(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception {
    	Map<String, Number> resultMap = null;

          try {
        	  
              final StringBuilder queryTxt = new StringBuilder("select min(").append(fileName).append(".")
                      .append(field).append("), max(").append(fileName).append(".").append(field)
                      .append(") from ");

              if (!filters.isEmpty()) {
                      queryTxt.append(fileName);
                      queryTxt.append(constructWhereClause(filters ,fileName));
                  }
               else {
                  queryTxt.append(fileName);
              }

              final String resultString = executeSQL(connection, queryTxt.toString());

              if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("queryTxt in fetchFilterMinMax() -->" + queryTxt);
              }
              if (resultString != null && resultString.length() > 0) {
            	  
            	 XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            	 XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(resultString));
            	 resultMap=new HashMap<String, Number>();
  				while(xmlEventReader.hasNext()){
  					XMLEvent xmlEvent = xmlEventReader.nextEvent();
  					if (xmlEvent.isStartElement()){
  	                       StartElement startElement = xmlEvent.asStartElement();
  	                       if(startElement.getName().getLocalPart().equals("Row") || startElement.getName().getLocalPart().equals("Dataset")){
  	                    	   continue;
  	                       }
  	                       else if(startElement.getName().getLocalPart().equals("minout1")){
  	                           xmlEvent = xmlEventReader.nextEvent();
  	                         if(xmlEvent.isCharacters())
  	                        	resultMap.put("min",new BigDecimal(xmlEvent.asCharacters().getData()));
  	                        else
  	                        	resultMap.put("min",new BigDecimal(0));
  	                       }
  	                       else{
	                           xmlEvent = xmlEventReader.nextEvent();
							if (xmlEvent.isCharacters())
								resultMap.put("max", new BigDecimal(xmlEvent
										.asCharacters().getData()));
							else
								resultMap.put("max", new BigDecimal(0));
						}
  	                }
  				}
            	  
              } else {
            	  throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
              }

          } catch (RemoteException e) {
              if (e.getMessage().contains(UNAUTHORIZED)) {
                 throw new HpccConnectionException("401 Unauthorized");
              }
              LOGGER.error(Constants.EXCEPTION, e);
             throw e;
          } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
              LOGGER.error(Constants.EXCEPTION, ex);
             throw new HpccConnectionException();
          }
          return resultMap;
    }

    @Override
    public ChartdataJSON getChartdata(Widget widget, HPCCConnection connection) throws  Exception{
    	
            ChartdataJSON dataObj = null;
            final String queryTxt = widget.generateSQL();

            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("WS_SQL Query ->" + queryTxt);
            }

            final String resultString = executeSQL(connection, queryTxt);
            return   parseChartdataResponse(widget.getColumns(), resultString);
    }
   
    private String constructWhereClause(List<Filter> filters, String fileName) {
        StringBuilder queryTxt = new StringBuilder();
        queryTxt.append(WHERE_WITH_SPACES);

            Iterator<Filter> iterator = filters.iterator();
            while (iterator.hasNext()) {
                Filter filter = iterator.next();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Contructing where clause " + filter.toString());
                }

                queryTxt.append("(");

                NumericFilter nFilter = null;
                StringFilter sfilter = null;
                
                if(filter instanceof StringFilter){
                    sfilter = (StringFilter)filter;
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" in ");
                    queryTxt.append(" (");

                    for (int i = 1; i <= sfilter.getValues().size(); i++) {

                        queryTxt.append(" '").append(sfilter.getValues().get(i - 1)).append("'");
                        queryTxt.append(",");
                    }
                    queryTxt.deleteCharAt(queryTxt.length()-1);
                    queryTxt.append(" )");
                } else if(filter instanceof NumericFilter){
                    nFilter = (NumericFilter)filter;
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(nFilter.getColumn());
                    queryTxt.append(" >= ");
                    queryTxt.append(nFilter.getMinValue());
                    queryTxt.append(" and ");
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" <= ");
                    queryTxt.append(((NumericFilter) filter).getMinValue());
                }

                queryTxt.append(")");

                if (iterator.hasNext()) {
                    queryTxt.append(" AND ");
                }
            }
        
    
        return queryTxt.toString();
    }
}
