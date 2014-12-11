package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.InputParams;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.InputListitem;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Panel;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class InputParametersController extends SelectorComposer<Panel>{

    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(InputParametersController.class);
    
    @Wire
    private Listbox inputParams;
    
    @WireVariable
    private HPCCQueryService hpccQueryService;
    
    private ChartData chartData;
    private Portlet portlet;
    private Component busyComponent;
    private Component parent;
    
    private EventListener<Event> addParamsListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            constructInputParameters();
        }
    };
    
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        Execution execution = Executions.getCurrent();
        chartData = (ChartData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Chart data - " + chartData);
            LOG.debug("Portlet - " + portlet);
        }
        
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doAfterCompose(Panel comp) throws Exception {
        super.doAfterCompose(comp);
        
        busyComponent = (Component) Executions.getCurrent().getArg().get(Constants.BUSY_COMPONENT);
        parent = (Component) Executions.getCurrent().getAttribute(Constants.PARENT);
        
        //Event listener to trigger params creation
        this.getSelf().getParent().addEventListener("onCreateParams", addParamsListener);
    }

    /**Gets input parameters for Roxie query
     * @throws Exception
     */
    private void constructInputParameters() throws Exception {
    	Map<String,Set<String>> paramValues = null;
      	 Set<String> inputParameter =  null;
      	 
		if (chartData.getInputParams() == null) {
			if(chartData.isGenericQuery()){
				QuerySchema querySchema = hpccQueryService.getQuerySchema(chartData.getFiles().iterator().next(),
						chartData.getHpccConnection(), chartData.isGenericQuery(),
						chartData.getInputParamQuery());
				inputParameter = querySchema.getInputParams().keySet();
				paramValues = querySchema.getInputParams();
			}else{
				inputParameter = hpccQueryService.getInputParameters(chartData
						.getFiles().iterator().next(),chartData.getHpccConnection(),
						chartData.isGenericQuery(),chartData.getInputParamQuery());
				
				paramValues = hpccQueryService.getInputParamDistinctValues(
						chartData.getFiles().iterator().next(), inputParameter,
						chartData.getHpccConnection(),chartData.isGenericQuery(),
						chartData.getInputParamQuery());
				
			}
				

			Map<String, String> parameterMap = new LinkedHashMap<String, String>();
			for (String param : inputParameter) {
				InputListitem listitem = new InputListitem(param,
						paramValues.get(param), String.valueOf(portlet.getId()));
				inputParams.appendChild(listitem);
				parameterMap.put(param, "");
			}

			InputParams inputParameters = new InputParams(parameterMap);
			List<InputParams> paramsList = new ArrayList<InputParams>();
			paramsList.add(inputParameters);

			chartData.setInputParams(paramsList);

		}else{//Retrieving from DB
           paramValues = hpccQueryService.getInputParamDistinctValues(
                    chartData.getFiles().iterator().next(),
                    chartData.getInputParams().iterator().next().getParams().keySet(),
                    chartData.getHpccConnection(),chartData.isGenericQuery(),
                    chartData.getInputParamQuery());
            
            for (InputParams inputParam : chartData.getInputParams()) {
                for (Entry<String,String> param : inputParam.getParams().entrySet() ) {
                    InputListitem listitem = new InputListitem(param.getKey(), 
                            paramValues.get(param.getKey()), String.valueOf(portlet.getId()));
                    if(param.getValue() != null 
                            && !param.getValue().isEmpty()) {
                        listitem.setInputValue(param.getValue());
                    }
                    inputParams.appendChild(listitem);
                }
            }
            
        }
        
    }
    
    @Listen("onClick=#saveParams")
    public void onSaveParameter(Event event){
        Map<String,String> inputs = new HashMap<String, String>();
        
        //multipleValues object was created for translating multiple value selection into List of Maps
        // This is currently not being used
        List<Object> multipleValues = new ArrayList<Object>();
        
        String multipleValueParamName = null;
        
        for ( Component comp : inputParams.getChildren()) {
            if(comp instanceof InputListitem) {
                InputListitem listitem = (InputListitem) comp;
                inputs.put(listitem.getParamName(), listitem.getInputValue());
            }
        }
        
        InputParams inputParameters;
        List<InputParams> paramsList = new ArrayList<InputParams>();
        if(multipleValues.isEmpty()) {
            inputParameters = new InputParams(inputs);
            paramsList.add(inputParameters);
        } else {
            Map<String,String> multiInputs;
            for (Object object : multipleValues) {
                multiInputs = new HashMap<String, String>();
                multiInputs.putAll(inputs);
                multiInputs.put(multipleValueParamName, object.toString());
                inputParameters = new InputParams(multiInputs);
                paramsList.add(inputParameters);
            }
        }
        
        chartData.setInputParams(paramsList);
        
        Clients.showBusy(busyComponent, "Retriving data");
        Events.echoEvent(new Event(Constants.DRAW_CHART_EVENT, parent));
    }
}
