package io.onedev.server.web.component.entity.reference;

import io.onedev.server.util.Referenceable;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class ReferencePanel extends Panel {
	
	public ReferencePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("referenceHelp") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("title", "Reference this " + getReferenceable().getType() 
						+ " in markdown or commit message via below string. "
						+ "Project name can be ommitted if reference from current project");
			}
			
		});
		
		String reference = Referenceable.asReference(getReferenceable());
		
		add(new Label("reference", reference));
		add(new CopyToClipboardLink("copy", Model.of(reference)));
	}
	
	protected abstract Referenceable getReferenceable();
}
