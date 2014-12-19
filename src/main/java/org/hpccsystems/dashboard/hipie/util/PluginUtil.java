package org.hpccsystems.dashboard.hipie.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.FieldInstance;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.OutputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.hipie.HipieSingleton;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.zkplus.spring.SpringUtil;
public class PluginUtil {

	private static final  Log LOG = LogFactory.getLog(PluginUtil.class);
	

	private static final String CHART_2D = "2DCHART";
	private static final String HIPIE_RAW_DATASET = "RawDataset";
	private static final String USER_CREDENTIAL = "userCredential";
	
		
	/**
	 * @param compName
	 * @param composition
	 * @param portletList
	 * @throws Exception
	 */
	public static ContractInstance createPlugin(String compName,Composition composition,List<Portlet> portletList) throws Exception {	
		
		Contract contract = new Contract();
		HIPIEService hipieService = HipieSingleton.getHipie();
		contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());		
		contract.setLabel(compName);
		compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
		contract.setName(compName);
		AuthenticationService authenticationService =(AuthenticationService) SpringUtil.getBean("authenticationService");
		contract.setAuthor(authenticationService.getUserCredential().getUserId());		
		contract.setDescription("Dashboard charts integrated with Hipie/Marshaller");
		contract.setProp(Contract.CATEGORY, "VISUALIZE");
		contract.setProp(Contract.VERSION, "0.1");
		
		InputElement input = new InputElement();
		input.setName("dsInput");		
		//TODO:need to change for roxie query
		input.setType(InputElement.TYPE_DATASET);	
		input.addOption(new ElementOption(Element.MAPBYNAME));
		
		
		//Set Fields for INPUT Element(measure and attribute fields)
		createInputFields(input,portletList);
		
        contract.getInputElements().add(input);

		
		OutputElement output = new OutputElement();
		output.setName("dsOutput");
		output.setType(OutputElement.TYPE_DATASET);
		output.setBase("dsInput");
		output.addOption(new ElementOption("WUID"));
		contract.getOutputElements().add(output);

		VisualElement visualization = new VisualElement();
        visualization.setName(compName);
        visualization.setType(VisualElement.VISUALIZE);
        //TODO:set title for visualization
        
        createVisualElements(visualization,portletList,output);
	    
        contract.getVisualElements().add(visualization);
        
        contract = hipieService.saveContractAs(authenticationService.getUserCredential().getUserId(), contract,contract.getName());
        
        ContractInstance pluginInstance = contract.createContractInstance();
      
        //Mapping the selected attribute/measure to the visualization element 
        mapChartFields(pluginInstance,portletList);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Visuslisation plugin - " + pluginInstance.toCompositionString());
		}
		return  pluginInstance;

	}
	
	/**
	 * @param pluginInstance
	 * @param portletList
	 * Maps the selected attribute/measure of chart to plugin's visualization element 
	 */
	private static void mapChartFields(ContractInstance pluginInstance,List<Portlet> portletList) {
		
		int count = 1;
		StringBuilder builder = null;
		XYChartData chartData = null;
		for(Portlet portlet :portletList){
			chartData = (XYChartData)portlet.getChartData();
			builder = new StringBuilder();
			builder.append("Attribute").append("_chart").append(count);			
			pluginInstance.setProperty(builder.toString(), chartData.getAttribute().getColumn());			
			
        	for(Measure measure :chartData.getMeasures()){
        		builder = new StringBuilder();
            	builder.append("Measure").append(count).append("_chart").append(count);
            	pluginInstance.setProperty(builder.toString(),measure.getColumn());
        	}
        	count++;			
		}
		
	}

	/**
	 * @param visualization
	 * @param portletList
	 * Creates visualization element for each chart/portlet
	 */
	private static void createVisualElements(VisualElement visualization,
			List<Portlet> portletList,OutputElement output) {
		
		StringBuilder builder = null;
		StringBuilder meaureLabels = null;
		XYChartData chartData = null;
		VisualElement ve = null;
		ChartService chartService = (ChartService)SpringUtil.getBean("chartService");
		ChartDetails chartInfo= null;
		int count = 1;
		for(Portlet portlet :portletList){
			chartInfo = chartService.getCharts().get(portlet.getChartType());
			chartData = (XYChartData) portlet.getChartData();
			ve=new VisualElement();
		    //TODO:Need to set chart type using Hipie's 'Element' class
		    if(Constants.PIE_CHART.equals(chartInfo.getName())){
		    	 ve.setType("PIE");
		    	 ve.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,"C3_PIE")));
		    }else if(Constants.BAR_CHART.equals(chartInfo.getName()))  {
		    	 ve.setType("BAR");
		    	 ve.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,"C3_BAR")));
		    }else if(Constants.LINE_CHART.equals(chartInfo.getName())){
		    	 ve.setType("LINE");
		    	 ve.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,"C3_LINE")));
		    }
	        ve.setName(StringUtils.remove(chartInfo.getName(), " "));
	        ve.setBasis(output);
	        
	        RecordInstance ri=new RecordInstance();
	        ve.setBasisQualifier(ri);
	        
	        //Attribute settings
	        builder = new StringBuilder();
			builder.append("Attribute").append("_chart").append(count);
	        ri.add(new FieldInstance(null,builder.toString()));
	        ve.addOption(new ElementOption(VisualElement.LABEL,new FieldInstance(null,builder.toString())));
	        
	        //Measures settings
	        meaureLabels = new StringBuilder();
	        for(Measure measure :chartData.getMeasures()){
	        	builder = new StringBuilder();
	        	builder.append("Measure").append(count).append("_chart").append(count);
	        	meaureLabels.append(builder.toString()).append(",");
	        	ri.add(new FieldInstance((measure.getAggregateFunction() != null) ? 
	        			measure.getAggregateFunction().toUpperCase() : null,builder.toString()));
	        
	        }
	       
			//TODO:check how behaves for multiple measures
	        meaureLabels.deleteCharAt(meaureLabels.length()-1);
	        ve.addOption(new ElementOption(VisualElement.WEIGHT,new FieldInstance(null,meaureLabels.toString())));
	        
	        
	        //Setting Tittle for chart
	        ve.addOption(new ElementOption(VisualElement.TITLE,new FieldInstance(null,chartInfo.getName())));
	        //TODO:need to look way to set more than one measure
	              
	        visualization.addChildElement(ve);
	        count++;
		}
		
	}

	/**
	 * @param input
	 * @param portletList
	 * Set Fields for INPUT Element(measure and attribute fields)
	 */
	private static void createInputFields(InputElement input,List<Portlet> portletList) {
		
		int count = 1;
		StringBuilder builder = null;
		XYChartData chartData = null;
		for(Portlet portlet :portletList){
			chartData = (XYChartData)portlet.getChartData();
			builder = new StringBuilder();
			builder.append("Attribute").append("_chart").append(count);
			InputElement attribute=new InputElement();
	        attribute.setName(builder.toString());
	        attribute.addOption(new ElementOption(Element.LABEL,new FieldInstance(null,chartData.getAttribute().getColumn())));
	        attribute.setType(InputElement.TYPE_FIELD);
	        input.addChildElement(attribute);
	        
	        InputElement measure = null;
	        for(Measure chartMeasure : chartData.getMeasures()){
	        	builder = new StringBuilder();
	        	builder.append("Measure").append(count).append("_chart").append(count);
	        	measure = new InputElement();
	            measure.setName(builder.toString());
	            measure.addOption(new ElementOption(Element.LABEL,new FieldInstance(null,chartMeasure.getColumn())));
	            measure.setType(InputElement.TYPE_FIELD);
	            input.addChildElement(measure);
	        }
	        count++;	        
		}
		
        	
	}

	/**
	 * @param composition
	 * @param filename
	 * @param hpccConnection
	 * @throws Exception
	 */
	public static void updateRawDataset(Composition composition,String filename,HPCCConnection hpccConnection) throws Exception {
		ContractInstance rawDatasetContract = composition.getContractInstanceByName(HIPIE_RAW_DATASET);
		rawDatasetContract.setFileName("");
		Map<String,String[]> paramMap = new HashMap<String, String[]>();
		paramMap.put("LogicalFilename", new String[]{filename});
		paramMap.put("Method", new String[]{"THOR"});
		
		String fieldseparator=null;
		RecordInstance recordInstance;
		recordInstance = hpccConnection.getHipieHPCCConnection().getDatasetFields(filename, fieldseparator);
		rawDatasetContract.setProperty("Structure", recordInstance);		
		rawDatasetContract.setAllProperties(paramMap);
		//TODO:Need to set FieldSeparator & other info for NON-THOR files
			
	}
}
