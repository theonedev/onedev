package com.pmease.commons.wicket.editable;

import org.apache.wicket.markup.head.JavaScriptHeaderItem;

public class EditableHeaderItem {
	
	public static JavaScriptHeaderItem get() {
		return JavaScriptHeaderItem.forReference(EditableResourceReference.get());
	}

}
