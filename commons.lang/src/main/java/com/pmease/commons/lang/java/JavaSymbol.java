package com.pmease.commons.lang.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.pmease.commons.lang.Symbol;

public abstract class JavaSymbol extends Symbol {

	private static final long serialVersionUID = 1L;

	public JavaSymbol(Symbol parent, String name, int lineNo) {
		super(parent, name, lineNo);
	}

	@Override
	public String getScope() {
		List<String> scopes = new ArrayList<>();
		Symbol parent = getParent();
		while(parent != null) {
			if (parent instanceof TypeDef)
				scopes.add(parent.getName());
			else {
				CompilationUnit compilationUnit = (CompilationUnit) parent;
				if (compilationUnit.getPackageName() != null)
					scopes.add(compilationUnit.getPackageName());
			}
			parent = parent.getParent();
		}
		Collections.reverse(scopes);
		return Joiner.on(".").join(scopes);
	}
	
}
