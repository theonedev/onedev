package com.gitplex.server.web.page.depot.moreinfo;

import java.util.Collection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.eclipse.jgit.lib.Constants;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.UrlManager;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.depotselector.DepotSelector;
import com.gitplex.server.web.component.link.DropdownLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.markdownviewer.MarkdownViewer;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.branches.DepotBranchesPage;
import com.gitplex.server.web.page.depot.commit.DepotCommitsPage;
import com.gitplex.server.web.page.depot.tags.DepotTagsPage;

@SuppressWarnings("serial")
public abstract class MoreInfoPanel extends Panel {

	private final IModel<Depot> depotModel;
	
	public MoreInfoPanel(String id, IModel<Depot> depotModel) {
		super(id);
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getDepot().getForkedFrom() != null) {
			Link<Void> link = new ViewStateAwarePageLink<Void>("forkedFromLink", 
					DepotBlobPage.class, DepotBlobPage.paramsOf(getDepot().getForkedFrom()));
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
		add(new WebMarkupContainer("copyUrl").add(new CopyClipboardBehavior(cloneUrlModel)));
		
		if (getDepot().getDescription() != null) {
			add(new MarkdownViewer("description", Model.of(getDepot().getDescription()), null));
		} else {
			add(new WebMarkupContainer("description").setVisible(false));
		}
		
		add(new ViewStateAwarePageLink<Void>("commitsLink", 
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
		
		add(new ViewStateAwarePageLink<Void>("branchesLink", 
				DepotBranchesPage.class, DepotBranchesPage.paramsOf(getDepot())) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("count", getDepot().getRefs(Constants.R_HEADS).size() + " branches"));
			}
			
		});
		
		add(new ViewStateAwarePageLink<Void>("tagsLink", 
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
							setResponsePage(DepotBlobPage.class, DepotBlobPage.paramsOf(depot));
						}

					};
				}
				
			});
		}
		
		add(new ModalLink("forkNow") {
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				super.onClick(target);
				onPromptForkOption(target);
			}

			@Override
			protected Component newContent(String id) {
				return new ForkOptionPanel(id, depotModel) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						close(target);
					}
					
				};
			}
			
		}.setVisible(SecurityUtils.getAccount() != null));
	}
	
	private Depot getDepot() {
		return depotModel.getObject();
	}

	@Override
	protected void onDetach() {
		depotModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MoreInfoResourceReference()));
	}

	protected abstract void onPromptForkOption(AjaxRequestTarget target);
	
}
