package io.onedev.server.web.component.issue.primary;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.servlet.http.Cookie;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.service.IssueChangeService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueReactionService;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CommentRevision;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.EmailAddressUtils;
import io.onedev.server.util.LinkDescriptor;
import io.onedev.server.util.LinkGroup;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.comment.CommentHistoryLink;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.comment.ReactionSupport;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
import io.onedev.server.web.component.issue.create.NewIssueEditor;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.DeleteCallback;

public abstract class IssuePrimaryPanel extends Panel {

	private static final String COOKIE_LINK_ISSUE_TAB = "linkIssue.tab";

	private final IModel<List<LinkSpec>> linkSpecsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<LinkSpec> load() {
			return getLinkSpecService().queryAndSort();
		}

	};

	private final IModel<List<LinkDescriptor>> addibleLinkDescriptorsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<LinkDescriptor> load() {
			var linkDescriptors = new ArrayList<LinkDescriptor>();
			for (LinkSpec spec: linkSpecsModel.getObject()) {
				if (SecurityUtils.canEditIssueLink(getProject(), spec)) {
					if (spec.getOpposite() != null) {
						if (spec.getOpposite().getParsedIssueQuery(getProject()).matches(getIssue()))
							linkDescriptors.add(new LinkDescriptor(spec, false));
						if (spec.getParsedIssueQuery(getProject()).matches(getIssue()))
							linkDescriptors.add(new LinkDescriptor(spec, true));	
					} else if (spec.getParsedIssueQuery(getProject()).matches(getIssue())) {
						linkDescriptors.add(new LinkDescriptor(spec, false));
					}
				}
			}	
			return linkDescriptors;		
		}
	};

	private final IModel<List<LinkGroup>> linkGroupsModel = new LoadableDetachableModel<>() {

		@Override
		protected List<LinkGroup> load() {
			List<LinkGroup> linkGroups = new ArrayList<>();
			for (LinkSpec spec : linkSpecsModel.getObject()) {
				if (spec.getOpposite() != null) {
					var targetIssues = getIssue().findLinkedIssues(spec, false).stream().filter(it->canAccessIssue(it)).collect(Collectors.toList());
					if (!targetIssues.isEmpty())
						linkGroups.add(new LinkGroup(new LinkDescriptor(spec, false), targetIssues));
					var sourceIssues = getIssue().findLinkedIssues(spec, true).stream().filter(it->canAccessIssue(it)).collect(Collectors.toList());
					if (!sourceIssues.isEmpty())
						linkGroups.add(new LinkGroup(new LinkDescriptor(spec, true), sourceIssues));
				} else {
					var issues = getIssue().findLinkedIssues(spec, false).stream().filter(it->canAccessIssue(it)).collect(Collectors.toList());
					if (!issues.isEmpty())
						linkGroups.add(new LinkGroup(new LinkDescriptor(spec, false), issues));
				}
			}
			return linkGroups;
		}

	};

	public IssuePrimaryPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		Issue issue = getIssue();
		add(new UserIdentPanel("submitter", issue.getSubmitter(), Mode.AVATAR_AND_NAME));
		add(new Label("submitDate", DateUtils.formatAge(issue.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(issue.getSubmitDate()))));

		if (issue.getOnBehalfOf() != null) {
			var onBehalfOfInfo = HtmlEscape.escapeHtml5(EmailAddressUtils.describe(issue.getOnBehalfOf(), SecurityUtils.canManageIssues(getProject())));
			add(new Label("submitOnBehalfOf", " " + MessageFormat.format(_T("(on behalf of <b>{0}</b>)"), onBehalfOfInfo)).setEscapeModelStrings(false));
		} else {
			add(new WebMarkupContainer("submitOnBehalfOf").setVisible(false));
		}

		add(new CommentPanel("description") {
			
			@Override
			protected String getComment() {
				return getIssue().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				var user = SecurityUtils.getUser();
				OneDev.getInstance(IssueChangeService.class).changeDescription(user, getIssue(), comment);
				((BasePage)getPage()).notifyObservablesChange(target, getIssue().getChangeObservables(false));
			}

			@Override
			protected List<User> getParticipants() {
				return getIssue().getParticipants();
			}
			
			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getIssue().getUUID(), 
						SecurityUtils.canManageIssues(getProject()));
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModifyIssue(getIssue());
			}

			@Override
			protected String getRequiredLabel() {
				return null;
			}

			@Override
			protected String getEmptyDescription() {
				return _T("No description");
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return () -> 0;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}

			@Override
			protected String getAutosaveKey() {
				return "issue:" + getIssue().getId() + ":description";
			}

			@Override
			protected ReactionSupport getReactionSupport() {
				return new ReactionSupport() {
					
					@Override
					public Collection<? extends EntityReaction> getReactions() {
						return getIssue().getReactions();
					}
					
					@Override
					public void onToggleEmoji(AjaxRequestTarget target, String emoji) {
						OneDev.getInstance(IssueReactionService.class).toggleEmoji(
							SecurityUtils.getUser(), 
							getIssue(), 
							emoji);
					}
		
				};
			}

			@Override
			protected Component newMoreActions(String componentId) {
				var fragment = new Fragment(componentId, "moreIssueActionsFrag", IssuePrimaryPanel.this) {
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!addibleLinkDescriptorsModel.getObject().isEmpty() || getIssue().getDescriptionRevisionCount() != 0);
					}
				};
				fragment.add(new MenuLink("linkIssues") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						for (LinkDescriptor descriptor: addibleLinkDescriptorsModel.getObject()) {
							var spec = descriptor.getSpec();
							var specId = spec.getId();
							var opposite = descriptor.isOpposite();
							var linkName = spec.getName(opposite);
							menuItems.add(new MenuItem() {
								@Override
								public String getLabel() {
									return linkName;
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxLink<Void>(id) {
										@Override
										public void onClick(AjaxRequestTarget target) {
											dropdown.close();
											onLinkIssue(target, specId, opposite, linkName);
										}
									};
								};
							});
						}
						return menuItems;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!addibleLinkDescriptorsModel.getObject().isEmpty());
					}

				});
				fragment.add(new CommentHistoryLink("history") {

					@Override
					protected Collection<? extends CommentRevision> getCommentRevisions() {
						return getIssue().getDescriptionRevisions();
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getIssue().getDescriptionRevisionCount() != 0);
					}

				});
				return fragment;
			}

		});
        
		var linksContainer = new WebMarkupContainer("links") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!linkGroupsModel.getObject().isEmpty());
			}

		};
		linksContainer.add(new ChangeObserver() {
			
			@Override
			protected Collection<String> findObservables() {
				return Collections.singleton(Issue.getDetailChangeObservable(getIssue().getId()));
			}

		});
		linksContainer.setOutputMarkupPlaceholderTag(true);
		add(linksContainer);
		linksContainer.add(new ListView<LinkGroup>("links", linkGroupsModel) {

			@Override
			protected void populateItem(ListItem<LinkGroup> item) {
				var group = item.getModelObject();
				var descriptor = group.getDescriptor();
				var spec = descriptor.getSpec();
				var specId = spec.getId();
				var opposite = descriptor.isOpposite();
				
				boolean canEditIssueLink = SecurityUtils.canEditIssueLink(getProject(), spec);
				
				String linkName = spec.getName(opposite);
				item.add(new Label("name", linkName));
				
				RepeatingView linkedIssuesView = new RepeatingView("linkedIssues");
				for (Issue linkedIssue: group.getIssues()) {
					LinkDeleteListener deleteListener;
					if (canEditIssueLink) { 
						deleteListener = new LinkDeleteListener() {
	
							@Override
							void onDelete(AjaxRequestTarget target, Issue linkedIssue) {
								getIssueChangeService().removeLink(SecurityUtils.getUser(), getLinkSpecService().load(specId), getIssue(), 
										linkedIssue, opposite);
								notifyIssueChange(target, getIssue());
							}
							
						};
					} else {
						deleteListener = null;
					}
					linkedIssuesView.add(newLinkedIssueContainer(linkedIssuesView.newChildId(), 
							linkedIssue, deleteListener));
				}
				item.add(linkedIssuesView);

				boolean applicable;
				if (spec.getOpposite() != null) {
					if (opposite)
						applicable = spec.getParsedIssueQuery(getProject()).matches(getIssue());
					else
						applicable = spec.getOpposite().getParsedIssueQuery(getProject()).matches(getIssue());
				} else {
					applicable = spec.getParsedIssueQuery(getProject()).matches(getIssue());
				}

				item.add(new AjaxLink<Void>("addLink") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						onLinkIssue(target, specId, opposite, linkName);
					}
				}.setVisible(applicable && canEditIssueLink));
			}
			
		});
	}

	private void onLinkIssue(AjaxRequestTarget target, Long specId, boolean opposite, String linkName) {
		var spec = getLinkSpecService().load(specId);
		if (!opposite && !spec.isMultiple() && getIssue().findLinkedIssue(spec, false) != null 
				|| opposite && !spec.getOpposite().isMultiple() && getIssue().findLinkedIssue(spec, true) != null) {
			Session.get().error(MessageFormat.format(_T("An issue already linked for {0}. Unlink it first"), spec.getName(opposite)));							
		} else {
			new ModalPanel(target) {

				private FormComponentPanel<Issue> newCreateNewPanel(String componentId) {
					var editor = new NewIssueEditor(componentId) {
						@Override
						protected Criteria<Issue> getTemplate() {
							String query;
							var spec = getLinkSpecService().load(specId);
							if (opposite)
								query = spec.getOpposite().getIssueQuery();
							else
								query = spec.getIssueQuery();
							return IssueQuery.parse(getProject(), query, new IssueQueryParseOption(), false).getCriteria();
						}
						
						@Override
						protected Project getProject() {
							return getIssue().getProject();
						}
					};
					editor.setOutputMarkupId(true);														
					return editor;
				}

				private IssueChoiceProvider getIssueChoiceProvider() {
					return new IssueChoiceProvider() {
						@Override
						protected Project getProject() {
							return getIssue().getProject();
						}
						
						@Override
						protected IssueQuery getBaseQuery() {
							LinkSpec spec = getLinkSpecService().load(specId);
							if (opposite) 
								return spec.getOpposite().getParsedIssueQuery(getProject());
							else 
								return spec.getParsedIssueQuery(getProject());
						}
						
					};						
				}

				private FormComponent<?> newLinkExistingPanel(String componentId) {
					LinkSpec spec = getLinkSpecService().load(specId);
					if (!opposite && spec.isMultiple() || opposite && spec.getOpposite().isMultiple()) {
						return new SelectIssuesPanel(componentId) {

							@Override
							protected IssueChoiceProvider getChoiceProvider() {
								return getIssueChoiceProvider();						
							}
						};
					} else {
						return new SelectIssuePanel(componentId) {
							@Override
							protected IssueChoiceProvider getChoiceProvider() {
								return getIssueChoiceProvider();
							}
						};
					}
				}

				private FormComponent<?> issuePopulator;

				@Override
				protected Component newContent(String id) {
					var frag = new Fragment(id, "addLinkFrag", IssuePrimaryPanel.this);
					
					var form = new Form<Void>("form");
					frag.add(form);

					// Read cookie to determine which tab to show initially
					WebRequest request = (WebRequest) RequestCycle.get().getRequest();
					Cookie cookie = request.getCookie(COOKIE_LINK_ISSUE_TAB);
					String selectedTab = cookie != null ? cookie.getValue() : "create";
					
					List<Tab> tabs = new ArrayList<>();
					
					AjaxActionTab createNewTab = new AjaxActionTab(Model.of(_T("Create New"))) {
						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							issuePopulator = newCreateNewPanel("tabContent");
							form.replace(issuePopulator);
							target.add(issuePopulator);
							
							// Save cookie
							WebResponse response = (WebResponse) RequestCycle.get().getResponse();
							Cookie cookie = new Cookie(COOKIE_LINK_ISSUE_TAB, "create");
							cookie.setPath("/");
							cookie.setMaxAge(Integer.MAX_VALUE);
							response.addCookie(cookie);
						}
					};
					tabs.add(createNewTab);
					
					AjaxActionTab selectExistingTab = new AjaxActionTab(Model.of(_T("Select Existing"))) {
						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							issuePopulator = newLinkExistingPanel("tabContent");
							form.replace(issuePopulator);
							target.add(issuePopulator);
							
							// Save cookie
							WebResponse response = (WebResponse) RequestCycle.get().getResponse();
							Cookie cookie = new Cookie(COOKIE_LINK_ISSUE_TAB, "select");
							cookie.setPath("/");
							cookie.setMaxAge(Integer.MAX_VALUE);
							response.addCookie(cookie);
						}
					};
					tabs.add(selectExistingTab);
					
					// Set the active tab based on cookie
					if ("select".equals(selectedTab)) {
						selectExistingTab.setSelected(true);
					} else {
						createNewTab.setSelected(true);
					}
					
					form.add(new Tabbable("tabs", tabs));
					
					// Set initial tab content based on cookie
					if ("select".equals(selectedTab)) {
						issuePopulator = newLinkExistingPanel("tabContent");
					} else {
						issuePopulator = newCreateNewPanel("tabContent");
					}
					form.add(issuePopulator);

					form.add(new Label("title", MessageFormat.format(_T("Add {0}"), linkName)));

					form.add(new AjaxButton("save") {
						@SuppressWarnings("unchecked")
						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							var convertedInput = issuePopulator.getConvertedInput();
							if (convertedInput instanceof Issue) {
								var linkIssue = (Issue) convertedInput;
								if (linkIssue.isNew()) {
									getIssueService().open(linkIssue);
									notifyIssueChange(target, linkIssue);
									addLink(target, linkIssue);
									close();
								} else if (checkLink(target, linkIssue)) {
									addLink(target, linkIssue);
									close();
								}
							} else {
								var linkIssues = (List<Issue>) convertedInput;
								if (linkIssues.stream().noneMatch(it->!checkLink(target, it))) {
									for (var linkIssue: linkIssues) 
										addLink(target, linkIssue);
									close();
								}
							}
						}

						private boolean checkLink(AjaxRequestTarget target, Issue linkIssue) {
							LinkSpec spec = getLinkSpecService().load(specId);
							if (getIssue().getId().equals(linkIssue.getId())) {
								form.error(_T("Can not link to self: " + linkIssue.getReference().toString(getProject())));
								target.add(form);
								return false;
							} else if (getIssue().findLinkedIssues(spec, opposite).contains(linkIssue)) { 
								form.error(_T("Issue already linked: " + linkIssue.getReference().toString(getProject())));
								target.add(form);
								return false;
							} else {
								return true;
							}	
						}

						private void addLink(AjaxRequestTarget target, Issue linkIssue) {
							LinkSpec spec = getLinkSpecService().load(specId);
							getIssueChangeService().addLink(SecurityUtils.getUser(), spec, getIssue(), linkIssue, opposite);
							notifyIssueChange(target, getIssue());
						}

						@Override
						protected void onError(AjaxRequestTarget target, Form<?> form) {
							target.add(form);
						}
					});

					form.add(new AjaxLink<Void>("close") {
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
						}
						@Override
						public void onClick(AjaxRequestTarget target) {
							close();
						}
					});
					form.add(new AjaxLink<Void>("cancel") {
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(form));
						}
						@Override
						public void onClick(AjaxRequestTarget target) {
							close();
						}
					});
					return frag;
				}
				
				@Override
				protected String getCssClass() {
					return "modal-lg";
				}
			};
		}
	}
	
	private Component newLinkedIssueContainer(String componentId, Issue linkedIssue, 
			@Nullable LinkDeleteListener deleteListener) {
		if (canAccessIssue(linkedIssue)) {
			Long linkedIssueId = linkedIssue.getId();
			Fragment fragment = new Fragment(componentId, "linkedIssueFrag", this);

			var link = new BookmarkablePageLink<Void>("title", IssueActivitiesPage.class, 
					IssueActivitiesPage.paramsOf(linkedIssue));
			link.add(new Label("label", linkedIssue.getTitle() + " (" + linkedIssue.getReference().toString(getProject()) + ")"));
			fragment.add(link);
			
			fragment.add(new AjaxLink<Void>("unlink") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmClickListener(
							_T("Do you really want to remove this link?")));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					Issue linkedIssue = getIssueService().load(linkedIssueId);
					deleteListener.onDelete(target, linkedIssue);
					notifyIssueChange(target, getIssue());
				}
				
			}.setVisible(deleteListener != null));

			AjaxLink<Void> stateLink = new TransitionMenuLink("state") {

				@Override
				protected Issue getIssue() {
					return getIssueService().load(linkedIssueId);
				}

			};

			stateLink.add(new IssueStateBadge("badge", new LoadableDetachableModel<>() {
				@Override
				protected Issue load() {
					return getIssueService().load(linkedIssueId);
				}
			}, true).add(AttributeAppender.append("class", "badge-sm")));
			
			fragment.add(stateLink);
			
			return fragment;
		} else {
			return new WebMarkupContainer(componentId).setVisible(false);
		}
	}

	private Project getProject() {
		return getIssue().getProject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssuePrimaryCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		linkSpecsModel.detach();
		addibleLinkDescriptorsModel.detach();
		linkGroupsModel.detach();
		super.onDetach();
	}

	private LinkSpecService getLinkSpecService() {
		return OneDev.getInstance(LinkSpecService.class);
	}

	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}
	
	private IssueChangeService getIssueChangeService() {
		return OneDev.getInstance(IssueChangeService.class);
	}
	
	private void notifyIssueChange(IPartialPageRequestHandler handler, Issue issue) {
		((BasePage)getPage()).notifyObservablesChange(handler, issue.getChangeObservables(true));
	}

    protected abstract Issue getIssue();

	private static abstract class LinkDeleteListener implements Serializable {
		
		abstract void onDelete(AjaxRequestTarget target, Issue linkedIssue);
		
	}
	
}
