package com.pmease.gitop.web.page.project.source.commit.diff;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader.PatchType;

@SuppressWarnings("serial")
public class DiffStatBar extends Panel {

	public DiffStatBar(String id, IModel<FileHeader> fileModel) {
		super(id, fileModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("totals", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getFile().getPatchType() == PatchType.BINARY) {
					return "BIN";
				} else {
					return String.valueOf(getTotals());
				}
			}
		}));
		
		add(new Label("additions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = getAdditionBlocks();
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) {
					sb.append("&#61556;");
				}
				return sb.toString();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = getDeletionBlocks();
				
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) {
					sb.append("&#61556;");
				}
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
		
		add(new Label("spacer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				int i = MAX_BLOCKS + 1 - getAdditionBlocks() - getDeletionBlocks();
				
				StringBuffer sb = new StringBuffer();
				while (i-- > 0) {
					sb.append("&#61556;");
				}
				
				return sb.toString();
			}
		}).setEscapeModelStrings(false));
	}
	
	static final int MAX_BLOCKS = 4;
	
	private int getAdditionBlocks() {
		int totals = getTotals();
		
		if (totals == 0) {
			if (getFile().getChangeType() != ChangeType.DELETE)
				return MAX_BLOCKS + 1;
			else
				return 0;
		}
		
		if (totals <= MAX_BLOCKS) {
			return getAdditions();
		}
		
		if (getChangeType() == ChangeType.ADD) {
			return MAX_BLOCKS;
		}
		
		return Math.round(Float.valueOf(getAdditions()) / getTotals() * MAX_BLOCKS);
	}
	
	private int getDeletionBlocks() {
		int totals = getTotals();
		if (totals == 0) {
			if (getFile().getChangeType() == ChangeType.DELETE)
				return MAX_BLOCKS + 1;
			else
				return 0;
		}
		
		if (totals <= MAX_BLOCKS)
			return getDeletions();
		
		if (getChangeType() == ChangeType.DELETE) {
			return MAX_BLOCKS;
		}
		
		return MAX_BLOCKS - getAdditionBlocks();
	}
	
	private ChangeType getChangeType() {
		return getFile().getChangeType();
	}
	
	private int getAdditions() {
		return getFile().getDiffStat().getAdditions();
	}
	
	private int getDeletions() {
		return getFile().getDiffStat().getDeletions();
	}
	
	private int getTotals() {
		return getAdditions() + getDeletions();
	}
	
	private FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}
