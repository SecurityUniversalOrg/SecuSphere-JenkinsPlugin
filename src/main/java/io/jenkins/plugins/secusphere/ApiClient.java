package io.jenkins.plugins.secusphere;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private String vulnManagerUrl;
    private String clientId;
    private String clientSecret;
    private String apiKey;
    private Map<String, String> request_headers;

    private static final String[] OAUTH_SCOPES = {"read:dockerimages", "write:dockerimages", "write:vulnerabilityscans",
            "write:vulnerabilities", "write:pipelinejobs", "write:appcodecomposition", "write:releaseversions",
            "write:servicetickets", "read:businessapplications"};

    public ApiClient(String clientId, String clientSecret, String vulnManagerUrl) {
        this.vulnManagerUrl = vulnManagerUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.apiKey = getAccessToken();
        this.request_headers = new HashMap<String, String>();
        this.request_headers.put("Authorization", "Bearer " + this.apiKey);
    }

    public String getAccessToken() {
        String url = this.vulnManagerUrl + "/oauth/token";
        List<NameValuePair> data = new ArrayList<NameValuePair>();
        data.add(new BasicNameValuePair("grant_type", "client_credentials"));
        data.add(new BasicNameValuePair("client_id", this.clientId));
        data.add(new BasicNameValuePair("client_secret", this.clientSecret));
        data.add(new BasicNameValuePair("scope", String.join(" ", OAUTH_SCOPES)));
        String response = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(data, StandardCharsets.UTF_8));
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                response = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject jsonResponse = new JSONObject(response);
        return jsonResponse.getString("access_token"); // Extract the access token value from the JSON response
    }

    public String sendGet(String endpoint) {
        String url = this.vulnManagerUrl + "/" + endpoint;
        String response = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            for (Map.Entry<String, String> entry : this.request_headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                response = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String sendSearch(String endpoint, String field, String value) {
        String url = this.vulnManagerUrl + "/" + endpoint;
        JSONObject data = new JSONObject();
        if (field.contains(":")) {
            String[] fields = field.split(":");
            String[] values = value.split(":");
            for (int i = 0; i < fields.length; i++) {
                data.put(fields[i], values[i]);
            }
        } else {
            data.put(field, value);
        }
        String response = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            for (Map.Entry<String, String> entry : this.request_headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
            StringEntity entity = new StringEntity(data.toString());
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                response = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String sendPost(String endpoint, JSONObject data) {
        String url = this.vulnManagerUrl + "/" + endpoint;
        String response = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            for (Map.Entry<String, String> entry : this.request_headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
            StringEntity entity = new StringEntity(data.toString());
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                response = EntityUtils.toString(httpResponse.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

