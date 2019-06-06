package com.sap.cloud.lm.sl.cf.core.helpers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MapToEnvironmentConverter {

    private final ObjectToEnvironmentValueConverter objectToEnvironmentValueConverter;

    public MapToEnvironmentConverter(boolean prettyPrinting) {
        this.objectToEnvironmentValueConverter = new ObjectToEnvironmentValueConverter(prettyPrinting);
    }

    public Map<String, String> asEnv(Map<String, Object> map) {
        Map<String, String> result = new TreeMap<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            String value = objectToEnvironmentValueConverter.convert(entry.getValue());
            result.put(entry.getKey(), value);
        }
        return result;
    }

}
