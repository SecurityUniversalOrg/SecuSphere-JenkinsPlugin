package io.jenkins.plugins.secusphere;

import org.json.JSONObject;


public class ReportResult {
    private JSONObject wrappedFindings;
    private JSONObject summaryReport;

    public ReportResult(JSONObject wrappedFindings, JSONObject summaryReport) {
        this.wrappedFindings = wrappedFindings;
        this.summaryReport = summaryReport;
    }

    public JSONObject getWrappedFindings() {
        return wrappedFindings;
    }

    public JSONObject getSummaryReport() {
        return summaryReport;
    }
}