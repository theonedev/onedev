package io.onedev.server.web.page.simple.security;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class EmailAddressVerificationPage extends SimplePage {

	private final String PARAM_EMAIL_ADDRESS = "emailAddress";
	
	private final String PARAM_VERIFICATION_CODE = "verificationCode";
	
	private final IModel<EmailAddress> emailAddressModel;
	
	public EmailAddressVerificationPage(PageParameters params) {
		super(params);
		
		Long emailAddressId = params.get(PARAM_EMAIL_ADDRESS).toLong();
		emailAddressModel = new LoadableDetachableModel<EmailAddress>() {

			@Override
			protected EmailAddress load() {
				return getEmailAddressManager().load(emailAddressId);
			}
			
		};
		
		EmailAddress emailAddress = emailAddressModel.getObject();
		String verificationCode = params.get(PARAM_VERIFICATION_CODE).toString();

		if (verificationCode.equals(emailAddress.getVerficationCode())) {
			emailAddress.setVerificationCode(null);
			getEmailAddressManager().save(emailAddress);
		} 
	}
	
	private EmailAddressManager getEmailAddressManager() {
		return OneDev.getInstance(EmailAddressManager.class);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		EmailAddress emailAddress = emailAddressModel.getObject();
		add(new WebMarkupContainer("successful").setVisible(emailAddress.isVerified()));
		add(new WebMarkupContainer("failed").setVisible(!emailAddress.isVerified()));
		add(new BookmarkablePageLink<Void>("goHome", getApplication().getHomePage()));
	}

	@Override
	protected void onDetach() {
		emailAddressModel.detach();
		super.onDetach();
	}

	@Override
	protected String getTitle() {
		return "Email Address Verification";
	}

	@Override
	protected String getSubTitle() {
		return null;
	}

}
