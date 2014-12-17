package org.hpccsystems.dashboard.hipie.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.Process;
import org.hpccsystems.dashboard.hipie.HipieSingleton;
import org.hpccsystems.dashboard.hipie.servic.CompositionService;
import org.hpccsystems.dashboard.hipie.util.HPCCConnection;
import org.hpccsystems.dashboard.hipie.util.PluginUtil;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service("compositionService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompositionServiceImpl implements CompositionService {

	private static final  Log LOG = LogFactory.getLog(CompositionServiceImpl.class);
	private static final String DASHBOARD_VISUALIZATION = "DashboardVisualization";


	private static final String HIPIE_RAW_DATASET  = "RawDataset";

	
	private AuthenticationService authenticationService;
	
	@Autowired
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public Composition createComposition(String compName,HPCCConnection hpccConnection,Portlet widget)
			throws Exception {
		String label = compName;
		HIPIEService hipieService = HipieSingleton.getHipie();
		Composition composition = hipieService.getCompositionTemplate(
				authenticationService.getUserCredential().getUserId(),"BasicTemplate");
		composition.setLabel(label);
		compName = label.replaceAll("[^a-zA-Z0-9]+", "");
		composition.setName(compName);
		PluginUtil.updateRawDataset(composition, "~" + widget.getChartData().getFiles().get(0),hpccConnection);
		
		/*Contract contract = HipieSingleton.getHipie().getContract(authenticationService.getUserCredential().getUserId(),
				DASHBOARD_VISUALIZATION);
		ContractInstance visualisationPlugin = contract.createContractInstance();
		visualisationPlugin.setProperty("attribute", ((XYChartData)widget.getChartData()).getAttribute().getColumn());
		visualisationPlugin.setProperty("measure", ((XYChartData)widget.getChartData()).getMeasures().get(0).getColumn());*/
		
		ContractInstance pluginContract = PluginUtil.createPlugin(label,composition,widget);		
		
		ContractInstance datasource=composition.getContractInstanceByName(HIPIE_RAW_DATASET);
		pluginContract.addPrecursor(datasource);	
				
		composition = HipieSingleton.getHipie().saveCompositionAs(authenticationService.getUserCredential().getUserId(), composition,
				 compName + ".cmp");
		return composition;
	}
	
	@Override
	public Composition updateComposition(String userId, String compName) {
		return null;
	}

	@Override
	public CompositionInstance runComposition(Composition composition,org.hpcc.HIPIE.utils.HPCCConnection hpccConnection) throws Exception {
		return HipieSingleton.getHipie().runComposition(composition, hpccConnection,
				authenticationService.getUserCredential().getUserId());
	}

	@Override
	public boolean deleteComposition(String userId, String compName) {
		// TODO Auto-generated method stub
		return false;
	}
	
	 @Override
	public Process getProcess(Composition composition) throws Exception {
		Process process = null;

		String name = composition.getLabel();
		if (name == null || name.isEmpty()) {
			name = composition.getName();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("composition Name ---->" + name);
		}

		CompositionInstance latestInstance = composition.getMostRecentInstance(
				authenticationService.getUserCredential().getUserId(), true);
		
		if(latestInstance == null){
			latestInstance = runComposition(composition,new HPCCConnection().getHipieHPCCConnection());
		} 
		//TODO:check for composition status complete/failed

		process = new Process(name, latestInstance);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Process -->:" + process);
		}

		return process;
	}

}
