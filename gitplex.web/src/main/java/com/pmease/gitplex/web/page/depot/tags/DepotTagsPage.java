package com.pmease.gitplex.web.page.depot.tags;

import java.io.IOException;
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
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ajaxlistener.ConfirmListener;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.commithash.CommitHashPanel;
import com.pmease.gitplex.web.component.revisionpicker.RevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage;
import com.pmease.gitplex.web.page.depot.file.DepotFilePage.HistoryState;
import com.pmease.gitplex.web.resource.ArchiveResource;
import com.pmease.gitplex.web.resource.ArchiveResourceReference;
import com.pmease.gitplex.web.util.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DepotTagsPage extends DepotPage {

	private WebMarkupContainer tagsContainer;
	
	private PagingNavigator pagingNavigator;
	
	private WebMarkupContainer noTagsContainer;
	
	public DepotTagsPage(PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));
	}
	
	@Override
	protected String getPageTitle() {
		return getDepot() + " - Tags";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new ClearableTextField<String>("searchTags", Model.of("")));
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
		
		IModel<List<Ref>> tagsModel = new AbstractReadOnlyModel<List<Ref>>() {

			@Override
			public List<Ref> getObject() {
				List<Ref> refs = getDepot().getTagRefs();
				String searchFor = searchField.getModelObject();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<Ref> it = refs.iterator(); it.hasNext();) {
						String tag = GitUtils.ref2tag(it.next().getName());
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
		
		PageableListView<Ref> tagsView;

		tagsContainer.add(tagsView = new PageableListView<Ref>("tags", tagsModel, 
				com.pmease.gitplex.web.Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Ref> item) {
				Ref ref = item.getModelObject();
				final String tagName = GitUtils.ref2tag(ref.getName());
				
				HistoryState state = new HistoryState();
				state.blobIdent.revision = tagName;
				AbstractLink link = new BookmarkablePageLink<Void>("tagLink", 
						DepotFilePage.class, DepotFilePage.paramsOf(getDepot(), state));
				link.add(new Label("name", tagName));
				item.add(link);

				RevObject revObject = getDepot().getRevObject(ref.getObjectId());
				if (revObject instanceof RevTag) {
					RevTag revTag = (RevTag) revObject;
					Fragment fragment = new Fragment("annotated", "annotatedFrag", DepotTagsPage.this);
					if (revTag.getTaggerIdent() != null) {
						fragment.add(new UserLink("author", revTag.getTaggerIdent()));
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

				RevCommit commit = getDepot().getRevCommit(ref.getObjectId());
				item.add(new CommitHashPanel("hash", commit.name()));
				PageParameters params = CommitDetailPage.paramsOf(getDepot(), commit.name());
				link = new BookmarkablePageLink<Void>("commitLink", CommitDetailPage.class, params);
				link.add(new Label("shortMessage", commit.getShortMessage()));
				item.add(link);
				
				item.add(new ResourceLink<Void>("download", new ArchiveResourceReference(), 
						ArchiveResource.paramsOf(getDepot(), tagName)));
				
				link = new Link<Void>("compare") {

					@Override
					public void onClick() {
						try (	Repository repository = getDepot().openRepository();
								RevWalk revWalk = new RevWalk(repository);) {
							RevCommit currentCommit = revWalk.lookupCommit(
									getDepot().getRevCommit(item.getModelObject().getObjectId()).getId());
							Ref prevAncestorRef = null;
							for (int i=item.getIndex()+1; i<tagsModel.getObject().size(); i++) {
								Ref prevRef = tagsModel.getObject().get(i);
								revWalk.setRevFilter(RevFilter.MERGE_BASE);
								revWalk.markStart(currentCommit);
								
								RevCommit prevCommit = revWalk.lookupCommit(
										getDepot().getRevCommit(prevRef.getObjectId()).getId());
								revWalk.markStart(prevCommit);
								if (prevCommit.equals(revWalk.next())) {
									prevAncestorRef = prevRef;
									break;
								}
								revWalk.reset();
							}
							DepotAndRevision target;
							if (prevAncestorRef != null) {
								target = new DepotAndRevision(getDepot(), 
										GitUtils.ref2tag(prevAncestorRef.getName()));
							} else {
								target = new DepotAndRevision(getDepot(), 
										GitUtils.ref2tag(item.getModelObject().getName()));
							}
							DepotAndRevision source = new DepotAndRevision(getDepot(), 
									GitUtils.ref2tag(item.getModelObject().getName()));
							PageParameters params = RevisionComparePage.paramsOf(getDepot(), target, source, true, "");
							setResponsePage(RevisionComparePage.class, params);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
				};
				link.setVisible(item.getIndex()<tagsModel.getObject().size()-1);
				
				item.add(link);
				
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

						Ref ref = item.getModelObject();
						ObjectId commit = getDepot().getRevCommit(ref.getObjectId());
						setVisible(SecurityUtils.canPushRef(getDepot(), ref.getName(), commit, ObjectId.zeroId()));
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
		response.render(CssHeaderItem.forReference(new CssResourceReference(
				DepotTagsPage.class, "depot-tags.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotTagsPage.class, paramsOf(depot));
	}
}
