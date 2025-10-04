package io.onedev.server.web.component.user.emailaddresses;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidationError;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;

import io.onedev.server.OneDev;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.EmailAddressVerificationStatusBadge;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.ConfirmClickModifier;

public class EmailAddressesPanel extends GenericPanel<User> {

	private String emailAddressValue;
	
	public EmailAddressesPanel(String id, IModel<User> model) {
		super(id, model);
	}

	private AuditService getAuditService() {
		return OneDev.getInstance(AuditService.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
				
		add(new ListView<EmailAddress>("emailAddresses", new AbstractReadOnlyModel<List<EmailAddress>>() {

			@Override
			public List<EmailAddress> getObject() {
				return getUser().getSortedEmailAddresses();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<EmailAddress> item) {
				EmailAddress emailAddress = item.getModelObject();
				item.add(new Label("value", emailAddress.getValue()));
				
				item.add(new WebMarkupContainer("primary")
						.setVisible(emailAddress.equals(getUser().getPrimaryEmailAddress())));
				item.add(new WebMarkupContainer("git")
						.setVisible(emailAddress.equals(getUser().getGitEmailAddress())));
				item.add(new WebMarkupContainer("public")
 						.setVisible(emailAddress.isOpen()));
				
				item.add(new EmailAddressVerificationStatusBadge("verificationStatus", item.getModel()));
				
				Long emailAddressId = emailAddress.getId();
				item.add(new MenuLink("operations") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						var emailAddress = getEmailAddressService().load(emailAddressId);
						if (!emailAddress.equals(getUser().getPrimaryEmailAddress())) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return _T("Set As Primary");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											var emailAddress = getEmailAddressService().load(emailAddressId);
											getEmailAddressService().setAsPrimary(emailAddress);
											if (getPage() instanceof UserPage)
												getAuditService().audit(null, "specified email address \"" + emailAddress.getValue() + "\" as primary in account \"" + getUser().getName() + "\"", null, null);
										}
										
									};
								}
								
							});
						}
						if (!emailAddress.equals(getUser().getGitEmailAddress())) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return _T("Use For Git Operations");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											var emailAddress = getEmailAddressService().load(emailAddressId);
											getEmailAddressService().useForGitOperations(emailAddress);
											if (getPage() instanceof UserPage)
												getAuditService().audit(null, "specified email address \"" + emailAddress.getValue() + "\" for git operations in account \"" + getUser().getName() + "\"", null, null);
										}
										
									};
								}
								
							});
						}
						if (!emailAddress.equals(getUser().getPublicEmailAddress())) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return _T("Set as Public");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											var emailAddress = getEmailAddressService().load(emailAddressId);
											getEmailAddressService().setAsPublic(emailAddress);
											if (getPage() instanceof UserPage)
												getAuditService().audit(null, "specified email address \"" + emailAddress.getValue() + "\" as public in account \"" + getUser().getName() + "\"", null, null);
										}
										
									};
								}
								
							});
						} else {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return _T("Set as Private");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											var emailAddress = getEmailAddressService().load(emailAddressId);
											getEmailAddressService().setAsPrivate(emailAddress);
											if (getPage() instanceof UserPage)
												getAuditService().audit(null, "specified email address \"" + emailAddress.getValue() + "\" as private in account \"" + getUser().getName() + "\"", null, null);
										}
										
									};
								}
								
							});
						}

						if (!emailAddress.isVerified()) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return _T("Resend Verification Email");
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxLink<Void>(id) {

										@Override
										public void onClick(AjaxRequestTarget target) {
											if (OneDev.getInstance(SettingService.class).getMailConnector() != null) {
												getEmailAddressService().sendVerificationEmail(item.getModelObject());
												Session.get().success(_T("Verification email sent, please check it"));
											} else {
												target.appendJavaScript(String.format("alert('%s');", 
														_T("Unable to send verification email as mail service is not configured yet")));
											}
											dropdown.close();
										}
										
									};
								}
								
							});
						}
						menuItems.add(new MenuItem() {

							@Override
							public String getLabel() {
								return _T("Delete");
							}

							@Override
							public WebMarkupContainer newLink(String id) {
								var hasMultipleEmailAddresses = getUser().getEmailAddresses().size() > 1;
								var link = new Link<Void>(id) {

									@Override
									public void onClick() {
										if (hasMultipleEmailAddresses) {
											var emailAddress = getEmailAddressService().load(emailAddressId);
											getEmailAddressService().delete(emailAddress);
											if (getPage() instanceof UserPage)
												getAuditService().audit(null, "deleted email address \"" + emailAddress.getValue() + "\" from account \"" + getUser().getName() + "\"", null, null);
										} else {
											Session.get().warn(_T("At least one email address should be configured, please add a new one first"));
										}
									}

								};
								if (getUser().getEmailAddresses().size() > 1)
									link.add(new ConfirmClickModifier(_T("Do you really want to delete this email address?")));
								return link;
							}
							
						});
						return menuItems;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!getMenuItems(null).isEmpty());
					}
					
				});
			}
			
		});
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				if (getEmailAddressService().findByValue(emailAddressValue) != null) {
					error(_T("This email address is being used"));
				} else {
					EmailAddress address = new EmailAddress();
					address.setValue(emailAddressValue);
					address.setOwner(getUser());
					if (SecurityUtils.isAdministrator())
						address.setVerificationCode(null);
					getEmailAddressService().create(address);
					if (getPage() instanceof UserPage)
						getAuditService().audit(null, "added email address \"" + address.getValue() + "\" in account \"" + getUser().getName() + "\"", null, null);
					emailAddressValue = null;
				}
			}
			
		};
		TextField<String> input = new TextField<String>("emailAddress", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return emailAddressValue;
			}

			@Override
			public void setObject(String object) {
				emailAddressValue = object;
			}

		});
		input.setLabel(Model.of(_T("Email address")));
		input.setRequired(true);
		input.add(validatable -> {
			String emailAddress = validatable.getValue();
			if (!new EmailValidator().isValid(emailAddress, null)) {
				validatable.error(new IValidationError() {

					@Override
					public Serializable getErrorMessage(IErrorMessageSource messageSource) {
						return _T("Malformed email address");
					}

				});
			}
		});
		form.add(input);
		add(new FencedFeedbackPanel("feedback", form));
		add(form);
	}
	
	private EmailAddressService getEmailAddressService() {
		return OneDev.getInstance(EmailAddressService.class);
	}

	private User getUser() {
		return getModelObject();
	}

}
