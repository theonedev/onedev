package io.onedev.server.model.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.event.Event;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.codecomment.CodeCommentEvent;
import io.onedev.server.event.issue.IssueEvent;
import io.onedev.server.event.pullrequest.PullRequestEvent;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class WebHook implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum EventType {
		CODE_PUSH {

			@Override
			public boolean includes(Event event) {
				return event instanceof RefUpdated;
			}
			
		}, 
		PULL_REQUEST {

			@Override
			public boolean includes(Event event) {
				return event instanceof PullRequestEvent;
			}
			
		}, 
		ISSUE {

			@Override
			public boolean includes(Event event) {
				return event instanceof IssueEvent;
			}
			
		}, 
		CODE_COMMENT {

			@Override
			public boolean includes(Event event) {
				return event instanceof CodeCommentEvent;
			}
			
		}, 
		BUILD {

			@Override
			public boolean includes(Event event) {
				return event instanceof BuildEvent;
			}
			
		};

		public abstract boolean includes(Event event);
	}
	
	private String postUrl;
	
	private List<EventType> eventTypes = new ArrayList<>();
	
	private String secret = RandomStringUtils.randomAlphanumeric(20);

	@Editable(order=100, description="The URL of the server endpoint that will receive the webhook POST requests")
	@NotEmpty
	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

	@Editable(order=200)
	@Size(min=1, message="At least one event type needs to be selected")
	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	@Editable(order=300, description="The secret which allows you to ensure that POST requests sent to the payload URL are "
			+ "from OneDev. When you set a secret you'll receive the X-OneDev-Signature header in the webhook POST request")
	@NotEmpty
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
}
