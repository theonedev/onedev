package io.onedev.server.web.page.project.packs.detail;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.detail.BuildDetailResourceReference;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class PackDetailPage extends ProjectPage {
	
	public static final String PARAM_PACK = "pack";
	
	protected final IModel<Pack> packModel;
	
	public PackDetailPage(PageParameters params) {
		super(params);
		
		String packIdString = params.get(PARAM_PACK).toString();
		if (StringUtils.isBlank(packIdString))
			throw new RestartResponseException(ProjectPacksPage.class, ProjectPacksPage.paramsOf(getProject(), null, 0));
			
		packModel = new LoadableDetachableModel<>() {

			@Override
			protected Pack load() {
				Long packId = params.get(PARAM_PACK).toLong();
				return OneDev.getInstance(PackManager.class).load(packId);
			}

		};
	}
	
	public Pack getPack() {
		return packModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadPack(getProject());
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<>(componentId, ProjectPacksPage.class, 
				ProjectPacksPage.paramsOf(project, 0));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPack().getVersion() + " (" + getPack().getType() + ")";
			}
			
		}));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		packModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Pack pack) {
		PageParameters params = ProjectPage.paramsOf(pack.getProject());
		params.add(PARAM_PACK, pack.getId());
		return params;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("packs", ProjectPacksPage.class, 
				ProjectPacksPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("packInfo", getPack().getVersion() + " (" + getPack().getType() + ")"));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		return String.format("%s:%s (%s)", 
				getProject().getPath(), getPack().getVersion(), getPack().getType());
	}

}
