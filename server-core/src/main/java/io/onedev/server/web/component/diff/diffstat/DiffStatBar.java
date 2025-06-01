package io.onedev.server.web.component.diff.diffstat;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

public class DiffStatBar extends Panel {

	private static final int MAX_BLOCKS = 6;
	
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
		
		add(AttributeAppender.append("data-tippy-content", MessageFormat.format(_T("{0} additions & {1} deletions"), additions, deletions)));			
	}
	
	private int getAdditionBlocks() {
		int totals = additions + deletions;
		
		if (totals == 0) {
			return 0;
		} else if (totals <= MAX_BLOCKS) {
			return additions;
		} else { 
			int additionBlocks = Math.round(Float.valueOf(additions) / totals * MAX_BLOCKS);
			if (additionBlocks <= 0 && additions != 0)
				additionBlocks = 1;
			return additionBlocks;
		}
	}
	
	private int getDeletionBlocks() {
		int totals = additions + deletions;
		
		if (totals == 0) { 
			return 0;
		} else if (totals <= MAX_BLOCKS) {
			return deletions;
		} else {
			int deletionBlocks = MAX_BLOCKS - getAdditionBlocks();
			if (deletionBlocks <= 0 && deletions != 0)
				deletionBlocks = 1;
			return deletionBlocks;
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DiffStatBarResourceReference()));
	}

}
