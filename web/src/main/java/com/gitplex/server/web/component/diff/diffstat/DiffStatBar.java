package com.gitplex.server.web.component.diff.diffstat;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.web.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class DiffStatBar extends Panel {

	private static final int MAX_BLOCKS = 5;
	
	private final int additions;
	
	private final int deletions;
	
	private final boolean showTooltip;
	
	public DiffStatBar(String id, int additions, int deletions, boolean showTooltip) {
		super(id);

		this.additions = additions;
		this.deletions = deletions;
		this.showTooltip = showTooltip;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("additions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<getAdditionBlocks(); i++)
					sb.append("&#xf0c8; ");
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<getDeletionBlocks(); i++)
					sb.append("&#xf0c8; ");
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		add(new Label("spacer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<MAX_BLOCKS - getAdditionBlocks() - getDeletionBlocks(); i++)
					sb.append("&#xf0c8; ");
				
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		if (showTooltip) {
			TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
			add(new TooltipBehavior(new LoadableDetachableModel<String>() {
	
				@Override
				protected String load() {
					return additions + " additions & " + deletions + " deletions";			
				}
				
			}, config));
		}
	}
	
	private int getAdditionBlocks() {
		int totals = additions + deletions;
		
		if (totals == 0)
			return 0;
		else if (totals <= MAX_BLOCKS)
			return additions;
		else 
			return Math.round(Float.valueOf(additions) / totals * MAX_BLOCKS);
	}
	
	private int getDeletionBlocks() {
		int totals = additions + deletions;
		
		if (totals == 0) 
			return 0;
		else if (totals <= MAX_BLOCKS)
			return deletions;
		else
			return MAX_BLOCKS - getAdditionBlocks();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DiffStatBarResourceReference()));
	}

}
