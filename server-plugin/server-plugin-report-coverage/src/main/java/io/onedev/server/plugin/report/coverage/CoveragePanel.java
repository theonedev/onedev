package io.onedev.server.plugin.report.coverage;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

 class CoveragePanel<T extends Coverage> extends GenericPanel<T> {

	public CoveragePanel(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var branchContainer = new WebMarkupContainer("branch");
		add(branchContainer.setVisible(getCoverageInfo().getBranchPercentage() >= 0));
		branchContainer.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T("Branches") + " " + getCoverageInfo().getBranchPercentage() + "%";
			}
			
		}));
		branchContainer.add(new PercentageBar("bar", new AbstractReadOnlyModel<>() {

			@Override
			public Integer getObject() {
				return getCoverageInfo().getBranchPercentage();
			}
			
		}));
		
		var lineContainer = new WebMarkupContainer("line");
		add(lineContainer.setVisible(getCoverageInfo().getLinePercentage() >= 0));
		lineContainer.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T("Lines") + " " + getCoverageInfo().getLinePercentage() + "%";
			}
			
		}));
		lineContainer.add(new PercentageBar("bar", new AbstractReadOnlyModel<>() {

			@Override
			public Integer getObject() {
				return getCoverageInfo().getLinePercentage();
			}
			
		}));		
		
	}

	private Coverage getCoverageInfo() {
		return getModelObject();
	}
	
}
