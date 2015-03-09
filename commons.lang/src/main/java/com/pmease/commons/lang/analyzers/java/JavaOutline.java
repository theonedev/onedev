package com.pmease.commons.lang.analyzers.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.lang.Outline;

public class JavaOutline implements Outline {
	
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
	
}
