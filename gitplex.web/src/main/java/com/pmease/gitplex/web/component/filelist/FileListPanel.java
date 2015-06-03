package com.pmease.gitplex.web.component.filelist;

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
import org.apache.wicket.markup.html.panel.Panel;
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
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.commitmessage.CommitMessagePanel;
import com.pmease.gitplex.web.component.personlink.PersonLink;
import com.pmease.gitplex.web.page.repository.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.commit.RepoCommitPage;
import com.pmease.gitplex.web.util.DateUtils;

@SuppressWarnings("serial")
public abstract class FileListPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final BlobIdent directory;
	
	public FileListPanel(String id, IModel<Repository> repoModel, BlobIdent directory) {
		super(id);

		this.repoModel = repoModel;
		this.directory = directory;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLazyLoadPanel("lastCommit") {
			
			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(
						AbstractDefaultAjaxBehavior.INDICATOR);
				return new Label(markupId, ""
						+ "<div class='message'>"
						+ "  <img src='" + RequestCycle.get().urlFor(handler) + "'/> Loading latest commit..."
						+ "</div>"
						+ "<div class='other-info'>&nbsp;</div>").setEscapeModelStrings(false);
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
							if (directory.path != null)
								log.addPath(directory.path);
							log.add(getCommitId());
							return log.call().iterator().next();
						} catch (MissingObjectException | IncorrectObjectTypeException | GitAPIException e) {
							throw new RuntimeException(e);
						} finally {
							git.close();
						}
					}
					
				};				
				Fragment fragment = new Fragment(id, "lastCommitFrag", FileListPanel.this) {

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

		WebMarkupContainer parent = new WebMarkupContainer("parent") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(directory.path != null);
			}
			
		};
		parent.add(new AjaxLink<Void>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (directory.path.indexOf('/') != -1) {
					onSelect(target, new BlobIdent(
							directory.revision, 
							StringUtils.substringBeforeLast(directory.path, "/"), 
							FileMode.TREE.getBits()));
				} else {
					onSelect(target, new BlobIdent(directory.revision, null, FileMode.TREE.getBits()));
				}
			}
			
		});
		add(parent);
		
		add(new ListView<BlobIdent>("children", new LoadableDetachableModel<List<BlobIdent>>() {

			@Override
			protected List<BlobIdent> load() {
				org.eclipse.jgit.lib.Repository jgitRepo = repoModel.getObject().openAsJGitRepo();
				try {
					RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
					TreeWalk treeWalk;
					if (directory.path != null) {
						treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, directory.path, revTree));
						treeWalk.enterSubtree();
					} else {
						treeWalk = new TreeWalk(jgitRepo);
						treeWalk.addTree(revTree);
					}
					List<BlobIdent> children = new ArrayList<>();
					while (treeWalk.next())
						children.add(new BlobIdent(directory.revision, treeWalk.getPathString(), treeWalk.getRawMode(0)));
					for (int i=0; i<children.size(); i++) {
						BlobIdent child = children.get(i);
						while (child.isTree()) {
							treeWalk = TreeWalk.forPath(jgitRepo, child.path, revTree);
							Preconditions.checkNotNull(treeWalk);
							treeWalk.enterSubtree();
							if (treeWalk.next()) {
								BlobIdent grandChild = new BlobIdent(directory.revision, 
										treeWalk.getPathString(), treeWalk.getRawMode(0));
								if (treeWalk.next()) 
									break;
								else
									child = grandChild;
							} else {
								break;
							}
						}
						children.set(i, child);
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
			protected void populateItem(ListItem<BlobIdent> item) {
				final BlobIdent blobIdent = item.getModelObject();
				
				WebMarkupContainer pathIcon = new WebMarkupContainer("pathIcon");
				String iconClass;
				if (blobIdent.isTree())
					iconClass = "fa fa-folder-o";
				else if (blobIdent.isGitLink()) 
					iconClass = "fa fa-ext fa-submodule-o";
				else if (blobIdent.isSymbolLink()) 
					iconClass = "fa fa-ext fa-symbol-link";
				else  
					iconClass = "fa fa-file-text-o";
				pathIcon.add(AttributeModifier.append("class", iconClass));
				
				item.add(pathIcon);
				
				AjaxLink<Void> pathLink = new AjaxLink<Void>("pathLink") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, blobIdent);
					}
					
				}; 
				
				if (directory.path != null)
					pathLink.add(new Label("label", blobIdent.path.substring(directory.path.length()+1)));
				else
					pathLink.add(new Label("label", blobIdent.path));
				item.add(pathLink);
				
				if (item.getIndex() == 0)
					item.add(new Label("lastCommitSummary", "Loading last commit info..."));
				else
					item.add(new Label("lastCommitSummary"));
			}
			
		});
		
		add(new AbstractDefaultAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				LastCommitsOfChildren lastCommits = repoModel.getObject().getLastCommitsOfChildren(
						directory.revision, directory.path);
				
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
				String script = String.format("gitplex.filelist.renderLastCommits('%s', %s);", 
						getMarkupId(), json);
				target.appendJavaScript(script);
			}
			
			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				
				response.render(JavaScriptHeaderItem.forReference(
						new JavaScriptResourceReference(FileListPanel.class, "file-list.js")));
				response.render(CssHeaderItem.forReference(
						new CssResourceReference(FileListPanel.class, "file-list.css")));
				
				response.render(OnDomReadyHeaderItem.forScript(
						String.format("gitplex.filelist.init('%s')", getMarkupId())));
				response.render(OnDomReadyHeaderItem.forScript(getCallbackScript()));
			}

		});
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, BlobIdent file);
	
	private ObjectId getCommitId() {
		return Preconditions.checkNotNull(repoModel.getObject().getObjectId(directory.revision));
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		
		super.onDetach();
	}

	@SuppressWarnings("unused")
	private static class LastCommitInfo {
		String url;
		
		String summary;
		
		String age;
	}
}
