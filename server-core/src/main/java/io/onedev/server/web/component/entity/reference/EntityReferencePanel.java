package io.onedev.server.web.component.entity.reference;

import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public abstract class EntityReferencePanel extends Panel {
	
	public EntityReferencePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WebMarkupContainer("referenceHelp") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				var help = "Reference this " + getReference().getType() 
						+ " in markdown or commit message via below string.";
				if (getReference().getProject().getKey() == null)
					help += " Project path can be omitted if reference from current project";
				tag.put("title", help);
			}
			
		});
		
		var referenceString = getReference().toString(null);
		if (!(getReference() instanceof IssueReference))
			referenceString = getReference().getType() + " " + referenceString;
		add(new Label("reference", referenceString));
		add(new CopyToClipboardLink("copy", Model.of(referenceString)));
	}
	
	protected abstract EntityReference getReference();
}
