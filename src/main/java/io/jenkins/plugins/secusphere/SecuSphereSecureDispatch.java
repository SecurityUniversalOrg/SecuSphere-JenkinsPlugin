package io.jenkins.plugins.secusphere;


import hudson.Extension;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.model.Result;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;

import org.jenkinsci.Symbol;
import org.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.net.URISyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SecuSphereSecureDispatch extends Builder implements SimpleBuildStep {

    private String reportType;
    private final String appName;
    private final String giturl;
    private final String gitBranch;


    @DataBoundConstructor
    public SecuSphereSecureDispatch(String reportType, String appName, String giturl, String gitBranch) {
        this.reportType = reportType;
        this.appName = appName;
        this.giturl = giturl;
        this.gitBranch = gitBranch;
    }

    public String getReportType() {
        return reportType;
    }

    @DataBoundSetter
    public void setReportTyper(String reportType) {
        this.reportType = reportType;
    }

    // Add a getter for the appName field
    public String getAppName() {
        return appName;
    }

    public String getGitUrl() {
        return giturl;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        try {
            EnvVars env = run.getEnvironment(listener);
            listener.getLogger().println("Parsing assessment report...");
            String workspacePath = workspace.getRemote();
            // Unarchive the threatbuster_results.json file
            SecuSphereResultsUpdater threatBusterResultsUpdater = new SecuSphereResultsUpdater();
            threatBusterResultsUpdater.unarchiveResultsFile(run, workspace, listener);
            ReportRunner reportRunner = new ReportRunner(listener);
            String filePath = workspacePath + reportRunner.mapReportFilepath(reportType.toLowerCase());

            if (filePath == null || filePath.isEmpty()) {
                listener.getLogger().println("File path is null or empty");
                return;
            }

            // Get the authorization URL from the global configuration
            SecuSphereGlobalConfiguration globalConfig = SecuSphereGlobalConfiguration.get();
            boolean archive = globalConfig.getEnableReportArchiving();
            boolean scorecard = globalConfig.getEnableScorecardReport();
            String giturl = getGitUrl();
            String gitBranch = getGitBranch();
            String appName = getAppName();
            String baseUrl = globalConfig.getBaseUrl();

            // Declare the credentials variable
            StandardUsernamePasswordCredentials credentials = null;

            // Get the credentials from the Credential store
            String credentialsId = globalConfig.getCredentialsId();
            if (credentialsId != null) {
                credentials = CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, run);
            } else {
                listener.getLogger().printf("Credentials not provided");
            }

            if (credentials != null) {
                String clientId = credentials.getUsername();
                String clientSecret = credentials.getPassword().getPlainText();

                // Read in and parse the assessment report
                ReportResult reportResult = reportRunner.readInReport(filePath, reportType.toLowerCase());
                String category = reportRunner.mapCategory(reportType.toLowerCase());
                listener.getLogger().println("Assessment Category: " + category);
                JSONObject wrappedFindings = reportResult.getWrappedFindings();
                JSONObject summaryReport = reportResult.getSummaryReport();


                listener.getLogger().println("Summary: " + summaryReport);

                // Instantiate the SummaryReportHandler and submit the summaryReport to the Parquet file
                if (scorecard) {
                    // Declare the credentials variable
                    StandardUsernamePasswordCredentials gitCredentials = null;

                    // Get the credentials from the Credential store
                    String gitCredentialsId = globalConfig.getGitHubCredId();
                    if (gitCredentialsId != null) {
                        gitCredentials = CredentialsProvider.findCredentialById(gitCredentialsId, StandardUsernamePasswordCredentials.class, run);
                    } else {
                        listener.getLogger().printf("Git Credentials not provided");
                    }

                    String gitRepoUrl = globalConfig.getGitRepoUrl();
                    String parquetFilePath = globalConfig.getParquetFilePath();

                    String gitUsername = null;
                    String gitPassword = null;
                    if (gitCredentials != null) {
                        gitUsername = gitCredentials.getUsername();
                        gitPassword = gitCredentials.getPassword().getPlainText();
                        SummaryReportHandler summaryReportHandler = new SummaryReportHandler(summaryReport);
                        try {
                            summaryReportHandler.submitToCsvFile(gitRepoUrl, parquetFilePath, gitUsername, gitPassword, listener, appName);
                            listener.getLogger().println("Summary report successfully added to the Parquet file");
                        } catch (IOException e) {
                            listener.getLogger().println("Error adding summary report to the Parquet file: " + e.getMessage());
                            throw new RuntimeException("Error adding summary report to the Parquet file", e);
                        }
                    } else {
                        listener.getLogger().println("Git credentials not found");
                    }


                }

                // Perform any further actions using fileContents, baseUrl, username, and password
                listener.getLogger().println("Sending input to RestfulAPI endpoint...");

                ApiClient apiClient = new ApiClient(clientId, clientSecret, baseUrl);

                // Add appId to wrappedFindings
                wrappedFindings.put("appName", appName);
                wrappedFindings.put("giturl", giturl);
                wrappedFindings.put("branch", gitBranch);
                wrappedFindings.put("scanType", category);
                if ("Container".equals(category)) {
                    wrappedFindings.put("dockerImg", reportRunner.mapDockerImage(reportType, filePath));
                }

                // Send the wrappedFindings with the added appId value
                String response = apiClient.sendPost("/add_vulnerabilities", wrappedFindings);

                // Convert the response string to a JSONObject
                String respStatus;
                if (response != null && !response.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(response);
                    respStatus = jsonResponse.getString("Status");
                } else {
                    respStatus = "Could not dispatch report to SecuSphere server.  Please contact your administrator.";
                }

                // Get the "Status" field from the jsonResponse
                listener.getLogger().println("Response: " + respStatus);

                run.addAction(new SecuSphereSidePanel(summaryReport, category));

                // Upload the original assessment report to the Azure Blob storage
                if (archive) {
                    listener.getLogger().println("Uploading report file to Blob Storage...");
                    String azAccountName = globalConfig.getAzureBlobAcctName();

                    // Get the azure credentials from the Credential store
                    StandardUsernamePasswordCredentials azCredentials = null;
                    String azCredentialsId = globalConfig.getAzureBlobCredId();
                    if (azCredentialsId != null) {
                        azCredentials = CredentialsProvider.findCredentialById(azCredentialsId, StandardUsernamePasswordCredentials.class, run);
                    } else {
                        listener.getLogger().printf("Azure Credentials not provided");
                    }
                    if (azCredentials != null) { // Add this null check
                        String azAccountKey = azCredentials.getPassword().getPlainText();
                        String azContainerName = azCredentials.getUsername();
                        String azFilePath = filePath;
                        String azFileName = reportRunner.mapReportFilepath(reportType.toLowerCase());
                        if (azFileName.startsWith("/")) {
                            azFileName = azFileName.substring(1);
                        };
                        String azContentType = "text/plain";
                        AzureBlobUploader uploader = new AzureBlobUploader(listener, azAccountName, azAccountKey, azContainerName);
                        try {
                            uploader.uploadFile(azFilePath, azFileName, azContentType, category, appName);
                            listener.getLogger().println("Successfully Uploaded report file to Blob Storage.");
                        } catch (IOException e) {
                            System.err.println("IOException while uploading the file:");
                            e.printStackTrace();
                        }
                    } else {
                        listener.getLogger().println("Azure credentials not found");
                    }
                }
                // Archive the updated threatbuster_results.json file
                File resultsFile = new File(workspace.getRemote(), "threatbuster_results.json");
                threatBusterResultsUpdater.updateResultsFile(category, summaryReport, resultsFile);

                threatBusterResultsUpdater.archiveResultsFile(run, workspace, listener);

                // Check if critical findings exceed threshold
                if (SecurityQualityGate.enforceGate("Critical", summaryReport, globalConfig)) {
                    listener.getLogger().println("Critical findings exceed the threshold");
                    throw new RuntimeException("Critical findings exceed the threshold");
                }
                // Check if high findings exceed threshold
                if (SecurityQualityGate.enforceGate("High", summaryReport, globalConfig)) {
                    listener.getLogger().println("High findings exceed the threshold");
                    throw new RuntimeException("High findings exceed the threshold");
                }
            } else {
                listener.getLogger().printf("Credentials not found: %s%n", credentialsId);
            }
        } catch (IOException e) {
            run.setResult(Result.FAILURE);
            listener.getLogger().println("Build failed due to an I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            run.setResult(Result.FAILURE);
            listener.getLogger().println("Build failed due to interruption: " + e.getMessage());
        } catch (JSONException e) {
            run.setResult(Result.FAILURE);
            listener.getLogger().println("Build failed due to a JSON error: " + e.getMessage());
        }

    }

    @Symbol("SecuSphereSecureDispatch")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(SecuSphereSecureDispatch.class);
        }

        @Override
        public String getDisplayName() {
            return "SecuSphere Secure Dispatch";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> clazz) {
            return true;
        }

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.SecuSphereSecureDispatch_DescriptorImpl_errors_missingName());
            return FormValidation.ok();
        }
    }
}
