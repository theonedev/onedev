package io.onedev.server.web.page.project.packs.detail;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.pack.side.PackSidePanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.packs.ProjectPacksPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

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
		
		add(new Label("reference", getPack().getReference(false)));
		add(new Label("type", "(" + getPack().getType() + ")"));

		add(new SideInfoLink("moreInfo"));
				
		add(getPack().getSupport().renderContent("content", getPack()));

		add(new SideInfoPanel("side") {

			@Override
			protected Component newBody(String componentId) {
				return new PackSidePanel(componentId) {

					@Override
					protected Pack getPack() {
						return PackDetailPage.this.getPack();
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						return new Link<Void>(componentId) {

							@Override
							public void onClick() {
								getPackManager().delete(getPack());

								Session.get().success("Package " + getPack().getReference(false) + " deleted");

								String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Pack.class);
								if (redirectUrlAfterDelete != null)
									throw new RedirectToUrlException(redirectUrlAfterDelete);
								else
									setResponsePage(ProjectPacksPage.class, ProjectPacksPage.paramsOf(getProject()));
							}

						}.add(new ConfirmClickModifier("Do you really want to delete this package?"));
					}

				};
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<Pack>(componentId, "package") {

					@Override
					protected EntityQuery<Pack> parse(String queryString, Project project) {
						return PackQuery.parse(project, queryString, true);
					}

					@Override
					protected Pack getEntity() {
						return getPack();
					}

					@Override
					protected List<Pack> query(EntityQuery<Pack> query, int offset, int count, ProjectScope projectScope) {
						return getPackManager().query(projectScope!=null?projectScope.getProject():null, query, false, offset, count);
					}

					@Override
					protected CursorSupport<Pack> getCursorSupport() {
						return new CursorSupport<Pack>() {

							@Override
							public Cursor getCursor() {
								return WebSession.get().getPackCursor();
							}

							@Override
							public void navTo(AjaxRequestTarget target, Pack entity, Cursor cursor) {
								WebSession.get().setPackCursor(cursor);
								setResponsePage(getPageClass(), getPageParameters().mergeWith(paramsOf(entity)));
							}

						};
					}

				};
			}

		});
	}
	
	private PackManager getPackManager() {
		return OneDev.getInstance(PackManager.class);
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
		fragment.add(new SpriteImage("packIcon", getPack().getSupport().getPackIcon()));
		fragment.add(new Label("packReference", getPack().getReference(false)));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		return String.format("[%s] %s", 
				getProject().getPath(), 
				getPack().getType() + " " + getPack().getReference(false));
	}

}
