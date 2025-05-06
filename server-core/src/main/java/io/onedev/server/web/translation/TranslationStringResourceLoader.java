package io.onedev.server.web.translation;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.resource.loader.IStringResourceLoader;

public class TranslationStringResourceLoader implements IStringResourceLoader {

    @Override
    public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
        return loadStringResource(key, locale);
    }

    @Override
    public String loadStringResource(Component component, String key, Locale locale, String style, String variation) {
        return loadStringResource(key, locale);
    }

    private String loadStringResource(String key, Locale locale) {
        var noCacheControl = new ResourceBundle.Control() {
            @Override
            public long getTimeToLive(String baseName, Locale locale) {
                return TTL_DONT_CACHE;
            }
        };        
        
        ResourceBundle resourceBundle;
        if (Application.get().getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) 
            resourceBundle = ResourceBundle.getBundle(Translation.class.getName(), locale, noCacheControl);
        else
            resourceBundle = ResourceBundle.getBundle(Translation.class.getName(), locale);

        if (key.startsWith("t:")) {
            key = key.substring(2);
            if (key.startsWith(" "))
                key = key.substring(1);
            if (resourceBundle.containsKey(key))
                return resourceBundle.getString(key);
            else
                return key;
        } else {
            return null;
        }            
        
    }
        
}
