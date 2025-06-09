package io.onedev.server.web.page.admin.authenticator;

import com.google.common.base.Joiner;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.taskbutton.TaskResult;
import io.onedev.server.web.component.taskbutton.TaskResult.HtmlMessgae;
import io.onedev.server.web.editable.*;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;

public class AuthenticatorPage extends AdministrationPage {

	private AuthenticationToken token = new AuthenticationToken();
	
	public AuthenticatorPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		AuthenticatorBean bean = new AuthenticatorBean();
		bean.setAuthenticator(OneDev.getInstance(SettingManager.class).getAuthenticator());
		
		PropertyEditor<Serializable> editor = 
				PropertyContext.edit("editor", bean, "authenticator");
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				OneDev.getInstance(SettingManager.class).saveAuthenticator(bean.getAuthenticator());
				getSession().success(_T("External authenticator settings saved"));
			}
			
		};
		var fullNameLabel = _T("Full Name");
		var emailLabel = _T("Email");
		var groupsLabel = _T("Groups");
		var sshKeysLabel = _T("Number of SSH Keys");
		var testSuccessfulLabel = _T("Test successful: authentication passed");
		var testSuccessfulWithInfoLabel = _T("Test successful: authentication passed with below information retrieved:");
		TaskButton testButton = new TaskButton("test") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor beanEditor = editor.visitChildren(BeanEditor.class, 
						new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
						visit.stop(component);
					}
					
				});
				setVisible(beanEditor != null && beanEditor.isVisibleInHierarchy());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (editor.isValid()) {
					new ModalPanel(target) {

						@Override
						protected Component newContent(String id) {
							Fragment fragment = new Fragment(id, "testFrag", AuthenticatorPage.this);
							BeanEditor tokenEditor = BeanContext.edit("editor", token);
							tokenEditor.setOutputMarkupId(true);
							Form<?> form = new Form<Void>("form") {

								@Override
								protected void onError() {
									super.onError();
									RequestCycle.get().find(AjaxRequestTarget.class).add(tokenEditor);
								}

								@Override
								protected void onSubmit() {
									super.onSubmit();
									AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);								
									target.add(tokenEditor);
									target.focusComponent(null);
									close();
									submitTask(target);
								}
								
							};
							form.add(new AjaxLink<Void>("close") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							form.add(tokenEditor);
							form.add(new AjaxButton("ok") {});
							form.add(new AjaxLink<Void>("cancel") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									close();
								}
								
							});
							fragment.add(form);
							return fragment;
						}
						
					};
				} else {
					target.add(form);
				}
			}

			@Override
			protected TaskResult runTask(TaskLogger logger) {
				Authenticated authenticated = bean.getAuthenticator().authenticate(
						new UsernamePasswordToken(token.getUserName(), token.getPassword()));
				StringBuilder retrievedInfoBuilder = new StringBuilder();
				if (authenticated.getFullName() != null) {
					retrievedInfoBuilder.append(fullNameLabel + ": ")
							.append(authenticated.getFullName())
							.append("<br>");
				}
				if (authenticated.getEmail() != null) {
					retrievedInfoBuilder.append(emailLabel + ": ")
							.append(authenticated.getEmail())
							.append("<br>");
				}
				if (authenticated.getGroupNames() != null) {
					retrievedInfoBuilder.append(groupsLabel + ": ")
							.append(Joiner.on(", ").join(authenticated.getGroupNames()))
							.append("<br>");
				}
				if (authenticated.getSshKeys() != null) {
					retrievedInfoBuilder.append(sshKeysLabel + ": ").append(authenticated.getSshKeys().size())
							.append("<br>");
				}
				
				if (retrievedInfoBuilder.length() != 0) 
					return new TaskResult(true, new HtmlMessgae(testSuccessfulWithInfoLabel + "<br>" + retrievedInfoBuilder));
				else 
					return new TaskResult(true, new HtmlMessgae(testSuccessfulLabel));
			}

		};
		
		Form<?> form = new Form<Void>("authenticator") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof PropertyUpdating) {
					PropertyUpdating propertyChanged = (PropertyUpdating) event.getPayload();
					propertyChanged.getHandler().add(testButton);
				}
				
			}

		};
		
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("External Authenticator"));
	}

}