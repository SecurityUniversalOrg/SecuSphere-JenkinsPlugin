package io.jenkins.plugins.secusphere;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import hudson.model.TaskListener;


public class ReportRunner {
    private final TaskListener listener;
    private final String CYCLONE_DX;
    private final String ANCHORE;

    public ReportRunner(TaskListener listener) {
        this.listener = listener;
        CYCLONE_DX = null;
        ANCHORE = null;
    }

    public JSONObject readInXmlFile(String filePath) throws IOException {
        String xmlInput = FileUtils.readFileToString(new File(filePath), "UTF-8");
        JSONObject json = XML.toJSONObject(xmlInput);
        return json;
    }

    public String readInJsonFile(String filePath) {
        String jsonString = "";

        try {
            jsonString = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Handle the IOException here, e.g., log a message or throw a custom exception
        }

        System.out.println("JSON content: " + jsonString);
        return jsonString;
    }

    public JSONObject readInCyclonedxReport() throws IOException {
        String endpoint = "sbom";
        JSONObject input = readInXmlFile(CYCLONE_DX);
        return input;
    }

    public ReportResult readInReport(String filePath, String reportType) throws IOException {
        String inputString = readInJsonFile(filePath);

        // Ensure that the input string is a valid JSON string
        if (inputString == null || inputString.isEmpty() || inputString.charAt(0) != '{') {
            throw new IOException("Invalid JSON string: " + inputString);
        }

        JSONObject input = new JSONObject(inputString);
        JSONArray mappedFindings = mapFields(reportType, input);
        JSONObject wrappedFindings = new JSONObject();
        wrappedFindings.put("findings", mappedFindings);
        String category = mapCategory(reportType);
        JSONObject findingSummary =  summarizeFindings(mappedFindings, category);
        return new ReportResult(wrappedFindings, findingSummary);
    }

    public JSONObject summarizeFindings(JSONArray findings, String category) {
        JSONObject summarizedFindings = new JSONObject();
        summarizedFindings.put("Assessment_Category", category.toString());
        summarizedFindings.put("Critical", 0);
        summarizedFindings.put("High", 0);
        summarizedFindings.put("Medium", 0);
        summarizedFindings.put("Low", 0);
        System.out.println(findings.toString(2));
        listener.getLogger().printf("Total Findings: " + findings.length() + "%n");

        for (int i = 0; i < findings.length(); i++) {
            JSONObject finding = findings.getJSONObject(i);
            String severity = finding.getString("Severity");

            if ("Critical".equals(severity)) {
                summarizedFindings.put("Critical", summarizedFindings.getInt("Critical") + 1);
            } else if ("High".equals(severity)) {
                summarizedFindings.put("High", summarizedFindings.getInt("High") + 1);
            } else if ("Medium".equals(severity)) {
                summarizedFindings.put("Medium", summarizedFindings.getInt("Medium") + 1);
            } else if ("Low".equals(severity)) {
                summarizedFindings.put("Low", summarizedFindings.getInt("Low") + 1);
            }
        }
        return summarizedFindings;
    }

    public String readInConfigFile(String filePath) {
        StringBuilder jsonContent = new StringBuilder();

        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + filePath);
            } else {
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    jsonContent.append(line);
                }
                br.close();
            }
        } catch (IOException e) {
            // Handle the IOException here, e.g., log a message or throw a custom exception
            System.err.println("Error reading JSON file: " + e.getMessage());
        }
        String jsonString = jsonContent.toString();
        System.out.println("JSON content: " + jsonString);
        return jsonString;
    }

    public String mapDockerImage(String source, String filePath) throws IOException {
        JSONObject newFields = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/field_maps/" + source + ".json"));
        String dockerImgLocation = newFields.getString("docker_img_name_location");

        String inputString = readInJsonFile(filePath);

        // Ensure that the input string is a valid JSON string
        if (inputString == null || inputString.isEmpty() || inputString.charAt(0) != '{') {
            throw new IOException("Invalid JSON string: " + inputString);
        }

        JSONObject input = new JSONObject(inputString);

        String dockerImgName = getNestedJSONValue(input, dockerImgLocation);
        return dockerImgName;
    }

    public static String getNestedJSONValue(JSONObject jsonObject, String path) {
        String[] keys = path.split("\\.");
        JSONObject currentObject = jsonObject;

        for (int i = 0; i < keys.length - 1; i++) {
            currentObject = currentObject.getJSONObject(keys[i]);
        }

        return currentObject.getString(keys[keys.length - 1]);
    }

    public JSONArray mapFields(String source, JSONObject findingData) throws IOException {
        JSONObject newFields = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/field_maps/" + source + ".json"));
        JSONObject severityFields = mapSeverityFields(source);

        String vulnerabilityListLocation = newFields.getString("vulnerability_list_location");
        JSONArray vulnerabilityList = getNestedJSONArray(findingData, vulnerabilityListLocation);

        JSONArray mappedFindings = mapFindings(source, newFields, severityFields, vulnerabilityList);
        return mappedFindings;
    }

    public JSONArray getNestedJSONArray(JSONObject jsonObject, String path) {
        String[] keys = path.split("\\.");
        JSONObject currentObject = jsonObject;

        for (int i = 0; i < keys.length - 1; i++) {
            currentObject = currentObject.getJSONObject(keys[i]);
        }

        return currentObject.getJSONArray(keys[keys.length - 1]);
    }


    public JSONObject mapSeverityFields(String source) throws IOException {
        JSONObject severityFields = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/severity_maps/" + source + ".json"));
        return severityFields;
    }

    public String mapReportFilepath(String source) throws IOException {
        JSONObject map = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/field_maps/" + source + ".json"));
        String filePath = (String) map.get("file_path"); // Cast the returned object to String
        return filePath;
    }

    public String mapCategory(String source) throws IOException {
        JSONObject map = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/field_maps/" + source + ".json"));
        JSONObject vulnerabilityFields = map.getJSONObject("vulnerability_fields");
        String category = vulnerabilityFields.getString("Classification"); // Cast the returned object to String
        return category;
    }

    public JSONObject mapSeverity(JSONObject newFinding, JSONObject severityFields, String source) {
        JSONObject severityMap = new JSONObject();
        for (String key : severityFields.keySet()) {
            if (severityFields.has(key)) {
                severityMap.put(severityFields.getString(key).toLowerCase(), key);
            }
        }
        String newFindingSeverity = newFinding.has("Severity") ? newFinding.getString("Severity") : "";
        String newFindingSeverityLower = newFindingSeverity.toLowerCase();
        if (severityMap.has(newFindingSeverityLower)) {
            newFinding.put("Severity", severityMap.getString(newFindingSeverityLower));
        }
        return newFinding;
    }

    public JSONObject addDateFields(JSONObject newFinding, String now) {
        newFinding.put("ReleaseDate", now);
        newFinding.put("LastModifiedDate", now);
        newFinding.put("AddDate", now);
        return newFinding;
    }

    public JSONArray mapFindings(String source, JSONObject newFields, JSONObject severityFields, JSONArray findingData) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = sdf.format(new Date());
        JSONArray mappedFindings = new JSONArray();
        String templateName = "TEMPLATE";
        JSONObject newFindingTemplate = new JSONObject(readInConfigFile("/io/jenkins/plugins/secusphere/ReportRunner/field_maps/" + templateName + ".json"));
        JSONObject vulnerabilityFields = newFields.getJSONObject("vulnerability_fields");

        for (int i = 0; i < findingData.length(); i++) {
            JSONObject v = findingData.getJSONObject(i);
            JSONObject newFinding = new JSONObject(newFindingTemplate.getJSONObject("vulnerability_fields").toString());

            for (String key : vulnerabilityFields.keySet()) {
                String field = vulnerabilityFields.getString(key);
                if (field.contains("{") && field.contains("}")) {
                    Pattern pattern = Pattern.compile("\\{(.+?)\\}");
                    Matcher matcher = pattern.matcher(field);
                    List<String> keys = new ArrayList<>();
                    while (matcher.find()) {
                        keys.add(matcher.group(1));
                    }

                    JSONObject formattedValues = new JSONObject();
                    for (String k : keys) {
                        if (checkKeys(k, v)) {
                            Object value = getValueForKeyPath(k, v);
                            formattedValues.put(k, value);
                        }
                    }

                    StringBuffer outputStringBuffer = new StringBuffer();
                    Matcher replacementMatcher = Pattern.compile("\\{(.+?)\\}").matcher(field);
                    while (replacementMatcher.find()) {
                        String anotherKey  = replacementMatcher.group(1);
                        String replacement = formattedValues.optString(anotherKey , replacementMatcher.group(0));
                        replacement = replacement.replace("$", "\\$");
                        replacementMatcher.appendReplacement(outputStringBuffer, replacement);
                    }
                    replacementMatcher.appendTail(outputStringBuffer);
                    String outputString = outputStringBuffer.toString();

                    if (outputString != null && !outputString.isEmpty()) {
                        newFinding.put(key, outputString);
                    }
                } else {
                    List<String> keysToSetToNull = Arrays.asList(
                        "SourceCodeFileStartLine",
                        "SourceCodeFileStartCol",
                        "SourceCodeFileEndLine",
                        "SourceCodeFileEndCol"
                    );

                    if (keysToSetToNull.contains(key) && field.isEmpty()) {
                        newFinding.remove(key);
                    } else {
                        newFinding.put(key, field);
                    }
                }
            }
            newFinding = mapSeverity(newFinding, severityFields, source);
            newFinding = addDateFields(newFinding, now);
            mappedFindings.put(newFinding);
        }
        return mappedFindings;
    }

    private boolean checkKeys(String keyPath, JSONObject jsonObject) {
        String[] keyParts = keyPath.split("\\.");
        JSONObject currentObject = jsonObject;
        for (String part : keyParts) {
            if (!currentObject.has(part)) {
                return false;
            }
            currentObject = currentObject.optJSONObject(part);
            if (currentObject == null) {
                break;
            }
        }
        return true;
    }

    private Object getValueForKeyPath(String keyPath, JSONObject obj) {
        String[] keys = keyPath.split("\\.");
        Object currentObj = obj;
        for (String key : keys) {
            if (currentObj instanceof JSONObject) {
                JSONObject currentJSONObj = (JSONObject) currentObj;
                if (key.contains("[")) {
                    String[] keyAndIndex = key.split("\\[|\\]");
                    String arrayKey = keyAndIndex[0];
                    int index = Integer.parseInt(keyAndIndex[1]);
                    JSONArray jsonArray = currentJSONObj.getJSONArray(arrayKey);
                    if (jsonArray.get(index) instanceof String) {
                        currentObj = jsonArray.getString(index);
                    } else if (jsonArray.get(index) instanceof JSONArray) {
                        JSONArray innerArray = jsonArray.getJSONArray(index);
                        if (innerArray.length() > 0 && innerArray.get(0) instanceof String) {
                            currentObj = innerArray.getString(0);
                        } else {
                            currentObj = innerArray.get(0);
                        }
                    } else {
                        currentObj = jsonArray.get(index);
                    }
                } else {
                    currentObj = currentJSONObj.get(key);
                    if (currentObj instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) currentObj;
                        if (jsonArray.length() == 1 && jsonArray.get(0) instanceof String) {
                            currentObj = jsonArray.getString(0);
                        }
                    }
                }
            }
        }
        return currentObj;
    }

    private String performFieldSubstitution(String template, JSONObject findingData) {
        String result = template;
        String pattern = "\\{(.+?)\\}";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(template);

        while (matcher.find()) {
            String keyPath = matcher.group(1);
            Object value = getValueForKeyPath(keyPath, findingData);

            String valueStr;
            if (value instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) value;
                if (jsonArray.length() == 1 && jsonArray.get(0) instanceof String) {
                    valueStr = jsonArray.getString(0);
                } else {
                    valueStr = jsonArray.toString();
                }
            } else {
                valueStr = value.toString();
            }

            result = result.replace("{" + keyPath + "}", valueStr);
        }

        return result;
    }

}


