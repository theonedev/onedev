package io.onedev.server.web.page.project.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.PageTabLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ProjectStatsPage extends ProjectPage {

	public ProjectStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new PageTab(Model.of("Contributions"), ProjectContribsPage.class) {

			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<>(linkId, ProjectContribsPage.class, 
								ProjectContribsPage.paramsOf(getProject()));
					}
					
				};
			}
			
		});
		tabs.add(new PageTab(Model.of("Source Lines"), SourceLinesPage.class) {
			
			@Override
			public Component render(String componentId) {
				return new PageTabLink(componentId, this) {

					@Override
					protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
						return new ViewStateAwarePageLink<>(linkId, SourceLinesPage.class, 
								SourceLinesPage.paramsOf(getProject()));
					}
					
				};
			}
			
		});
		
		add(new Tabbable("statsTabs", tabs).setOutputMarkupId(true));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject().getFacade());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new ProjectStatsResourceReference()));
	}

}
