package org.aecid.alfresco.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CheckIfDocIsEmailedEvaluator extends BaseEvaluator {
    private static final String ASPECT_EMAILED = "cm:emailed";

    public boolean evaluate(JSONObject jsonObject) {
        try {
            JSONArray nodeAspects = getNodeAspects(jsonObject);
            if (nodeAspects == null) {
                return false;
            } else {
                if (nodeAspects.contains(ASPECT_EMAILED)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception err) {
            throw new RuntimeException("JSONException whilst running action evaluator: " + err.getMessage());
        }
    }
}