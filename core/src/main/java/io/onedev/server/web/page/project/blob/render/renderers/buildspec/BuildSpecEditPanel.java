package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.nio.charset.Charset;

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

import io.onedev.server.build.BuildSpec;
import io.onedev.server.build.JobSpec;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
class BuildSpecEditPanel extends FormComponentPanel<byte[]> {

	private RepeatingView jobsView;
	
	private Serializable parseResult;
	
	public BuildSpecEditPanel(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		String buildSpecString = new String(getModelObject(), charset); 
		if (StringUtils.isNotBlank(buildSpecString)) {
			try {
				parseResult = (Serializable) VersionedDocument.fromXML(buildSpecString).toBean();
			} catch (Exception e) {
				parseResult = e;
			}
		} else {
			parseResult = new BuildSpec();
		}
	}

	private WebMarkupContainer newJobContainer(String componentId, JobSpec job) {
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
		if (parseResult instanceof BuildSpec) {
			fragment = new Fragment("content", "validFrag", this);
			BuildSpec buildSpec = (BuildSpec) parseResult;
			jobsView = new RepeatingView("jobs");
			fragment.add(jobsView);
			
			for (JobSpec job: buildSpec.getJobs()) {
				jobsView.add(newJobContainer(jobsView.newChildId(), job));
			}
			fragment.add(new AjaxLink<Void>("addJob") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					JobSpec job = new JobSpec();
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
		if (parseResult instanceof BuildSpec) {
			BuildSpec buildSpec = (BuildSpec) parseResult;
			buildSpec.getJobs().clear();
			for (Component child: jobsView) {
				BeanEditor jobEditor = (BeanEditor) child.get("editor");
				buildSpec.getJobs().add((JobSpec) jobEditor.getConvertedInput());
			}
			setConvertedInput(VersionedDocument.fromBean(buildSpec).toXML().getBytes(Charsets.UTF_8));
		} else {
			setConvertedInput(getModelObject());
		}
	}

}
