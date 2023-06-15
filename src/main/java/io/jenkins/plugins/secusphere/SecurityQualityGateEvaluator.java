package io.jenkins.plugins.secusphere;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import hudson.model.Run;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import org.json.JSONArray;
import java.util.List;
import hudson.plugins.git.Branch;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.util.BuildData;
import jenkins.model.Jenkins;
import org.eclipse.jgit.transport.RemoteConfig;
import hudson.model.AbstractProject;
import java.util.Optional;
import hudson.plugins.git.Revision;


public class SecurityQualityGateEvaluator {
    private Map<String, Map<String, Integer>> thresholds = new HashMap<>();
    private Map<String, Map<String, Integer>> report;
    private File reportFile;
    private File configFile;
    private Run<?, ?> run;

    public SecurityQualityGateEvaluator(File configFile, File resultsFile, Run<?, ?> run) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.thresholds = (Map<String, Map<String, Integer>>) objectMapper.readValue(configFile, HashMap.class).get("thresholds");
        this.report = (Map<String, Map<String, Integer>>) objectMapper.readValue(resultsFile, HashMap.class).get("report");
        this.reportFile = resultsFile;
        this.configFile = configFile;
        this.run = run;
    }

    private JSONObject getJobData(String appName, String gitUrl, String gitBranch) {
        JSONObject jobData = new JSONObject();
        jobData.put("appName", appName);
        jobData.put("gitUrl", gitUrl);
        jobData.put("gitBranch", gitBranch);
        jobData.put("jobName", run.getParent().getFullName());

        Optional.ofNullable(run.getParent().getUrl()).ifPresent(url -> jobData.put("jobUrl", url));

        jobData.put("buildNumber", run.getNumber());

        Optional.ofNullable(run.getUrl()).ifPresent(url -> jobData.put("buildUrl", url));

        jobData.put("jobDescription", run.getParent().getDescription());
        jobData.put("buildStartTime", run.getStartTimeInMillis());
        jobData.put("buildDuration", run.getDuration());
        hudson.model.Result result = run.getResult();
        if (result != null) {
            String buildResult = result.toString();
            jobData.put("buildResult", buildResult);
        }
        List<Cause> causes = run.getCauses();
        JSONArray causesJsonArray = new JSONArray();
        for (Cause cause : causes) {
            causesJsonArray.put(cause.getShortDescription());
        }
        jobData.put("buildCauses", causesJsonArray);
        ParametersAction parametersAction = run.getAction(ParametersAction.class);
        if (parametersAction != null) {
            List<ParameterValue> parameters = parametersAction.getParameters();
            JSONObject parametersJsonObject = new JSONObject();
            for (ParameterValue parameter : parameters) {
                parametersJsonObject.put(parameter.getName(), Optional.ofNullable(parameter.getValue()).orElse(""));
            }
            jobData.put("buildParameters", parametersJsonObject);
        }
        // Get the git data
        BuildData buildData = run.getAction(BuildData.class);
        if (buildData != null) {
            Revision lastBuiltRevision = buildData.getLastBuiltRevision();
            if (lastBuiltRevision != null) {
                jobData.put("gitCommit", Optional.ofNullable(lastBuiltRevision.getSha1().name()).orElse(""));
            }
        }

        return jobData;
    }



    public void enforce(TaskListener listener, ApiClient apiClient, String appName, String gitUrl, String gitBranch) throws Exception {


        // Read the content of the threatbuster_results.json file
        String threatBusterResultsJson = new String(Files.readAllBytes(reportFile.toPath()), StandardCharsets.UTF_8);

        // Read the content of the configFile
        String configJson = new String(Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8);

        // Convert the content to JSONObjects
        JSONObject threatBusterResultsJsonObject = new JSONObject(threatBusterResultsJson);
        JSONObject configJsonObject = new JSONObject(configJson);

        // Create a combined JSONObject
        JSONObject combinedJsonObject = new JSONObject();
        combinedJsonObject.put("config", configJsonObject);
        combinedJsonObject.put("results", threatBusterResultsJsonObject);
        combinedJsonObject.put("jobData", getJobData(appName, gitUrl, gitBranch));


        // Send the combined JSONObject to the /add_sg_results endpoint using the ApiClient
        String response = apiClient.sendPost("add_sg_results", combinedJsonObject);
        listener.getLogger().println("Sent data to /add_sg_results endpoint. Response: " + response);

        // Enforce the thresholds
        for (Map.Entry<String, Map<String, Integer>> stageEntry : thresholds.entrySet()) {
            String stage = stageEntry.getKey();
            Map<String, Integer> stageThresholds = stageEntry.getValue();
            Map<String, Integer> stageResults = report.get(stage);

            if (stageResults == null) {
                throw new Exception(String.format("No results found for stage %s", stage));
            }

            for (Map.Entry<String, Integer> levelEntry : stageThresholds.entrySet()) {
                String level = levelEntry.getKey();
                Integer threshold = levelEntry.getValue();
                Integer result = stageResults.get(level);

                if (result == null) {
                    throw new Exception(String.format("No results found for level %s in stage %s", level, stage));
                }

                if (threshold != null && result > threshold) {
                    throw new Exception(String.format("Security Quality Gate failed for stage %s and level %s: %d issues found, maximum allowed is %d", stage, level, result, threshold));
                }
            }
        }


    }

}
