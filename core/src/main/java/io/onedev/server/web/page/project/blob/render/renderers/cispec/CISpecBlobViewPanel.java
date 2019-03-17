package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;

import com.google.common.base.Throwables;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.git.Blob;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
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
				Fragment validFrag = new Fragment("content", "validFrag", this);			
				if (!ciSpec.getJobs().isEmpty()) {
					Fragment hasJobsFrag = new Fragment("body", "hasJobsFrag", this);
					List<Tab> tabs = new ArrayList<>();

					for (Job job: ciSpec.getJobs()) {
						tabs.add(new AjaxActionTab(Model.of(job.getName()) ) {

							@Override
							protected void onSelect(AjaxRequestTarget target, Component tabLink) {
								Component content = BeanContext.viewBean("content", job); 
								hasJobsFrag.replace(content.setOutputMarkupId(true));
								target.add(content);
							}
							
						});
					}
					
					hasJobsFrag.add(new Tabbable("navs", tabs));

					Component content = BeanContext.viewBean("content", ciSpec.getJobs().iterator().next()); 
					hasJobsFrag.add(content.setOutputMarkupId(true));
					validFrag.add(hasJobsFrag);
				} else {
					validFrag.add(new Label("body", "No jobs defined"));
				}
				add(validFrag);
			} catch (Exception e) {
				Fragment invalidFrag = new Fragment("content", "invalidFrag", this);
				invalidFrag.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
				add(invalidFrag);
			}
		} else {
			add(new Label("content", "Build spec not defined"));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CISpecResourceReference()));
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
