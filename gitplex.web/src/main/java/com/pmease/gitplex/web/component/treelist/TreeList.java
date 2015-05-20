package com.pmease.gitplex.web.component.treelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitPath;
import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.user.PersonLink;
import com.pmease.gitplex.web.page.repository.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public class TreeList extends GenericPanel<String> {

	private final IModel<Repository> repoModel;
	
	private final IModel<String> revModel;
	
	public TreeList(String id, IModel<Repository> repoModel, IModel<String> revModel, IModel<String> pathModel) {
		super(id, pathModel);

		this.repoModel = repoModel;
		this.revModel = revModel;
	}

	private String getNormalizedPath() {
		return GitPath.normalize(getModelObject());
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer parent = new WebMarkupContainer("parent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getNormalizedPath() != null);
			}
			
		};
		parent.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				String path = getNormalizedPath();
				if (path.indexOf('/') != -1)
					path = StringUtils.substringBeforeLast(path, "/");
				else
					path = "";
				selectPath(target, path);
			}
			
		});
		add(parent);
		
		add(new ListView<GitPath>("children", new LoadableDetachableModel<List<GitPath>>() {

			@Override
			protected List<GitPath> load() {
				String path = getNormalizedPath();
				org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
				try {
					RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
					TreeWalk treeWalk;
					if (path != null) {
						treeWalk = TreeWalk.forPath(jgitRepo, path, revTree);
						if (treeWalk != null) {
							treeWalk.enterSubtree();
						} else {
							path = null;
							treeWalk = new TreeWalk(jgitRepo);
							treeWalk.addTree(revTree);
						}
					} else {
						treeWalk = new TreeWalk(jgitRepo);
						treeWalk.addTree(revTree);
					}
					List<GitPath> children = new ArrayList<>();
					while (treeWalk.next())
						children.add(new GitPath(treeWalk.getPathString(), treeWalk.getRawMode(0)));
					for (int i=0; i<children.size(); i++) {
						GitPath gitPath = children.get(i);
						while (FileMode.TREE.equals(gitPath.getMode())) {
							treeWalk = TreeWalk.forPath(jgitRepo, gitPath.getName(), revTree);
							Preconditions.checkNotNull(treeWalk);
							treeWalk.enterSubtree();
							if (treeWalk.next()) {
								GitPath childGitPath = new GitPath(treeWalk.getPathString(), treeWalk.getRawMode(0));
								if (treeWalk.next()) 
									break;
								else
									gitPath = childGitPath;
							} else {
								break;
							}
						}
						children.set(i, gitPath);
					}
					
					Collections.sort(children);
					return children;
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					jgitRepo.close();
				}
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<GitPath> item) {
				final GitPath pathInfo = item.getModelObject();
				
				WebMarkupContainer pathIcon = new WebMarkupContainer("pathIcon");
				String iconClass;
				if (FileMode.TREE.equals(pathInfo.getMode()))
					iconClass = "fa fa-folder-o";
				else if (FileMode.GITLINK.equals(pathInfo.getMode())) 
					iconClass = "fa fa-ext fa-submodule-o";
				else if (FileMode.SYMLINK.equals(pathInfo.getMode())) 
					iconClass = "fa fa-ext fa-symbol-link";
				else  
					iconClass = "fa fa-file-text-o";
				pathIcon.add(AttributeModifier.append("class", iconClass));
				
				item.add(pathIcon);
				
				AjaxLink<Void> pathLink = new AjaxLink<Void>("pathLink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						selectPath(target, pathInfo.getName());
					}
					
				}; 
				
				String parentPath = getNormalizedPath();
				if (parentPath != null)
					pathLink.add(new Label("label", pathInfo.getName().substring(parentPath.length()+1)));
				else
					pathLink.add(new Label("label", pathInfo.getName()));
				item.add(pathLink);
			}
			
		});
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				LastCommitsOfChildren lastCommits = repoModel.getObject().getLastCommitsOfChildren(
						revModel.getObject(), getModelObject());
				
				Map<String, LastCommitInfo> map = new HashMap<>();
				for (Map.Entry<String, LastCommitsOfChildren.Value> entry: lastCommits.entrySet()) {
					LastCommitInfo info = new LastCommitInfo();
					PageParameters params = RepoCommitPage.paramsOf(
							repoModel.getObject(), entry.getValue().getId().name());
					info.url = RequestCycle.get().urlFor(RepoBlobPage.class, params).toString();
					info.summary = entry.getValue().getSummary();
					info.age = DateUtils.formatAge(new Date(entry.getValue().getTimestamp()*1000L));
					map.put(entry.getKey(), info);
				}
				String json;
				try {
					json = GitPlex.getInstance(ObjectMapper.class).writeValueAsString(map);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
				String script = String.format("gitplex.treelist.renderLastCommits('%s', %s);", 
						getMarkupId(), json);
				target.appendJavaScript(script);
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(TreeList.class, "tree-list.js")));
				response.render(CssHeaderItem.forReference(new CssResourceReference(TreeList.class, "tree-list.css")));
				response.render(OnDomReadyHeaderItem.forScript(getCallbackScript()));
			}

		});
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onBeforeRender() {
		addOrReplace(new AjaxLazyLoadPanel("lastCommit") {
			
			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(
						AbstractDefaultAjaxBehavior.INDICATOR);
				return new Label(markupId, "<img src=\"" +
					RequestCycle.get().urlFor(handler) + "\"/> Loading latest commit...").setEscapeModelStrings(false);
			}

			@Override
			public Component getLazyLoadComponent(String id) {
				final IModel<RevCommit> commitModel = new LoadableDetachableModel<RevCommit>(){

					@Override
					protected RevCommit load() {
						Git git = Git.wrap(repoModel.getObject().openAsJGitRepo());
						try {
							LogCommand log = git.log();
							log.setMaxCount(1);
							String path = getNormalizedPath();
							if (path != null)
								log.addPath(path);
							log.add(getCommitId());
							return log.call().iterator().next();
						} catch (MissingObjectException | IncorrectObjectTypeException | GitAPIException e) {
							throw new RuntimeException(e);
						} finally {
							git.close();
						}
					}
					
				};				
				Fragment fragment = new Fragment(id, "lastCommitFrag", TreeList.this) {

					@Override
					protected void onDetach() {
						commitModel.detach();
						
						super.onDetach();
					}
					
				};
				
				fragment.add(new CommitMessagePanel("message", repoModel, commitModel));
				
				fragment.add(new PersonLink("author", new AbstractReadOnlyModel<PersonIdent>() {

					@Override
					public PersonIdent getObject() {
						return commitModel.getObject().getAuthorIdent();
					}
					
				}));
				
				fragment.add(new Label("date", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return DateUtils.formatAge(commitModel.getObject().getAuthorIdent().getWhen());
					}
					
				}));
				
				PageParameters params = RepoCommitPage.paramsOf(
						repoModel.getObject(), commitModel.getObject().name());
				BookmarkablePageLink<Void> commitLink = new BookmarkablePageLink<Void>(
						"commitLink", RepoBlobPage.class, params);
				commitLink.add(new Label("commitId", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return GitUtils.abbreviateSHA(commitModel.getObject().name());
					}
					
				}));

				
				fragment.add(commitLink);
				
				return fragment;
			}
			
		});
		
		super.onBeforeRender();
	}

	private void selectPath(AjaxRequestTarget target, String path) {
		if (path.length() == 0)
			path = null;
		setModelObject(path);
		target.add(this);
	}
	
	private ObjectId getCommitId() {
		return Preconditions.checkNotNull(repoModel.getObject().resolveRevision(revModel.getObject()));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		revModel.detach();
		
		super.onDetach();
	}

	@SuppressWarnings("unused")
	private static class LastCommitInfo {
		String url;
		
		String summary;
		
		String age;
	}
}
