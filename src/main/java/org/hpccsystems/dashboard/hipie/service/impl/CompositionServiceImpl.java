package org.hpccsystems.dashboard.hipie.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
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
	public Composition createComposition(String compName,HPCCConnection hpccConnection, List<Portlet> portletList)
			throws Exception {
		String label = compName;
		HIPIEService hipieService = HipieSingleton.getHipie();
		Composition composition = hipieService.getCompositionTemplate(
				authenticationService.getUserCredential().getUserId(),"BasicTemplate");
		composition = new Composition(composition);
		
		composition.setLabel(label);
		compName = label.replaceAll("[^a-zA-Z0-9]+", "");
		composition.setName(compName);
		PluginUtil.updateRawDataset(composition, "~" + portletList.get(0).getChartData().getFiles().get(0),hpccConnection);
		
		ContractInstance pluginContract = PluginUtil.createPlugin(label,composition,portletList);		
		//refreshes the plugins
        hipieService.refreshData();
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
		if(LOG.isDebugEnabled()) {
			LOG.debug("Running composition - \n" + composition.toString());
		}
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
