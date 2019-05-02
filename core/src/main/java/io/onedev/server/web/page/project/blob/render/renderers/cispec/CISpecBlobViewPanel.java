package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;

import com.google.common.base.Throwables;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.job.Job;
import io.onedev.server.git.Blob;
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
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
		try {
			CISpec ciSpec = CISpec.parse(blob.getBytes());
			if (ciSpec != null) {
				Fragment validFrag = new Fragment("content", "validFrag", this);			
				if (!ciSpec.getJobs().isEmpty()) {
					Fragment hasJobsFrag = new Fragment("body", "hasJobsFrag", this);
					
					RepeatingView navsView = new RepeatingView("navs");
					RepeatingView jobsView = new RepeatingView("contents");
					for (Job job: ciSpec.getJobs()) {
						WebMarkupContainer nav = new WebMarkupContainer(navsView.newChildId());
						nav.add(new Label("jobName", job.getName()));
						nav.add(AttributeAppender.append("data-name", job.getName()));
						navsView.add(nav);
						jobsView.add(BeanContext.viewBean(jobsView.newChildId(), job));
					}
					hasJobsFrag.add(navsView);
					hasJobsFrag.add(jobsView);
					
					validFrag.add(hasJobsFrag);
				} else {
					validFrag.add(new Label("body", "No jobs defined"));
				}
				add(validFrag);
			} else {
				add(new Label("content", "CI spec not defined"));
			}
		} catch (Exception e) {
			Fragment invalidFrag = new Fragment("content", "invalidFrag", this);
			invalidFrag.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
			add(invalidFrag);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CISpecResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.ciSpec.onDomReady();"));
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
