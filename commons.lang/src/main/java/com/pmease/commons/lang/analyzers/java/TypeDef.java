package com.pmease.commons.lang.analyzers.java;

import java.util.ArrayList;
import java.util.List;

public class TypeDef {

	public enum Kind {
		CLASS, 
		INTERFACE, 
		ANNOTATION {

			@Override
			public String toString() {
				return "@interface";
			}
			
		}, 
		ENUM
	};
	
	public String name;
	
	public Kind kind;

	public List<FieldDef> fieldDefs = new ArrayList<>();
	
	public List<MethodDef> methodDefs = new ArrayList<>();
	
	public List<TypeDef> typeDefs = new ArrayList<>();
	
	public int line;
	
	public List<Modifier> modifiers = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");
		builder.append(kind.toString().toLowerCase()).append(" ");
		builder.append(name).append(" {\n\n");
		for (FieldDef fieldDef: fieldDefs) 
			builder.append("\t").append(fieldDef).append("\n\n");
		for (MethodDef methodDef: methodDefs) 
			builder.append("\t").append(methodDef).append("\n\n");
		for (TypeDef typeDef: typeDefs) 
			builder.append("\t").append(typeDef).append("\n\n");
		
		builder.append("}");
		
		return builder.toString();
	}
	
}
