package org.hpccsystems.dashboard.service.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.hpcc.HIPIE.utils.ErrorBlock;
import org.hpcc.HIPIE.utils.HError;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.CompositionException;
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
import org.zkoss.util.resource.Labels;
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
    public void createComposition(Dashboard dashboard, Widget widget,String user) throws CompositionException {
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
        
        //validates composition, if valid saves it
        ErrorBlock errorBlock = composition.validate();
        for(HError error : errorBlock){
            LOGGER.error(Constants.EXCEPTION,error);
            throw new Exception(error.getErrorString());
        }
        composition = HipieSingleton.getHipie().saveCompositionAs(user, composition,compName + ".cmp");
        dashboard.setCompositionName(composition.getCanonicalName());   
    } catch (Exception e) {
        throw new CompositionException(Labels.getLabel("unableToCreateComposition"),e);
    }
    }
    
    @Override
    public void addCompositionChart(Dashboard dashboard, Widget widget,String user) throws CompositionException {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        try {           
            composition = hipieService.getComposition(user, dashboard.getCompositionName());
            Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
            
            if(checkFileExistence(composition,widget.getLogicalFile())){
                ContractInstance rawdatasetWithSameFile = getFileRawdataset(composition,"~"+widget.getLogicalFile());
                appendOnVisualElement(composition,widget,user,rawdatasetWithSameFile); 
            }else{
                addOnRawdataset(composition,"~" + widget.getLogicalFile(),widget,dashboard.getHpccConnection(),user);
            }
            validateSaveCompositionContract(composition,contract,user);
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToFetchComposition"),e);
        }
    }
    
    private ContractInstance getFileRawdataset(Composition composition,
            String logicalFile) {

        ContractInstance contractInstance = null;
        Map<String, ContractInstance> contractInstances = composition
                .getContractInstances();

       for(ContractInstance instance : contractInstances.values()){
           if (instance.getProperty("LogicalFilename") != null) {
               if (logicalFile.equals(instance
                       .getProperty("LogicalFilename"))) {
                   contractInstance = instance;
                   break;
               }
           }
       }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Rawdataset contractInstance --->" + contractInstance);
        }
        return contractInstance;
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
        
        //Adding additional input
        InputElement input=new InputElement();
        //setting Input Element name as dsInput2
        input.setName("dsInput"+getlastElementId(contract.getInputElements()));
        input.setType(InputElement.TYPE_DATASET);
        input.addOption(new ElementOption(Element.MAPBYNAME));
        input.addOption(new ElementOption(Element.OPTIONAL));
        contract.getInputElements().add(input);        
        
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );

        //Adding additional output
        OutputElement output=new OutputElement();
        //setting Output Element name as dsOutput2
        output.setName("dsOutput"+getlastElementId(contract.getInputElements()));
        output.setType(OutputElement.TYPE_DATASET);
        output.setBase(input.getName());
        output.addOption(new ElementOption("WUID"));
        output.setParentContainer(contract);
        contract.getOutputElements().add(output);
        
        VisualElement visualElement = widget.generateVisualElement();
        visualElement.setBasis(output);

        VisualElement visualization=contract.getVisualElements().iterator().next();
        visualization.addChildElement(visualElement);
        
        ContractInstance contractInstance = composition.getContractInstanceByName(composition.getName());
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue)
        );
        //Adding additional Rawdataset
        ContractInstance datasource2 = cloneRawdataset(composition,fileName,hpcc);        
        
        //here "dsOutput" -> Rawdataset's output
        contractInstance.addPrecursor(datasource2,"dsOutput",input.getName()); 
        
    }


    /**
     * Method to return last Element's last character which helps to generate new Elemnet's name
     * If Last Element name is dsInput3/dsOuput3, it return 4 to generate Input/Output as dsInput4/dsOuput4
     * @param inputElements
     * @return
     */
    private int getlastElementId(List<Element> inputElements) {
        String lastElementame = inputElements.get(inputElements.size()-1).getName().trim();
       LOGGER.debug("int -->{}", Character.getNumericValue(lastElementame.charAt(lastElementame.length()-1)));
       LOGGER.debug("int -->{}",(int)lastElementame.charAt(lastElementame.length()-1));
        int id = (int)lastElementame.charAt(lastElementame.length()-1);
        return id+1;
    }

    private ContractInstance cloneRawdataset(Composition composition,
            String fileName, HPCCConnection hpcc) throws Exception {
        //gets any of the Rawdataset which is used in the composition
        ContractInstance contractInstance  = composition.getContractInstanceByName(composition.getName());        
        Contract contract = contractInstance.getContract();
        VisualElement visualElement = (VisualElement)contract.getVisualElements().iterator().next().getChildElement(0);
        ContractInstance datasource1 = contractInstance.getPrecursors().get(visualElement.getBasis().getBase());
        
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
    public CompositionInstance runComposition(Dashboard dashboard,String user) throws CompositionException {
        HIPIEService hipieService=HipieSingleton.getHipie();
        CompositionInstance compositionInstance = null;
        try {
           Composition comp = hipieService.getComposition(user,dashboard.getCompositionName());
           compositionInstance = hipieService.runComposition(comp, dashboard.getHpccConnection(), user);
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToGetOrRunComposition"),e);
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
    private void appendOnVisualElement(Composition composition, Widget widget,
            String user, ContractInstance rawdatasetWithSameFile)
            throws Exception {
        
        Contract contract = composition.getContractInstanceByName(composition.getName()).getContract();
        String inputName = getInputUsedSameFile(rawdatasetWithSameFile,composition.getContractInstanceByName(composition.getName()));
        Element input = contract.getInputElements().stream()
                .filter(element -> inputName.equals(element.getName()))
                .findFirst().get();
        LOGGER.debug("input used same file-->{}",input);
        widget.generateInputElement().stream().forEach(inputElement->
            input.addChildElement(inputElement)
        );
        Element output = contract
                .getOutputElements()
                .stream()
                .filter(element -> ((OutputElement) element).getBase().equals(
                        input.getName())).findFirst().get();
        LOGGER.debug("output used same file-->{}",output);
        VisualElement visualization=contract.getVisualElements().iterator().next();
        VisualElement visualElement = widget.generateVisualElement();
        //Sets basis for visual element
        visualElement.setBasis(output);
        visualization.addChildElement(visualElement);
               
        ContractInstance contractInstance = composition.getContractInstanceByName(composition.getName());
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue) );
      
    }
    
    private String getInputUsedSameFile(
            ContractInstance rawdatasetWithSameFile,ContractInstance contractInstance) {
        LOGGER.debug("id -->{}",rawdatasetWithSameFile.getInstanceID());
        LOGGER.debug("precursors -->{}",contractInstance.getProps());
        for(Entry<String, String[]> entry : contractInstance.getProps().entrySet()){
            for(String valueStr :entry.getValue() ){
                if(valueStr.contains(rawdatasetWithSameFile.getInstanceID())){
                    return  entry.getKey();
                }
            }
        }
        return null;
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
        input.addOption(new ElementOption(Element.OPTIONAL));
        
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
        
        VisualElement ve = widget.generateVisualElement();
        ve.setBasis(output);
        visualization.addChildElement(ve);
        contract.getVisualElements().add(visualization);
        
        //validates contract, if valid saves it
        ErrorBlock errorBlock = contract.validate();
        for(HError error : errorBlock){
            LOGGER.error(Constants.EXCEPTION,error);
            throw new Exception(error.getErrorString());
        }
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
    public String getWorkunitId(Dashboard dashboard,String user) throws CompositionException {
        Composition composition = null;
        CompositionInstance latestInstance = null;
        try {
            composition =  HipieSingleton.getHipie().getComposition(
                    user,
                    dashboard.getCompositionName());
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToFetchComposition"),e);
        }
      
        if(composition != null) {
            try {
                latestInstance = composition.getMostRecentInstance(user, true);
            } catch (Exception e) {
                throw new CompositionException(Labels.getLabel("unableToGetRecentInstance"),e);
            }
            //Compare last updated date
            LOGGER.debug("composition last updated date -->{}", new Date(composition.getLastModified()));
            
            if (latestInstance != null) {
                // TODO : Get the Zone Id from HIPIE instead of hard coding '-0500'
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.of("-0500"));
                ZonedDateTime lastRun = ZonedDateTime.parse(latestInstance.getWorkunitId().substring(1), formatter);
                
                ZonedDateTime lastModified = ZonedDateTime.ofInstant(Instant.ofEpochMilli(composition.getLastModified()), ZoneId.of("-0500"));
                
                if(LOGGER.isDebugEnabled()) {
                    try {
                        LOGGER.debug("Last run instance - {} date - {}, \n Modified date -{} ", latestInstance.getDate(latestInstance.getWorkunitId()).getTime(), lastRun, lastModified);
                    } catch (Exception e) {
                        LOGGER.error(Constants.EXCEPTION,e);
                    }
                }
                LOGGER.debug("Date - compared result-->{}",lastRun.compareTo(lastModified));
                
                if(lastRun.compareTo(lastModified) < 0) {
                    latestInstance = runComposition(dashboard, user);
                }
            } else {
                latestInstance = runComposition(dashboard, user);
            }
            try {
                if(latestInstance.getWorkunitStatus(true).contains("failed")) {
                    return null;
                 }
            } catch (Exception e) {
                throw new CompositionException(Labels.getLabel("failedToGetWorkUnitStatus"),e);
            }
            
        }
        
        return latestInstance.getWorkunitId();
    }

    @Override
    public void editCompositionChart(Dashboard dashboard, Widget widget,
            String user) throws CompositionException {
        HIPIEService hipieService = HipieSingleton.getHipie();
        Composition composition;
        
        try {
            composition = hipieService.getComposition(user, dashboard.getCompositionName());
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToFetchComposition"),e);
        }
        ContractInstance contractInstance;
        try {
            contractInstance = composition.getContractInstanceByName(composition.getName());
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToGetContractInstance"),e);
        }
        Contract contract = contractInstance.getContract();
        
        VisualElement visualElement = HipieUtil.getVisualElement(contract,widget.getName());
        
        //Removing previous input fields
        String inputName = visualElement.getBasis().getBase();
        Element visualInput =  contract.getInputElements().stream().filter(element -> inputName.equals(element.getName())).findFirst().get();
        
        List<String> labelWeighFiltertNames = HipieUtil.getWeightLabelFilterNames(visualElement);
        
        
        widget.removeInput((InputElement)visualInput,labelWeighFiltertNames);
        
        //Removing previous weight and label
        HipieUtil.removeWeightAndLabel(visualElement);
               
        //Removing instance properties
        widget.removeInstanceProperty(contractInstance.getProps(),labelWeighFiltertNames);
        
        //Adding present input
        widget.generateInputElement().stream().forEach(inputElement->
            ((InputElement)visualInput).addChildElement(inputElement)
        );
        widget.editVisualElement(visualElement);
        
        widget.getInstanceProperties().forEach((propertyName,propertyValue)->
        contractInstance.setProperty(propertyName,propertyValue) );
        
        validateSaveCompositionContract(composition,contract,user);
        
    }


    private void validateSaveCompositionContract(Composition composition,
            Contract contract,String userId) throws CompositionException{
        
        ErrorBlock errorBlock = contract.validate();
        for (HError error : errorBlock) {
            LOGGER.error(Constants.EXCEPTION,error.toString());
            throw new CompositionException(error.getErrorString(), new Exception(error.getErrorString()));            
        }
       
        ErrorBlock error = composition.validate();
        for (HError herror : error) {
            LOGGER.error(Constants.EXCEPTION,herror.toString());
            throw new CompositionException(herror.getErrorString(), new Exception(herror.getErrorString()));
        }
        HIPIEService hipieService = HipieSingleton.getHipie(); 
        
        contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
        try {
            hipieService.saveContract(userId, contract);
            hipieService.refreshData();
            
            HipieSingleton.getHipie().saveComposition(userId, composition);  
            hipieService.refreshData();
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToReachHipieServices"), e);
        }
    }

    @Override
    public void deleteCompositionChart(Dashboard dashboard,String userId, String chartName) throws CompositionException{
        Composition composition = null;
        ContractInstance contractInstance = null;
        HIPIEService hipieService = HipieSingleton.getHipie();
        
        try {
            composition = hipieService.getComposition(userId, dashboard.getCompositionName());
        contractInstance = composition.getContractInstanceByName(composition.getName());        
        Contract contract = contractInstance.getContract();
        
        VisualElement visualElement = HipieUtil.getVisualElement(contract, chartName);
        
        LOGGER.debug("Output -->"+visualElement.getBasis().getName());
        LOGGER.debug("Input -->"+visualElement.getBasis().getBase());
        
        if (contract.getVisualElements().iterator().next().getChildElements().size() == 1) {
            //delete dud file
            hipieService.getRepositoryManager().getDefaultRepository().deleteFile(contract.getFileName());
            hipieService.getRepositoryManager().refreshAll();

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
                ContractInstance precursor = contractInstance.getPrecursors().get(visualElement.getBasis().getBase());
                contractInstance.removePrecursor(precursor, visualElement.getBasis().getBase());
                //Removing unused Rawdataset
                composition.removeContractInstance(precursor);
                HipieUtil.deleteInputOutputAndVisualElement(contractInstance,visualElement);
                LOGGER.debug("precursor -->{}",precursor);
               
            }
            contract.setRepository(hipieService.getRepositoryManager().getDefaultRepository());
            hipieService.saveContract(userId, contract);
            
            HipieSingleton.getHipie().saveComposition(userId, composition);
        }
        //updating dashboard's last updated date
        dashboardService.updateDashboard(dashboard);
        
        } catch (Exception e) {
            throw new CompositionException(Labels.getLabel("unableToDeleteCompositionChart"), e);
        }
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
