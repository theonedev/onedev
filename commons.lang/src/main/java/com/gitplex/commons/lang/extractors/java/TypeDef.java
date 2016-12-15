package com.gitplex.commons.lang.extractors.java;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.commons.lang.extractors.Symbol;
import com.gitplex.commons.lang.extractors.TokenPosition;
import com.gitplex.commons.lang.extractors.java.icons.Icons;
import com.gitplex.commons.util.Range;
import com.gitplex.commons.wicket.component.HighlightableLabel;

public class TypeDef extends JavaSymbol {

	private static final long serialVersionUID = 1L;

	public enum Kind {CLASS, INTERFACE, ANNOTATION, ENUM};

	private final Kind kind;
	
	private final String packageName;
	
	private final List<Modifier> modifiers;

	public TypeDef(@Nullable Symbol parent, @Nullable String packageName, 
			String name, TokenPosition pos, Kind kind, List<Modifier> modifiers) {
		super(parent, name, pos);

		this.packageName = packageName;
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
	public Component render(String componentId, Range matchRange) {
		return new HighlightableLabel(componentId, getName(), matchRange);
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String getScope() {
		if (getParent() != null) {
			String scope = getParent().getScope();
			if (scope != null)
				return scope + "." + getParent().getName();
			else
				return getParent().getName();
		} else {
			if (packageName != null)
				return packageName;
			else
				return null;
		}
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

	@Override
	public boolean isPrimary() {
		return true;
	}

}
