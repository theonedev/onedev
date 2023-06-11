package io.onedev.server.web.component.gitsignature;

import io.onedev.server.OneDev;
import io.onedev.server.git.signatureverification.SignatureVerificationManager;
import io.onedev.server.git.signatureverification.VerificationResult;
import io.onedev.server.git.signatureverification.VerificationSuccessful;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.svg.SpriteImage;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.revwalk.RevObject;

@SuppressWarnings("serial")
public abstract class SignatureStatusPanel extends DropdownLink {

	private IModel<VerificationResult> model = new LoadableDetachableModel<>() {

		@Override
		protected VerificationResult load() {
			return OneDev.getInstance(SignatureVerificationManager.class).verifySignature(getRevObject());
		}

	};
	
	public SignatureStatusPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setEscapeModelStrings(false);

		add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (model.getObject() instanceof VerificationSuccessful)
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
				var verification = model.getObject();
				
				String icon;
				if (verification instanceof VerificationSuccessful)
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
		return model.getObject().renderDetail(id, getRevObject());
	}
	
	protected abstract RevObject getRevObject();
	
}
