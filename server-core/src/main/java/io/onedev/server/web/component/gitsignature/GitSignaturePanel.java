package io.onedev.server.web.component.gitsignature;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.revwalk.RevObject;

import io.onedev.server.OneDev;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.signature.SignatureVerification;
import io.onedev.server.git.signature.SignatureVerificationKeyLoader;
import io.onedev.server.git.signature.SignatureVerified;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public abstract class GitSignaturePanel extends DropdownLink {

	private IModel<SignatureVerification> model = 
			new LoadableDetachableModel<SignatureVerification>() {

		@Override
		protected SignatureVerification load() {
			return GitUtils.verifySignature(getRevObject(), 
					OneDev.getInstance(SignatureVerificationKeyLoader.class));
		}
		
	};
	
	public GitSignaturePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);

		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (model.getObject() instanceof SignatureVerified)
					return "link-success";
				else
					return "link-danger";
			}
			
		}));
	}

	@Override
	public IModel<?> getBody() {
		return new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				SignatureVerification verification = model.getObject();
				
				String icon;
				if (verification instanceof SignatureVerified)
					icon = "locked";
				else
					icon = "unlocked";
				return String.format(
						"<svg class='icon'><use xlink:href='%s'/></svg>", 
						SpriteImage.getVersionedHref(icon));
			}
			
		};
	}

	@Override
	protected void onConfigure() {
		setVisible(model.getObject() != null);
	}

	@Override
	protected void onDetach() {
		model.detach();
		super.onDetach();
	}

	@Override
	protected Component newContent(String id, FloatingPanel dropdown) {
		return new GitSignatureDetailPanel(id) {

			@Override
			protected SignatureVerification getVerification() {
				return model.getObject();
			}

			@Override
			protected RevObject getRevObject() {
				return GitSignaturePanel.this.getRevObject();
			}
			
		};
	}
	
	protected abstract RevObject getRevObject();
	
}
