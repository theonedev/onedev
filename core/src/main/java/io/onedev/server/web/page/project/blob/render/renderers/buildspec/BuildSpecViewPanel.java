package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;

import com.google.common.base.Throwables;

import io.onedev.server.build.BuildSpec;
import io.onedev.server.git.Blob;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class BuildSpecViewPanel extends BlobViewPanel {

	public BuildSpecViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		String buildSpecString = blob.getText().getContent();
		if (StringUtils.isNotBlank(buildSpecString)) {
			try {
				BuildSpec buildSpec = (BuildSpec) VersionedDocument.fromXML(buildSpecString).toBean();
				add(BeanContext.viewBean("buildSpec", buildSpec));
			} catch (Exception e) {
				Fragment fragment = new Fragment("buildSpec", "invalidFrag", this);
				fragment.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
				add(fragment);
			}
		} else {
			add(new Fragment("buildSpec", "emptyFrag", this));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new BuildSpecCssResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return true;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return true;
	}

}
