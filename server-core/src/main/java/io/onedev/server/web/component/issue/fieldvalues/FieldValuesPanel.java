package io.onedev.server.web.component.issue.fieldvalues;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.support.issue.field.spec.TextField;
import io.onedev.server.web.component.MultilineLabel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.model.support.inputspec.InputSpec;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.issue.field.FieldUtils;
import io.onedev.server.model.support.issue.field.spec.ChoiceField;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ColorUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.Input;
import io.onedev.server.web.ajaxlistener.AttachAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.DisableGlobalAjaxIndicatorListener;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.issues.milestones.MilestoneIssuesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.ProjectAware;

@SuppressWarnings("serial")
public abstract class FieldValuesPanel extends Panel implements EditContext, ProjectAware {

	private final Mode userFieldDisplayMode;
	
	private final boolean delayPermissionCheck;
	
	public FieldValuesPanel(String id, Mode userFieldDisplayMode, boolean delayPermissionCheck) {
		super(id);
		this.userFieldDisplayMode = userFieldDisplayMode;
		this.delayPermissionCheck = delayPermissionCheck;
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	private InplacePropertyEditLink newInplaceEditLink(String componentId) {
		return new InplacePropertyEditLink(componentId) {
			
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				if (delayPermissionCheck && !canEditField())
					return new Label(id, "<div class='px-3 py-2'><i>No permission to edit field</i></div>").setEscapeModelStrings(false);
				else
					return super.newContent(id, dropdown);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
				AttachAjaxIndicatorListener ajaxIndicatorListener = getInplaceEditAjaxIndicator();
				if (ajaxIndicatorListener != null)
					attributes.getAjaxCallListeners().add(ajaxIndicatorListener);
			}

			@Override
			protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
				BeanDescriptor beanDescriptor = new BeanDescriptor(bean.getClass());
				FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getField().getName());
				Collection<String> dependentFields = fieldSpec.getTransitiveDependents();
				boolean hasVisibleEditableDependents = dependentFields.stream()
						.anyMatch(it->SecurityUtils.canEditIssueField(getIssue().getProject(), it) 
								&& FieldUtils.isFieldVisible(beanDescriptor, bean, it));
				
				Map<String, Object> fieldValues = new HashMap<>();
				Object propertyValue = new PropertyDescriptor(bean.getClass(), propertyName).getPropertyValue(bean);
				fieldValues.put(getField().getName(), propertyValue);
				
				if (hasVisibleEditableDependents) {
					Collection<String> propertyNames = FieldUtils.getEditablePropertyNames(
							getIssue().getProject(), bean.getClass(), dependentFields);
					class DependentFieldsEditor extends BeanEditModalPanel<Serializable> 
							implements ProjectAware, InputContext {

						public DependentFieldsEditor(IPartialPageRequestHandler handler, Serializable bean,
								Collection<String> propertyNames, boolean exclude, String title) {
							super(handler, bean, propertyNames, exclude, title);
						}

						@Override
						public Project getProject() {
							return getIssue().getProject();
						}

						@Override
						public List<String> getInputNames() {
							throw new UnsupportedOperationException();
						}

						@Override
						public InputSpec getInputSpec(String inputName) {
							return getIssueSetting().getFieldSpec(inputName);
						}

						@Override
						protected boolean isDirtyAware() {
							return false;
						}

						@Override
						protected void onSave(AjaxRequestTarget target, Serializable bean) {
							fieldValues.putAll(FieldUtils.getFieldValues(
									FieldUtils.newBeanComponentContext(beanDescriptor, bean), 
									bean, FieldUtils.getEditableFields(getProject(), dependentFields)));
							OneDev.getInstance(IssueChangeManager.class).changeFields(getIssue(), fieldValues);
							close();
						}
						
					}
					
					new DependentFieldsEditor(handler, bean, propertyNames, false, "Dependent Fields");
				} else {
					OneDev.getInstance(IssueChangeManager.class).changeFields(getIssue(), fieldValues);
				}
				
				FieldValuesPanel.this.onUpdated(handler);
			}
			
			@Override
			protected String getPropertyName() {
				BeanDescriptor descriptor = new BeanDescriptor(FieldUtils.getFieldBeanClass());
				return FieldUtils.getPropertyName(descriptor, getField().getName());
			}
			
			@Override
			protected Serializable getBean() {
				Class<?> fieldBeanClass = FieldUtils.getFieldBeanClass();
				return getIssue().getFieldBean(fieldBeanClass, true); 
			}

			@Override
			protected Project getProject() {
				return getIssue().getProject();
			}

		};
	}
	
	private boolean canEditField() {
		if (getField() != null && getIssueSetting().getFieldSpec(getField().getName()) != null) {
			User user = SecurityUtils.getUser();
			String initialState = OneDev.getInstance(SettingManager.class).getIssueSetting().getInitialStateSpec().getName();
			if (SecurityUtils.canManageIssues(getIssue().getProject())) {
				return true;
			} else {
				return SecurityUtils.canEditIssueField(getIssue().getProject(), getField().getName()) 
						&& user != null 
						&& user.equals(getIssue().getSubmitter()) 
						&& getIssue().getState().equals(initialState);
			}
		} else {
			return false;
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer link;
		if (delayPermissionCheck) {
			link = newInplaceEditLink("edit");
			link.add(AttributeAppender.append("style", "cursor:pointer;"));
			link.add(AttributeAppender.append("class", "editable"));
		} else {
			if (canEditField()) {
				link = newInplaceEditLink("edit");
				link.add(AttributeAppender.append("style", "cursor:pointer;"));
				link.add(AttributeAppender.append("class", "editable"));
			} else {
				link = new WebMarkupContainer("edit");
			}
		}
		add(link);
		
		if (getField() != null && !getField().getValues().isEmpty()) {
			Fragment fragment = new Fragment("content", "nonEmptyValuesFrag", this);
			
			RepeatingView valuesView = new RepeatingView("values");
			for (String value: getField().getValues()) {
				WebMarkupContainer valueContainer = new WebMarkupContainer(valuesView.newChildId());
				valuesView.add(valueContainer);
				if (getField().getType().equals(FieldSpec.USER)) {
					User user = OneDev.getInstance(UserManager.class).findByName(value);
					if (user != null)
						valueContainer.add(new UserIdentPanel("value", user, userFieldDisplayMode));
					else 
						valueContainer.add(new Label("value", value));
				} else if (getField().getType().equals(FieldSpec.ISSUE)) {
					Issue issue = OneDev.getInstance(IssueManager.class).get(Long.valueOf(value));
					if (issue != null) {
						Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
						Link<Void> issueLink = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, 
								IssueActivitiesPage.paramsOf(issue));
						if (issue.getNumberScope().equals(getIssue().getProject().getForkRoot()))
							issueLink.add(new Label("label", "#" + issue.getNumber()));
						else
							issueLink.add(new Label("label", issue.getFQN().toString()));
						linkFrag.add(issueLink);
						valueContainer.add(linkFrag);
					} else {
						valueContainer.add(new Label("value", "<i>Not Found</i>").setEscapeModelStrings(false));
					}
				} else if (getField().getType().equals(FieldSpec.BUILD)) {
					Build build = OneDev.getInstance(BuildManager.class).get(Long.valueOf(value));
					if (build != null) {
						Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
						Link<Void> buildLink = new BookmarkablePageLink<Void>("link", 
								BuildDashboardPage.class, BuildDashboardPage.paramsOf(build));
						String buildInfo = "#" + build.getNumber();
						if (build.getNumberScope().equals(getIssue().getProject().getForkRoot()))
							buildInfo = "#" + build.getNumber();
						else
							buildInfo = build.getFQN().toString();
						
						if (build.getVersion() != null)
							buildInfo += " (" + build.getVersion() + ")";
						buildLink.add(new Label("label", buildInfo));
						linkFrag.add(buildLink);
						valueContainer.add(linkFrag);
					} else {
						valueContainer.add(new Label("value", "<i>Not Found</i>").setEscapeModelStrings(false));
					}
				} else if (getField().getType().equals(FieldSpec.PULL_REQUEST)) {
					PullRequest request = OneDev.getInstance(PullRequestManager.class).get(Long.valueOf(value));
					if (request != null) {
						Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
						Link<Void> requestLink = new BookmarkablePageLink<Void>("link", PullRequestActivitiesPage.class, 
								PullRequestActivitiesPage.paramsOf(request));
						if (request.getNumberScope().equals(getIssue().getProject().getForkRoot()))
							requestLink.add(new Label("label", "#" + request.getNumber()));
						else
							requestLink.add(new Label("label", request.getFQN().toString()));
						linkFrag.add(requestLink);
						valueContainer.add(linkFrag);
					} else {
						valueContainer.add(new Label("value", "<i>Not Found</i>").setEscapeModelStrings(false));
					}
				} else if (getField().getType().equals(FieldSpec.MILESTONE)) {
					Milestone milestone = OneDev.getInstance(MilestoneManager.class).findInHierarchy(getIssue().getProject(), value);
					if (milestone != null) {
						Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
						Link<Void> milestoneLink = new BookmarkablePageLink<Void>("link", MilestoneIssuesPage.class, 
								MilestoneIssuesPage.paramsOf(getIssue().getProject(), milestone));
						milestoneLink.add(new Label("label", milestone.getName()));
						linkFrag.add(milestoneLink);
						valueContainer.add(linkFrag);
					} else {
						valueContainer.add(new Label("value", "<i>Not Found</i>").setEscapeModelStrings(false));
					}
				} else if (getField().getType().equals(FieldSpec.COMMIT)) {
					if (ObjectId.isId(value)) {
						Fragment commmitFragment = new Fragment("value", "commitFrag", FieldValuesPanel.this);
						Project project = getIssue().getProject();
						CommitDetailPage.State commitState = new CommitDetailPage.State();
						commitState.revision = value;
						PageParameters params = CommitDetailPage.paramsOf(project, commitState);
						Link<Void> hashLink = new BookmarkablePageLink<Void>("hashLink", CommitDetailPage.class, params);
						commmitFragment.add(hashLink);
						hashLink.add(new Label("hash", GitUtils.abbreviateSHA(value)));
						commmitFragment.add(new CopyToClipboardLink("copyHash", Model.of(value)));
						valueContainer.add(commmitFragment);
					} else {
						valueContainer.add(new Label("value", value));
					}
				} else {
					FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getField().getName());
					Component label;
					if (getField().getType().equals(FieldSpec.SECRET))
						label = new Label("value", SecretInput.MASK);
					else if (fieldSpec instanceof TextField && ((TextField) fieldSpec).isMultiline())
						label = new MultilineLabel("value", value);
					else 
						label = new Label("value", value);
					
					if (fieldSpec != null && fieldSpec instanceof ChoiceField) {
						ChoiceProvider choiceProvider = ((ChoiceField)fieldSpec).getChoiceProvider();
						ComponentContext.push(new ComponentContext(this));
						try {
							String backgroundColor = choiceProvider.getChoices(false).get(value);
							if (backgroundColor == null)
								backgroundColor = "#E4E6EF";
							String fontColor = ColorUtils.isLight(backgroundColor)?"#3F4254":"white"; 
							String style = String.format(
									"background-color: %s; color: %s;", 
									backgroundColor, fontColor);
							label.add(AttributeAppender.append("style", style));
							label.add(AttributeAppender.append("class", "badge"));
						} finally {
							ComponentContext.pop();
						}
					} 
					valueContainer.add(label);
				}
				valueContainer.add(AttributeAppender.append("title", getField().getName()));
			}
			fragment.add(valuesView);
			link.add(fragment);
		} else if (getField() != null) {
			FieldSpec fieldSpec = null;
			if (getField() != null)
				fieldSpec = getIssueSetting().getFieldSpec(getField().getName());
			String displayValue;
			if (fieldSpec != null && fieldSpec.getNameOfEmptyValue() != null) 
				displayValue = fieldSpec.getNameOfEmptyValue();
			else
				displayValue = "Undefined";
			displayValue = HtmlEscape.escapeHtml5(displayValue);
			Label label = new Label("content", "<i class='mb-2 mr-2'>" + displayValue + "</i>");
			label.setEscapeModelStrings(false);
			label.add(AttributeAppender.append("title", getField().getName()));
			link.add(label);
			add(AttributeAppender.append("class", "undefined"));
		} else {
			link.add(new WebMarkupContainer("content"));
			setVisible(false);
		}
	}

	@Override
	public Object getInputValue(String name) {
		Input field = getIssue().getFieldInputs().get(name);
		FieldSpec fieldSpec = getIssueSetting().getFieldSpec(name);
		if (field != null && fieldSpec != null && field.getType().equals(EditableUtils.getDisplayName(fieldSpec.getClass()))) {
			return fieldSpec.convertToObject(field.getValues());
		} else {
			return null;
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FieldValuesCssResourceReference()));
	}

	@Nullable
	protected AttachAjaxIndicatorListener getInplaceEditAjaxIndicator() {
		return null;
	}
	
	@Override
	public Project getProject() {
		return getIssue().getProject();
	}

	protected abstract Issue getIssue();
	
	@Nullable
	protected abstract Input getField();

	protected void onUpdated(IPartialPageRequestHandler handler) {
	}
	
}
