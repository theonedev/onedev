package com.pmease.commons.lang.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import com.pmease.commons.lang.Symbol;

public class CompilationUnit extends Symbol {
	
	private static final long serialVersionUID = 1L;
	
	private String packageName;

	public CompilationUnit(@Nullable String packageName, int lineNo) {
		super(null, null, lineNo);
		
		this.packageName = packageName;
	}
	
	@Override
	public String describe(List<Symbol> symbols) {
		StringBuilder builder =  new StringBuilder();
		if (packageName != null)
			builder.append("package ").append(packageName).append(";\n\n");
		
		for (Symbol symbol: symbols) {
			if (symbol.getParent() == this)
				builder.append(symbol.describe(symbols)).append("\n\n");
		}
		
		return builder.toString();
	}

	@Nullable
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public Component render(String componentId) {
		throw new UnsupportedOperationException();
	}
	
}
