package io.onedev.server.web.component.issue.link;

import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueLink;
import io.onedev.server.model.LinkSpec;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import java.util.List;

public abstract class IssueLinksPanel extends Panel {
	
	private RepeatingView linksView;
	
	private String expandedLink;
	
	public IssueLinksPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		linksView = new RepeatingView("links");

		for (String linkName: getDisplayLinks()) {
			int count = 0;
			for (IssueLink link: getIssue().getTargetLinks()) {
				LinkSpec spec = link.getSpec();
				if (spec.getName().equals(linkName))
					count++;
			}
			for (IssueLink link: getIssue().getSourceLinks()) {
				LinkSpec spec = link.getSpec();
				if (spec.getOpposite() == null && spec.getName().equals(linkName) 
						|| spec.getOpposite() != null && spec.getOpposite().getName().equals(linkName)) {
					count++;
				}
			}
			if (count != 0) {
				AjaxLink<Void> link = new AjaxLink<Void>(linksView.newChildId()) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (linkName.equals(expandedLink))
							expandedLink = null;
						else
							expandedLink = linkName;
						onToggleExpand(target);
					}

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (linkName.equals(expandedLink))
							tag.put("class", tag.getAttribute("class") + " expanded");
					}

				};
				link.add(new Label("label", linkName));
				linksView.add(link);
			}
		}
		add(linksView);
		
		setOutputMarkupId(true);
	}

	protected abstract Issue getIssue();
	
	protected abstract List<String> getDisplayLinks();

	protected abstract void onToggleExpand(AjaxRequestTarget target);
	
	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(linksView.size() != 0);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueLinksCssResourceReference()));
	}

	public String getExpandedLink() {
		return expandedLink;
	}
	
}
