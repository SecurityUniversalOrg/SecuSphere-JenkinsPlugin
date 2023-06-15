package io.jenkins.plugins.secusphere;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

@Extension
public class SecuSphereGlobalConfiguration extends GlobalConfiguration {

    private String baseUrl;
    private String credentialsId;
    private String azureBlobAcctName;
    private String azureBlobCredId;
    private String gitRepoUrl;
    private String parquetFilePath;
    private String gitHubCredId;
    private boolean enforceSecurityGates;
    private boolean enableReportArchiving;
    private boolean enableScorecardReport;
    private int secretLimitCritical;
    private int secretLimitHigh;
    private int secretLimitMedium;
    private int secretLimitLow;
    private int scaLimitCritical;
    private int scaLimitHigh;
    private int scaLimitMedium;
    private int scaLimitLow;
    private int sastLimitCritical;
    private int sastLimitHigh;
    private int sastLimitMedium;
    private int sastLimitLow;
    private int iacLimitCritical;
    private int iacLimitHigh;
    private int iacLimitMedium;
    private int iacLimitLow;
    private int containerLimitCritical;
    private int containerLimitHigh;
    private int containerLimitMedium;
    private int containerLimitLow;
    private int dastLimitCritical;
    private int dastLimitHigh;
    private int dastLimitMedium;
    private int dastLimitLow;
    private int infraLimitCritical;
    private int infraLimitHigh;
    private int infraLimitMedium;
    private int infraLimitLow;
    private int policyLimitCritical;
    private int policyLimitHigh;
    private int policyLimitMedium;
    private int policyLimitLow;

    public SecuSphereGlobalConfiguration() {
        load();
    }

    @Nonnull
    public static SecuSphereGlobalConfiguration get() {
        SecuSphereGlobalConfiguration config = GlobalConfiguration.all().get(SecuSphereGlobalConfiguration.class);
        if (config != null) {
            return config;
        } else {
            throw new IllegalStateException("Unable to get SecuSphereGlobalConfiguration instance");
        }
    }



    @CheckForNull
    public String getBaseUrl() {
        return baseUrl;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        save();
    }

    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
        save();
    }

    @CheckForNull
    public String getAzureBlobAcctName() {
        return azureBlobAcctName;
    }

    @DataBoundSetter
    public void setAzureBlobAcctName(String azureBlobAcctName) {
        this.azureBlobAcctName = azureBlobAcctName;
        save();
    }

    @CheckForNull
    public String getAzureBlobCredId() {
        return azureBlobCredId;
    }

    @DataBoundSetter
    public void setAzureBlobCredId(String azureBlobCredId) {
        this.azureBlobCredId = azureBlobCredId;
        save();
    }

    @CheckForNull
    public String getGitRepoUrl() {
        return gitRepoUrl;
    }

    @DataBoundSetter
    public void setGitRepoUrl(String gitRepoUrl) {
        this.gitRepoUrl = gitRepoUrl;
        save();
    }

    @CheckForNull
    public String getParquetFilePath() {
        return parquetFilePath;
    }

    @DataBoundSetter
    public void setParquetFilePath(String parquetFilePath) {
        this.parquetFilePath = parquetFilePath;
        save();
    }

    @CheckForNull
    public String getGitHubCredId() {
        return gitHubCredId;
    }

    @DataBoundSetter
    public void setGitHubCredId(String gitHubCredId) {
        this.gitHubCredId = gitHubCredId;
        save();
    }

    @CheckForNull
    public boolean getEnforceSecurityGates() {
        return enforceSecurityGates;
    }

    @DataBoundSetter
    public void setEnforceSecurityGates(boolean enforceSecurityGates) {
        this.enforceSecurityGates = enforceSecurityGates;
        save();
    }

    @CheckForNull
    public boolean getEnableReportArchiving() {
        return enableReportArchiving;
    }

    @DataBoundSetter
    public void setEnableReportArchiving(boolean enableReportArchiving) {
        this.enableReportArchiving = enableReportArchiving;
        save();
    }

    @CheckForNull
    public boolean getEnableScorecardReport() {
        return enableScorecardReport;
    }

    @DataBoundSetter
    public void setEnableScorecardReport(boolean enableScorecardReport) {
        this.enableScorecardReport = enableScorecardReport;
        save();
    }

//     Secrets Section
    @CheckForNull
    public int getSecretLimitCritical() {
        return secretLimitCritical;
    }

    @DataBoundSetter
    public void setSecretLimitCritical(int secretLimitCritical) {
        this.secretLimitCritical = secretLimitCritical;
        save();
    }

    @CheckForNull
    public int getSecretLimitHigh() {
        return secretLimitHigh;
    }

    @DataBoundSetter
    public void setSecretLimitHigh(int secretLimitHigh) {
        this.secretLimitHigh = secretLimitHigh;
        save();
    }

    @CheckForNull
    public int getSecretLimitMedium() {
        return secretLimitMedium;
    }

    @DataBoundSetter
    public void setSecretLimitMedium(int secretLimitMedium) {
        this.secretLimitMedium = secretLimitMedium;
        save();
    }

    @CheckForNull
    public int getSecretLimitLow() {
        return secretLimitLow;
    }

    @DataBoundSetter
    public void setSecretLimitLow(int secretLimitLow) {
        this.secretLimitLow = secretLimitLow;
        save();
    }

    //     SCA Section
    @CheckForNull
    public int getScaLimitCritical() {
        return scaLimitCritical;
    }

    @DataBoundSetter
    public void setScaLimitCritical(int scaLimitCritical) {
        this.scaLimitCritical = scaLimitCritical;
        save();
    }

    @CheckForNull
    public int getScaLimitHigh() {
        return scaLimitHigh;
    }

    @DataBoundSetter
    public void setScaLimitHigh(int scaLimitHigh) {
        this.scaLimitHigh = scaLimitHigh;
        save();
    }

    @CheckForNull
    public int getScaLimitMedium() {
        return scaLimitMedium;
    }

    @DataBoundSetter
    public void setScaLimitMedium(int scaLimitMedium) {
        this.scaLimitMedium = scaLimitMedium;
        save();
    }

    @CheckForNull
    public int getScaLimitLow() {
        return scaLimitLow;
    }

    @DataBoundSetter
    public void setScaLimitLow(int scaLimitLow) {
        this.scaLimitLow = scaLimitLow;
        save();
    }

    //     SAST Section
    @CheckForNull
    public int getSastLimitCritical() {
        return sastLimitCritical;
    }

    @DataBoundSetter
    public void setSastLimitCritical(int sastLimitCritical) {
        this.sastLimitCritical = sastLimitCritical;
        save();
    }

    @CheckForNull
    public int getSastLimitHigh() {
        return sastLimitHigh;
    }

    @DataBoundSetter
    public void setSastLimitHigh(int sastLimitHigh) {
        this.sastLimitHigh = sastLimitHigh;
        save();
    }

    @CheckForNull
    public int getSastLimitMedium() {
        return sastLimitMedium;
    }

    @DataBoundSetter
    public void setSastLimitMedium(int sastLimitMedium) {
        this.sastLimitMedium = sastLimitMedium;
        save();
    }

    @CheckForNull
    public int getSastLimitLow() {
        return sastLimitLow;
    }

    @DataBoundSetter
    public void setSastLimitLow(int sastLimitLow) {
        this.sastLimitLow = sastLimitLow;
        save();
    }

    //     IaC Section
    @CheckForNull
    public int getIacLimitCritical() {
        return iacLimitCritical;
    }

    @DataBoundSetter
    public void setIacLimitCritical(int iacLimitCritical) {
        this.iacLimitCritical = iacLimitCritical;
        save();
    }

    @CheckForNull
    public int getIacLimitHigh() {
        return iacLimitHigh;
    }

    @DataBoundSetter
    public void setIacLimitHigh(int iacLimitHigh) {
        this.iacLimitHigh = iacLimitHigh;
        save();
    }

    @CheckForNull
    public int getIacLimitMedium() {
        return iacLimitMedium;
    }

    @DataBoundSetter
    public void setIacLimitMedium(int iacLimitMedium) {
        this.iacLimitMedium = iacLimitMedium;
        save();
    }

    @CheckForNull
    public int getIacLimitLow() {
        return iacLimitLow;
    }

    @DataBoundSetter
    public void setIacLimitLow(int iacLimitLow) {
        this.iacLimitLow = iacLimitLow;
        save();
    }

//     Container Section
    @CheckForNull
    public int getContainerLimitCritical() {
        return containerLimitCritical;
    }

    @DataBoundSetter
    public void setContainerLimitCritical(int containerLimitCritical) {
        this.containerLimitCritical = containerLimitCritical;
        save();
    }

    @CheckForNull
    public int getContainerLimitHigh() {
        return containerLimitHigh;
    }

    @DataBoundSetter
    public void setContainerLimitHigh(int containerLimitHigh) {
        this.containerLimitHigh = containerLimitHigh;
        save();
    }

    @CheckForNull
    public int getContainerLimitMedium() {
        return containerLimitMedium;
    }

    @DataBoundSetter
    public void setContainerLimitMedium(int containerLimitMedium) {
        this.containerLimitMedium = containerLimitMedium;
        save();
    }

    @CheckForNull
    public int getContainerLimitLow() {
        return containerLimitLow;
    }

    @DataBoundSetter
    public void setContainerLimitLow(int containerLimitLow) {
        this.containerLimitLow = containerLimitLow;
        save();
    }

    //     DAST Section
    @CheckForNull
    public int getDastLimitCritical() {
        return dastLimitCritical;
    }

    @DataBoundSetter
    public void setDastLimitCritical(int dastLimitCritical) {
        this.dastLimitCritical = dastLimitCritical;
        save();
    }

    @CheckForNull
    public int getDastLimitHigh() {
        return dastLimitHigh;
    }

    @DataBoundSetter
    public void setDastLimitHigh(int dastLimitHigh) {
        this.dastLimitHigh = dastLimitHigh;
        save();
    }

    @CheckForNull
    public int getDastLimitMedium() {
        return dastLimitMedium;
    }

    @DataBoundSetter
    public void setDastLimitMedium(int dastLimitMedium) {
        this.dastLimitMedium = dastLimitMedium;
        save();
    }

    @CheckForNull
    public int getDastLimitLow() {
        return dastLimitLow;
    }

    @DataBoundSetter
    public void setDastLimitLow(int dastLimitLow) {
        this.dastLimitLow = dastLimitLow;
        save();
    }

    //     Infrastructure Section
    @CheckForNull
    public int getInfraLimitCritical() {
        return infraLimitCritical;
    }

    @DataBoundSetter
    public void setInfraLimitCritical(int infraLimitCritical) {
        this.infraLimitCritical = infraLimitCritical;
        save();
    }

    @CheckForNull
    public int getInfraLimitHigh() {
        return infraLimitHigh;
    }

    @DataBoundSetter
    public void setInfraLimitHigh(int infraLimitHigh) {
        this.infraLimitHigh = infraLimitHigh;
        save();
    }

    @CheckForNull
    public int getInfraLimitMedium() {
        return infraLimitMedium;
    }

    @DataBoundSetter
    public void setInfraLimitMedium(int infraLimitMedium) {
        this.infraLimitMedium = infraLimitMedium;
        save();
    }

    @CheckForNull
    public int getInfraLimitLow() {
        return infraLimitLow;
    }

    @DataBoundSetter
    public void setInfraLimitLow(int infraLimitLow) {
        this.infraLimitLow = infraLimitLow;
        save();
    }

        //     Policy Section
    @CheckForNull
    public int getPolicyLimitCritical() {
        return policyLimitCritical;
    }

    @DataBoundSetter
    public void setPolicyLimitCritical(int policyLimitCritical) {
        this.policyLimitCritical = policyLimitCritical;
        save();
    }

    @CheckForNull
    public int getPolicyLimitHigh() {
        return policyLimitHigh;
    }

    @DataBoundSetter
    public void setPolicyLimitHigh(int policyLimitHigh) {
        this.policyLimitHigh = policyLimitHigh;
        save();
    }

    @CheckForNull
    public int getPolicyLimitMedium() {
        return policyLimitMedium;
    }

    @DataBoundSetter
    public void setPolicyLimitMedium(int policyLimitMedium) {
        this.policyLimitMedium = policyLimitMedium;
        save();
    }

    @CheckForNull
    public int getPolicyLimitLow() {
        return policyLimitLow;
    }

    @DataBoundSetter
    public void setPolicyLimitLow(int policyLimitLow) {
        this.policyLimitLow = policyLimitLow;
        save();
    }

}