package io.onedev.server.web.page.project.blob;

import io.onedev.server.model.Project;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.project.gitprotocol.GitProtocolPanel;
import io.onedev.server.web.resource.ArchiveResource;
import io.onedev.server.web.resource.ArchiveResourceReference;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("serial")
public abstract class GetCodePanel extends Panel {

	private final DropdownLink dropdown;
	
	public GetCodePanel(String id, DropdownLink dropdown) {
		super(id);
		this.dropdown = dropdown;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GitProtocolPanel("clone") {
			
			@Override
			protected Component newContent(String componentId) {
				Fragment fragment = new Fragment(componentId, "urlFrag", GetCodePanel.this);
				IModel<String> urlModel = new LoadableDetachableModel<String>() {

					@Override
					protected String load() {
						return getProtocolUrl();
					}
					
				};
				fragment.add(new TextField<String>("value", urlModel));
				fragment.add(new CopyToClipboardLink("copy", urlModel));
				
				fragment.add(new ExternalLink("vscode", new LoadableDetachableModel<>() {
					@Override
					protected String load() {
						return "vscode://vscode.git/clone?url=" + encode(getProtocolUrl(), UTF_8);
					}
				}));
				fragment.add(new ExternalLink("intellij", new LoadableDetachableModel<>() {
					@Override
					protected String load() {
						return "jetbrains://idea/checkout/git?idea.required.plugins.id=Git4Idea&checkout.repo=" + encode(getProtocolUrl(), UTF_8);
					}
				}));
				return fragment;
			}
			
			@Override
			protected Project getProject() {
				return GetCodePanel.this.getProject();
			}
			
		});
		
		
		add(new ResourceLink<Void>("downloadAsZip", new ArchiveResourceReference(), 
				ArchiveResource.paramsOf(getProject().getId(), getRevision(), ArchiveResource.FORMAT_ZIP)) {

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				return dropdown.closeBeforeClick(super.getOnClickScript(url));
			}
			
		});
		
		add(new ResourceLink<Void>("downloadAsTgz", new ArchiveResourceReference(), 
				ArchiveResource.paramsOf(getProject().getId(), getRevision(), ArchiveResource.FORMAT_TGZ)) {

			@Override
			protected CharSequence getOnClickScript(CharSequence url) {
				return dropdown.closeBeforeClick(super.getOnClickScript(url));
			}
			
		});
		
	}
	
	protected abstract Project getProject();
	
	protected abstract String getRevision();
	
}
