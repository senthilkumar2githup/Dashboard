package org.hpccsystems.dashboard.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.Composition;
import org.hpcc.HIPIE.CompositionInstance;
import org.hpcc.HIPIE.Contract;
import org.hpcc.HIPIE.ContractInstance;
import org.hpcc.HIPIE.HIPIEService;
import org.hpcc.HIPIE.dude.Element;
import org.hpcc.HIPIE.dude.ElementOption;
import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.OutputElement;
import org.hpcc.HIPIE.dude.RecordInstance;
import org.hpcc.HIPIE.dude.VisualElement;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.manage.Interactivity;
import org.hpccsystems.dashboard.manage.LiveWidget;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.hpccsystems.dashboard.util.HipieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.zkplus.spring.SpringUtil;

@Service("compositionService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompositionServiceImpl implements CompositionService{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositionServiceImpl.class);
    private static final String BASIC_TEMPLATE = "BasicTemplate";
    private static final String HIPIE_RAW_DATASET  = "RawDataset";
    
    private DashboardService dashboardService;
    
    @Autowired
    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public void createComposition(Dashboard dashboard, Widget widget,String user) throws Exception {
    HIPIEService hipieService = HipieSingleton.getHipie();
    Composition composition;
    try {
        composition = hipieService.getCompositionTemplate(user,BASIC_TEMPLATE);
        composition.setLabel(dashboard.getName());
        String compName = dashboard.getName().replaceAll("[^a-zA-Z0-9]+", "");
        composition.setName(compName);
        
        updateRawDataset(composition, "~" + widget.getLogicalFile(),dashboard.getHpccConnection());
        
        ContractInstance contractInstance = createVisualContractInstance(compName,widget);   
        
        //refreshes the plugins
        hipieService.refreshData();
        ContractInstance datasource=composition.getContractInstanceByName(HIPIE_RAW_DATASET);
        contractInstance.addPrecursor(datasource); 
        
        composition = HipieSingleton.getHipie().saveCompositionAs(user, composition,compName + ".cmp");
        dashboard.setCompositionName(composition.getCanonicalName());   
    } catch (Exception e) {
        LOGGER.error(Constants.EXCEPTION, e);
        throw e;
    }
    }
    
    @Override
    public void addCompositionChart(Dashboard dashboard, Widget widget,String user) {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        try {           
            composition = hipieService.getComposition(user, dashboard.getCompositionName());
            if(checkFileExistence(composition,widget.getLogicalFile())){
                appendOnVisualElement(composition,widget,user); 
            }else{
                addOnRawdataset(composition,"~" + widget.getLogicalFile(),widget,dashboard.getHpccConnection(),user);
            }
            
            HipieSingleton.getHipie().saveComposition(user, composition);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
        }
    }
    
    private boolean checkFileExistence(Composition composition, String logicalFile) {
        try {
            List<String> files = new ArrayList<String>();
            Map<String, ContractInstance> contractInstances = composition.getContractInstances();
          
            contractInstances.forEach((key, instance) ->{
                if(instance.getProperty("LogicalFilename")!= null){
                    files.add(instance.getProperty("LogicalFilename"));
                }
            });
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("files --->"+files);
            }
            
           return (files.contains("~"+logicalFile) ? true : false);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION,e);
            return false;
        }
    }

    /**
     * @param composition
     * @param fileName
     * @param widget
     * @param hpcc
     * @param user
     * @throws Exception
     * Adds additional input,output and rawdataset to the plugin
     */
    private void addOnRawdataset(Composition composition, String fileName,Widget widget,HPCCConnection hpcc,String user) throws Exception {
        
        Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
        HIPIEService hipieService=HipieSingleton.getHipie();
        
        //Adding additional input
        InputElement input=new InputElement();
        //setting Input Element name as dsInput2
        input.setName("dsInput"+(contract.getInputElements().size()+1));
        input.setType(InputElement.TYPE_DATASET);
        input.addOption(new ElementOption(Element.MAPBYNAME));
        contract.getInputElements().add(input);        
        
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );

        //Adding additional output
        OutputElement output=new OutputElement();
        //setting Output Element name as dsOutput2
        output.setName("dsOutput"+(contract.getOutputElements().size()+1));
        output.setType(OutputElement.TYPE_DATASET);
        output.setBase(input.getName());
        output.addOption(new ElementOption("WUID"));
        contract.getOutputElements().add(output);
        
        VisualElement visualElement = widget.generateVisualElement();
        visualElement.setBasis(output);

        VisualElement visualization=contract.getVisualElements().iterator().next();
        visualization.addChildElement(visualElement);
        
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        hipieService.saveContract(user, contract);
        
        hipieService.refreshData();
        
        
        ContractInstance contractInstance = composition.getContractInstanceByName(composition.getName());
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue)
        );
        //Adding additional Rawdataset
        ContractInstance datasource2 = cloneRawdataset(composition,fileName,hpcc);        
        
        //here "dsOutput" -> Rawdataset's output
        contractInstance.addPrecursor(datasource2,"dsOutput",input.getName()); 
    }


    private ContractInstance cloneRawdataset(Composition composition,
            String fileName, HPCCConnection hpcc) throws Exception {
        ContractInstance datasource1 = composition.getContractInstanceByName(HIPIE_RAW_DATASET);
        ContractInstance datasource2 = new ContractInstance(datasource1.getContract());
        datasource2.setFileName("");
        Map<String,String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("LogicalFilename", new String[]{fileName});
        paramMap.put("Method", new String[]{"THOR"});
        String fieldseparator=null;
        RecordInstance recordInstance;
        recordInstance = hpcc.getDatasetFields(fileName, fieldseparator);
        datasource2.setProperty("Structure", recordInstance);        
        datasource2.setAllProperties(paramMap);
       
        return  datasource2;
    }


    @Override
    public CompositionInstance runComposition(Dashboard dashboard,String user) throws Exception {
        HIPIEService hipieService=HipieSingleton.getHipie();
        CompositionInstance compositionInstance = null;
        try {
           Composition comp = hipieService.getComposition(user,dashboard.getCompositionName());
           compositionInstance = hipieService.runComposition(comp, dashboard.getHpccConnection(), user);
        } catch (Exception e) {
            LOGGER.error(Constants.EXCEPTION, e);
            throw e;
        }
        return compositionInstance;
    }
    
    private void updateRawDataset(Composition composition,String filename,HPCCConnection hpccConnection) throws Exception {
        ContractInstance rawDatasetContract = composition.getContractInstanceByName(HIPIE_RAW_DATASET);
        rawDatasetContract.setFileName("");
        Map<String,String[]> paramMap = new HashMap<String, String[]>();
        paramMap.put("LogicalFilename", new String[]{filename});
        paramMap.put("Method", new String[]{"THOR"});
        String fieldseparator=null;
        RecordInstance recordInstance;
        recordInstance = hpccConnection.getDatasetFields(filename, fieldseparator);
        rawDatasetContract.setProperty("Structure", recordInstance);        
        rawDatasetContract.setAllProperties(paramMap);
        //TODO:Need to set FieldSeparator & other info for NON-THOR files
    }
    
    /**
     * @param composition
     * @param widget
     * @param user
     * @throws Exception
     * Adds additional visual element to the plugin file,with single(existing) input/output
     */
    private void appendOnVisualElement(Composition composition,Widget widget,String user) throws Exception {
        
        HIPIEService hipieService=HipieSingleton.getHipie();
        Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
        
        Element input=contract.getInputElements().iterator().next();
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        
        VisualElement visualization=contract.getVisualElements().iterator().next();
        VisualElement visualElement = widget.generateVisualElement();
        //Sets basis for visual element
        Element output = contract.getOutputElements().iterator().next();
        visualElement.setBasis(output);
        visualization.addChildElement(visualElement);
        visualization.getChildElements().stream().forEach(visual ->{
            LOGGER.debug("visual -->{}",visual);
        });
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        hipieService.saveContract(user, contract);
        
        ContractInstance contractInstance = composition.getContractInstanceByName(composition.getName());
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue) );
        
      
    }
    
    private ContractInstance createVisualContractInstance(String compName,Widget widget) throws Exception { 
        
        Contract contract = new Contract();
        HIPIEService hipieService = HipieSingleton.getHipie();
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());     
        contract.setLabel(compName);
        contract.setName(compName);
        
        AuthenticationService authenticationService =(AuthenticationService) SpringUtil.getBean("authenticationService");
        contract.setAuthor(authenticationService.getUserCredential().getId());      
        contract.setDescription("Dashboard charts integrated with Hipie/Marshaller");
        contract.setProp(Contract.CATEGORY, "VISUALIZE");
        contract.setProp(Contract.VERSION, "0.1");
        
        InputElement input = new InputElement();
        input.setName("dsInput1");       
        //TODO:need to change for roxie query
        input.setType(InputElement.TYPE_DATASET);   
        input.addOption(new ElementOption(Element.MAPBYNAME));
        
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        
        contract.getInputElements().add(input);
    
        OutputElement output = new OutputElement();
        output.setName("dsOutput1");
        output.setType(OutputElement.TYPE_DATASET);
        output.setBase(input.getName());
        output.addOption(new ElementOption("WUID"));
        contract.getOutputElements().add(output);
    
        VisualElement visualization = new VisualElement();
        visualization.setName(compName);
        visualization.setType(VisualElement.VISUALIZE);
        
        //TODO:set title for visualization
        VisualElement ve = widget.generateVisualElement();
        ve.setBasis(output);
        visualization.addChildElement(ve);
        contract.getVisualElements().add(visualization);
        
        contract = hipieService.saveContractAs(authenticationService.getUserCredential().getId(), contract,contract.getName());
        
        ContractInstance contractInstance = contract.createContractInstance();
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue)
        );
      
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Visuslisation contract - " + contractInstance.toCompositionString());
        }
        return  contractInstance;
    }

    /* 
     * Gets the composition's latest workuntit ID
     */
    @Override
    public String getWorkunitId(Dashboard dashboard,String user) throws Exception {
        Composition composition = null;
        CompositionInstance latestInstance = null;
        composition =  HipieSingleton.getHipie().getComposition(
                user,
                dashboard.getCompositionName());
        if(composition != null) {
            latestInstance = composition.getMostRecentInstance(
                    user, true);
            //Compare last updated date
            LOGGER.debug("composition last updated date -->{}", new Date(composition.getLastModified()));
            
            if (latestInstance == null
                    || latestInstance.getDate(latestInstance
                                    .getWorkunitId()).before(
                            new Date(composition.getLastModified()))) {
                latestInstance = runComposition(dashboard, user);
            } 
            
            if(latestInstance.getWorkunitStatus().contains("failed")) {
               return null;
            }
        }
        
        return latestInstance.getWorkunitId();
    }

    @Override
    public void editCompositionChart(Dashboard dashboard, Widget widget,
            String user) throws Exception {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        
        composition = hipieService.getComposition(user, dashboard.getCompositionName());
        ContractInstance contractInstance = composition.getContractInstanceByName(composition.getName());
        Contract contract = contractInstance.getContract();
        
        VisualElement visualElement = HipieUtil.getVisualElement(contract,widget.getName());
        
        //Removing previous input fields
        String inputName = visualElement.getBasis().getBase();
        Element visualInput =  contract.getInputElements().stream().filter(element -> inputName.equals(element.getName())).findFirst().get();
        widget.removeInput((InputElement)visualInput);
        
        //Removing previous weight and label
        HipieUtil.removeWeightAndLabel(visualElement);
               
        //Removing instance properties
        widget.removeInstanceProperty(contractInstance.getProps());
        
        //Adding present input
        Element input=contract.getInputElements().iterator().next();
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        widget.editVisualElement(visualElement);
        
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        hipieService.saveContract(user, contract);
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue) );
        
        HipieSingleton.getHipie().saveComposition(user, composition);
    }

    @Override
    public void deleteCompositionChart(Dashboard dashboard,String userId, String chartName) throws Exception{
        Composition composition = null;
        ContractInstance contractInstance = null;
        HIPIEService hipieService = HipieSingleton.getHipie();
        
        composition = hipieService.getComposition(userId, dashboard.getCompositionName());
        contractInstance = composition.getContractInstanceByName(composition.getName());        
        Contract contract = contractInstance.getContract();
        
        VisualElement visualElement = HipieUtil.getVisualElement(contract, chartName);
        
        LOGGER.debug("Output -->"+visualElement.getBasis().getName());
        LOGGER.debug("Input -->"+visualElement.getBasis().getBase());
        
        if (contract.getVisualElements().iterator().next().getChildElements().size() == 1) {
            // TODO:delete dud file
            
            // deleting CMP file
            hipieService.deleteComposition(composition);
            hipieService.refreshData();
            
            // updating DB with null composition name
            dashboard.setCompositionName(null);
           
        }else{
            boolean outputExists = HipieUtil.checkOutputExists(contract,visualElement.getBasis().getName(),chartName);
            LOGGER.debug("outputExists -->{}",outputExists);
            if(outputExists){
                //Remove visual element and input fields,instance properties
                HipieUtil.removeFieldsAndVisualElement(contractInstance,visualElement);
            }else{
                //Remove visual element and input Element,output element,instance properties
               HipieUtil.removeInputOutputAndVisualElement(contractInstance,visualElement);
               ContractInstance precursor = contractInstance.getPrecursors().get(visualElement.getBasis().getBase());
               LOGGER.debug("precursor -->{}",precursor);
               contractInstance.removePrecursor(precursor, visualElement.getBasis().getBase());
            }
            contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
            hipieService.saveContract(userId, contract);
            
            HipieSingleton.getHipie().saveComposition(userId, composition);
        }
        //updating dashboard's last updated date
        dashboardService.updateDashboard(dashboard);
        
    }
    
    
    
    @Override
    public List<LiveWidget> extractLiveWidgets(Dashboard dashboard, String user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Interactivity> extractInteractivities(Dashboard dashboard, String user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addInteractivity(Dashboard dashboard, String user, Interactivity interactivity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeInteractivity(Dashboard dashboard, String user, Interactivity interactivity) {
        // TODO Auto-generated method stub
        
    }
}
