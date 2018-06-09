package io.onedev.server.web.component.milestoneprogress;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Milestone;

@SuppressWarnings("serial")
public class MilestoneProgressBar extends GenericPanel<Milestone> {

	public MilestoneProgressBar(String id, IModel<Milestone> model) {
		super(id, model);
	}

	private Milestone getMilestone() {
		return getModelObject();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		int progress;
		int totalIssues = getMilestone().getNumOfOpenIssues()+getMilestone().getNumOfClosedIssues();
		if (totalIssues != 0)
			progress = 100 * getMilestone().getNumOfClosedIssues() / totalIssues;
		else
			progress = 0;
		if (progress > 100)
			progress = 100;
		
		WebMarkupContainer outer = new WebMarkupContainer("outer");
		outer.add(AttributeAppender.append("title", progress + "% completed"));
		add(outer);
		
		outer.add(new WebMarkupContainer("inner")
				.add(AttributeAppender.append("style", "width: " + progress + "%;")));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MilestoneProgressResourceReference()));
	}

}
