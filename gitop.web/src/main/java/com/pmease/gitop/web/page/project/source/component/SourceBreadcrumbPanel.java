package com.pmease.gitop.web.page.project.source.component;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
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

import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.common.component.tab.BootstrapTabbedPanel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.ProjectHomePage;
import com.pmease.gitop.web.page.project.source.SourceTreePage;

@SuppressWarnings("serial")
public class SourceBreadcrumbPanel extends AbstractSourcePagePanel {

	public SourceBreadcrumbPanel(String id, 
			IModel<Project> projectModel, 
			IModel<String> revisionModel, 
			IModel<List<String>> pathsModel) {
		super(id, projectModel, revisionModel, pathsModel);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer revSelector = new WebMarkupContainer("revselector");
		revSelector.setOutputMarkupId(true);
		add(revSelector);
		revSelector.add(new Label("rev", Model.of(getRevision())));
		
		DropdownPanel dropdown = new DropdownPanel("dropdown", false) {

			@Override
			protected Component newContent(String id) {
				Fragment frag = new Fragment(id, "dropdownfrag", SourceBreadcrumbPanel.this);
				List<ITab> tabs = Lists.newArrayList();
				tabs.add(new AbstractTab(Model.of("Branches")) {

					@Override
					public WebMarkupContainer getPanel(String panelId) {
						Fragment frag = new Fragment(panelId, "revfrag", SourceBreadcrumbPanel.this);
						frag.add(createRefList("revs", RefType.BRANCH));
						return frag;
					}
					
				});
				
				tabs.add(new AbstractTab(Model.of("Tags")) {

					@Override
					public WebMarkupContainer getPanel(String panelId) {
						Fragment frag = new Fragment(panelId, "revfrag", SourceBreadcrumbPanel.this);
						frag.add(createRefList("revs", RefType.TAG));
						return frag;
					}
					
				});

				frag.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
				
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
	
	private Component createRefList(String id, final RefType type) {
		IModel<List<String>> refs = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				Git git = getProject().getCodeRepo();
				if (type == RefType.BRANCH) {
					return Lists.newArrayList(git.listBranches());
				} else {
					return Lists.newArrayList(git.listTags());
				}
			}
		};
		
		ListView<String> view = new ListView<String>(id, refs) {
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
						return StringUtils.equalsIgnoreCase(getRevision(), ref) ? "checked" : "unchecked";
					}
				}));
				
				link.add(new Label("name", ref).setEscapeModelStrings(true));
				item.add(link);
			}
		};
		
		return view;
	}

	private Class<? extends Page> getPageClass() {
		Page page = getPage();
		if (page instanceof ProjectHomePage) {
			return SourceTreePage.class;
		} else {
			return page.getClass();
		}
	}
}
