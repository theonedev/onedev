package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.pmease.commons.lang.Symbol;

public class CompilationUnit extends Symbol {
	
	@Override
	public String toString() {
		StringBuilder builder =  new StringBuilder();
		if (name != null)
			builder.append("package ").append(name).append(";\n\n");
		
		for (Symbol child: children)
			builder.append(child).append("\n\n");
		
		return builder.toString();
	}

	@Override
	protected String getSearchable() {
		return null;
	}

	@Override
	public Component render(String componentId) {
		return null;
	}
	
}
