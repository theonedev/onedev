package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.lang.LangToken;
import com.pmease.commons.lang.Outline;

public class JavaOutline implements Outline {
	
	private static final long serialVersionUID = 1L;

	@Nullable
	public String packageName;
	
	public List<TypeDef> typeDefs = new ArrayList<>();

	@Override
	public String toString() {
		StringBuilder builder =  new StringBuilder();
		if (packageName != null)
			builder.append("package ").append(packageName).append(";\n\n");
		
		for (TypeDef typeDef: typeDefs)
			builder.append(typeDef).append("\n\n");
		
		return builder.toString();
	}

	@Override
	public List<LangToken> getSymbols() {
		List<LangToken> symbols = new ArrayList<>();
		for (TypeDef typeDef: typeDefs)
			symbols.addAll(typeDef.getSymbols());
		
		return symbols;
	}
	
}
