package com.pmease.gitplex.web.page.depot.overview;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.commons.wicket.component.markdown.MarkdownPanel;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.CommitInfoManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.web.component.AccountLink;
import com.pmease.gitplex.web.component.accountchoice.AccountSingleChoice;
import com.pmease.gitplex.web.component.accountchoice.AdministrativeAccountChoiceProvider;
import com.pmease.gitplex.web.component.depotfile.filelist.FileListPanel;
import com.pmease.gitplex.web.component.depotselector.DepotSelector;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.branches.DepotBranchesPage;
import com.pmease.gitplex.web.page.depot.commit.DepotCommitsPage;
import com.pmease.gitplex.web.page.depot.tags.DepotTagsPage;

@SuppressWarnings("serial")
public class DepotOverviewPage extends DepotPage {

	private final IModel<BlobIdent> readmeModel = new LoadableDetachableModel<BlobIdent>() {

		@Override
		protected BlobIdent load() {
			try (	RevWalk revWalk = new RevWalk(getDepot().getRepository());
					TreeWalk treeWalk = new TreeWalk(getDepot().getRepository());) {
				RevCommit commit = revWalk.parseCommit(getDepot().getObjectId(getDepot().getDefaultBranch()));
				treeWalk.addTree(commit.getTree());
				while (treeWalk.next()) {
					String fileName = treeWalk.getNameString();
					String readme = StringUtils.substringBefore(fileName, ".");
					if (readme.equalsIgnoreCase(FileListPanel.README_NAME)) {
						return new BlobIdent(getDepot().getDefaultBranch(), 
								treeWalk.getPathString(), treeWalk.getRawMode(0));
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}
		
	};
	
	public DepotOverviewPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("title", getDepot().getName()));
		add(new AccountLink("accountLink", getDepot().getAccount()));
		if (getDepot().getForkedFrom() != null) {
			Link<Void> link = new BookmarkablePageLink<Void>("forkedFromLink", 
					DepotOverviewPage.class, DepotOverviewPage.paramsOf(getDepot().getForkedFrom()));
			link.add(new Label("name", getDepot().getForkedFrom().getFQN()));
			add(link);
		} else {
			WebMarkupContainer link = new WebMarkupContainer("forkedFromLink");
			link.add(new Label("name"));
			link.setVisible(false);
			add(link);
		}
		add(new Label("id", getDepot().getId()));
		
		UrlManager urlManager = GitPlex.getInstance(UrlManager.class);
		Model<String> cloneUrlModel = Model.of(urlManager.urlFor(getDepot()));
		add(new TextField<String>("cloneUrl", cloneUrlModel));
		
		if (getDepot().getDescription() != null) {
			add(new MarkdownPanel("description", Model.of(getDepot().getDescription()), null));
		} else {
			add(new WebMarkupContainer("description").setVisible(false));
		}
		
		add(new BookmarkablePageLink<Void>("commitsLink", 
				DepotCommitsPage.class, DepotCommitsPage.paramsOf(getDepot())) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(GitPlex.getInstance(CommitInfoManager.class).getCommitCount(getDepot()) != 0);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				CommitInfoManager commitInfoManager = GitPlex.getInstance(CommitInfoManager.class);
				add(new Label("count", commitInfoManager.getCommitCount(getDepot()) + " commits"));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("branchesLink", 
				DepotBranchesPage.class, DepotBranchesPage.paramsOf(getDepot())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", getDepot().getRefs(Constants.R_HEADS).size() + " branches"));
			}
			
		});
		
		add(new BookmarkablePageLink<Void>("tagsLink", 
				DepotTagsPage.class, DepotTagsPage.paramsOf(getDepot())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", getDepot().getRefs(Constants.R_TAGS).size() + " tags"));
			}
			
		});
		
		if (getDepot().getForks().isEmpty()) {
			add(new WebMarkupContainer("forks") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", "0 forks"));
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("span");
				}
				
			});
		} else {
			add(new DropdownLink("forks") {

				@Override
				protected void onInitialize() {
					super.onInitialize();
					add(new Label("label", getDepot().getForks().size() + " forks <i class='fa fa-caret-down'></i>").setEscapeModelStrings(false));
				}

				@Override
				protected Component newContent(String id) {
					return new DepotSelector(id, new LoadableDetachableModel<Collection<Depot>>() {

						@Override
						protected Collection<Depot> load() {
							return getDepot().getForks();
						}
						
					}, Depot.idOf(getDepot())) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Depot depot) {
							setResponsePage(DepotOverviewPage.class, DepotOverviewPage.paramsOf(depot));
						}

					};
				}
				
			});
		}
		
		add(new ModalLink("forkNow") {

			private Long ownerId;
			
			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "forkFrag", DepotOverviewPage.this);
				Depot depot = new Depot();
				depot.setForkedFrom(getDepot());
				depot.setName(getDepot().getName());
				
				BeanEditor<?> editor = BeanContext.editBean("editor", depot);
				
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				form.add(editor);
				
				IModel<Account> choiceModel = new IModel<Account>() {

					@Override
					public void detach() {
					}

					@Override
					public Account getObject() {
						return getOwner();
					}

					@Override
					public void setObject(Account object) {
						ownerId = object.getId();
					}
					
				};
				form.add(new AccountSingleChoice("owner", choiceModel, new AdministrativeAccountChoiceProvider()).setRequired(true));
				
				form.add(new AjaxButton("save") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						Account owner = getOwner();
						depot.setAccount(owner);
						DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
						Depot depotWithSameName = depotManager.find(owner, depot.getName());
						if (depotWithSameName != null) {
							editor.getErrorContext(new PathSegment.Property("name"))
									.addError("This name has already been used by another repository in account '" + owner.getDisplayName() + "'");
							target.add(form);
						} else {
							depotManager.fork(getDepot(), depot);
							Session.get().success("Repository forked");
							setResponsePage(DepotOverviewPage.class, DepotOverviewPage.paramsOf(depot));
						}
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}
					
				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						close(target);
					}
					
				});
				
				fragment.add(form);				
				
				return fragment;
			}
			
			private Account getOwner() {
				if (ownerId == null)
					return getLoginUser();
				else
					return GitPlex.getInstance(AccountManager.class).load(ownerId);
			}
			
		}.setVisible(isLoggedIn()));
	}

	@Override
	protected void onDetach() {
		readmeModel.detach();
		
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				DepotOverviewPage.class, "depot-overview.css")));
	}

}
