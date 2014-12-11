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
		//if(chartInfo.getName().equals("PIE"))
		contract.setDescription("PIE");
		
		InputElement input = new InputElement();
		input.setName("dsInput");
		//TODO:need to change for roxie query
		input.setType(InputElement.TYPE_DATASET);
		//TODO:set measure and attribute
		
		input.addOption(new ElementOption(Element.MAPBYNAME));
		contract.getInputElements().add(input);
		
		OutputElement output = new OutputElement();
		output.setName("dsOutput");
		output.setType(OutputElement.TYPE_DATASET);
		output.setBase("dsInput");
		output.addOption(new ElementOption("WUID"));
		contract.getOutputElements().add(output);

		
        VisualElement visualization = new VisualElement();
        visualization.setName("PIE");
        visualization.setType(VisualElement.VISUALIZE);
        
        VisualElement visualElement = new VisualElement();
        visualElement.setType(CHART_2D);
        visualElement.setName("PIE");
        visualElement.setBasis(output);
        
        RecordInstance ri=new RecordInstance();
        ri.add(new FieldInstance(null,((XYChartData)widget.getChartData()).getAttribute().getColumn()));
        //TODO:Check how to set aggregate function
        ri.add(new FieldInstance(null,((XYChartData)widget.getChartData()).getMeasures().get(0).getColumn()));        
        visualElement.setBasisQualifier(ri);
        
		visualElement.addOption(new ElementOption("LABEL", new FieldInstance(
				null, ((XYChartData)widget.getChartData()).getAttribute().getColumn())));
		visualElement.addOption(new ElementOption("WEIGHT", new FieldInstance(
				null, ((XYChartData)widget.getChartData()).getMeasures().get(0).getColumn())));
        visualElement.addCustomOption(new ElementOption("_chartType",new FieldInstance(null,"PIE")));
        visualization.addChildElement(visualElement);
        
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
