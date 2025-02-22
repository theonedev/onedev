package io.onedev.server.web.component.diff.difftitle;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import io.onedev.server.git.BlobChange;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.component.svg.SpriteImage;

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

		if (change.getOldBlobIdent().mode != null && change.getNewBlobIdent().mode != null
				&& !change.getOldBlobIdent().mode.equals(change.getNewBlobIdent().mode)) {
			String modeChange = Integer.toString(change.getOldBlobIdent().mode, 8) 
					+ " " + SpriteImage.getVersionedHref(IconScope.class, "arrow3") + " " 
					+ Integer.toString(change.getNewBlobIdent().mode, 8);
			add(new Label("modeChange", modeChange).setEscapeModelStrings(false));
		} else {
			add(new WebMarkupContainer("modeChange").setVisible(false));
		}

		boolean isLfs = change.getNewBlobIdent().path != null && change.getNewBlob().getLfsPointer() != null
				|| change.getOldBlobIdent().path != null && change.getOldBlob().getLfsPointer() != null;
		add(new WebMarkupContainer("lfsHint").setVisible(isLfs));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new DiffTitleResourceReference()));
	}

}
