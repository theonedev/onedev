package com.pmease.gitplex.web.component.diff.diffstat;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class DiffStatBar extends Panel {

	private static final int MAX_BLOCKS = 8;
	
	private final int additions;
	
	private final int deletions;
	
	public DiffStatBar(String id, int additions, int deletions) {
		super(id);

		this.additions = additions;
		this.deletions = deletions;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("additions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = getAdditionBlocks();
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) 
					sb.append("&#xf0c8; ");
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = getDeletionBlocks();
				
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) {
					sb.append("&#xf0c8; ");
				}
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		add(new Label("spacer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = MAX_BLOCKS + 1 - getAdditionBlocks() - getDeletionBlocks();
				
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) 
					sb.append("&#xf0c8; ");
				
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		TooltipConfig config = new TooltipConfig().withPlacement(Placement.right);
		add(new TooltipBehavior(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return additions + " additions & " + deletions + " deletions";			
			}
			
		}, config));
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
		response.render(CssHeaderItem.forReference(new CssResourceReference(DiffStatBar.class, "diff-stat.css")));
	}

}
