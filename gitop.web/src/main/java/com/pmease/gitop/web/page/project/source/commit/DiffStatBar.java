package com.pmease.gitop.web.page.project.source.commit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.page.project.source.commit.patch.DiffStat;

@SuppressWarnings("serial")
public class DiffStatBar extends Panel {

	public DiffStatBar(String id, IModel<DiffStat> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
//		add(new Label("totals", new AbstractReadOnlyModel<Integer>() {
//
//			@Override
//			public Integer getObject() {
//				return getStat().getAdditions() + getStat().getDeletions();
//			}
//		}));
		
		add(new Label("additions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "+" + getStat().getAdditions();
			}
			
		}).setEscapeModelStrings(false));
		
		add(new Label("deletions", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "-" + getStat().getDeletions();
			}
		}).setEscapeModelStrings(false));
	}
	
	private DiffStat getStat() {
		return (DiffStat) getDefaultModelObject();
	}
}
