package com.pmease.commons.lang.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.pmease.commons.lang.Symbol;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.commons.lang.java.icons.Icons;

public class FieldDef extends JavaSymbol {

	private static final long serialVersionUID = 1L;

	private final String type;
	
	private final List<Modifier> modifiers;
	
	public FieldDef(TypeDef parent, String name, TokenPosition pos, 
			@Nullable String type, List<Modifier> modifiers) {
		super(parent, name, pos);
		
		this.type = type;
		this.modifiers = modifiers;
	}
	
	/**
	 * Get type of this field.
	 * 
	 * @return 
	 * 			type of this field, or <tt>null</tt> for enum constant
	 */
	@Nullable
	public String getType() {
		return type;
	}

	public List<Modifier> getModifiers() {
		return modifiers;
	}

	@Override
	public Component render(String componentId) {
		return new FieldDefPanel(componentId, this);
	}

	@Override
	public String describe(List<Symbol> symbols) {
		StringBuilder builder = new StringBuilder();
		for (Modifier modifier: modifiers) 
			builder.append(modifier.name().toLowerCase()).append(" ");
		if (type != null)
			builder.append(type).append(" ");
		builder.append(getName()).append(";");
		return builder.toString();
	}

	@Override
	public ResourceReference getIcon() {
		String icon;
		if (modifiers.contains(Modifier.PRIVATE))
			icon = "field_private_obj.png";
		else if (modifiers.contains(Modifier.PROTECTED))
			icon = "field_protected_obj.png";
		else if (modifiers.contains(Modifier.PUBLIC))
			icon = "field_public_obj.png";
		else
			icon = "field_default_obj.png";
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
