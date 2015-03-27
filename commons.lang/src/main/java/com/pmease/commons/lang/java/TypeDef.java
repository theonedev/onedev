package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.pmease.commons.lang.Symbol;

public class TypeDef extends Symbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	public Kind kind;
	
	public List<Modifier> modifiers = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");

		if (kind == Kind.ANNOTATION)
			builder.append("@interface").append(" ");
		else
			builder.append(kind.toString().toLowerCase()).append(" ");
		builder.append(name).append(" {\n\n");
		
		List<String> enumConstants = new ArrayList<>();
		for (Symbol child: children) {
			if (child instanceof FieldDef) {
				FieldDef fieldDef = (FieldDef) child;
				if (fieldDef.type == null)  
					enumConstants.add(fieldDef.name);
			}
		}
		if (!enumConstants.isEmpty())
			builder.append("  ").append(Joiner.on(", ").join(enumConstants)).append(";\n\n");
		else if (kind == Kind.ENUM)
			builder.append("  ;\n\n");
		for (Symbol child: children) {
			if (child instanceof FieldDef) {
				FieldDef fieldDef = (FieldDef) child;
				if (fieldDef.type != null)
					builder.append("  ").append(fieldDef).append("\n\n");
			}
		}
		for (Symbol child: children) { 
			if (child instanceof MethodDef) {
				MethodDef methodDef = (MethodDef) child;
				builder.append("  ").append(methodDef).append("\n\n");
			}
		}
		for (Symbol child: children) {
			if (child instanceof TypeDef) {
				TypeDef typeDef = (TypeDef) child;
				for (String line: Splitter.on('\n').omitEmptyStrings().split(typeDef.toString()))
					builder.append("  ").append(line).append("\n\n");
			}
		}
		
		builder.append("}");
		
		return builder.toString();
	}
	
	@Override
	public Component render(String componentId) {
		return null;
	}
}
