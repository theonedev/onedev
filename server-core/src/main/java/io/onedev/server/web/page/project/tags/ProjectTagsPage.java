package io.onedev.server.web.page.project.tags;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.ajaxlistener.ConfirmListener;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.component.commit.status.CommitStatusPanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.datatable.HistoryAwarePagingNavigator;
import io.onedev.server.web.component.link.ArchiveMenuLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.revisionpicker.RevisionPicker;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.PersonIdentPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.PagingHistorySupport;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public class ProjectTagsPage extends ProjectPage {

	private static final String PARAM_PAGE = "page";
	
	private static final String PARAM_QUERY = "query";
	
	private WebMarkupContainer tagsContainer;
	
	private PageableListView<RefInfo> tagsView;
	
	private final PagingHistorySupport pagingHistorySupport;	
	
	private String query;
	
	private final IModel<List<RefInfo>> tagsModel = new LoadableDetachableModel<List<RefInfo>>() {

		@Override
		protected List<RefInfo> load() {
			List<RefInfo> refs = getProject().getTagRefInfos();
			if (query != null) {
				for (Iterator<RefInfo> it = refs.iterator(); it.hasNext();) {
					String tag = GitUtils.ref2tag(it.next().getRef().getName());
					if (!tag.toLowerCase().contains(query.trim().toLowerCase()))
						it.remove();
				}
			}
			return refs;
		}
		
	}; 
	
	public ProjectTagsPage(PageParameters params) {
		super(params);
		
		query = params.get(PARAM_QUERY).toString();
		
		pagingHistorySupport = new PagingHistorySupport() {
			
			@Override
			public PageParameters newPageParameters(int currentPage) {
				PageParameters params = paramsOf(getProject());
				params.add(PARAM_PAGE, currentPage+1);
				if (query != null)
					params.add(PARAM_QUERY, query);
				return params;
			}
			
			@Override
			public int getCurrentPage() {
				return getPageParameters().get(PARAM_PAGE).toInt(1)-1;
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TextField<String> searchField;
		add(searchField = new TextField<String>("searchTags", Model.of(query)));
		searchField.add(new OnTypingDoneBehavior(200) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				query = searchField.getInput();
				if (StringUtils.isBlank(query))
					query = null;
				target.add(tagsContainer);
				newPagingNavigation(target);
			}
			
		});
		
		add(new ModalLink("createTag") {

			private String tagName;
			
			private String tagMessage;
			
			private String tagRevision = getProject().getDefaultBranch();
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(SecurityUtils.canCreateTag(getProject(), Constants.R_TAGS));
			}

			private RevisionPicker newRevisionPicker() {
				return new RevisionPicker("revision", projectModel, tagRevision) {

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
			protected Component newContent(String id, ModalPanel modal) {
				Fragment fragment = new Fragment(id, "createTagFrag", ProjectTagsPage.this);
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
						} else if (tagRevision == null) {
							form.error("Create from is required.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (!Repository.isValidRefName(Constants.R_HEADS + tagName)) {
							form.error("Invalid tag name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else if (getProject().getObjectId(GitUtils.tag2ref(tagName), false) != null) {
							form.error("Tag '" + tagName + "' already exists, please choose a different name.");
							target.focusComponent(nameInput);
							target.add(form);
						} else {
							Project project = getProject();
							TagProtection protection = project.getTagProtection(tagName, getLoginUser());
							if (protection.isPreventCreation()) {
								form.error("Unable to create protected tag");
								target.focusComponent(nameInput);
								target.add(form);
							} else {
								getProject().createTag(tagName, tagRevision, getLoginUser().asPerson(), tagMessage);
								modal.close();
								target.add(tagsContainer);
								newPagingNavigation(target);
								target.addListener(new AjaxRequestTarget.IListener() {

									@Override
									public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {
									}

									@Override
									public void onAfterRespond(Map<String, Component> map, IJavaScriptResponse response) {
										OneDev.getInstance(WebSocketManager.class).observe(ProjectTagsPage.this);
									}

									@Override
									public void updateAjaxAttributes(AbstractDefaultAjaxBehavior behavior,
											AjaxRequestAttributes attributes) {
									}
									
								});
							}
						}
					}

				});
				form.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						modal.close();
					}
					
				});
				fragment.add(form);
				return fragment;
			}
			
		});
		
		add(tagsContainer = new WebMarkupContainer("tagsContainer") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!tagsModel.getObject().isEmpty());
			}

			@Override
			protected void onBeforeRender() {
				List<ObjectId> commitIdsToDisplay = new ArrayList<>();
				for (long i=tagsView.getFirstItemOffset(); i<tagsModel.getObject().size(); i++) {
					if (i-tagsView.getFirstItemOffset() >= tagsView.getItemsPerPage())
						break;
					RefInfo ref = tagsModel.getObject().get((int)i); 
					commitIdsToDisplay.add(ref.getPeeledObj().copy());
				}
				
				BuildManager buildManager = OneDev.getInstance(BuildManager.class);
				getProject().cacheCommitStatus(buildManager.queryStatus(getProject(), commitIdsToDisplay));
				super.onBeforeRender();
			}
			
		});
		
		tagsContainer.setOutputMarkupPlaceholderTag(true);
		
		tagsContainer.add(new FencedFeedbackPanel("feedback", tagsContainer).setEscapeModelStrings(false));
		
		tagsContainer.add(tagsView = new PageableListView<RefInfo>("tags", tagsModel, 
				io.onedev.server.web.WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<RefInfo> item) {
				RefInfo ref = item.getModelObject();
				String tagName = GitUtils.ref2tag(ref.getRef().getName());
				
				BlobIdent blobIdent = new BlobIdent(tagName, null, FileMode.TREE.getBits());
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				AbstractLink link = new ViewStateAwarePageLink<Void>("tagLink", 
						ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), state));
				link.add(new Label("name", tagName));
				item.add(link);
				
				item.add(new CommitStatusPanel("buildStatus", ref.getPeeledObj().copy()) {

					@Override
					protected Project getProject() {
						return ProjectTagsPage.this.getProject();
					}
					
				});

				if (ref.getObj() instanceof RevTag) {
					RevTag revTag = (RevTag) ref.getObj();
					Fragment fragment = new Fragment("annotated", "annotatedFrag", ProjectTagsPage.this);
					if (revTag.getTaggerIdent() != null) 
						fragment.add(new PersonIdentPanel("author", revTag.getTaggerIdent(), "Tagger", Mode.NAME));
					else 
						fragment.add(new WebMarkupContainer("author").setVisible(false));
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
				PageParameters params = CommitDetailPage.paramsOf(getProject(), commit.name());
				
				link = new ViewStateAwarePageLink<Void>("messageLink", CommitDetailPage.class, params);
				link.add(new Label("message", commit.getShortMessage()));
				item.add(link);
				
				item.add(new ContributorPanel("contributor", commit.getAuthorIdent(), commit.getCommitterIdent()));
				
				item.add(new ArchiveMenuLink("download", projectModel) {

					@Override
					protected String getRevision() {
						return tagName;
					}
					
				});
				
				item.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete tag " + tagName + "?"));
					}

					@Override
					protected void disableLink(ComponentTag tag) {
						super.disableLink(tag);
						tag.append("class", "disabled", " ");
						tag.put("title", "Deletion not allowed due to branch protection rule");
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						try {
							OneDev.getInstance(ProjectManager.class).deleteTag(getProject(), tagName);
							newPagingNavigation(target);
						} catch (OneException e) {
							tagsContainer.error(e.getMessage());
						}
						target.add(tagsContainer);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						Project project = getProject();
						if (SecurityUtils.canWriteCode(project)) 
							setEnabled(!project.getTagProtection(tagName, getLoginUser()).isPreventDeletion());
						else 
							setVisible(false);
					}
					
				});
			}
			
		});

		tagsView.setCurrentPage(pagingHistorySupport.getCurrentPage());

		newPagingNavigation(null);
	}
	
	private void newPagingNavigation(@Nullable AjaxRequestTarget target) {
		Component pagingNavigator = new HistoryAwarePagingNavigator("tagsPageNav", tagsView, pagingHistorySupport);
		pagingNavigator.setVisible(tagsView.getPageCount() > 1);
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		Component noTagsContainer = new WebMarkupContainer("noTags");
		noTagsContainer.setVisible(tagsModel.getObject().isEmpty());
		noTagsContainer.setOutputMarkupPlaceholderTag(true);
		
		if (target != null) {
			replace(pagingNavigator);
			replace(noTagsContainer);
			target.add(pagingNavigator);
			target.add(noTagsContainer);
		} else {
			add(pagingNavigator);
			add(noTagsContainer);
		}
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectTagsResourceReference()));
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

}
