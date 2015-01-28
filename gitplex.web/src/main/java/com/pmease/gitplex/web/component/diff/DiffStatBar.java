package com.pmease.gitplex.web.component.diff;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.util.diff.DiffLine;
import com.pmease.commons.wicket.behavior.TooltipBehavior;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;

@SuppressWarnings("serial")
public class DiffStatBar extends Panel {

	private static final int MAX_BLOCKS = 8;
	
	private IModel<List<DiffLine>> diffsModel;
	
	private IModel<Integer> additionsModel;
	
	private IModel<Integer> deletionsModel;
	
	public DiffStatBar(String id, IModel<List<DiffLine>> diffsModel) {
		super(id);
		
		this.diffsModel = diffsModel;

		additionsModel = new LoadableDetachableModel<Integer>() {

			@Override
			protected Integer load() {
				int additions = 0;
				for (DiffLine diff: DiffStatBar.this.diffsModel.getObject()) {
					if (diff.getAction() == DiffLine.Action.ADD)
						additions++;
				}
				return additions;
			}
			
		};
		
		deletionsModel = new LoadableDetachableModel<Integer>() {

			@Override
			protected Integer load() {
				int deletions = 0;
				for (DiffLine diff: DiffStatBar.this.diffsModel.getObject()) {
					if (diff.getAction() == DiffLine.Action.DELETE)
						deletions++;
				}
				return deletions;
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("totals", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(additionsModel.getObject()+deletionsModel.getObject());
			}
		}));
		
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
				return additionsModel.getObject() + " additions & " + deletionsModel.getObject() + " deletions";			
			}
			
		}, config));
	}
	
	private int getAdditionBlocks() {
		int totals = additionsModel.getObject() + deletionsModel.getObject();
		
		if (totals == 0)
			return 0;
		else if (totals <= MAX_BLOCKS)
			return additionsModel.getObject();
		else 
			return Math.round(Float.valueOf(additionsModel.getObject()) / totals * MAX_BLOCKS);
	}
	
	private int getDeletionBlocks() {
		int totals = additionsModel.getObject() + deletionsModel.getObject();
		
		if (totals == 0) 
			return 0;
		else if (totals <= MAX_BLOCKS)
			return deletionsModel.getObject();
		else
			return MAX_BLOCKS - getAdditionBlocks();
	}

	@Override
	protected void onDetach() {
		diffsModel.detach();
		additionsModel.detach();
		deletionsModel.detach();
		super.onDetach();
	}
	
}
