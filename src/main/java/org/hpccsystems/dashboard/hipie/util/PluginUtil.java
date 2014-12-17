package org.hpccsystems.dashboard.hipie.util;

import java.util.HashMap;
import java.util.Map;

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
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.hipie.HipieSingleton;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zkplus.spring.SpringUtil;

public class PluginUtil {

	private static AuthenticationService authenticationService;
	private static ChartService chartService;
	
	@Autowired
	public static ChartService getChartService() {
		return chartService;
	}

	private static final String CHART_2D = "2DCHART";
	private static final String HIPIE_RAW_DATASET = "RawDataset";
	private static final String USER_CREDENTIAL = "userCredential";
	
	@Autowired
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
		
	/**
	 * @param compName
	 * @param composition
	 * @param widget
	 * @throws Exception
	 */
	public static ContractInstance createPlugin(String compName,Composition composition,Portlet widget) throws Exception {	
		
		Contract contract = new Contract();
		HIPIEService hipieService = HipieSingleton.getHipie();
		contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());		
		contract.setLabel(compName);
		compName = compName.replaceAll("[^a-zA-Z0-9]+", "");
		contract.setName(compName);
		authenticationService =(AuthenticationService) SpringUtil.getBean("authenticationService");
		contract.setAuthor(authenticationService.getUserCredential().getUserId());
		chartService = (ChartService)SpringUtil.getBean("chartService");
		ChartDetails chartInfo=chartService.getCharts().get(widget.getChartType());
		contract.setDescription(chartInfo.getDescription());
		
		InputElement input = new InputElement();
		input.setName("dsInput");		
		//TODO:need to change for roxie query
		input.setType(InputElement.TYPE_DATASET);
		//TODO:set measure and attribute
		/*input.addOption(new ElementOption(Element.LABEL,new FieldInstance(null,"attribute")));
		input.addOption(new ElementOption(Element.LABEL,new FieldInstance(null,"measure")));*/
		
		input.addOption(new ElementOption(Element.MAPBYNAME));
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
        //visualization.addOption(new ElementOption(VisualElement.TITLE,new FieldInstance(null,compName)));
        
	    VisualElement ve=new VisualElement();
	    //TODO:Need to set chart type using Hipie's 'Element' class
        ve.setType("PIE");
        ve.setName(chartInfo.getName());
        ve.setBasis(output);
        
        RecordInstance ri=new RecordInstance();
        ri.add(new FieldInstance(null,((XYChartData)widget.getChartData()).getAttribute().getColumn()));
        ri.add(new FieldInstance(null, ((XYChartData)widget.getChartData()).getMeasures().get(0).getColumn()));
        ve.setBasisQualifier(ri);
        
        ve.addOption(new ElementOption(VisualElement.TITLE,new FieldInstance(null,chartInfo.getName())));
        ve.addOption(new ElementOption(VisualElement.WEIGHT,new FieldInstance(null,((XYChartData)widget.getChartData()).getMeasures().get(0).getColumn())));
        ve.addOption(new ElementOption(VisualElement.LABEL,new FieldInstance(null,((XYChartData)widget.getChartData()).getAttribute().getColumn())));
        //chartType, uses addCustomOption instead of addOption       
        ve.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,"C3_PIE")));
        
        visualization.addChildElement(ve);
        contract.getVisualElements().add(visualization);
        contract = hipieService.saveContractAs(authenticationService.getUserCredential().getUserId(), contract,contract.getName());
		return  contract.createContractInstance();

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
