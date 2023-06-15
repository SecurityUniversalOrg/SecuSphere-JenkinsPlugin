package io.jenkins.plugins.secusphere;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class SecuSphereResultsUpdater {

    public static void updateResultsFile(String category, JSONObject updatedCounts, File resultsFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode report;

        if (resultsFile.exists()) {
            report = (ObjectNode) objectMapper.readTree(resultsFile);
        } else {
            report = objectMapper.createObjectNode();
        }

        // Convert keys in updatedCounts to lowercase
        JSONObject updatedCountsLowercase = new JSONObject();
        for (String key : updatedCounts.keySet()) {
            updatedCountsLowercase.put(key.toLowerCase(), updatedCounts.get(key));
        }

        // Convert the JSONObject updatedCountsLowercase to JsonNode
        JsonNode updatedCountsNode = objectMapper.readTree(updatedCountsLowercase.toString());

        // Update the "report" object with the new counts for the given category
        ObjectNode reportNode = (ObjectNode) report.get("report");
        reportNode.set(category.toLowerCase(), updatedCountsNode);

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(resultsFile, report);
    }

    public void archiveResultsFile(Run<?, ?> run, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        FilePath threatBusterResultsFile = workspace.child("threatbuster_results.json");

        if (threatBusterResultsFile.exists()) {
            FilePath artifactsDir = new FilePath(run.getArtifactsDir());
            artifactsDir.mkdirs();
            FilePath target = artifactsDir.child("threatbuster_results.json");
            threatBusterResultsFile.copyTo(target);
            listener.getLogger().println("Archived threatbuster_results.json");
        } else {
            listener.getLogger().println("threatbuster_results.json not found. Nothing to archive.");
        }
    }


    public void unarchiveResultsFile(Run<?, ?> run, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        FilePath artifactsDir = new FilePath(run.getArtifactsDir());
        FilePath source = artifactsDir.child("threatbuster_results.json");
        FilePath target = workspace.child("threatbuster_results.json");

        if (source.exists()) {
            source.copyTo(target);
            listener.getLogger().println("Unarchived threatbuster_results.json");
        } else {
            listener.getLogger().println("No archived threatbuster_results.json found. A new file will be created.");

            // Create a new file and write an empty JSON object to it
            target.write("{ \"report\": {\"secret\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"sca\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"sast\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"iac\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"container\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"infrastructure\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"dast\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0},\"dastapi\": {\"low\": 0, \"medium\": 0, \"high\": 0, \"critical\": 0}	}}", null);
        }
    }


}
