package io.onedev.server.web.editable;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.model.Project;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

public abstract class InplacePropertyEditLink extends DropdownLink {

	public InplacePropertyEditLink(String id) {
		super(id, new AlignPlacement(0, 0, 0, 0));
	}

	public InplacePropertyEditLink(String id, AlignPlacement placement) {
		super(id, placement);
	}
	
	@Override
	protected void onInitialize(FloatingPanel dropdown) {
		super.onInitialize(dropdown);
		dropdown.add(AttributeAppender.append("class", "inplace-property-edit"));
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new InplacePropertyEditPanel(id, getBean(), getPropertyName()) {

			@Override
			protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
				InplacePropertyEditLink.this.onUpdated(handler, bean, propertyName);
				dropdown.close();
			}

			@Override
			protected void onCancelled(IPartialPageRequestHandler handler) {
				dropdown.close();
			}

			@Override
			public IssueQuery getIssueQuery() {
				return InplacePropertyEditLink.this.getIssueQuery();
			}

			@Override
			public Project getProject() {
				return InplacePropertyEditLink.this.getProject();
			}

		};
	}
	
	protected abstract Serializable getBean();
	
	protected abstract String getPropertyName();

	protected abstract void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName);

	@Nullable
	protected IssueQuery getIssueQuery() {
		return null;
	}
	
	@Nullable
	protected abstract Project getProject();
	
}
