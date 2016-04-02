package com.pmease.commons.lang.extractors.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.commons.lang.extractors.Symbol;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.commons.lang.extractors.java.icons.Icons;
import com.pmease.commons.util.Range;
import com.pmease.commons.wicket.component.EmphasizeAwareLabel;

public class CompilationUnit extends JavaSymbol {
	
	private static final long serialVersionUID = 1L;
	
	private String packageName;

	public CompilationUnit(@Nullable String packageName, TokenPosition pos) {
		super(null, null, pos);
		
		this.packageName = packageName;
	}
	
	@Override
	public String describe(List<Symbol> symbols) {
		StringBuilder builder =  new StringBuilder();
		if (packageName != null)
			builder.append("package ").append(packageName).append(";\n\n");
		
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
	public Component render(String componentId, Range matchRange) {
		return new EmphasizeAwareLabel(componentId, packageName, matchRange);
	}

	@Override
	public ResourceReference getIcon() {
		return new PackageResourceReference(Icons.class, "package_obj.png");
	}

	@Override
	public String getScope() {
		return packageName;
	}

	@Override
	public boolean isPrimary() {
		return false;
	}

}
