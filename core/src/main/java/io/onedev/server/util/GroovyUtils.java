package io.onedev.server.util;

import java.util.HashMap;
import java.util.Map;

import org.unbescape.java.JavaEscape;

import com.google.common.collect.MapMaker;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import io.onedev.server.exception.ScriptException;

public class GroovyUtils {
	
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
				if (name.equals("onedev")) 
					return OneContext.get();
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
    
    public static Object evalScript(String scriptText, Map<String, Object> variables) {
    	try {
	    	Class<?> scriptClass = compile(scriptText);
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
			throw new ScriptException(scriptText, e);
		}
    }
    
    public static Object evalScript(String scriptText) {
    	return evalScript(scriptText, new HashMap<>());
    }
    
    public static String evalGString(String gstring, Map<String, Object> variables) {
    	if (gstring.contains("$")) 
    		return evalScript("\"" + JavaEscape.escapeJava(gstring) + "\"", variables).toString();
    	else
    		return gstring;
    }
    
    public static String evalGString(String gstring) {
    	return evalGString(gstring, new HashMap<>());
    }
    
}
