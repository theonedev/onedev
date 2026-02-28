package io.onedev.server.web.component.fileview;

import static io.onedev.server.web.translation.Translation._T;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import io.onedev.server.util.ContentDetector;
import io.onedev.server.util.FileData;

public class FileViewPanel extends GenericPanel<FileData> {

	private static final long serialVersionUID = 1L;

	private String fileContent;

	public FileViewPanel(String id, IModel<FileData> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		FileData fileData = getModelObject();

		if (fileData.isPartial()) {
			add(new Label("notice", _T("File is too large to be displayed")));
			add(new WebMarkupContainer("code").setVisible(false));
		} else if (ContentDetector.isBinary(fileData.getContent(), fileData.getName())) {
			add(new Label("notice", _T("File is binary")));
			add(new WebMarkupContainer("code").setVisible(false));
		} else {
			add(new WebMarkupContainer("notice").setVisible(false));
			fileContent = ContentDetector.convertToText(fileData.getContent(), fileData.getName());
			if (fileContent == null)
				fileContent = new String(fileData.getContent());
			add(new WebMarkupContainer("code"));
		}

		setOutputMarkupId(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		FileData fileData = getModelObject();
		if (!fileData.isPartial() && !ContentDetector.isBinary(fileData.getContent(), fileData.getName())) {
			response.render(JavaScriptHeaderItem.forReference(new FileViewResourceReference()));
			String script = String.format("onedev.server.fileView.onDomReady('%s', '%s', '%s');",
					getMarkupId(), escapeJavaScript(fileData.getName()),
					escapeJavaScript(fileContent));
			response.render(OnDomReadyHeaderItem.forScript(script));
		}
	}

}
