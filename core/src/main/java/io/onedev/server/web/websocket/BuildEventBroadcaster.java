package io.onedev.server.web.websocket;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.event.build.BuildSubmitted;
import io.onedev.server.model.Build;
import io.onedev.server.web.util.WicketUtils;

@Singleton
public class BuildEventBroadcaster {
	
	private final WebSocketManager webSocketManager;
	
	@Inject
	public BuildEventBroadcaster(WebSocketManager webSocketManager) {
		this.webSocketManager = webSocketManager;
	}

	@Listen
	public void on(BuildEvent event) {
		PageKey pageKey = WicketUtils.getPageKey();
		webSocketManager.notifyObservableChange(Build.getWebSocketObservable(event.getBuild().getId()), pageKey);
		webSocketManager.notifyObservableChange("commit-status:" + event.getBuild().getCommitHash(), pageKey);
		if (event instanceof BuildSubmitted) { 
			String observable = "commit-builds:" + event.getProject().getId() + ":" + event.getBuild().getCommitHash();
			webSocketManager.notifyObservableChange(observable, pageKey);
		}
	}

}