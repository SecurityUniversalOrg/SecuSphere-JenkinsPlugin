package io.jenkins.plugins.secusphere;

import hudson.model.Action;
import org.json.JSONObject;

import java.io.IOException;

public class SecuSphereSidePanel implements Action {
    private final JSONObject result;
    private final String category;

    public SecuSphereSidePanel(JSONObject result, String category) {
        this.result = result;
        this.category = category;
    }

    public JSONObject getResult() {
        return result;
    }

    public String getCriticalFindingCnt() {
        return result.get("Critical").toString();
    }

    public String getHighFindingCnt() {
        return result.get("High").toString();
    }

    public String getMediumFindingCnt() {
        return result.get("Medium").toString();
    }

    public String getLowFindingCnt() {
        return result.get("Low").toString();
    }

    @Override
    public String getIconFileName() {
        return "/plugin/secusphere/images/security.png";
    }

    @Override
    public String getDisplayName() {
        return "SecuSphere-" + category;
    }

    @Override
    public String getUrlName() {
        return "secusphere-" + category.toLowerCase();
    }

}
