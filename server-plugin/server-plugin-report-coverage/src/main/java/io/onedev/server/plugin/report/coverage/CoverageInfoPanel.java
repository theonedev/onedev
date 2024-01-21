package io.onedev.server.plugin.report.coverage;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
 class CoverageInfoPanel<T extends CoverageInfo> extends GenericPanel<T> {

	public CoverageInfoPanel(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var branchContainer = new WebMarkupContainer("branch");
		add(branchContainer.setVisible(getCoverageInfo().getBranchCoverage() >= 0));
		branchContainer.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Branches " + getCoverageInfo().getBranchCoverage() + "%";
			}
			
		}));
		branchContainer.add(new CoverageBar("bar", new AbstractReadOnlyModel<>() {

			@Override
			public Integer getObject() {
				return getCoverageInfo().getBranchCoverage();
			}
			
		}));
		
		var lineContainer = new WebMarkupContainer("line");
		add(lineContainer.setVisible(getCoverageInfo().getLineCoverage() >= 0));
		lineContainer.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Lines " + getCoverageInfo().getLineCoverage() + "%";
			}
			
		}));
		lineContainer.add(new CoverageBar("bar", new AbstractReadOnlyModel<>() {

			@Override
			public Integer getObject() {
				return getCoverageInfo().getLineCoverage();
			}
			
		}));		
		
	}

	private CoverageInfo getCoverageInfo() {
		return getModelObject();
	}
	
}
