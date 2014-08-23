package com.pmease.gitplex.web.component.diff;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class FileDiffTitle extends Panel {

	private final RevAwareChange change;
	
	private final List<String> alerts;
	
	public FileDiffTitle(String id, RevAwareChange change, List<String> alerts) {
		super(id);
		
		int oldBlobType = change.getOldMode() & FileMode.TYPE_MASK;
		int newBlobType = change.getNewMode() & FileMode.TYPE_MASK;
		
		Preconditions.checkArgument(oldBlobType == FileMode.TYPE_FILE && newBlobType == FileMode.TYPE_FILE);
		
		this.change = change;
		
		this.alerts = new ArrayList<>();
		if (change.getOldMode() != change.getNewMode()) {
			this.alerts.add("File mode is changed from " + Integer.toString(change.getOldMode(), 8) 
					+ " to " + Integer.toString(change.getNewMode(), 8));
		}
		if (alerts != null)
			this.alerts.addAll(alerts);
	}
	
	public FileDiffTitle(String id, RevAwareChange diffInfo) {
		this(id, diffInfo, null);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("renamedTitle", change.getOldPath())
				.setVisible(!change.getOldPath().equals(change.getNewPath())));
		add(new Label("title", change.getNewPath()));
		
		if (alerts.size() == 0) {
			add(new WebMarkupContainer("alerts").setVisible(false));
		} else if (alerts.size() == 1) {
			WebMarkupContainer alertsTrigger = new WebMarkupContainer("alerts");
			TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
			alertsTrigger.add(new TooltipBehavior(Model.of(alerts.get(0)), config));
			add(alertsTrigger);
		} else {
			WebMarkupContainer alertsTrigger = new WebMarkupContainer("alerts");
			alertsTrigger.add(AttributeAppender.append("data-html", "true"));
			StringBuffer html = new StringBuffer();
			html.append("<ul class='diff-alerts'>");
			for (String alert: alerts)
				html.append("<li>").append(alert).append("</li>");
			html.append("</ul>");
			TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
			alertsTrigger.add(new TooltipBehavior(Model.of(html.toString()), config));
			add(alertsTrigger);
		}
	}

}
