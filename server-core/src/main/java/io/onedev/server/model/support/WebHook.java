package io.onedev.server.model.support;

import org.apache.commons.lang3.RandomStringUtils;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.project.codecomment.CodeCommentEvent;
import io.onedev.server.event.project.issue.IssueEvent;
import io.onedev.server.event.project.pack.PackEvent;
import io.onedev.server.event.project.pullrequest.PullRequestEvent;
import io.onedev.server.event.project.workspace.WorkspaceEvent;
import io.onedev.server.annotation.Editable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class WebHook implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	public enum EventType {
		CODE_PUSH {

			@Override
			public boolean includes(Object event) {
				return event instanceof RefUpdated;
			}
			
		}, 
		PULL_REQUEST {

			@Override
			public boolean includes(Object event) {
				return event instanceof PullRequestEvent;
			}
			
		}, 
		ISSUE {

			@Override
			public boolean includes(Object event) {
				return event instanceof IssueEvent;
			}
			
		}, 
		CODE_COMMENT {

			@Override
			public boolean includes(Object event) {
				return event instanceof CodeCommentEvent;
			}
			
		}, 
		BUILD {

			@Override
			public boolean includes(Object event) {
				return event instanceof BuildEvent;
			}
			
		},
		PACKAGE {

			@Override
			public boolean includes(Object event) {
				return event instanceof PackEvent;
			}

		},
		WORKSPACE {

			@Override
			public boolean includes(Object event) {
				return event instanceof WorkspaceEvent;
			}

		};

		public abstract boolean includes(Object event);
	}
	
	private String postUrl;
	
	private List<EventType> eventTypes = new ArrayList<>();
	
	private String secret = RandomStringUtils.secure().nextAlphanumeric(40);

	private List<WebHookHeader> headers = new ArrayList<>();

	@Editable(order=100, description="The URL of the server endpoint that will receive the webhook POST requests")
	@NotEmpty
	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

	@Editable(order=200)
	@NotEmpty(message="At least one event type needs to be selected")
	public List<@NotNull EventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	@Editable(order=300, description="The shared secret used to verify webhook POST requests from ${productName}. "
			+ "X-OneDev-Signature contains the lowercase hexadecimal HMAC-SHA256 of the raw request body, "
			+ "using the UTF-8 encoded secret as the key. Verify it with a constant-time comparison before parsing the body. "
			+ "The secret itself is not sent. Use HTTPS to protect the webhook payload")
	@NotEmpty
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Editable(order=400, name="Custom Headers", description="Optionally specify additional HTTP headers to include in "
			+ "the webhook POST request, for example an Authorization header required by the receiving endpoint. "
			+ "X-OneDev-Signature is generated automatically and cannot be overridden")
	@NotNull
	public List<@NotNull @Valid WebHookHeader> getHeaders() {
		return headers;
	}

	public void setHeaders(List<WebHookHeader> headers) {
		this.headers = headers;
	}
	
}
