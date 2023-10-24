package io.onedev.server.ee.xsearch;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.ee.xsearch.match.BlobMatch;
import io.onedev.server.ee.xsearch.match.ContentMatch;
import io.onedev.server.ee.xsearch.query.FileQuery;
import io.onedev.server.ee.xsearch.query.SymbolQuery;
import io.onedev.server.ee.xsearch.query.TextQuery;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.search.code.query.FileQueryOption;
import io.onedev.server.search.code.query.QueryOption;
import io.onedev.server.search.code.query.SymbolQueryOption;
import io.onedev.server.search.code.query.TextQueryOption;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.RunTaskBehavior;
import io.onedev.server.web.component.project.ProjectAvatar;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;
import java.util.*;

import static io.onedev.server.git.GitUtils.branch2ref;
import static io.onedev.server.util.ReflectionUtils.getTypeArguments;
import static io.onedev.server.web.WebConstants.PAGE_SIZE;
import static io.onedev.server.web.page.project.blob.render.BlobRenderer.getSourcePosition;

public abstract class CodeSearchPage<T extends Serializable> extends LayoutPage {

	private static final MetaDataKey<HashMap<Class<?>, QueryOption>> QUERY_OPTIONS =
			new MetaDataKey<>() {};

	private static final MetaDataKey<String> PROJECTS =
			new MetaDataKey<>() {};
	
	private QueryOption option;
	
	private String projects;
	
	private int page = 1;
	
	public CodeSearchPage(PageParameters params) {
		super(params);
		
		List<Class<?>> typeArguments = getTypeArguments(CodeSearchPage.class, getClass());

		try {
			option = (QueryOption) typeArguments.get(0).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
		
		Map<Class<?>, QueryOption> savedOptions = getSavedOptions();
		if (savedOptions.containsKey(option.getClass()))
			option = savedOptions.get(option.getClass());

		String projects = WebSession.get().getMetaData(PROJECTS);
		if (projects != null)
			this.projects = projects;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var projectsBean = new ProjectsBean();
		projectsBean.setProjects(projects);
		
		var form = new Form<Void>("form");
		
		FormComponent<? extends QueryOption> optionEditor = option.newOptionEditor("option");			
		form.add(optionEditor);
		form.add(BeanContext.edit("projects", projectsBean));
		
		var foot = new WebMarkupContainer("foot");
		form.add(foot.setOutputMarkupId(true));
		
		foot.add(new AjaxButton("search") {

			private RunTaskBehavior runTaskBehavior;

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(runTaskBehavior = new RunTaskBehavior() {

					@Override
					protected void runTask(AjaxRequestTarget target) {
						page = 1;
						
						var blobMatches = search();

						if (!blobMatches.isEmpty()) {
							var hasMatchesFrag = new Fragment("result", "hasMatchesFrag", CodeSearchPage.this);

							var blobMatchesView = new RepeatingView("blobMatches");
							var firstItem = true;
							for (var blobMatch: blobMatches) {
								blobMatchesView.add(newBlobMatchItem(blobMatchesView.newChildId(), blobMatch, firstItem));
								firstItem = false;
							}
							hasMatchesFrag.add(blobMatchesView);

							hasMatchesFrag.add(new AjaxLink<Void>("more") {

								private RunTaskBehavior runTaskBehavior;

								@Override
								protected void onInitialize() {
									super.onInitialize();
									
									var moreLink = this;
									add(runTaskBehavior = new RunTaskBehavior() {

										@Override
										protected void runTask(AjaxRequestTarget target) {
											page++;
											var blobMatches = search();
											var firstItem = true;
											for (var blobMatch: blobMatches) {
												var item = newBlobMatchItem(blobMatchesView.newChildId(), blobMatch, firstItem);
												blobMatchesView.add(item);
												var script = String.format("$('.blob-matches').append('<li id=\"%s\"></li>');",
														item.getMarkupId());
												target.prependJavaScript(script);
												target.add(item);
												firstItem = false;
											}
											setVisible(blobMatches.size() == PAGE_SIZE);
											target.add(moreLink);
										}

									});
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									runTaskBehavior.requestRun(target);
									target.focusComponent(null);
								}

							}.setVisible(blobMatches.size() == PAGE_SIZE));
							
							hasMatchesFrag.setOutputMarkupId(true);
							CodeSearchPage.this.replace(hasMatchesFrag);
							target.add(hasMatchesFrag);
						} else {
							var noMatchesFrag = new Fragment("result", "noMatchesFrag", CodeSearchPage.this);
							noMatchesFrag.setOutputMarkupId(true);
							CodeSearchPage.this.replace(noMatchesFrag);
							target.add(noMatchesFrag);
						}
					}

				});
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				option = optionEditor.getModelObject();
				projects = projectsBean.getProjects();

				var savedOptions = getSavedOptions();
				savedOptions.put(option.getClass(), option);
				WebSession.get().setMetaData(QUERY_OPTIONS, savedOptions);
				WebSession.get().setMetaData(PROJECTS, projects);
				target.add(form);
				runTaskBehavior.requestRun(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		
		var indexing = new WebMarkupContainer("indexing") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(OneDev.getInstance(ClusterManager.class).runOnAllServers(() -> {
					return OneDev.getInstance(CodeIndexManager.class).isIndexing();	
				}).entrySet().stream().anyMatch(Map.Entry::getValue));
			}
			
		};
		foot.add(indexing.setOutputMarkupPlaceholderTag(true));
		
		foot.add(new ChangeObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> observables) {
				handler.add(indexing);
			}

			@Override
			protected Collection<String> findObservables() {
				return Sets.newHashSet(CodeIndexStatusChanged.getChangeObservable());
			}

		});

		add(form);
		
		add(new WebMarkupContainer("result").setOutputMarkupPlaceholderTag(true).setVisible(false));
	}
	
	private List<BlobMatch> search() {
		var count = page * PAGE_SIZE;
		var searchManager = OneDev.getInstance(CodeSearchManager.class);
		List<BlobMatch> blobMatches;
		if (option instanceof TextQueryOption) {
			var query = new TextQuery.Builder((TextQueryOption) option)
					.projects(projects)
					.count(count)
					.build();
			blobMatches = searchManager.search(query);
		} else if (option instanceof FileQueryOption) {
			var query = new FileQuery.Builder((FileQueryOption) option)
					.projects(projects)
					.count(count)
					.build();
			blobMatches = searchManager.search(query);
		} else {
			var query = new SymbolQuery.Builder((SymbolQueryOption) option)
					.primary(true)
					.projects(projects)
					.count(count)
					.build();
			blobMatches = searchManager.search(query);

			if (blobMatches.size() < count) {
				query = new SymbolQuery.Builder((SymbolQueryOption) option)
						.primary(false)
						.projects(projects)
						.count(count - blobMatches.size())
						.build();
				blobMatches.addAll(searchManager.search(query));
			}
		}
		var newIndex = (page-1) * PAGE_SIZE;
		if (newIndex < blobMatches.size())
			return blobMatches.subList(newIndex, blobMatches.size());
		else 
			return new ArrayList<>();
	}
	
	private Component newBlobMatchItem(String componentId, BlobMatch blobMatch, boolean firstItem) {
		WebMarkupContainer item = new WebMarkupContainer(componentId);
		var projectId = blobMatch.getProjectId();
		var project = getProjectManager().load(projectId);
		var projectLink = new BookmarkablePageLink<Void>("project",
				ProjectDashboardPage.class,
				ProjectDashboardPage.paramsOf(projectId));
		projectLink.add(new ProjectAvatar("avatar", projectId));
		projectLink.add(new Label("label", project.getPath()));
		item.add(projectLink);

		var revision = Preconditions.checkNotNull(project.getDefaultBranch());
		if (project.getTagRef(revision) != null)
			revision = branch2ref(revision);
		var blobIdent = new BlobIdent(revision, blobMatch.getBlobPath());
		var blobLink = new BookmarkablePageLink<Void>("blob",
				ProjectBlobPage.class,
				ProjectBlobPage.paramsOf(project, blobIdent));
		blobLink.add(new Label("label", blobMatch.getBlobPath()));
		item.add(blobLink);

		var contentMatchesView = new ListView<>("contentMatches", blobMatch.getContentMatches()) {

			@Override
			protected void populateItem(ListItem<ContentMatch> item) {
				var contentMatch = item.getModelObject();
				var project = getProjectManager().load(projectId);
				var state = new ProjectBlobPage.State();
				state.blobIdent = new BlobIdent(blobIdent.revision, blobIdent.path);
				state.position = getSourcePosition(contentMatch.getPosition());
				var link = new BookmarkablePageLink<>("link",
						ProjectBlobPage.class,
						ProjectBlobPage.paramsOf(project, state));
				link.add(contentMatch.renderIcon("icon"));
				link.add(new Label("lineNo", contentMatch.getPosition().getFromRow() + 1));
				link.add(contentMatch.render("content"));
				item.add(link);
			}

		};
		contentMatchesView.setVisible(firstItem);
		item.add(contentMatchesView);

		item.add(new AjaxLink<>("expand") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						if (contentMatchesView.isVisible())
							return "expanded";
						else
							return "";
					}
				}));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				contentMatchesView.setVisible(!contentMatchesView.isVisible());
				target.add(item);
			}

		}.setVisible(!blobMatch.getContentMatches().isEmpty()));
		
		item.setOutputMarkupId(true);
		return item;
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}

	private HashMap<Class<?>, QueryOption> getSavedOptions() {
		HashMap<Class<?>, QueryOption> savedOptions = WebSession.get().getMetaData(QUERY_OPTIONS);
		if (savedOptions == null)
			savedOptions = new HashMap<>();
		return savedOptions;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CodeSearchCssResourceReference()));
	}
}
