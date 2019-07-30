package com.sap.cloud.lm.sl.cf.core.filters;

import java.util.Map;
import java.util.function.BiPredicate;

import com.sap.cloud.lm.sl.common.util.JsonUtil;

public class ContentFilter implements BiPredicate<String, Map<String, Object>> {

    @Override
    public boolean test(String content, Map<String, Object> requiredProperties) {
        if (requiredProperties == null || requiredProperties.isEmpty()) {
            return true;
        }
        Map<String, Object> parsedContent = getParsedContent(content);
        if (parsedContent == null) {
            return false;
        }
        return requiredProperties.entrySet()
            .stream()
            .allMatch(requiredEntry -> exists(parsedContent, requiredEntry));
    }

    private boolean exists(Map<String, Object> content, Map.Entry<String, Object> requiredEntry) {
        Object actualValue = content.get(requiredEntry.getKey());
        return actualValue != null && actualValue.equals(requiredEntry.getValue());
    }

    private Map<String, Object> getParsedContent(String content) {
        if (content == null) {
            return null;
        }
        try {
            return JsonUtil.convertJsonToMap(content);
        } catch (Exception e) {
            return null;
        }
    }

}
