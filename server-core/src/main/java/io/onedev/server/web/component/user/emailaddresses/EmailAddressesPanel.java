package io.onedev.server.web.component.user.emailaddresses;

import static io.onedev.server.model.User.Type.ORDINARY;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidationError;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.jspecify.annotations.Nullable;

import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.web.component.EmailAddressVerificationStatusBadge;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.ConfirmClickModifier;

public class EmailAddressesPanel extends GenericPanel<User> {

	@Inject
	private AuditService auditService;

	@Inject
	private SettingService settingService;

	@Inject
	private UserService userService;

	@Inject
	private EmailAddressService emailAddressService;

	public EmailAddressesPanel(String id, IModel<User> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var ordinaryContainer = new WebMarkupContainer("ordinary") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getType() == ORDINARY);
			}

		};
		add(ordinaryContainer);

		var backupEmailAddressesSection = new WebMarkupContainer("backupEmailAddresses") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getPrimaryEmailAddress() != null);
			}

		};
		backupEmailAddressesSection.setOutputMarkupPlaceholderTag(true);

		Form<Void> primaryForm = new Form<>("primaryForm");
		
		AtomicLong primaryEmailAddressId = new AtomicLong(0);
		if (getUser().getPrimaryEmailAddress() != null) 
			primaryEmailAddressId.set(getUser().getPrimaryEmailAddress().getId());
		var primaryChoice = new DropDownChoice<>(
			"select",
				new IModel<EmailAddress>() {

					@Override
					public EmailAddress getObject() {
						if (primaryEmailAddressId.get() != 0)
							return emailAddressService.load(primaryEmailAddressId.get());
						else
							return null;
					}

					@Override
					public void setObject(EmailAddress object) {
						primaryEmailAddressId.set(object.getId());
					}

					@Override
					public void detach() {
					}

				}, new LoadableDetachableModel<List<EmailAddress>>() {

					@Override
					public List<EmailAddress> load() {
						return getUser().getSortedEmailAddresses();
					}
					
				}, new ChoiceRenderer<>("value")) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getUser().getEmailAddresses().isEmpty());
			}

		};

		primaryChoice.setRequired(true);
		primaryChoice.add(new AjaxFormSubmitBehavior(primaryForm, "change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				emailAddressService.setAsPrimary(emailAddressService.load(primaryEmailAddressId.get()));
				Session.get().success(_T("Primary email address changed"));
				auditIfNecessary("specified email address \"" + getUser().getPrimaryEmailAddress().getValue() + "\" as primary");
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				super.onError(target);
				target.add(primaryForm);
			}

		});
		primaryForm.add(primaryChoice);

		var primaryInput = new TextField<String>("input", Model.of("")) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getEmailAddresses().isEmpty());
			}
		};
		primaryInput.setLabel(Model.of(_T("Email address")));
		primaryInput.setRequired(true);
		primaryInput.add(newEmailAddressValidator());
		primaryForm.add(primaryInput);

		var primarySubmit = new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var emailAddress = newEmailAddress(primaryForm, primaryInput.getModelObject());
				if (emailAddress != null) {
					emailAddressService.create(emailAddress);
					Session.get().success(_T("Primary email address added"));
					auditIfNecessary("added primary email address \"" + getUser().getPrimaryEmailAddress().getValue() + "\"");
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				} else {
					target.add(primaryForm);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(primaryForm);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getEmailAddresses().isEmpty());
			}
		};

		primaryForm.add(primarySubmit);
		primaryForm.add(new FencedFeedbackPanel("feedback", primaryForm));
		
		var unverifiedNoteContainer = new WebMarkupContainer("unverifiedNote") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getPrimaryEmailAddress() != null && !getUser().getPrimaryEmailAddress().isVerified());
			}

		};
		unverifiedNoteContainer.setOutputMarkupPlaceholderTag(true);
		unverifiedNoteContainer.add(new AjaxLink<Void>("resendVerificationEmail") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (settingService.getMailConnector() != null) {
					emailAddressService.sendVerificationEmail(getUser().getPrimaryEmailAddress());
					Session.get().success(_T("Verification email sent, please check it"));
				} else {
					target.appendJavaScript(String.format("alert('%s');",
							_T("Unable to send verification email as mail service is not configured yet")));
				}
			}

		});
		primaryForm.add(unverifiedNoteContainer);

		primaryForm.setOutputMarkupId(true);
		ordinaryContainer.add(primaryForm);

		backupEmailAddressesSection.add(new ListView<EmailAddress>("list", new AbstractReadOnlyModel<>() {

			@Override
			public List<EmailAddress> getObject() {
				return getUser().getSortedEmailAddresses().stream()
						.filter(it -> !it.equals(getUser().getPrimaryEmailAddress()))
						.collect(Collectors.toList());
			}

		}) {

			@Override
			protected void populateItem(ListItem<EmailAddress> item) {
				var emailAddress = item.getModelObject();
				Long emailAddressId = emailAddress.getId();
				item.add(new Label("value", emailAddress.getValue()));
				item.add(new EmailAddressVerificationStatusBadge("verificationStatus", item.getModel()));
				item.add(new AjaxLink<Void>("resendVerificationEmail") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						if (settingService.getMailConnector() != null) {
							emailAddressService.sendVerificationEmail(emailAddressService.load(emailAddressId));
							Session.get().success(_T("Verification email sent, please check it"));
						} else {
							target.appendJavaScript(String.format("alert('%s');",
									_T("Unable to send verification email as mail service is not configured yet")));
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!emailAddress.isVerified());
					}

				});

				var deleteLink = new Link<Void>("delete") {

					@Override
					public void onClick() {
						var emailAddress = emailAddressService.load(emailAddressId);
						emailAddressService.delete(emailAddress);
						auditIfNecessary("deleted backup email address \"" + emailAddress.getValue() + "\"");
						setResponsePage(getPage().getClass(), getPage().getPageParameters());
					}
				};
				deleteLink.add(new ConfirmClickModifier(_T("Do you really want to delete this email address?")));
				item.add(deleteLink);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getEmailAddresses().size() > 1);
			}

		});
		ordinaryContainer.add(backupEmailAddressesSection);

		Form<Void> backupForm = new Form<>("form");

		var backupInput = new TextField<String>("input", Model.of(""));
		backupInput.setRequired(true);
		backupInput.add(newEmailAddressValidator());
		backupForm.add(backupInput);

		var backupSubmit = new AjaxButton("submit") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				var emailAddress = newEmailAddress(backupForm, backupInput.getModelObject());
				if (emailAddress != null) {
					emailAddressService.create(emailAddress);
					auditIfNecessary("added backup email address \"" + emailAddress.getValue() + "\"");
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				} else {
					target.add(backupForm);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(backupForm);
			}
		};
		backupForm.add(backupSubmit);
		backupForm.add(new FencedFeedbackPanel("feedback", backupForm));
		backupEmailAddressesSection.add(backupForm);

		Form<Void> privacyForm = new Form<>("privacyForm");
		var keepEmailAddressesPrivateCheck = new CheckBox("check", Model.of(getUser().isKeepEmailAddressesPrivate()));

		var privacyToggleLabel = new WebMarkupContainer("label");
		privacyToggleLabel.setOutputMarkupId(true);
		privacyToggleLabel.add(keepEmailAddressesPrivateCheck);
		privacyForm.add(privacyToggleLabel);
		keepEmailAddressesPrivateCheck.add(new AjaxFormSubmitBehavior(privacyForm, "change") {

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				super.onSubmit(target);
				getUser().setKeepEmailAddressesPrivate(keepEmailAddressesPrivateCheck.getModelObject());
				userService.update(getUser(), null);
				Session.get().success(_T("Email addresses privacy updated"));
				auditIfNecessary(getUser().isKeepEmailAddressesPrivate() ? "made email addresses private" : "made email addresses public");
				setResponsePage(getPage().getClass(), getPage().getPageParameters());
			}

		});

		var privateNote = new WebMarkupContainer("privateNote") {

			@Override
			protected void onConfigure() {
				setVisible(getUser().isKeepEmailAddressesPrivate());
			}

		};
		privateNote.setOutputMarkupPlaceholderTag(true);
		privateNote.add(new Label("generatedEmailAddress",
				Model.of(getUser().getName() + "@" + User.getNoreplyEmailDomain())));
		privacyForm.add(privateNote);

		ordinaryContainer.add(privacyForm);

		var serviceOrAiContainer = new WebMarkupContainer("serviceOrAi") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUser().getType() != ORDINARY);
			}

		};
		serviceOrAiContainer.add(new Label("generatedEmailAddress",
				Model.of(getUser().getName() + "@" + User.getNoreplyEmailDomain())));
		add(serviceOrAiContainer);
	}

	private IValidator<String> newEmailAddressValidator() {
		return new IValidator<String>() {
			@Override
			public void validate(IValidatable<String> validatable) {
				if (!new EmailValidator().isValid(validatable.getValue(), null)) {
					validatable.error(new IValidationError() {
						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							return _T("Malformed email address");
						}
					});
				} else if (User.getLoginName(validatable.getValue()) != null) {
					validatable.error(new IValidationError() {
						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							return _T("Email address with noreply domain is not allowed");
						}
					});
				}
			}
		};
	}

	@Nullable
	private EmailAddress newEmailAddress(Form<?> form, String emailAddressValue) {
		if (emailAddressService.findByValue(emailAddressValue) != null) {
			form.error(_T("This email address is being used"));
			return null;
		}
		var address = new EmailAddress();
		address.setValue(emailAddressValue);
		address.setOwner(getUser());
		if (SecurityUtils.isAdministrator())
			address.setVerificationCode(null);
		return address;
	}

	private void auditIfNecessary(String action) {
		if (getPage() instanceof UserPage) {
			auditService.audit(null,
					action + " in account \"" + getUser().getName() + "\"", null, null);
		}
	}

	private User getUser() {
		return getModelObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new EmailAddressesCssResourceReference()));
	}

}
