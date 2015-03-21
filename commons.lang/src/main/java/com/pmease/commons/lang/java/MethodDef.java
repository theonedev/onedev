package com.pmease.commons.lang.java;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Symbol;

public class MethodDef extends Symbol {

	@Nullable
	public String type; // null for constructor
	
	@Nullable
	public String params;

	public List<Modifier> modifiers = new ArrayList<>();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");
		if (type != null)
			builder.append(type).append(" ");
		builder.append(name);
		if (params != null)
			builder.append("(").append(params).append(");");
		else
			builder.append("();");
		return builder.toString();
	}

	@Override
	public Component render(String componentId) {
		return null;
	}
	
}
