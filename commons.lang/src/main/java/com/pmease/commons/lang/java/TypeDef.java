package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.pmease.commons.lang.Symbol;
import com.pmease.commons.lang.java.icons.Icons;

public class TypeDef extends JavaSymbol {

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
		return new Label(componentId, getName());
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

	@Override
	public ResourceReference getIcon() {
		String icon;
		switch (kind) {
		case ENUM:
			icon = "enum_obj.png";
			break;
		case INTERFACE:
			icon = "int_obj.png";
			break;
		case ANNOTATION:
			icon = "annotation_obj.png";
			break;
		case CLASS:
			if (modifiers.contains(Modifier.PRIVATE))
				icon = "innerclass_private_obj.png";
			else if (modifiers.contains(Modifier.PROTECTED))
				icon = "innerclass_protected_obj.png";
			else if (modifiers.contains(Modifier.PUBLIC))
				icon = "class_obj.png";
			else
				icon = "class_default_obj.png";
			break;
		default:
			throw new IllegalStateException();
		}
		return new PackageResourceReference(Icons.class, icon);
	}

}
