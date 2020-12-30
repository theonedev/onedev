package io.onedev.server.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.script.ScriptContribution;
import io.onedev.server.util.script.identity.ScriptIdentity;

public class GroovyUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(GroovyUtils.class);
	
	private static Map<String, Class<?>> scriptClassCache = new MapMaker().weakValues().makeMap();
	
    public static Class<?> compile(String script) {
		Class<?> scriptClass = scriptClassCache.get(script);
		if (scriptClass == null) {
			try (GroovyClassLoader classLoader = new GroovyClassLoader(GroovyUtils.class.getClassLoader())) {
				scriptClass = classLoader.parseClass(script);
				scriptClassCache.put(script, scriptClass);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} 
		return scriptClass;
    }
    
    private static Binding getBinding(Map<String, Object> variables) {
    	return new Binding() {

			@Override
			public Object getVariable(String name) {
				if (name.equals("logger")) 
					return logger;
				else if (variables.containsKey(name))
					return variables.get(name);
				else if (name.equals("editContext")) 
					return EditContext.get();
				else
					return super.getVariable(name);
			}
			
			@Override
			public void setVariable(String name, Object value) {
				throw new UnsupportedOperationException();
			}
			
		};    	
    }
    
    public static Object evalScriptByName(String scriptName) {
    	return evalScriptByName(scriptName, new HashMap<>());
    }

    public static Object evalScriptByName(String scriptName, Map<String, Object> variables) {
    	GroovyScript script = null;
    	if (scriptName.startsWith(GroovyScript.BUILTIN_PREFIX)) {
    		String builtInScriptName = scriptName.substring(GroovyScript.BUILTIN_PREFIX.length());
        	for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
        		if (contribution.getScript().getName().equals(builtInScriptName)) {
        			script = contribution.getScript();
        			break;
        		}
        	}
    	} else {
        	for (GroovyScript each: OneDev.getInstance(SettingManager.class).getGroovyScripts()) {
        		if (each.getName().equals(scriptName)) {
        			script = each;
        			break;
        		}
        	}
    	}
    	if (script != null) {
    		if (script.isAuthorized(ScriptIdentity.get())) {
    			try {
    				return evalScript(StringUtils.join(script.getContent(), "\n"), variables);
    			} catch (Exception e) {
    				throw new RuntimeException("Error evaluating groovy script: " + scriptName, e);
    			}
    		} else {
    			throw new ExplicitException("Unauthorized groovy script: " + scriptName);
    		}
    	} else {
    		throw new ExplicitException("Groovy script not found: " + scriptName);
    	}
    }
    
    public static Object evalScript(String scriptContent, Map<String, Object> variables) {
    	try {
	    	Class<?> scriptClass = compile(scriptContent);
			Script script;
			try {
				Object instance = scriptClass.newInstance();
				if (!(instance instanceof Script))
					return scriptClass;
				else 
					script = (Script) instance;					
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			script.setBinding(getBinding(variables));
			return script.run();
		} catch (RuntimeException e) {
			throw new ScriptException(scriptContent, e);
		}
    }
    
    public static Object evalScript(String scriptContent) {
    	return evalScript(scriptContent, new HashMap<>());
    }
    
}