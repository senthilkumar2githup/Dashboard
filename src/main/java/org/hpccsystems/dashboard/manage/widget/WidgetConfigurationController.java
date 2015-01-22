package org.hpccsystems.dashboard.manage.widget;

import java.util.HashMap;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.FLOW;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.hpccsystems.dashboard.util.DashboardExecutorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Include;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class WidgetConfigurationController extends SelectorComposer<Component> implements EventListener<Event>{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetConfigurationController.class);

    
    @Wire
    private Include holder;    
    @WireVariable
    private Desktop desktop;
    @WireVariable
    private CompositionService compositionService;
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private AuthenticationService authenticationService;
    
    private WidgetConfiguration configuration;
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        configuration = 
                (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        configuration.setHolder(holder);
        
        holder.setDynamicProperty(Constants.WIDGET_CONFIG, configuration);
        
        if(FLOW.NEW.equals(configuration.getFlowType())){
            holder.setSrc("widget/chartList.zul");
        }else if(FLOW.EDIT.equals(configuration.getFlowType())){
            holder.setSrc(configuration.getWidget().getChartConfiguration().getEditLayout());
        }
        
        holder.addEventListener(WidgetConfiguration.ON_CHART_TYPE_SELECT, event -> {
                holder.setSrc("widget/fileBrowser.zul");
        });
        
        holder.addEventListener(WidgetConfiguration.ON_FILE_SELECT, event -> {
            holder.setSrc(configuration.getWidget().getChartConfiguration().getEditLayout());
        });
    }
    
    
    /**
     * Saves/Updates Composition
     * Runs the composition
     * Pushes the preview chart to dashboard view
     * @throws Exception 
     */
    @Listen("onClick = #configOkButton")
    public void onClickOk() throws Exception {
        if (configuration.getWidget() == null || !configuration.getWidget().isConfigured()) {
            Clients.showNotification(Labels.getLabel("widgetNotConfigured"), 
                    Clients.NOTIFICATION_TYPE_ERROR,
                    configuration.getChartDiv(), "middle_center", 5000, true);
            return;
        }
        
        String userId = authenticationService.getUserCredential().getId();
       
        if (configuration.getDashboard().getCompositionName() == null) { //Dashboard with no chart
            compositionService.createComposition(configuration.getDashboard(), configuration.getWidget(), userId);
        } else if(FLOW.NEW.equals(configuration.getFlowType())) { //Dashboard with charts,and user adding new chart
            compositionService.addCompositionChart(configuration.getDashboard(), configuration.getWidget(), userId);
        } else if(FLOW.EDIT.equals(configuration.getFlowType())){//Dashboard with charts,and user edits existing chart
            compositionService.editCompositionChart(configuration.getDashboard(), configuration.getWidget(), userId);
        }
        
     // Run composition in separate thread
        desktop.enableServerPush(true);
        Runnable runComposition = () -> {
            try {
                compositionService.runComposition(configuration.getDashboard(), userId);
                Executions.schedule(desktop, this, new Event("OnRunCompositionCompleted",null,configuration.getDashboard().getCompositionName()));
            } catch (Exception e) {
                Executions.schedule(desktop, this, new Event("OnRunCompositionFailed", null, configuration.getDashboard().getCompositionName()));
                LOGGER.error(Constants.EXCEPTION, e);
            }
        };
        DashboardExecutorHolder.getExecutor().execute(runComposition);
        
        dashboardService.updateDashboard(configuration.getDashboard());
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Composition {}, being Run in the background", configuration.getDashboard().getCompositionName());
        }
        
        this.getSelf().detach();
       drawChart();
    }

    /**
     * Renders chart in dashboard container
     */
	private void drawChart() {
	    if(FLOW.EDIT.equals(configuration.getFlowType())){
	        Clients.evalJavaScript("injectPreviewChart('"+FLOW.EDIT.toString()+"')");
        }else{
            Clients.evalJavaScript("injectPreviewChart('"+FLOW.NEW.toString()+"')");
        }
	    
	}
	
	@Override
    public void onEvent(Event arg0) throws Exception {
        
        HashMap<String,String> paramMap=new HashMap<String,String>();
	    if ("OnRunCompositionCompleted".equals(arg0.getName())) {
            LOGGER.debug("Composition {}, Run sucessfully", arg0.getData());
            paramMap.put(Constants.SUCCESS, "Composition "+arg0.getData()+" Run sucessfully");
        } else {
            LOGGER.debug("Composition {}, Failed to Run", arg0.getData());
            paramMap.put(Constants.FAIL, "Composition "+arg0.getData()+" Failed to Run...See the log for more details.");
        }
	    //Post event to the dashboard controller for showing the notification to the user.
	    Events.postEvent(Constants.ON_RUN_COMPOSITION, configuration.getChartDiv(), paramMap);
    }

}
