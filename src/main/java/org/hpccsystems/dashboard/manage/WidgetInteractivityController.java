package org.hpccsystems.dashboard.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.FLOW;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.manage.widget.WidgetConfigurationController;
import org.hpccsystems.dashboard.service.CompositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Anchorchildren;
import org.zkoss.zul.Anchorlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class WidgetInteractivityController extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetInteractivityController.class);
    
    @WireVariable
    private CompositionService compositionService;
    
    private Interactivity interactivity;
    
    @Wire
    private Anchorlayout interactivityList;
   
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
       /* compositionService.extractLiveWidgets(dashboard, user);
        
        List<LiveWidget> widgets = new ArrayList<LiveWidget>() {
        	{
        		LiveWidget liveWidget = new LiveWidget();
        		liveWidget.setWidgetLabel("Chart 1");
        		liveWidget.setAttributeColumn("product_line");
        		add(liveWidget);
        		
        		liveWidget = new LiveWidget();
        		liveWidget.setWidgetLabel("Chart 1");
        		liveWidget.setAttributeColumn("product_line");
        		add(liveWidget);
        		
        		liveWidget = new LiveWidget();
        		liveWidget.setWidgetLabel("Chart 1");
        		liveWidget.setAttributeColumn("product_line");
        		add(liveWidget);
        	}
        };
        
       // interactivity = (Interactivity) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        
      
        for (Entry<String, ChartConfiguration> entry : Constants.CHART_CONFIGURATIONS.entrySet()) {
        	
        	Anchorchildren anchorChildren = new Anchorchildren();
        	Vbox vbox = new Vbox();
        	Label label = new Label();
        	label.setValue(entry.getValue().getName()+" Chart");
        	Image img = new Image();
        	img.setAttribute("config", entry.getValue());
        	img.setSrc(entry.getValue().getStaticImage());
        	img.setHeight("100px");
        	img.setWidth("200px");
        	img.setStyle("cursor:pointer");
			img.addEventListener(Events.ON_CLICK, editChartPanel);
        	anchorChildren.setParent(chartList);
        	label.setParent(vbox);
        	img.setParent(vbox);
        	vbox.setParent(anchorChildren);
        	
        }*/
    }
}
