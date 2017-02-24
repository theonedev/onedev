package com.gitplex.server.web.page.depot.tags;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.RefInfo;
import com.gitplex.server.model.Depot;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.StringUtils;
import com.gitplex.server.web.behavior.OnTypingDoneBehavior;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.contributorpanel.ContributorPanel;
import com.gitplex.server.web.component.link.AccountLink;
import com.gitplex.server.web.component.link.ArchiveMenuLink;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.revisionpicker.RevisionPicker;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.NoBranchesPage;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.commit.CommitDetailPage;
import com.gitplex.server.web.util.DateUtils;
import com.gitplex.server.web.util.ajaxlistener.ConfirmListener;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotTagsPage extends DepotPage {

	private WebMarkupContainer tagsContainer;
	
	private PagingNavigator pagingNavigator;
	
	private WebMarkupContainer noTagsContainer;
	
	public DepotTagsPage(PageParameters params) {
		super(params);
		
		if (getDepot().getDefaultBranch() == null) 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getDepot()));
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("searchTags", Model.of("")));
		searchField.add(new OnTypingDoneBehavior(200) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(tagsContainer);
				target.add(pagingNavigator);
				target.add(noTagsContainer);
			}
			
		});
		
		add(new ModalLink("createTag") {

			private String tagName;
			
			private String tagMessage;
			
			private String tagRevision = getDepot().getDefaultBranch();
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				ObjectId commit = getDepot().getObjectId(tagRevision);
				setVisible(SecurityUtils.canPushRef(getDepot(), Constants.R_TAGS, ObjectId.zeroId(), commit));
			}

			private RevisionPicker newRevisionPicker() {
				return new RevisionPicker("revision", depotModel, tagRevision) {

					@Override
					protected void onSelect(AjaxRequestTarget target, String revision) {
						tagRevision = revision; 
						RevisionPicker revisionPicker = newRevisionPicker();
						getParent().replace(revisionPicker);
						target.add(revisionPicker);
					}
					
				};
			}
			
			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "createTagFrag", DepotTagsPage.this);
				Form<?> form = new Form<Void>("form");
				form.setOutputMarkupId(true);
				form.add(new NotificationPanel("feedback", form));
				tagName = null;
				final TextField<String> nameInput;
				form.add(nameInput = new TextField<String>("name", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return tagName;
					}

					@Override
					public void setObject(String object) {
						tagName = object;
					}
					
				}));
				nameInput.setOutputMarkupId(true);
				
				tagMessage = null;
				form.add(new TextArea<String>("message", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return tagMessage;
					}

					@Override
					public void setObject(String object) {
						tagMessage = object;
					}
					
				}));
				form.add(newRevisionPicker());
				form.add(new AjaxButton("create") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);
						
						if (tagName == null) {
							form.error("Tag name is required.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (!Repository.isValidRefName(Constants.R_HEADS + tagName)) {
							form.error("Invalid tag name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (getDepot().getObjectId(GitUtils.tag2ref(tagName), false) != null) {
							form.error("Tag '" + tagName + "' already exists, please choose a different name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else {
							getDepot().tag(tagName, tagRevision, getLoginUser().asPerson(), tagMessage);
							close(target);
							target.add(tagsContainer);
							target.add(pagingNavigator);
							target.add(noTagsContainer);
							searchField.setModelObject(null);
							target.add(searchField);
						}
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
			
		});
		
		IModel<List<RefInfo>> tagsModel = new LoadableDetachableModel<List<RefInfo>>() {

			@Override
			protected List<RefInfo> load() {
				List<RefInfo> refs = getDepot().getTags();
				String searchFor = searchField.getModelObject();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<RefInfo> it = refs.iterator(); it.hasNext();) {
						String tag = GitUtils.ref2tag(it.next().getRef().getName());
						if (!tag.toLowerCase().contains(searchFor))
							it.remove();
					}
				}
				return refs;
			}
			
		}; 
		
		add(tagsContainer = new WebMarkupContainer("tagsContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!tagsModel.getObject().isEmpty());
			}
			
		});
		tagsContainer.setOutputMarkupPlaceholderTag(true);
		
		PageableListView<RefInfo> tagsView;

		tagsContainer.add(tagsView = new PageableListView<RefInfo>("tags", tagsModel, 
				com.gitplex.server.web.WebConstants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<RefInfo> item) {
				RefInfo ref = item.getModelObject();
				String tagName = GitUtils.ref2tag(ref.getRef().getName());
				
				DepotBlobPage.State state = new DepotBlobPage.State();
				state.blobIdent.revision = tagName;
				AbstractLink link = new ViewStateAwarePageLink<Void>("tagLink", 
						DepotBlobPage.class, DepotBlobPage.paramsOf(getDepot(), state));
				link.add(new Label("name", tagName));
				item.add(link);

				if (ref.getObj() instanceof RevTag) {
					RevTag revTag = (RevTag) ref.getObj();
					Fragment fragment = new Fragment("annotated", "annotatedFrag", DepotTagsPage.this);
					if (revTag.getTaggerIdent() != null) {
						fragment.add(new AccountLink("author", revTag.getTaggerIdent()));
						fragment.add(new Label("date", DateUtils.formatDate(revTag.getTaggerIdent().getWhen())));
					} else {
						fragment.add(new WebMarkupContainer("author").setVisible(false));
						fragment.add(new WebMarkupContainer("date").setVisible(false));
					}
					Label message = new Label("message", revTag.getFullMessage());
					message.setOutputMarkupId(true);
					fragment.add(message);
					String toggleScript = String.format("$('#%s').toggle();", message.getMarkupId());
					WebMarkupContainer messageToggle = new WebMarkupContainer("messageToggle"); 
					messageToggle.add(AttributeAppender.append("onclick", toggleScript));
					messageToggle.setVisible(StringUtils.isNotBlank(revTag.getFullMessage()));
					fragment.add(messageToggle);
					item.add(fragment);
				} else {
					item.add(new WebMarkupContainer("annotated").setVisible(false));
				}

				RevCommit commit = (RevCommit) ref.getPeeledObj();
				PageParameters params = CommitDetailPage.paramsOf(getDepot(), commit.name());
				
				link = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
				link.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
				item.add(link);
				item.add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));
				
				item.add(new ContributorPanel("contributor", commit.getAuthorIdent(), commit.getCommitterIdent(), true));
				
				link = new ViewStateAwarePageLink<Void>("messageLink", CommitDetailPage.class, params);
				link.add(new Label("message", commit.getShortMessage()));
				item.add(link);
				
				item.add(new ArchiveMenuLink("download", depotModel) {

					@Override
					protected String getRevision() {
						return tagName;
					}
					
				});
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this tag?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getDepot().deleteTag(tagName);
						target.add(tagsContainer);
						target.add(pagingNavigator);
						target.add(noTagsContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						RefInfo ref = item.getModelObject();
						setVisible(SecurityUtils.canPushRef(getDepot(), ref.getRef().getName(), ref.getPeeledObj(), ObjectId.zeroId()));
					}
					
				});
			}
			
		});

		add(pagingNavigator = new BootstrapAjaxPagingNavigator("tagsPageNav", tagsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(tagsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		add(noTagsContainer = new WebMarkupContainer("noTags") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(tagsModel.getObject().isEmpty());
			}
			
		});
		noTagsContainer.setOutputMarkupPlaceholderTag(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DepotTagsResourceReference()));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotTagsPage.class, paramsOf(depot));
	}
}
