package io.onedev.server.web.page.project.blob;

import javax.servlet.http.Cookie;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.download.ArchiveDownloadResource;
import io.onedev.server.web.download.ArchiveDownloadResourceReference;
import io.onedev.server.web.page.my.sshkeys.MySshKeysPage;

@SuppressWarnings("serial")
public abstract class CloneOrDownloadPanel extends Panel {

	private static final String COOKIE_CLONE_VIA_SSH = "cloneViaSSH";
	
	private final DropdownLink dropdown;
	
	private boolean cloneViaSSH;
	
	public CloneOrDownloadPanel(String id, DropdownLink dropdown) {
		super(id);
		
		this.dropdown = dropdown;
		
		if (SecurityUtils.getUser() != null) {
			WebRequest request = (WebRequest) RequestCycle.get().getRequest();
			Cookie cookie = request.getCookie(COOKIE_CLONE_VIA_SSH);
			cloneViaSSH = cookie!=null && String.valueOf(true).equals(cookie.getValue());
		} else {
			cloneViaSSH = false;
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("currentCloneProtocol", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (cloneViaSSH)
					return "Clone via SSH";
				else
					return "Clone via HTTP(S)";
			}
			
		}));
		
		AjaxLink<Void> switchLink = new AjaxLink<Void>("switchCloneProtocol") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				switchCloneProtocol(target, !cloneViaSSH);
			}
			
		};
		switchLink.add(new Label("label", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (cloneViaSSH)
					return "Use HTTP(S)";
				else
					return "Use SSH";
			}
			
		}));
		add(switchLink.setVisible(SecurityUtils.getUser() != null));
		
		WebMarkupContainer noSshKeysWarning = new WebMarkupContainer("noSshKeysWarning") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(cloneViaSSH && SecurityUtils.getUser().getSshKeys().isEmpty());
			}
			
		};
		noSshKeysWarning.add(new BookmarkablePageLink<Void>("sshKeys", MySshKeysPage.class));
		noSshKeysWarning.add(new AjaxLink<Void>("useHttp") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				switchCloneProtocol(target, false);
			}
			
		});
		add(noSshKeysWarning);
		
		IModel<String> cloneUrlModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				UrlManager urlManager = OneDev.getInstance(UrlManager.class);
				if (cloneViaSSH)
					return urlManager.sshCloneUrlFor(getProject());
				else
					return urlManager.httpCloneUrlFor(getProject());
			}
			
		};
		add(new TextField<String>("cloneUrl", cloneUrlModel));
		add(new CopyToClipboardLink("copyCloneUrl", cloneUrlModel));

		add(new Label("fingerPrint", OneDev.getInstance(SettingManager.class).getSshSetting().getFingerPrint()) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(cloneViaSSH);
			}
			
		});
		
		add(new ResourceLink<Void>("downloadAsZip", new ArchiveDownloadResourceReference(), 
				ArchiveDownloadResource.paramsOf(getProject(), getRevision(), ArchiveDownloadResource.FORMAT_ZIP)) {

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				return dropdown.closeBeforeClick(super.getOnClickScript(url));
			}
			
		});
		
		add(new ResourceLink<Void>("downloadAsTgz", new ArchiveDownloadResourceReference(), 
				ArchiveDownloadResource.paramsOf(getProject(), getRevision(), ArchiveDownloadResource.FORMAT_TGZ)) {

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				return dropdown.closeBeforeClick(super.getOnClickScript(url));
			}
			
		});
		
	}
	
	private void switchCloneProtocol(AjaxRequestTarget target, boolean cloneViaSSH) {
		WebResponse response = (WebResponse) RequestCycle.get().getResponse();
		
		this.cloneViaSSH = cloneViaSSH;
		
		Cookie cookie;
		cookie = new Cookie(COOKIE_CLONE_VIA_SSH, String.valueOf(cloneViaSSH));
		cookie.setMaxAge(Integer.MAX_VALUE);
		cookie.setPath("/");
		
		response.addCookie(cookie);
		
		target.add(this);
	}

	protected abstract Project getProject();
	
	protected abstract String getRevision();
	
}
