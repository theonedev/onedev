package io.onedev.server.web.page.project.blob.render.renderers.cispec;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
public class CISpecEditPanel extends FormComponentPanel<byte[]> {

	private RepeatingView jobsView;
	
	private Serializable parseResult;
	
	public CISpecEditPanel(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		String ciSpecString = new String(getModelObject(), charset); 
		if (StringUtils.isNotBlank(ciSpecString)) {
			try {
				parseResult = (Serializable) VersionedDocument.fromXML(ciSpecString).toBean();
			} catch (Exception e) {
				parseResult = e;
			}
		} else {
			parseResult = new CISpec();
		}
	}

	private WebMarkupContainer newJobContainer(String componentId, Job job) {
		WebMarkupContainer container = new WebMarkupContainer(jobsView.newChildId());
		container.add(BeanContext.editBean("editor", job));
		container.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavaScript($(container).chain("remove").get());
				jobsView.remove(container);
			}
			
		});
		container.setOutputMarkupId(true);
		return container;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment fragment;
		if (parseResult instanceof CISpec) {
			fragment = new Fragment("content", "validFrag", this);
			CISpec ciSpec = (CISpec) parseResult;
			jobsView = new RepeatingView("jobs");
			fragment.add(jobsView);
			
			for (Job job: ciSpec.getJobs()) {
				jobsView.add(newJobContainer(jobsView.newChildId(), job));
			}
			fragment.add(new AjaxLink<Void>("addJob") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Job job = new Job();
					WebMarkupContainer jobContainer = newJobContainer(jobsView.newChildId(), job);
					jobsView.add(jobContainer);
					String script = String.format("$(\"#%s\").before(\"<div id='%s'></div>\");", 
							getMarkupId(), jobContainer.getMarkupId());
					target.prependJavaScript(script);
					target.add(jobContainer);
				}
				
			}.setOutputMarkupId(true));
			
		} else {
			fragment = new Fragment("content", "invalidFrag", this);
			fragment.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString((Throwable) parseResult)));
		}
		add(fragment);
	}

	@Override
	public void convertInput() {
		CISpec editingCISpec = getCISpec();
		if (editingCISpec != null) {
			setConvertedInput(VersionedDocument.fromBean(editingCISpec).toXML().getBytes(Charsets.UTF_8));
		} else {
			setConvertedInput(getModelObject());
		}
	}

	@Nullable
	public CISpec getCISpec() {
		if (parseResult instanceof CISpec) {
			CISpec ciSpec = new CISpec();
			for (Component child: jobsView) {
				BeanEditor jobEditor = (BeanEditor) child.get("editor");
				ciSpec.getJobs().add((Job) jobEditor.getConvertedInput());
			}
			return ciSpec;
		} else {
			return null;
		}
	}
	
}
