package io.onedev.server.web.component.diff.difftitle;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import io.onedev.server.git.BlobChange;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class BlobDiffTitle extends Panel {

	private final BlobChange change;
	
	public BlobDiffTitle(String id, BlobChange change) {
		super(id);

		this.change = change;		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("renamedTitle", change.getOldBlobIdent().path)
				.setVisible(change.getType() == ChangeType.RENAME));
		add(new Label("title", change.getPath()));

		String modeChange;
		if (change.getOldBlobIdent().mode != null && change.getNewBlobIdent().mode != null
				&& !change.getOldBlobIdent().mode.equals(change.getNewBlobIdent().mode)) {
			modeChange = Integer.toString(change.getOldBlobIdent().mode, 8) 
					+ " " + SpriteImage.getVersionedHref(IconScope.class, "arrow2") + " " 
					+ Integer.toString(change.getNewBlobIdent().mode, 8);
		} else {
			modeChange = null;
		}

		add(new Label("modeChange", modeChange).setEscapeModelStrings(false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DiffTitleResourceReference()));
	}

}
