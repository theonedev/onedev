package io.onedev.server.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
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
			scriptClass = new GroovyClassLoader(GroovyUtils.class.getClassLoader()).parseClass(script);
			scriptClassCache.put(script, scriptClass);
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
    	if (scriptName.startsWith(GroovyScript.BUILTIN_PREFIX)) {
    		scriptName = scriptName.substring(GroovyScript.BUILTIN_PREFIX.length());
        	for (ScriptContribution contribution: OneDev.getExtensions(ScriptContribution.class)) {
        		GroovyScript script = contribution.getScript();
        		if (script.getName().equals(scriptName) && script.isAuthorized(ScriptIdentity.get())) {
        			try {
        				return evalScript(StringUtils.join(script.getContent(), "\n"), variables);
        			} catch (Exception e) {
        				throw new OneException("Error evaluating builtin groovy script: " + scriptName, e);
        			}
        		}
        	}
    	}
    	for (GroovyScript script: OneDev.getInstance(SettingManager.class).getGroovyScripts()) {
    		if (script.getName().equals(scriptName) && script.isAuthorized(ScriptIdentity.get())) {
    			try {
    				return evalScript(StringUtils.join(script.getContent(), "\n"), variables);
    			} catch (Exception e) {
    				throw new OneException("Error evaluating named groovy script: " + scriptName, e);
    			}
    		}
    	}
    	throw new OneException("No authorized groovy script found: " + scriptName);
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