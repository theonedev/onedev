package io.onedev.server.web.page.project.imports;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.wicket.Session;
import org.apache.wicket.request.flow.RedirectToUrlException;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import io.onedev.commons.utils.ExplicitException;

@SuppressWarnings("serial")
public abstract class OAuthAwareImportPanel extends ProjectImportPanel {

	public static final String STATE_PREFIX = "import-";
	
	private static final String SESSION_ATTR_STATE = "state";
	
	public OAuthAwareImportPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (!getImportPage().isCallbackStage()) {
	        String state = STATE_PREFIX + UUID.randomUUID().toString();
	        Session.get().setAttribute(SESSION_ATTR_STATE, state);
	        
	        call(new OAuthServiceAwareCallable<String>() {

				@Override
				public String call(OAuth20Service service) {
			        throw new RedirectToUrlException(service.getAuthorizationUrl(state));
				}
				
			});
	        
		} 
		
		String state = (String) Session.get().getAttribute(SESSION_ATTR_STATE);
		if (state == null || !state.equals(getAuthState()))
			throw new ExplicitException("Unmatched OAuth state");

		call(new OAuthServiceAwareCallable<Void>() {

			@Override
			public Void call(OAuth20Service service) {
				try {
					onInitialize(service, service.getAccessToken(getAuthCode()));
				} catch (IOException | InterruptedException | ExecutionException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
			
		});
	}
	
	protected <T> T call(OAuthServiceAwareCallable<T> callable) {
        try(OAuth20Service service = new ServiceBuilder(getApiKey())
                .apiSecret(getApiSecret())  
                .callback(getCallbackUrl())
                .build(getApiEndpoint())) {
        	return callable.call(service);
        } catch (IOException e) {
        	throw new RuntimeException(e);
		}
	}
	
	private String getAuthState() {
		return getPage().getPageParameters().get("state").toOptionalString();
	}

	private String getAuthCode() {
		return getPage().getPageParameters().get("code").toOptionalString();
	}
	
	protected abstract DefaultApi20 getApiEndpoint();
	
	protected abstract String getApiKey();
	
	protected abstract String getApiSecret();
	
	protected abstract String getCallbackUrl();
	
	protected abstract void onInitialize(OAuth20Service service, OAuth2AccessToken token);
	
}
