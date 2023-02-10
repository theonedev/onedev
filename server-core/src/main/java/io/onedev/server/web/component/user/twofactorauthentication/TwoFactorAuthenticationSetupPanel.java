package io.onedev.server.web.component.user.twofactorauthentication;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.model.support.TwoFactorAuthentication;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.web.page.simple.security.LoginPage;
import org.apache.commons.codec.binary.Base32;
import org.apache.tika.mime.MediaType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public abstract class TwoFactorAuthenticationSetupPanel extends GenericPanel<User> {

	private static final int QR_CODE_SIZE = 160;
	
	public TwoFactorAuthenticationSetupPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String secretKey = generateSecretKey();
		
		List<String> scratchCodes = new ArrayList<>();
		for (int i=0; i<16; i++) 
			scratchCodes.add(CryptoUtils.generateSecret());
		
		TwoFactorAuthentication authentication = new TwoFactorAuthentication(secretKey, scratchCodes);

		Fragment fragment = new Fragment("content", "pendingVerifyFrag", this);
		Form<?> form = new Form<Void>("form");
		
		form.add(new WebMarkupContainer("enforceNotice") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPage() instanceof LoginPage);
			}
			
		});
		
		form.add(new Image("QRCode", new AbstractResource() {

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				response.setContentType(MediaType.image("png").toString());
				response.disableCaching();
				
				response.setWriteCallback(new WriteCallback() {

					@Override
					public void writeData(Attributes attributes) throws IOException {
 						authentication.writeQRCode(getUser(), QR_CODE_SIZE, 
 								attributes.getResponse().getOutputStream());
					}				
					
				});		
				return response;
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("width", QR_CODE_SIZE + "px");
				tag.put("height", QR_CODE_SIZE + "px");
			}
			
		});
		
		TextField<String> input = new TextField<String>("passcode", Model.of(""));
		form.add(input);
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(new AjaxButton("verify") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				String passcode = input.getModelObject();
				if (StringUtils.isBlank(passcode)) {
					form.error("Please input passcode");
					target.add(form);
				} else if (!passcode.equals(authentication.getTOTPCode())) {
					form.error("Passcode incorrect");
					target.add(form);
				} else {
					getUser().setTwoFactorAuthentication(authentication);
					OneDev.getInstance(UserManager.class).save(getUser());
					
					Fragment fragment = new Fragment("content", "verifiedFrag", TwoFactorAuthenticationSetupPanel.this);
					RepeatingView recoveryCodesView = new RepeatingView("recoveryCodes");
					for (String scratchCode: authentication.getScratchCodes())
						recoveryCodesView.add(new Label(recoveryCodesView.newChildId(), scratchCode));
					fragment.add(recoveryCodesView);
					fragment.add(new ResourceLink<Void>("download", new AbstractResource() {

						@Override
						protected ResourceResponse newResourceResponse(Attributes attributes) {
							ResourceResponse response = new ResourceResponse();
							response.setContentType(MediaType.TEXT_PLAIN.toString());
							response.setFileName("onedev-recovery-codes.txt");
							response.setContentDisposition(ContentDisposition.ATTACHMENT);
							response.disableCaching();
							
							response.setWriteCallback(new WriteCallback() {

								@Override
								public void writeData(Attributes attributes) throws IOException {
		 							String content = StringUtils.join(authentication.getScratchCodes(), "\n");
									attributes.getResponse().write(content);
								}				
								
							});		
							return response;
						}
						
					}));
					fragment.add(new AjaxLink<Void>("ok") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onEnabled(target);
						}
						
					});
					fragment.add(new AjaxLink<Void>("close") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							onEnabled(target);
						}
						
					});
					TwoFactorAuthenticationSetupPanel.this.replace(fragment);		
					target.add(fragment);
				}
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
		form.setOutputMarkupId(true);
		
		fragment.add(form);	
		
		fragment.setOutputMarkupId(true);
		add(fragment);
	}

	public static String generateSecretKey() {
	    SecureRandom random = new SecureRandom();
	    byte[] bytes = new byte[20];
	    random.nextBytes(bytes);
	    Base32 base32 = new Base32();
	    return base32.encodeToString(bytes);
	}
	
	protected abstract User getUser();
	
	protected abstract void onEnabled(AjaxRequestTarget target);
	
	protected abstract void onCancelled(AjaxRequestTarget target);
	
}
