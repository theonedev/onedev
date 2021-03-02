package org.server.plugin.report.clover;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.Coverage;
import io.onedev.server.web.component.coveragebar.CoverageBar;

@SuppressWarnings("serial")
 class CoverageInfoPanel<T extends CoverageInfo> extends GenericPanel<T> {

	public CoverageInfoPanel(String id, IModel<T> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("methodLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Methods " + getCoverageInfo().getMethodCoverage();
			}
			
		}));
		
		add(new CoverageBar("methodCoverage", new AbstractReadOnlyModel<Coverage>() {

			@Override
			public Coverage getObject() {
				return getCoverageInfo().getMethodCoverage();
			}
			
		}));
		
		add(new Label("statementLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Statements " + getCoverageInfo().getStatementCoverage();
			}
			
		}));
		
		add(new CoverageBar("statementCoverage", new AbstractReadOnlyModel<Coverage>() {

			@Override
			public Coverage getObject() {
				return getCoverageInfo().getStatementCoverage();
			}
			
		}));
		
		add(new Label("branchLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Branches " + getCoverageInfo().getBranchCoverage();
			}
			
		}));
		
		add(new CoverageBar("branchCoverage", new AbstractReadOnlyModel<Coverage>() {

			@Override
			public Coverage getObject() {
				return getCoverageInfo().getBranchCoverage();
			}
			
		}));
		
		add(new Label("lineLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "Lines " + getCoverageInfo().getLineCoverage();
			}
			
		}));
		
		add(new CoverageBar("lineCoverage", new AbstractReadOnlyModel<Coverage>() {

			@Override
			public Coverage getObject() {
				return getCoverageInfo().getLineCoverage();
			}
			
		}));		
		
	}

	private CoverageInfo getCoverageInfo() {
		return getModelObject();
	}
	
}
