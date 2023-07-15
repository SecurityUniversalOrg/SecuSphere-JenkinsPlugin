package io.jenkins.plugins.secusphere;

import hudson.model.Action;
import org.json.JSONObject;

public class SecuSphereSidePanel implements Action {
    private final String resultString;
    private final String category;

    public SecuSphereSidePanel(JSONObject result, String category) {
        this.resultString = result.toString();
        this.category = category;
    }

    public JSONObject getResult() {
        return new JSONObject(this.resultString);
    }

    public String getCriticalFindingCnt() {
        return getResult().get("Critical").toString();
    }

    public String getHighFindingCnt() {
        return getResult().get("High").toString();
    }

    public String getMediumFindingCnt() {
        return getResult().get("Medium").toString();
    }

    public String getLowFindingCnt() {
        return getResult().get("Low").toString();
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

    public String getCategory() {
        return category;
    }

}
