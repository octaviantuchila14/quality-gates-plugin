package quality.gates.jenkins.plugin;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.*;
import java.util.logging.Logger;

public class JobConfigurationService {

    private static final Logger log = Logger.getLogger( JobConfigurationService.class.getName() );

    public ListBoxModel getListOfSonarInstanceNames(GlobalConfig globalConfig) {
        ListBoxModel listBoxModel = new ListBoxModel();
        for (GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance : globalConfig.fetchListOfGlobalConfigData()) {
            listBoxModel.add(globalConfigDataForSonarInstance.getName());
        }
        return listBoxModel;
    }

    public JobConfigData createJobConfigData(JSONObject formData, GlobalConfig globalConfig) {
        JobConfigData firstInstanceJobConfigData = new JobConfigData();
        String projectKey = formData.getString("projectKey");
        if(projectKey.startsWith("$"))
        {
            String systemVariableName = projectKey;
            String getEnvVariable = systemVariableName.substring(2, systemVariableName.length()-1);
            projectKey = System.getenv(getEnvVariable);
//            if(projectKey == null) {
//                throw new QGException("Environment variable with name '" + getEnvVariable + "' does not exist.");
//            }
        }
        else {
            try {
                projectKey = URLDecoder.decode(projectKey, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new QGException("Error while decoding the project key. UTF-8 not supported.", e);
            }
        }
        String name;

        if(!globalConfig.fetchListOfGlobalConfigData().isEmpty()) {
            name = hasFormDataKey(formData, globalConfig);
        }
        else {
            name = "";
        }
        firstInstanceJobConfigData.setProjectKey(projectKey);
        firstInstanceJobConfigData.setSonarInstanceName(name);
        return firstInstanceJobConfigData;
    }

    protected String hasFormDataKey(JSONObject formData, GlobalConfig globalConfig) {
        String instanceName;
        if (formData.containsKey("sonarInstancesName"))
            instanceName = formData.getString("sonarInstancesName");
        else
            instanceName = globalConfig.fetchListOfGlobalConfigData().get(0).getName();
        return instanceName;
    }

    public JobConfigData checkProjectKeyIfVariable(JobConfigData jobConfigData, AbstractBuild build, BuildListener listener) throws QGException {
        log.info("In checkProjectKeyIfVariable");
        String projectKey = jobConfigData.getProjectKey();
        if(projectKey.isEmpty()) {
            throw new QGException("Empty project key.");
        }

        projectKey = Util.replaceMacro(projectKey, build.getBuildVariables());

//        Logger logger = Logger.getLogger("my.logger");
//        logger.info("projectKey: " + projectKey);
//        logger.info("build.getBuildVariables(): " + build.getBuildVariables());


        try {
            try {
                FileHandler fh = new FileHandler("/Users/octavian/.jenkins/myLogsJobConfiguration");
                log.addHandler(fh);
            } catch(Exception e) {

            }
            log.info("In try clause");
            EnvVars env = build.getEnvironment(listener);
            log.info("env is: " + env);
            log.info("projectKey is: " + projectKey);


            projectKey = replaceVariable(projectKey, env);
            log.info("projectKey becomes: " + projectKey);

        } catch (IOException e) {
            throw new QGException(e);
        } catch (InterruptedException e) {
            throw new QGException(e);
        }

        JobConfigData envVariableJobConfigData = new JobConfigData();
        envVariableJobConfigData.setProjectKey(projectKey);
        envVariableJobConfigData.setSonarInstanceName(jobConfigData.getSonarInstanceName());
        return envVariableJobConfigData;
    }

    public String replaceVariable(String str, TreeMap<String, String> tm) {
        for ( Map.Entry< String, String > entry : tm.entrySet() ) {
            String cr_str = entry.getKey();
            if(!cr_str.equals("_")) {
                //System.out.println("cr_str: " + cr_str);
                str = str.replace(cr_str, entry.getValue());
                //System.out.println("Str is: " + str);
            }
        }

//        while(it.hasNext()) {
//            String cr_str = it.next();
//            if(!cr_str.equals("_")) {
//                //System.out.println("cr_str: " + cr_str);
//                str = str.replace(cr_str, tm.get(cr_str));
//                //System.out.println("Str is: " + str);
//            }
//        }

        str = str.replace("#", "").replace("{", "").replace("}", "");
        //System.out.println("Str is: " + str);

        return str;
    }
}
