package io.onedev.server.web.component.issue.workflowreconcile;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.ajaxlistener.ChangeTextListener;
import io.onedev.server.web.ajaxlistener.DisableGlobalAjaxIndicatorListener;
import io.onedev.server.web.ajaxlistener.SelfDisableListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValueResolution.FixType;
import io.onedev.server.web.editable.BeanContext;

public abstract class WorkflowReconcilePanel extends Panel {

	private String CONTENT_ID = "content";
	
	public WorkflowReconcilePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(checkStates(CONTENT_ID));
	}
	
	private Component newLoadingComponent(String id, String loadingMessage) {
		return new Label(id, loadingMessage) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.setName("h5");
			}
			
		}.add(AttributeAppender.append("class", "p-4 m-0"));
	}
	
	private IssueService getIssueService() {
		return OneDev.getInstance(IssueService.class);
	}
	
	private Component checkStates(String markupId) {
		return new AjaxLazyLoadPanel(markupId) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Collection<String> undefinedStates = getIssueService().getUndefinedStates();
				if (!undefinedStates.isEmpty()) {
					Fragment fragment = new Fragment(markupId, "fixStatesFrag", WorkflowReconcilePanel.this);
					Form<?> form = new Form<Void>("form") {

						@Override
						protected void onError() {
							super.onError();
							RequestCycle.get().find(AjaxRequestTarget.class).add(this);
						}
						
					};
					
					Map<String, UndefinedStateResolution> resolutions = new HashMap<>();
					for (String undefinedState: undefinedStates)
						resolutions.put(undefinedState, new UndefinedStateResolution());
					
					RepeatingView rows = new RepeatingView("rows");
					for (String undefinedState: undefinedStates) {
						WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
						row.add(new Label("undefined", undefinedState));
						row.add(BeanContext.edit("resolution", resolutions.get(undefinedState)));
						rows.add(row);
					}
					form.add(rows);
					
					form.add(new AjaxLink<Void>("close") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					form.add(new AjaxLink<Void>("cancel") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					
					form.add(new AjaxButton("fix") {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
							attributes.getAjaxCallListeners().add(new SelfDisableListener());
							attributes.getAjaxCallListeners().add(new ChangeTextListener(_T("Fixing...")));
						}

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							getIssueService().fixUndefinedStates(resolutions);
							Component content = checkFields(CONTENT_ID);
							WorkflowReconcilePanel.this.replace(content);
							target.add(content);
						}
						
					});
					
					form.setOutputMarkupId(true);
					fragment.add(form);
					return fragment;
				} else {
					return checkFields(markupId);
				}
			}

			@Override
			public Component getLoadingComponent(String markupId) {
				return newLoadingComponent(markupId, _T("Checking state..."));
			}
			
		};
	}

	private Component checkFields(String markupId) {
		return new AjaxLazyLoadPanel(markupId) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Collection<String> undefinedFields = getIssueService().getUndefinedFields();
				if (!undefinedFields.isEmpty()) {
					Fragment fragment = new Fragment(markupId, "fixFieldsFrag", WorkflowReconcilePanel.this);
					Form<?> form = new Form<Void>("form") {

						@Override
						protected void onError() {
							super.onError();
							RequestCycle.get().find(AjaxRequestTarget.class).add(this);
						}
						
					};

					RepeatingView rows = new RepeatingView("rows");
					Map<String, UndefinedFieldResolution> resolutions = new HashMap<>();
					for (String undefinedField: undefinedFields) { 
						UndefinedFieldResolution resolution = new UndefinedFieldResolution();
						resolutions.put(undefinedField, resolution);
						WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
						row.add(new Label("name", undefinedField));
						row.add(BeanContext.edit("resolution", resolution));
						rows.add(row);
					}
					form.add(rows);
					
					form.add(new AjaxLink<Void>("close") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					form.add(new AjaxLink<Void>("cancel") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					form.add(new AjaxButton("fix") {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
							attributes.getAjaxCallListeners().add(new SelfDisableListener());
							attributes.getAjaxCallListeners().add(new ChangeTextListener(_T("Fixing...")));
						}

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);
							getIssueService().fixUndefinedFields(resolutions);
							
							Component content = checkFieldValues(CONTENT_ID);
							WorkflowReconcilePanel.this.replace(content);
							target.add(content);
						}
						
					});
					form.setOutputMarkupId(true);
					fragment.add(form);
					return fragment;
				} else {
					return checkFieldValues(markupId);
				}
			}

			@Override
			public Component getLoadingComponent(String markupId) {
				return newLoadingComponent(markupId, _T("Checking fields..."));
			}
			
		};
	}
	
	private Component checkFieldValues(String markupId) {
		return new AjaxLazyLoadPanel(markupId) {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Collection<UndefinedFieldValue> undefinedFieldValues = getIssueService().getUndefinedFieldValues();
				if (!undefinedFieldValues.isEmpty()) {
					Fragment fragment = new Fragment(markupId, "fixFieldValuesFrag", WorkflowReconcilePanel.this);
					Form<?> form = new Form<Void>("form") {

						@Override
						protected void onError() {
							super.onError();
							RequestCycle.get().find(AjaxRequestTarget.class).add(this);
						}
						
					};
					
					RepeatingView rows = new RepeatingView("rows");
					Map<UndefinedFieldValue, UndefinedFieldValueResolution> resolutions = new HashMap<>();
					for (UndefinedFieldValue undefinedFieldValue: undefinedFieldValues) {
						UndefinedFieldValueResolution resolution = new UndefinedFieldValueResolution();
						resolutions.put(undefinedFieldValue, resolution);
						UndefinedFieldValueContainer row = new UndefinedFieldValueContainer(rows.newChildId(), undefinedFieldValue.getFieldName());
						row.add(new Label("name", undefinedFieldValue.getFieldName()));
						row.add(new Label("value", undefinedFieldValue.getFieldValue()));
						row.add(BeanContext.edit("resolution", resolution));
						rows.add(row);
					}
					form.add(rows);
					form.add(new AjaxLink<Void>("close") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					form.add(new AjaxLink<Void>("cancel") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onCancel(target);
						}
						
					});
					form.add(new AjaxButton("fix") {

						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							attributes.getAjaxCallListeners().add(new DisableGlobalAjaxIndicatorListener());
							attributes.getAjaxCallListeners().add(new SelfDisableListener());
							attributes.getAjaxCallListeners().add(new ChangeTextListener("Fixing..."));
						}

						@Override
						protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
							super.onSubmit(target, form);

							Map<String, UndefinedFieldValuesResolution> edits = new HashMap<>();
							Collection<String> fieldNames = new HashSet<>();
							for (UndefinedFieldValue key: resolutions.keySet())
								fieldNames.add(key.getFieldName());
							for (String fieldName: fieldNames) {
								Map<String, String> renames = new HashMap<>();
								Collection<String> deletions = new HashSet<>();
								for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
									if (entry.getKey().getFieldName().equals(fieldName)) {
										if (entry.getValue().getFixType() == FixType.CHANGE_TO_ANOTHER_VALUE) 
											renames.put(entry.getKey().getFieldValue(), entry.getValue().getNewValue());
										else
											deletions.add(entry.getKey().getFieldValue());
									}
								}
								edits.put(fieldName, new UndefinedFieldValuesResolution(renames, deletions));
							}
							
							getIssueService().fixUndefinedFieldValues(edits);
							
							Component content = checkStateAndFieldOrdinals(CONTENT_ID);
							WorkflowReconcilePanel.this.replace(content);
							target.add(content);
						}
						
					});
					form.setOutputMarkupId(true);
					
					fragment.add(form);
					return fragment;
				} else {
					return checkStateAndFieldOrdinals(markupId);
				}
			}

			@Override
			public Component getLoadingComponent(String markupId) {
				return newLoadingComponent(markupId, _T("Checking field values..."));
			}
			
		};
	}
	
	private Component checkStateAndFieldOrdinals(String markupId) {
		return new Label(markupId, _T("Checking state and field ordinals...")) {

			private AbstractPostAjaxBehavior behavior;
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(behavior = new AbstractPostAjaxBehavior() {
					
					@Override
					protected void respond(AjaxRequestTarget target) {
						getIssueService().fixStateAndFieldOrdinals();
						
						SettingService settingService = OneDev.getInstance(SettingService.class);
						GlobalIssueSetting issueSetting = settingService.getIssueSetting();
						issueSetting.setReconciled(true);
						settingService.saveIssueSetting(issueSetting);
						Session.get().success(_T("Workflow reconciliation completed"));
						onCompleted(target);
					}
					
				});
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.setName("h5");
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript(behavior.getCallbackScript()));
			}
			
		}.add(AttributeAppender.append("class", "p-4 m-0"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WorkflowReconcileResourceReference()));
	}

	protected abstract void onCompleted(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
	static class UndefinedFieldValueContainer extends WebMarkupContainer {
		
		final String fieldName;
		
		UndefinedFieldValueContainer(String id, String fieldName) {
			super(id);
			
			this.fieldName = fieldName;
		}

		public String getFieldName() {
			return fieldName;
		}
		
	}

}
