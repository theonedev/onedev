package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.pmease.commons.lang.Symbol;

public class TypeDef extends Symbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	private final Kind kind;
	
	private final List<Modifier> modifiers;

	public TypeDef(@Nullable Symbol parent, String name, int lineNo, Kind kind, List<Modifier> modifiers) {
		super(parent, name, lineNo);
		
		this.kind = kind;
		this.modifiers = modifiers;
	}
	
	public Kind getKind() {
		return kind;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public Component render(String componentId) {
		return null;
	}

	@Override
	public String describe(List<Symbol> symbols) {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");

		if (kind == Kind.ANNOTATION)
			builder.append("@interface").append(" ");
		else
			builder.append(kind.toString().toLowerCase()).append(" ");
		builder.append(getName()).append(" {\n\n");
		
		List<String> enumConstants = new ArrayList<>();
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == this && (symbol instanceof FieldDef)) {
				FieldDef fieldDef = (FieldDef) symbol;
				if (fieldDef.getType() == null)  
					enumConstants.add(fieldDef.getName());
			}
		}
		if (!enumConstants.isEmpty())
			builder.append("  ").append(Joiner.on(", ").join(enumConstants)).append(";\n\n");
		else if (kind == Kind.ENUM)
			builder.append("  ;\n\n");
		
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == this && (symbol instanceof FieldDef)) {
				FieldDef fieldDef = (FieldDef) symbol;
				if (fieldDef.getType() != null)
					builder.append("  ").append(fieldDef.describe(symbols)).append("\n\n");
			}
		}
		
		for (Symbol symbol: symbols) { 
			if (symbol.getParent() == this && (symbol instanceof MethodDef)) {
				MethodDef methodDef = (MethodDef) symbol;
				builder.append("  ").append(methodDef.describe(symbols)).append("\n\n");
			}
		}

		for (Symbol symbol: symbols) { 
			if (symbol.getParent() == this && (symbol instanceof TypeDef)) {
				TypeDef typeDef = (TypeDef) symbol;
				for (String line: Splitter.on('\n').omitEmptyStrings().split(typeDef.describe(symbols)))
					builder.append("  ").append(line).append("\n\n");
			}
		}
		
		builder.append("}");
		
		return builder.toString();
	}
}
