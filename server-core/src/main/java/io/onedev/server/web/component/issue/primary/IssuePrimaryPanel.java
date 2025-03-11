package io.onedev.server.web.component.issue.primary;

import static io.onedev.server.security.SecurityUtils.canAccessIssue;
import static io.onedev.server.security.SecurityUtils.canEditIssueLink;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueReactionManager;
import io.onedev.server.entitymanager.LinkSpecManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
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
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.issue.IssueStateBadge;
import io.onedev.server.web.component.issue.choice.IssueAddChoice;
import io.onedev.server.web.component.issue.choice.IssueChoiceProvider;
import io.onedev.server.web.component.issue.create.CreateIssuePanel;
import io.onedev.server.web.component.issue.operation.TransitionMenuLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.util.DeleteCallback;

public abstract class IssuePrimaryPanel extends Panel {

	private final IModel<List<LinkSpec>> linkSpecsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<LinkSpec> load() {
			return getLinkSpecManager().queryAndSort();
		}

	};

	private final IModel<List<LinkDescriptor>> addibleLinkDescriptorsModel = new LoadableDetachableModel<>() {
		@Override
		protected List<LinkDescriptor> load() {
			var linkDescriptors = new ArrayList<LinkDescriptor>();
			for (LinkSpec spec: linkSpecsModel.getObject()) {
				if (canEditIssueLink(getProject(), spec)) {
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
					var targetIssues = getIssue().findLinkedIssues(spec, false).stream().filter(it->canAccessIssue(it)).collect(toList());
					if (!targetIssues.isEmpty())
						linkGroups.add(new LinkGroup(new LinkDescriptor(spec, false), targetIssues));
					var sourceIssues = getIssue().findLinkedIssues(spec, true).stream().filter(it->canAccessIssue(it)).collect(toList());
					if (!sourceIssues.isEmpty())
						linkGroups.add(new LinkGroup(new LinkDescriptor(spec, true), sourceIssues));
				} else {
					var issues = getIssue().findLinkedIssues(spec, false).stream().filter(it->canAccessIssue(it)).collect(toList());
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
		add(new UserIdentPanel("submitterAvatar", issue.getSubmitter(), Mode.AVATAR));
		add(new Label("submitterName", issue.getSubmitter().getDisplayName()));
		add(new Label("submitDate", DateUtils.formatAge(issue.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(issue.getSubmitDate()))));

		if (issue.getOnBehalfOf() != null)
			add(new Label("submitOnBehalfOf", " on behalf of <b>" + HtmlEscape.escapeHtml5(EmailAddressUtils.describe(issue.getOnBehalfOf(), SecurityUtils.canManageIssues(getProject()))) + "</b>").setEscapeModelStrings(false));
		else 
			add(new WebMarkupContainer("submitOnBehalfOf").setVisible(false));

		add(new CommentPanel("description") {
			
			@Override
			protected String getComment() {
				return getIssue().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(IssueChangeManager.class).changeDescription(getIssue(), comment);
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
				return "No description";
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
			protected Collection<? extends EntityReaction> getReactions() {
				return getIssue().getReactions();
			}

			@Override
			protected void onToggleEmoji(AjaxRequestTarget target, String emoji) {
				OneDev.getInstance(IssueReactionManager.class).toggleEmoji(
					SecurityUtils.getUser(), 
					getIssue(), 
					emoji);
			}

			@Override
			protected Component newMoreActions(String componentId) {
				var fragment = new Fragment(componentId, "linkIssuesActionFrag", IssuePrimaryPanel.this) {
					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!addibleLinkDescriptorsModel.getObject().isEmpty());
					}
				};
				fragment.add(new MenuLink("linkIssues") {


					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						Long lastSpecId = null;
						for (LinkDescriptor side: addibleLinkDescriptorsModel.getObject()) {
							var spec = side.getSpec();
							var specId = spec.getId();
							var opposite = side.isOpposite();
							if (lastSpecId != null && lastSpecId != specId) 
								menuItems.add(null);
							lastSpecId = specId;
							var linkName = side.getName();
							menuItems.add(new MenuItem() {
								@Override
								public String getLabel() {
									return linkName + " (create new)";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return newLinkNewIssueLink(id, specId, opposite, dropdown);
								}
							});

							menuItems.add(new MenuItem() {
								@Override
								public String getLabel() {
									return linkName + " (link existing)";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return newLinkExistingIssueLink(id, specId, opposite, dropdown);
								}

							});
						}
						return menuItems;
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
				
				boolean canEditIssueLink = canEditIssueLink(getProject(), spec);
				
				String name = spec.getName(opposite);
				item.add(new Label("name", name));
				
				RepeatingView linkedIssuesView = new RepeatingView("linkedIssues");
				for (Issue linkedIssue: group.getIssues()) {
					LinkDeleteListener deleteListener;
					if (canEditIssueLink) { 
						deleteListener = new LinkDeleteListener() {
	
							@Override
							void onDelete(AjaxRequestTarget target, Issue linkedIssue) {
								getIssueChangeManager().removeLink(getLinkSpecManager().load(specId), getIssue(), 
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

				item.add(newLinkNewIssueLink("createNew", specId, opposite, null)
						.setVisible(applicable && canEditIssueLink));
				item.add(newLinkExistingIssueLink("linkExisting", specId, opposite, null)
						.setVisible(applicable && canEditIssueLink));
			}
			
		});
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
							"Do you really want to remove this link?"));
				}

				@Override
				public void onClick(AjaxRequestTarget target) {
					Issue linkedIssue = getIssueManager().load(linkedIssueId);
					deleteListener.onDelete(target, linkedIssue);
					notifyIssueChange(target, getIssue());
				}
				
			}.setVisible(deleteListener != null));

			AjaxLink<Void> stateLink = new TransitionMenuLink("state") {

				@Override
				protected Issue getIssue() {
					return getIssueManager().load(linkedIssueId);
				}

			};

			stateLink.add(new IssueStateBadge("badge", new LoadableDetachableModel<>() {
				@Override
				protected Issue load() {
					return getIssueManager().load(linkedIssueId);
				}
			}, true).add(AttributeAppender.append("class", "badge-sm")));
			
			fragment.add(stateLink);
			
			return fragment;
		} else {
			return new WebMarkupContainer(componentId).setVisible(false);
		}
	}

	private boolean checkLinkSingular(Long specId, boolean opposite) {
		var spec = getLinkSpecManager().load(specId);
		if (!opposite && !spec.isMultiple() && getIssue().findLinkedIssue(spec, false) != null 
				|| opposite && !spec.getOpposite().isMultiple() && getIssue().findLinkedIssue(spec, true) != null) {
			Session.get().error("An issue already linked for " + spec.getName(opposite) + ". Unlink it first");							
			return false;
		} else {
			return true;
		}
	}

	private WebMarkupContainer newLinkNewIssueLink(String componentId, Long specId, boolean opposite, @Nullable FloatingPanel dropdown) {
		return new AjaxLink<Void>(componentId) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (dropdown != null)
					dropdown.close();
				if (checkLinkSingular(specId, opposite)) {
					new ModalPanel(target) {

						@Override
						protected Component newContent(String id) {
							return new CreateIssuePanel(id) {
			
								@Override
								protected Criteria<Issue> getTemplate() {
									String query;
									var spec = getLinkSpecManager().load(specId);
									if (opposite)
										query = spec.getOpposite().getIssueQuery();
									else
										query = spec.getIssueQuery();
									return IssueQuery.parse(getProject(), query, new IssueQueryParseOption(), false).getCriteria();
								}
			
								@Override
								protected void onSave(AjaxRequestTarget target, Issue issue) {
									getIssueManager().open(issue);
									notifyIssueChange(target, issue);
									var spec = getLinkSpecManager().load(specId);
									getIssueChangeManager().addLink(spec, getIssue(), issue, opposite);
									notifyIssueChange(target, getIssue());
									close();
								}
			
								@Override
								protected void onCancel(AjaxRequestTarget target) {
									close();
								}
			
								@Override
								protected Project getProject() {
									return getIssue().getProject();
								}
			
							};
						}
					};	
				}
			}
		};

	}

	private WebMarkupContainer newLinkExistingIssueLink(String componentId, Long specId, boolean opposite, @Nullable FloatingPanel dropdown) {
		return new DropdownLink(componentId) {

			@Override
			protected void onInitialize(FloatingPanel dropdown) {
				super.onInitialize(dropdown);
				dropdown.add(AttributeModifier.append("class", "inplace-edit issue-link-choice"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (checkLinkSingular(specId, opposite))
					super.onClick(target);
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown2) {
				var fragment = new Fragment(id, "selectIssueFrag", IssuePrimaryPanel.this);
				var form = new Form<Void>("form");
				fragment.add(form);
				form.add(new IssueAddChoice("choice", new IssueChoiceProvider() {
					
					@Override
					protected Project getProject() {
						return getIssue().getProject();
					}
					
					@Override
					protected IssueQuery getBaseQuery() {
						LinkSpec spec = getLinkSpecManager().load(specId);
						if (opposite) 
							return spec.getOpposite().getParsedIssueQuery(getProject());
						else 
							return spec.getParsedIssueQuery(getProject());
					}
					
				}) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Issue selection) {
						if (dropdown != null) 
							dropdown.close();
						dropdown2.close();

						LinkSpec spec = getLinkSpecManager().load(specId);
						if (getIssue().equals(selection)) {
							getSession().warn("Can not link to self");
						} else if (getIssue().findLinkedIssues(spec, opposite).contains(selection)) { 
							getSession().warn("Issue already linked");
						} else {
							getIssueChangeManager().addLink(spec, getIssue(), selection, opposite);
							notifyIssueChange(target, getIssue());
						}
					}

					@Override
					protected String getPlaceholder() {
						return "Select issue...";
					}
					
				});
				return fragment;
			}
		};						
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

	private LinkSpecManager getLinkSpecManager() {
		return OneDev.getInstance(LinkSpecManager.class);
	}

	private IssueManager getIssueManager() {
		return OneDev.getInstance(IssueManager.class);
	}
	
	private IssueChangeManager getIssueChangeManager() {
		return OneDev.getInstance(IssueChangeManager.class);
	}
	
	private void notifyIssueChange(IPartialPageRequestHandler handler, Issue issue) {
		((BasePage)getPage()).notifyObservablesChange(handler, issue.getChangeObservables(true));
	}

    protected abstract Issue getIssue();

	private static abstract class LinkDeleteListener implements Serializable {
		
		abstract void onDelete(AjaxRequestTarget target, Issue linkedIssue);
		
	}
	
}
