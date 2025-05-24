package io.onedev.server.web.component;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.EmailAddress;

public class EmailAddressVerificationStatusBadge extends Label {

	private final IModel<EmailAddress> emailAddressModel;
	
	public EmailAddressVerificationStatusBadge(String id, IModel<EmailAddress> emailAddressModel) {
		super(id);
		this.emailAddressModel = emailAddressModel;
		setDefaultModel(new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !emailAddressModel.getObject().isVerified()?_T("Unverified"):"";
			}
			
		});
	}
	
	@Override
	protected void onDetach() {
		emailAddressModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!emailAddressModel.getObject().isVerified());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (!emailAddressModel.getObject().isVerified()) 
					return "badge badge-sm badge-warning";
				else
					return "";
			}
			
		}));
	}

}
