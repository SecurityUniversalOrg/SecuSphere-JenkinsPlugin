package io.jenkins.plugins.secusphere;

import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;

import javax.inject.Inject;
import jenkins.model.Jenkins;

public class SecurityQualityGateStep extends AbstractStepImpl {

    private final String appName;
    private final String gitUrl;
    private final String gitBranch;
    private final String configFile;
    private final String resultsFile;


    @DataBoundConstructor
    public SecurityQualityGateStep(String appName, String gitUrl, String gitBranch, String configFile, String resultsFile) {
        this.appName = appName;
        this.gitUrl = gitUrl;
        this.gitBranch = gitBranch;
        this.configFile = configFile;
        this.resultsFile = resultsFile;
    }

    public String getAppName() {
        return appName;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getResultsFile() {
        return resultsFile;
    }

    public static class Execution extends AbstractSynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient SecurityQualityGateStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient FilePath workspace;

        @StepContextParameter
        private transient Run<?, ?> run;

        @Override
        protected Void run() throws Exception {
            FilePath configFilePath = workspace.child(step.getConfigFile());
            FilePath resultsFilePath = workspace.child(step.getResultsFile());

            SecurityQualityGateEvaluator evaluator = new SecurityQualityGateEvaluator(
                new File(configFilePath.getRemote()), new File(resultsFilePath.getRemote()), run);

            SecuSphereGlobalConfiguration globalConfig = SecuSphereGlobalConfiguration.get();
            String baseUrl = globalConfig.getBaseUrl();
            String credentialsId = globalConfig.getCredentialsId();

            if (run != null) {
                StandardUsernamePasswordCredentials credentials = CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, run, new DomainRequirement[0]);
                if (credentials != null) {
                    String client_id = credentials.getUsername();
                    String client_secret = credentials.getPassword().getPlainText();
                    ApiClient apiClient = new ApiClient(client_id, client_secret, baseUrl);
                    evaluator.enforce(listener, apiClient, step.getAppName(), step.getGitUrl(), step.getGitBranch()); // Pass the appName as a parameter
                } else {
                    listener.getLogger().println("Credentials not found.");
                }
            } else {
                listener.getLogger().println("Run object is null.");
            }

            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "enforceSecurityQualityGate";
        }

        @Override
        public String getDisplayName() {
            return "Enforce Security Quality Gate";
        }
    }
}
