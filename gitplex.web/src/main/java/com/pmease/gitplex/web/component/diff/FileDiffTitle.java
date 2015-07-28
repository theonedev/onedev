package com.pmease.gitplex.web.component.diff;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Change;
import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class FileDiffTitle extends Panel {

	private final Change change;
	
	private final String alert;
	
	public FileDiffTitle(String id, Change change) {
		super(id);

		this.change = change;
		
		if (change.getOldBlobIdent().mode != 0 && change.getNewBlobIdent().mode != 0 
				&& change.getOldBlobIdent().mode != change.getNewBlobIdent().mode) {
			alert = "Blob mode is changed from " + Integer.toString(change.getOldBlobIdent().mode, 8) 
					+ " to " + Integer.toString(change.getNewBlobIdent().mode, 8);
		} else {
			alert = null;
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("renamedTitle", change.getOldBlobIdent().path)
				.setVisible(change.getStatus() == Change.Status.RENAMED));
		add(new Label("title", change.getPath()));
		
		if (alert == null) {
			add(new WebMarkupContainer("alerts").setVisible(false));
		} else {
			WebMarkupContainer alertsTrigger = new WebMarkupContainer("alert");
			TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
			alertsTrigger.add(new TooltipBehavior(Model.of(alert), config));
			add(alertsTrigger);
		}
	}

}
