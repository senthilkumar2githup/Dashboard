package org.hpccsystems.dashboard.hipie;

import javax.servlet.ServletContext;

import org.hpcc.HIPIE.HIPIEFactory;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.utils.ErrorBlock;
import org.hpcc.HIPIE.web.CommonContext;
import org.hpccsystems.dashboard.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppInit;

public class HipieSingleton implements WebAppInit {
    private static final String HIPIE_CONFIG_FILE = "HipieConfigLocation";
    private static ServletContext context = null;
    private static String absolutePath="";
    private static String repositoryConfigFile = "";
    private static String HIPIE="HIPIE";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HipieSingleton.class);
    
    @Override
    public void init(WebApp wapp) throws Exception {
        context = wapp.getServletContext();
        System.out.println("context 1 -->"+context);
         try {

            String realPath = context.getRealPath("");  
            context.setAttribute("absolutePath", realPath);
            HipieSingleton.absolutePath=realPath;      
            System.out.println("--->"+context.getInitParameter(HIPIE_CONFIG_FILE));
            if (context.getInitParameter(HIPIE_CONFIG_FILE) != null){
            	System.out.println("here -->");
                repositoryConfigFile=context.getInitParameter(HIPIE_CONFIG_FILE);
            }
          } catch (Exception e) {
                LOGGER.error(Constants.EXCEPTION, e);
            }
            loadHIPIE(context);
    }
    
    private static ErrorBlock loadHIPIE(ServletContext ctx) {
        ErrorBlock er=new ErrorBlock();
        try {
        	 System.out.println("========" + absolutePath + repositoryConfigFile);
            LOGGER.debug("absolutePath+repositoryConfigFile-->" + absolutePath + repositoryConfigFile);
            HIPIEService rm=HIPIEFactory.getInstance().getService(absolutePath + repositoryConfigFile);
            ctx.setAttribute(HIPIE, rm);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
        return er;

    }
    
    public static HIPIEService getHipie() {
        HIPIEService cb=null;
        try {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("CommonContext.HIPIE --> " + CommonContext.HIPIE);
            }
            System.out.println("context -->"+context);
            System.out.println("CommonContext.HIPIE -->"+CommonContext.HIPIE);
            if (context.getAttribute(CommonContext.HIPIE) == null) {
                loadHIPIE(context);
            }
            cb=(HIPIEService) context.getAttribute(CommonContext.HIPIE);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
        return cb;  
    }
}
