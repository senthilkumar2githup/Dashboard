package org.hpccsystems.dashboard.hipie.servic;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.Process;
import org.hpccsystems.dashboard.hipie.util.HPCCConnection;

public interface CompositionService {

	Composition createComposition(String compName,
			HPCCConnection hpccConnection, Portlet widget) throws Exception;

	Composition updateComposition(String userId, String compName);

	CompositionInstance runComposition(Composition composition,org.hpcc.HIPIE.utils.HPCCConnection hpccConnection) throws Exception;

	boolean deleteComposition(String userId, String compName);
	
	Process getProcess(Composition composition) throws Exception;

}
