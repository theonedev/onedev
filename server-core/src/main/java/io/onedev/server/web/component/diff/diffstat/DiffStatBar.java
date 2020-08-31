package io.onedev.server.web.component.diff.diffstat;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

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
					sb.append("&#9632; ");
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<getDeletionBlocks(); i++)
					sb.append("&#9632; ");
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		add(new Label("spacer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<MAX_BLOCKS - getAdditionBlocks() - getDeletionBlocks(); i++)
					sb.append("&#9632; ");
				
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		if (showTooltip)
			add(AttributeAppender.append("title", additions + " additions & " + deletions + " deletions"));			
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
