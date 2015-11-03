package com.pmease.commons.wicket.component.menu;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.floating.Alignment;
import com.pmease.commons.wicket.component.floating.FloatingPanel;

@SuppressWarnings("serial")
public abstract class MenuLink<T> extends DropdownLink<T> {

	public MenuLink(String id) {
		super(id);
	}
	
	public MenuLink(String id, Alignment alignment) {
		super(id, alignment);
	}

	public MenuLink(String id, @Nullable IModel<T> model, Alignment alignment) {
		super(id, model, alignment);
	}
	
	public MenuLink(String id, @Nullable Component alignWith, Alignment alignment) {
		super(id, alignWith, alignment);
	}
	
	public MenuLink(String id, @Nullable IModel<T> model, 
			@Nullable Component alignWith, Alignment alignment) {
		super(id, model, alignWith, alignment);
	}
	
	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", " menu"));
	}

	@Override
	protected Component newContent(String id) {
		return new ContentPanel(id) {

			@Override
			protected List<MenuItem> getMenuItems() {
				return MenuLink.this.getMenuItems();
			}
			
		};
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(MenuLink.class, "menu.css")));
	}

	protected abstract List<MenuItem> getMenuItems();
}
