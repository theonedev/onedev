package io.onedev.server.web.component.user.emailaddresses;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.EmailAddressVerificationStatusBadge;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.page.my.MyPage;
import io.onedev.server.web.util.ConfirmClickModifier;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class EmailAddressesPanel extends GenericPanel<User> {

	private String emailAddressValue;
	
	public EmailAddressesPanel(String id, IModel<User> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getPage() instanceof MyPage)
			add(new Label("who", "you are "));
		else
			add(new Label("who", "this user is "));
		
		add(new ListView<EmailAddress>("emailAddresses", new AbstractReadOnlyModel<List<EmailAddress>>() {

			@Override
			public List<EmailAddress> getObject() {
				return getUser().getSortedEmailAddresses();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<EmailAddress> item) {
				EmailAddress address = item.getModelObject();
				item.add(new Label("value", address.getValue()));
				
				item.add(new WebMarkupContainer("primary")
						.setVisible(address.equals(getUser().getPrimaryEmailAddress())));
				item.add(new WebMarkupContainer("git")
						.setVisible(address.equals(getUser().getGitEmailAddress())));
				
				item.add(new EmailAddressVerificationStatusBadge("verificationStatus", item.getModel())); 
				
				item.add(new MenuLink("operations") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						EmailAddress address = item.getModelObject();
						Long addressId = address.getId();
						if (!address.equals(getUser().getPrimaryEmailAddress())) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return "Set As Primary";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											getEmailAddressManager().setAsPrimary(getEmailAddressManager().load(addressId));
										}
										
									};
								}
								
							});
						}
						if (!address.equals(getUser().getGitEmailAddress())) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return "Use For Git Operations";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new Link<Void>(id) {

										@Override
										public void onClick() {
											getEmailAddressManager().useForGitOperations(getEmailAddressManager().load(addressId));
										}
										
									};
								}
								
							});
						}
						if (!address.isVerified()) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return "Resend Verification Email";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									return new AjaxLink<Void>(id) {

										@Override
										public void onClick(AjaxRequestTarget target) {
											if (OneDev.getInstance(SettingManager.class).getMailService() != null) {
												getEmailAddressManager().sendVerificationEmail(item.getModelObject());
												Session.get().success("Verification email sent, please check it");
											} else {
												target.appendJavaScript(String.format("alert('%s');", 
														"Unable to send verification email as mail service is not configured yet"));
											}
											dropdown.close();
										}
										
									};
								}
								
							});
						}
						if (getUser().getEmailAddresses().size() > 1) {
							menuItems.add(new MenuItem() {

								@Override
								public String getLabel() {
									return "Delete";
								}

								@Override
								public WebMarkupContainer newLink(String id) {
									Link<Void> link = new Link<Void>(id) {

										@Override
										public void onClick() {
											getEmailAddressManager().delete(getEmailAddressManager().load(addressId));
										}

									};
									link.add(new ConfirmClickModifier("Do you really want to delete this email address?"));
									return link;
								}
								
							});
						}
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
				
				if (getEmailAddressManager().findByValue(emailAddressValue) != null) {
					error("This email address is being used");
				} else {
					EmailAddress address = new EmailAddress();
					address.setValue(emailAddressValue);
					address.setOwner(getUser());
					if (SecurityUtils.isAdministrator())
						address.setVerificationCode(null);
					getEmailAddressManager().create(address);
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
		input.setLabel(Model.of("Email address"));
		input.setRequired(true);
		input.add(validatable -> {
			String emailAddress = validatable.getValue();
			if (!new EmailValidator().isValid(emailAddress, null)) {
				validatable.error(new IValidationError() {

					@Override
					public Serializable getErrorMessage(IErrorMessageSource messageSource) {
						return "Malformed email address";
					}

				});
			}
		});
		form.add(input);
		add(new FencedFeedbackPanel("feedback", form));
		add(form);
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}

	private User getUser() {
		return getModelObject();
	}

}
