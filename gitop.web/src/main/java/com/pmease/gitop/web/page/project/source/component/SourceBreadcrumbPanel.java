package com.pmease.gitop.web.page.project.source.component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmease.commons.git.Git;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.common.wicket.component.tab.BootstrapTabbedPanel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;
import com.pmease.gitop.web.page.project.source.SourceTreePage;
import com.pmease.gitop.web.util.GitUtils;

@SuppressWarnings("serial")
public class SourceBreadcrumbPanel extends AbstractSourcePagePanel {

	private final IModel<Map<RefType, List<String>>> refsModel;
	
	public SourceBreadcrumbPanel(String id, 
			IModel<Project> projectModel, 
			IModel<String> revisionModel, 
			IModel<List<String>> pathsModel) {
		super(id, projectModel, revisionModel, pathsModel);
		
		this.refsModel = new LoadableDetachableModel<Map<RefType, List<String>>>() {

			@Override
			protected Map<RefType, List<String>> load() {
				Git git = getProject().code();
				Map<RefType, List<String>> map = Maps.newHashMapWithExpectedSize(RefType.values().length);
				map.put(RefType.BRANCH, Lists.newArrayList(git.listBranches()));
				List<String> tags = Lists.newArrayList(git.listTags());
				Collections.reverse(tags);
				map.put(RefType.TAG, tags);
				return map;
			}
		};
	}
	
	private RefType getRevisionType(String revision) {
		Map<RefType, List<String>> map = refsModel.getObject();
		for (RefType each : RefType.values()) {
			List<String> list = map.get(each);
			for (String ref : list) {
				if (ref.equalsIgnoreCase(revision)) {
					return each;
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer revSelector = new WebMarkupContainer("revselector");
		revSelector.setOutputMarkupId(true);
		add(revSelector);
		revSelector.add(new Label("rev", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String revision = getRevision();
				RefType type = getRevisionType(revision);
				if (type == null) {
					return GitUtils.abbreviateSHA(revision);
				} else {
					return revision;
				}
			}
			
		}));
		revSelector.add(new Icon("reftype", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String revision = getRevision();
				RefType type = getRevisionType(revision);
				if (type == null) {
					return "icon-commit";
				} else if (type == RefType.BRANCH) {
					return "icon-git-branch";
				} else {
					return "icon-git-tag";
				}
			}
			
		}));
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", true) {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "dropdownfrag", SourceBreadcrumbPanel.this) {
					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						
						response.render(OnDomReadyHeaderItem.forScript("$('.commitish-list').liveFilter('.commitish-filter-field', 'li', {filterChildSelector: 'a.menu-item'})"));
					}
				};
				
				List<ITab> tabs = Lists.newArrayList();
				tabs.add(new AbstractTab(Model.of("Branches")) {

					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return createRefList(panelId, RefType.BRANCH);
					}
					
				});
				
				tabs.add(new AbstractTab(Model.of("Tags")) {

					@Override
					public WebMarkupContainer getPanel(String panelId) {
						return createRefList(panelId, RefType.TAG);
					}
					
				});

				frag.add(new BootstrapTabbedPanel<ITab>("tabs", tabs, new AbstractReadOnlyModel<Integer>() {

					@Override
					public Integer getObject() {
						String revision = getRevision();
						Map<RefType, List<String>> map = refsModel.getObject();
						for (RefType each : RefType.values()) {
							List<String> refs = map.get(each);
							for (String ref : refs) {
								if (ref.equalsIgnoreCase(revision)) {
									return each.ordinal();
								}
							}
						}
						
						return 0;
					}
					
				}));
				
				return frag;
			}
			
		};
		
		add(dropdown);
		
		revSelector.add(new DropdownBehavior(dropdown));

		BookmarkablePageLink<Void> homeLink = new BookmarkablePageLink<Void>("home", 
				SourceTreePage.class, 
				PageSpec.forProject(getProject()).add(PageSpec.OBJECT_ID, getRevision()));
		add(homeLink);
		homeLink.add(new Label("name", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getProject().getName();
			}
		}));
		
		ListView<String> pathsView = new ListView<String>("paths", pathsModel) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String path = item.getModelObject();
				
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
						"link", 
						SourceTreePage.class,
						SourceTreePage.newParams(getProject(), 
													 getRevision(), 
													 getPaths().subList(0, item.getIndex() + 1)));
				
				item.add(link);
				link.add(new Label("name", path));
				if (item.getIndex() == getList().size() - 1) {
					item.add(AttributeAppender.append("class", "active"));
					link.setEnabled(false);
				}

			}
		};
		
		add(pathsView);
	}
	
	static enum RefType {
		BRANCH, TAG
	}
	
	private Fragment createRefList(String id, final RefType type) {
		Fragment frag = new Fragment(id, "revfrag", SourceBreadcrumbPanel.this);
		IModel<List<String>> refs = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return refsModel.getObject().get(type);
			}
		};
		
		ListView<String> view = new ListView<String>("revs", refs) {
			@Override
			protected void populateItem(ListItem<String> item) {
				final String ref = item.getModelObject();
				Project project = getProject();
				PageParameters params = new PageParameters();
				params.add(PageSpec.USER, project.getOwner().getName());
				params.add(PageSpec.PROJECT, project.getName());
				params.add(PageSpec.OBJECT_ID, ref);
				List<String> paths = getPaths();
				int i = 0;
				for (String each : paths) {
					params.set(i, each);
					i++;
				}
				
				BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
						"link", 
						getPageClass(),
						params);
				link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return Objects.equal(getRevision(), ref) ? "checked" : "unchecked";
					}
				}));
				
				link.add(new Label("name", ref).setEscapeModelStrings(true));
				item.add(link);
			}
		};
		
		frag.add(view);
		return frag;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(LiveFilterResourceReference.get()));
	}
	
	private Class<? extends Page> getPageClass() {
		Page page = getPage();
		if (page instanceof ProjectHomePage) {
			return SourceTreePage.class;
		} else {
			return page.getClass();
		}
	}
	
	@Override
	public void onDetach() {
		if (refsModel != null) {
			refsModel.detach();
		}
		
		super.onDetach();
	}
}
