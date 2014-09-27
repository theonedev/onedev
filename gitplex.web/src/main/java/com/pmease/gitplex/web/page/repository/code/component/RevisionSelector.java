package com.pmease.gitplex.web.page.repository.code.component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
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
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.common.wicket.component.tab.BootstrapTabbedPanel;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.RepositoryHomePage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

@SuppressWarnings("serial")
public class RevisionSelector extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private final String path;
	
	private final IModel<Map<RefType, List<String>>> refsModel;
	
	public RevisionSelector(String id, IModel<Repository> repoModel, @Nullable String revision, @Nullable String path) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
		this.path = path;
		this.refsModel = new LoadableDetachableModel<Map<RefType, List<String>>>() {

			@Override
			protected Map<RefType, List<String>> load() {
				Git git = RevisionSelector.this.repoModel.getObject().git();
				Map<RefType, List<String>> map = Maps.newHashMapWithExpectedSize(RefType.values().length);
				map.put(RefType.BRANCH, Lists.newArrayList(git.listBranches().keySet()));
				List<String> tags = Lists.newArrayList(git.listTags());
				Collections.reverse(tags);
				map.put(RefType.TAG, tags);
				return map;
			}
		};
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
				String revision = repoModel.getObject().defaultBranchIfNull(RevisionSelector.this.revision);
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
				RefType type = getRevisionType(repoModel.getObject().defaultBranchIfNull(revision));
				if (type == null) {
					return "fa-commit";
				} else if (type == RefType.BRANCH) {
					return "fa-branch";
				} else {
					return "fa-tag";
				}
			}
			
		}));
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", true) {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "dropdownfrag", RevisionSelector.this) {
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
						Map<RefType, List<String>> map = refsModel.getObject();
						for (RefType each : RefType.values()) {
							List<String> refs = map.get(each);
							for (String ref : refs) {
								if (ref.equalsIgnoreCase(repoModel.getObject().defaultBranchIfNull(revision))) {
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
	
	static enum RefType {
		BRANCH, TAG
	}
	
	private Fragment createRefList(String id, final RefType type) {
		Fragment frag = new Fragment(id, "revfrag", RevisionSelector.this);
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
				
				AbstractLink link = newRefLink("link", ref);
				link.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return Objects.equal(repoModel.getObject().defaultBranchIfNull(revision), ref) ? "checked" : "unchecked";
					}
				}));
				
				link.add(new Label("name", ref).setEscapeModelStrings(true));
				item.add(link);
			}
		};
		
		frag.add(view);
		
		frag.add(new Label("norev", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (type == RefType.BRANCH) {
					return "No branches found";
				} else {
					return "No tags found";
				}
			}
			
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisibilityAllowed(refsModel.getObject().get(type).isEmpty());
			}
		});
		return frag;
	}
	
	protected AbstractLink newRefLink(String id, String ref) {
		PageParameters params = RepositoryPage.paramsOf(repoModel.getObject(), ref, path);
		
		return new BookmarkablePageLink<Void>("link", getPageClass(), params);
	}

	private Class<? extends Page> getPageClass() {
		Page page = getPage();
		if (page instanceof RepositoryHomePage) {
			return RepoTreePage.class;
		} else {
			return page.getClass();
		}
	}
	
	@Override
	public void onDetach() {
		repoModel.detach();
		refsModel.detach();
		
		super.onDetach();
	}
}
