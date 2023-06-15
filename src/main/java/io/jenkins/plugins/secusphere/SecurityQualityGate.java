package io.jenkins.plugins.secusphere;

import org.json.JSONObject;

public class SecurityQualityGate {

    public static boolean enforceGate(String severity, JSONObject summaryReport, SecuSphereGlobalConfiguration globalConfig) {
        String category = summaryReport.getString("Assessment_Category");
        boolean enforcing = globalConfig.getEnforceSecurityGates();
        if (enforcing) {
            int total = 0;
            // Get the thresholds from the global configuration
            if ("Critical".equals(severity)) {
                if ("Container".equals(category)) {
                    total = globalConfig.getContainerLimitCritical();
                } else if ("Secrets".equals(category)) {
                    total = globalConfig.getSecretLimitCritical();
                }
            } else if ("High".equals(severity)) {
                if ("Container".equals(category)) {
                    total = globalConfig.getContainerLimitHigh();
                } else if ("Secrets".equals(category)) {
                    total = globalConfig.getSecretLimitHigh();
                }
            }

            // Check if the findings exceed the threshold
            if (summaryReport.getInt(severity) > total) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }
}
