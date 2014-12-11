package org.hpccsystems.dashboard.hipie.service.impl;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpccsystems.dashboard.entity.Portlet;
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
		//TODO:Need to iterate widget list create rawdataset
		PluginUtil.updateRawDataset(composition,widget.getChartData().getFiles().get(0),hpccConnection);
		ContractInstance pluginContract = PluginUtil.createPlugin(label,composition,widget);		
		
		System.out.println("count 1-->"+composition.countContractInstances());
		System.out.println(composition.getContractInstances());
		ContractInstance datasource=composition.getContractInstanceByName(HIPIE_RAW_DATASET);
		pluginContract.addPrecursor(datasource);	
		
		System.out.println("count 2-->"+composition.countContractInstances());
		
		System.out.println(composition.getContractInstances());

		
		composition = HipieSingleton.getHipie().saveCompositionAs(authenticationService.getUserCredential().getUserId(), composition,
				 compName + ".cmp");
		return composition;
	}
	
	@Override
	public Composition updateComposition(String userId, String compName) {
		return null;
	}

	@Override
	public CompositionInstance runComposition(String userId, String compName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteComposition(String userId, String compName) {
		// TODO Auto-generated method stub
		return false;
	}

}
