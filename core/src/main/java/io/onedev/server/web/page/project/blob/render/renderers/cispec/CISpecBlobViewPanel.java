package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.google.common.base.Throwables;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.git.Blob;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class CISpecBlobViewPanel extends BlobViewPanel {

	public CISpecBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		String ciSpecString = blob.getText().getContent();
		if (StringUtils.isNotBlank(ciSpecString)) {
			try {
				CISpec ciSpec = (CISpec) VersionedDocument.fromXML(ciSpecString).toBean();
				Fragment fragment = new Fragment("content", "validFrag", this);
				RepeatingView jobsView = new RepeatingView("jobs");
				for (Job job: ciSpec.getJobs()) {
					jobsView.add(BeanContext.viewBean(jobsView.newChildId(), job));
				}
				fragment.add(jobsView);
				add(fragment);
			} catch (Exception e) {
				Fragment fragment = new Fragment("content", "invalidFrag", this);
				fragment.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
				add(fragment);
			}
		} else {
			add(new Fragment("content", "emptyFrag", this));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CISpecCssResourceReference()));
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
