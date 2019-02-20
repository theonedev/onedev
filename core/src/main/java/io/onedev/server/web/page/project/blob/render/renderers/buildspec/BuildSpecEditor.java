package io.onedev.server.web.page.project.blob.render.renderers.buildspec;

import java.nio.charset.Charset;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import io.onedev.server.build.BuildSpec;
import io.onedev.server.migration.VersionedDocument;
import io.onedev.server.util.ContentDetector;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.utils.StringUtils;

@SuppressWarnings("serial")
class BuildSpecEditor extends FormComponentPanel<byte[]> {

	private BeanEditor beanEditor;
	
	public BuildSpecEditor(String id, BlobRenderContext context, byte[] initialContent) {
		super(id, Model.of(initialContent));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Charset detectedCharset = ContentDetector.detectCharset(getModelObject());
		Charset charset = detectedCharset!=null?detectedCharset:Charset.defaultCharset();
		String buildSpecString = new String(getModelObject(), charset); 
		BuildSpec buildSpec = null;
		if (StringUtils.isNotBlank(buildSpecString)) {
			try {
				buildSpec = (BuildSpec) VersionedDocument.fromXML(buildSpecString).toBean();
			} catch (Exception e) {
				Fragment fragment = new Fragment("buildSpec", "invalidFrag", this);
				fragment.add(new MultilineLabel("errorMessage", Throwables.getStackTraceAsString(e)));
				add(fragment);
			}
		} else {
			buildSpec = new BuildSpec();
		}
		
		if (buildSpec != null)
			add(beanEditor = BeanContext.editBean("buildSpec", buildSpec));
	}

	@Override
	public void convertInput() {
		if (beanEditor != null)
			setConvertedInput(VersionedDocument.fromBean(beanEditor.getConvertedInput()).toXML().getBytes(Charsets.UTF_8));
		else
			setConvertedInput(getModelObject());
	}

}
