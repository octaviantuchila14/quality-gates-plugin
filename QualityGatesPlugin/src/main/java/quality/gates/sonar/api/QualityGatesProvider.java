package quality.gates.sonar.api;


import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.jenkins.plugin.JobConfigData;
import quality.gates.jenkins.plugin.QGException;
import org.json.JSONException;

//import java.util.logging.FileHandler;
//import java.util.logging.Logger;

public class QualityGatesProvider {

//    private static final Logger log = Logger.getLogger( QualityGatesProvider.class.getName() );

    private QualityGateResponseParser qualityGateResponseParser;
    private SonarHttpRequester sonarHttpRequester;
    private SonarInstanceValidationService sonarInstanceValidationService;

    public QualityGatesProvider() {
        this.qualityGateResponseParser = new QualityGateResponseParser();
        this.sonarHttpRequester = new SonarHttpRequester();
        this.sonarInstanceValidationService = new SonarInstanceValidationService();
    }

    public QualityGatesProvider(QualityGateResponseParser qualityGateResponseParser, SonarHttpRequester sonarHttpRequester, SonarInstanceValidationService sonarInstanceValidationService) {
        this.qualityGateResponseParser = qualityGateResponseParser;
        this.sonarHttpRequester = sonarHttpRequester;
        this.sonarInstanceValidationService = sonarInstanceValidationService;
    }

    public QualityGatesStatus getAPIResultsForQualityGates(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws JSONException {
//        try {
//            FileHandler fh = new FileHandler("/Users/octavian/.jenkins/myLogsQualityGates");
//            log.addHandler(fh);
//        } catch(Exception e) {
//
//        }
//        log.info("QualityGatesProvider hello world");
//        System.out.println("QualityGatesProvider hello world");
        GlobalConfigDataForSonarInstance validatedData = sonarInstanceValidationService.validateData(globalConfigDataForSonarInstance);
        String requesterResult = getRequesterResult(jobConfigData, validatedData);
        return qualityGateResponseParser.getQualityGateResultFromJSON(requesterResult);
    }

    public String getRequesterResult(JobConfigData jobConfigData, GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) throws QGException {
        return sonarHttpRequester.getAPIInfo(jobConfigData, globalConfigDataForSonarInstance);
    }
}
