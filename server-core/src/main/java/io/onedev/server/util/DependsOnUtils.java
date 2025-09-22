package io.onedev.server.util;

import io.onedev.server.annotation.DependsOn;

public class DependsOnUtils {
    
    public static boolean isPropertyVisible(DependsOn dependsOn, Class<?> dependencyPropertyType, Object dependencyPropertyValue) {
        if (dependsOn.value().length() != 0) {
            if (dependencyPropertyValue != null && dependencyPropertyValue.toString().equals(dependsOn.value())) {
                if (dependsOn.inverse())
                    return false;
            } else if (!dependsOn.inverse()) {
                return false;
            }
        } else {
            if (dependencyPropertyType == boolean.class) {
                boolean requiredPropertyValue = !dependsOn.inverse();
                if (requiredPropertyValue != (boolean)dependencyPropertyValue)
                    return false;
            } else if (dependencyPropertyType == int.class || dependencyPropertyType == long.class || dependencyPropertyType == double.class || dependencyPropertyType == float.class) {
                int dependencyPropertyIntValue = (int) dependencyPropertyValue;
                if (dependsOn.inverse() && dependencyPropertyIntValue != 0 || !dependsOn.inverse() && dependencyPropertyIntValue == 0)
                    return false;
            } else {
                if (dependsOn.inverse() && dependencyPropertyValue != null || !dependsOn.inverse() && dependencyPropertyValue == null)
                    return false;
            }
        }
        return true;
    }

}