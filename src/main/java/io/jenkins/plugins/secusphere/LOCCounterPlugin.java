package io.jenkins.plugins.secusphere;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import java.nio.charset.StandardCharsets;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import hudson.model.Run;
import hudson.model.TaskListener;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import org.jenkinsci.Symbol;
import org.json.JSONArray;
import hudson.util.IOUtils;
import java.io.File;
import java.io.FileWriter;
import org.json.JSONObject;
import java.util.Collections;
import java.util.Collection;
import hudson.model.Action;
import hudson.model.AbstractProject;


public class LOCCounterPlugin extends Builder implements SimpleBuildStep {

    private final String appName;

    @DataBoundConstructor
    public LOCCounterPlugin(Map<String, Object> config) {
        this.appName = (String) config.get("appName");
    }

    public String getAppName() {
        return appName;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        if (workspace != null) {
            LOCCounter locCounter = new LOCCounter();
            Map<String, Integer> locData = locCounter.countLinesOfCode(workspace);

            // Count the number of files per language
            Map<String, Integer> fileData = locCounter.countFilesPerLanguage(workspace);

            SecuSphereGlobalConfiguration globalConfig = SecuSphereGlobalConfiguration.get();
            String baseUrl = globalConfig.getBaseUrl();

            // Declare the credentials variable
            StandardUsernamePasswordCredentials credentials = null;

            // Get the credentials from the Credential store
            String credentialsId = globalConfig.getCredentialsId();
            if (credentialsId != null) {
                credentials = CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, run);
            } else {
                listener.getLogger().printf("Credentials not provided%n");
            }

            if (credentials != null) {
                String clientId = credentials.getUsername();
                String clientSecret = credentials.getPassword().getPlainText();

                ApiClient apiClient = new ApiClient(clientId, clientSecret, baseUrl);
                JSONArray jsonArray = new JSONArray();
                for (Map.Entry<String, Integer> entry : locData.entrySet()) {
                    String language = entry.getKey();
                    int loc = entry.getValue();
                    int fileCount = fileData.getOrDefault(language, 0);
                    jsonArray.put(createJSONPayload(language, loc, fileCount));
                }

                // Send a single API request with the jsonArray
                JSONObject payload = new JSONObject();
                String appName = getAppName();
                payload.put("appName", appName);
                payload.put("data", jsonArray);
                apiClient.sendPost("/add_loc", payload);

                listener.getLogger().printf("API request sent%n");

                // Save the output to a file called loc.json
                FilePath locJsonFile = workspace.child("loc.json");
                try (FileWriter fileWriter = new FileWriter(locJsonFile.getRemote(), StandardCharsets.UTF_8)) {
                    fileWriter.write(jsonArray.toString(4));
                }

                listener.getLogger().printf("loc.json file saved%n");

                // Archive the file in Jenkins
                run.pickArtifactManager().archive(workspace, launcher, (BuildListener) listener, Collections.singletonMap("loc.json", "loc.json"));

                listener.getLogger().printf("loc.json file archived%n");
            }
        } else {
            listener.getLogger().printf("No workspace found%n");
        }
        listener.getLogger().println("LOCCounterPlugin executed successfully.");
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Collections.emptyList();
    }

    private JSONObject createJSONPayload(String language, int loc, int fileCount) {
        JSONObject payload = new JSONObject();
        payload.put("language", language);
        payload.put("loc", loc);
        payload.put("fileCount", fileCount);
        return payload;
    }

    @Extension
    @Symbol("countLinesOfCode")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Count Lines of Code and Categorize by Language";
        }
    }
}
