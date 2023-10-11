package io.onedev.server.ee.dashboard.widgets;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ProjectQuery;
import io.onedev.server.model.support.Widget;
import io.onedev.server.web.component.project.list.ProjectListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

@Editable(name="Project list", order=100)
public class ProjectListWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private String baseQuery;
	
	@Editable(order=100, placeholder="All accessible", description="Optionally specify a base query for the list")
	@ProjectQuery
	public String getBaseQuery() {
		return baseQuery;
	}

	public void setBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}
	
	@Override
	protected Component doRender(String componentId) {
		return new ProjectListPanel(componentId, Model.of((String)null), 0) {

			private static final long serialVersionUID = 1L;

			@Override
			protected io.onedev.server.search.entity.project.ProjectQuery getBaseQuery() {
				return io.onedev.server.search.entity.project.ProjectQuery.parse(baseQuery);
			}
			
		};
	}

}
