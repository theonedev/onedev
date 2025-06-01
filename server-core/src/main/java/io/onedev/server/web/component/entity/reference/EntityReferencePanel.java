package io.onedev.server.web.component.entity.reference;

import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.IssueReference;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

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
				var help = MessageFormat.format(_T("Reference this {0} in markdown or commit message via below string."), 
						_T(getReference().getType()));
				if (getReference().getProject().getKey() == null)
					help += _T(" Project path can be omitted if reference from current project");
				tag.put("data-tippy-content", help);
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
