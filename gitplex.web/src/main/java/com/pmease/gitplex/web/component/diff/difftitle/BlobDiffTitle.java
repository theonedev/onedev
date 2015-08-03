package com.pmease.gitplex.web.component.diff.difftitle;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.commons.git.BlobChange;
import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class BlobDiffTitle extends Panel {

	private final BlobChange change;
	
	private final String alert;
	
	public BlobDiffTitle(String id, BlobChange change) {
		super(id);

		this.change = change;
		
		if (change.getOldBlobIdent().mode != null && change.getNewBlobIdent().mode != null
				&& !change.getOldBlobIdent().mode.equals(change.getNewBlobIdent().mode)) {
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
				.setVisible(change.getType() == ChangeType.RENAME));
		add(new Label("title", change.getPath()));
		
		if (alert == null) {
			add(new WebMarkupContainer("alert").setVisible(false));
		} else {
			WebMarkupContainer alertTrigger = new WebMarkupContainer("alert");
			TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
			alertTrigger.add(new TooltipBehavior(Model.of(alert), config));
			add(alertTrigger);
		}
	}

}
