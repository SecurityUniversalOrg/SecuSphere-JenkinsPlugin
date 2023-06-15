package io.jenkins.plugins.secusphere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import hudson.model.TaskListener;
import org.json.JSONObject;

public class SummaryReportHandler {
    private JSONObject summaryReport;

    public SummaryReportHandler(JSONObject summaryReport) {
        this.summaryReport = summaryReport;
    }

    public void submitToCsvFile(String gitRepoUrl, String csvFilePath, String gitUsername, String gitPassword, TaskListener listener, String appName) throws IOException {
        // Step 1: Clone the GitHub repository locally using JGit
        File localRepo = new File("local-repo");

        // Delete the existing local-repo directory if it exists
        if (localRepo.exists()) {
            deleteDirectory(localRepo);
        }

        try (Git git = Git.cloneRepository().setURI(gitRepoUrl).setDirectory(localRepo)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword)).call()) {

            // Update the csvFilePath to include the localRepo path
            String fullPath = Paths.get(localRepo.getPath(), csvFilePath).toString();

            // Step 2: Add the summaryReport JSON object as a new row in the CSV file
            appendJsonToCsvFile(summaryReport, fullPath, appName);

            // Step 3: Push the changes back to the GitHub repository using JGit
            git.add().addFilepattern(csvFilePath).call();
            git.commit().setMessage("Update CSV file with new summary report").call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitUsername, gitPassword)).call();
        } catch (GitAPIException e) {
            listener.getLogger().printf("Error working with the Git repository: %s%n", e.getMessage());
            e.printStackTrace(listener.getLogger());
            throw new IOException("Error working with the Git repository", e);
        }
    }

    private void appendJsonToCsvFile(JSONObject json, String csvFilePath, String appName) throws IOException {
        File csvFile = new File(csvFilePath);
        boolean isNewFile = !csvFile.exists();

        if (isNewFile) {
            // Create the file if it does not exist
            boolean created = csvFile.createNewFile();
            if (!created) {
                throw new IOException("Failed to create the CSV file: " + csvFilePath);
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(csvFile, true), StandardCharsets.UTF_8)) {
            if (isNewFile) {
                // Write CSV header if the file is new
                writer.write("AppID,Timestamp,High,Medium,Low,Critical,Assessment_Category\n");
            }

            // Generate the current timestamp
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestamp = now.format(formatter);

            // Convert the JSON object to a CSV row
            writer.write(appName + ",");
            writer.write(timestamp + ",");
            writer.write(json.optString("High", "") + ",");
            writer.write(json.optString("Medium", "") + ",");
            writer.write(json.optString("Low", "") + ",");
            writer.write(json.optString("Critical", "") + ",");
            writer.write(json.optString("Assessment_Category", "") + "\n");

            // Flush the writer to ensure all data is written to the file
            writer.flush();
        }
    }


    private static void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            throw new IOException("Failed to delete file: " + file);
                        }
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory);
        }
    }
}
