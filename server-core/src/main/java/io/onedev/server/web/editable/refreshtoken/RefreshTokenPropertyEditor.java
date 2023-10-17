package io.onedev.server.web.editable.refreshtoken;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.unbescape.javascript.JavaScriptEscape;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.OAuthUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.annotation.RefreshToken;
import io.onedev.server.web.page.simple.security.OAuthCallbackPage;

@SuppressWarnings("serial")
public class RefreshTokenPropertyEditor extends PropertyEditor<String> {

	private PasswordTextField input;
	
	private AbstractPostAjaxBehavior ajaxBehavior;
	
	public RefreshTokenPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<String> propertyModel) {
		super(id, propertyDescriptor, propertyModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		input = new PasswordTextField("input", Model.of(getModelObject()));
		input.setRequired(true);
		input.setResetPassword(false);
		input.setLabel(Model.of(getDescriptor().getDisplayName()));
		add(input);

		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});

		add(new AjaxLink<Void>("generate") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				ComponentContext.push(new ComponentContext(RefreshTokenPropertyEditor.this));
				try {
					RefreshToken authentication = descriptor.getPropertyGetter().getAnnotation(RefreshToken.class);
					RefreshToken.Callback callback = (RefreshToken.Callback) ReflectionUtils.invokeStaticMethod(
							descriptor.getBeanClass(), authentication.value());
					
					Charset utf8 = StandardCharsets.UTF_8;
					String redirectUri = getRedirectUrl();
					UrlEncoder urlEncoder = UrlEncoder.QUERY_INSTANCE;
					String encodedRedirectUri = urlEncoder.encode(redirectUri, utf8);
					
					String state = UUID.randomUUID().toString();
					
					Session.get().setAttribute("oauthState", state);
					
					String authorizeUrl = String.format("%s"
							+ "?response_type=code"
							+ "&client_id=%s"
							+ "&redirect_uri=%s"
							+ "&scope=%s"
							+ "&state=%s", 
							callback.getAuthorizeEndpoint(),
							urlEncoder.encode(callback.getClientId(), utf8), 
							encodedRedirectUri, 
							urlEncoder.encode(StringUtils.join(callback.getScopes(), " "), utf8), 
							urlEncoder.encode(state, utf8));
					for (Map.Entry<String, String> entry: callback.getAuthorizeParams().entrySet()) {
						String encodedKey = urlEncoder.encode(entry.getKey(), utf8);
						String encodedValue = urlEncoder.encode(entry.getValue(), utf8);
						authorizeUrl += "&" + encodedKey + "=" + encodedValue;
					}
					
					target.appendJavaScript(String.format("onedev.server.refreshToken.onGenerate('%s', '%s', '%s', %s);", 
							RefreshTokenPropertyEditor.this.getMarkupId(), 
							authorizeUrl, 
							JavaScriptEscape.escapeJavaScript(state),
							ajaxBehavior.getCallbackFunction()));
				} catch (Exception e) {
					ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
					if (explicitException != null)
						Session.get().error(explicitException.getMessage());
					else
						throw e;
				} finally {
					ComponentContext.pop();
				}
			}

		});
		
		add(ajaxBehavior = new AbstractPostAjaxBehavior() {
			
			@Override
			protected void respond(AjaxRequestTarget target) {
				String refreshTokenValue = "";
				ComponentContext.push(new ComponentContext(RefreshTokenPropertyEditor.this));
				try {
					RefreshToken authentication = descriptor.getPropertyGetter().getAnnotation(RefreshToken.class);
					RefreshToken.Callback callback = (RefreshToken.Callback) ReflectionUtils.invokeStaticMethod(
							descriptor.getBeanClass(), authentication.value());
					
					AuthorizationCode authorizationCode = new AuthorizationCode((String) Session.get().getAttribute("oauthCode"));
					URI redirectUri = new URI(getRedirectUrl());
					AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authorizationCode, redirectUri);
					
					ClientID clientID = new ClientID(callback.getClientId());
					Secret clientSecret = new Secret(callback.getClientSecret());
					ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

					URI tokenEndpoint = new URI(callback.getTokenEndpoint());

					TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
					TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

					if (response.indicatesSuccess()) {
						AccessTokenResponse successResponse = response.toSuccessResponse();
						refreshTokenValue = successResponse.getTokens().getRefreshToken().getValue();
						Session.get().success("Refresh token generated successfully");
					} else {
					    TokenErrorResponse errorResponse = response.toErrorResponse();
					    throw new ExplicitException(OAuthUtils.getErrorMessage(errorResponse.getErrorObject()));
					}
				} catch (Exception e) {
					ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
					if (explicitException != null)
						Session.get().error(explicitException.getMessage());
					else
						throw ExceptionUtils.unchecked(e);
				} finally {
					ComponentContext.pop();
					target.appendJavaScript(String.format("onedev.server.refreshToken.onGenerated('%s', '%s');", 
							RefreshTokenPropertyEditor.this.getMarkupId(), refreshTokenValue));
				}
			}
			
		});
	}

	private String getRedirectUrl() {
		String serverUrl = OneDev.getInstance(SettingManager.class).getSystemSetting().getServerUrl();
		return serverUrl + "/" + OAuthCallbackPage.MOUNT_PATH;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new RefreshTokenResourceReference()));
	}

	@Override
	protected String convertInputToValue() throws ConversionException {
		return input.getConvertedInput();
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
