package io.onedev.server.web.page.project.setting.webhook;

import java.io.Serializable;
import java.util.ArrayList;

import io.onedev.server.model.support.WebHook;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class WebHooksBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<WebHook> webHooks = new ArrayList<>();

	@Editable
	public ArrayList<WebHook> getWebHooks() {
		return webHooks;
	}

	public void setWebHooks(ArrayList<WebHook> webHooks) {
		this.webHooks = webHooks;
	}
	
}
