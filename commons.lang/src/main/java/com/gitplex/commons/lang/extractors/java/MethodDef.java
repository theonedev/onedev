package com.gitplex.commons.lang.extractors.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.commons.lang.extractors.TokenPosition;
import com.gitplex.commons.lang.extractors.java.icons.Icons;
import com.gitplex.commons.util.Range;

public class MethodDef extends JavaSymbol {

	private static final long serialVersionUID = 1L;

	private final String type; 
	
	private final String params;

	private final List<Modifier> modifiers;
	
	public MethodDef(TypeDef parent, String name, TokenPosition pos, 
			@Nullable String type, @Nullable String params, List<Modifier> modifiers) {
		super(parent, name, pos);
		
		this.type = type;
		this.params = params;
		this.modifiers = modifiers;
	}

	/**
	 * Get type of this method. 
	 * 
	 * @return
	 * 			type of this method, or <tt>null</tt> for constructor
	 */
	@Nullable
	public String getType() {
		return type;
	}

	/**
	 * Get params of this method.
	 * 
	 * @return
	 * 			params of this method, or <tt>null</tt> if no params
	 */
	@Nullable
	public String getParams() {
		return params;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public Component render(String componentId, Range matchRange) {
		return new MethodDefPanel(componentId, this, matchRange);
	}

	@Override
	public ResourceReference getIcon() {
		String icon;
		if (modifiers.contains(Modifier.PRIVATE))
			icon = "methpri_obj.png";
		else if (modifiers.contains(Modifier.PROTECTED))
			icon = "methpro_obj.png";
		else if (modifiers.contains(Modifier.PUBLIC))
			icon = "methpub_obj.png";
		else
			icon = "methdef_obj.png";
		return new PackageResourceReference(Icons.class, icon);
	}

	@Override
	public String getScope() {
		String scope = getParent().getScope();
		if (scope != null)
			return scope + "." + getParent().getName();
		else
			return getParent().getName();
	}

	@Override
	public boolean isPrimary() {
		return false;
	}
	
}
