package io.onedev.server.plugin.pack.maven;

import static io.onedev.server.plugin.pack.maven.MavenPackHandler.FILE_METADATA;
import static io.onedev.server.plugin.pack.maven.MavenPackHandler.NONE;
import static io.onedev.server.util.GroovyUtils.evalTemplate;
import static io.onedev.server.web.translation.Translation._T;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unbescape.html.HtmlEscape;

import com.google.common.io.Resources;

import io.onedev.server.OneDev;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.codesnippet.CodeSnippetPanel;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

public class MavenPackPanel extends GenericPanel<Pack> {
	
	private transient Map<String, PackBlob> packBlobs;
	
	public MavenPackPanel(String id, IModel<Pack> model) {
		super(id, model);
	}

	private byte[] readBlob(PackBlob packBlob) {
		var baos = new ByteArrayOutputStream();
		getPackBlobService().downloadBlob(packBlob.getProject().getId(), packBlob.getSha256Hash(), baos);
		return baos.toByteArray();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (getPack().getName().endsWith(NONE) && getPack().getVersion().equals(NONE)) {
			var packBlob = getPackBlobs().get(FILE_METADATA);
			if (packBlob != null) {
				var bytes = readBlob(packBlob);
				add(new CodeSnippetPanel("content", Model.of(new String(bytes, UTF_8))));
			} else {
				add(new Label("content", _T("Plugin metadata not found"))
						.add(AttributeAppender.append("class", "alert alert-notice alert-light-warning")));
			}
		} else {
			var artifactFrag = new Fragment("content", "artifactFrag", this);
			
			Element pomElement = null;
			var fileNames = new ArrayList<>(getPackBlobs().keySet());
			Collections.sort(fileNames);
			Collections.reverse(fileNames);
			for (var fileName: fileNames) {
				if (fileName.endsWith(".pom")) {
					var packBlob = getPackBlobs().get(fileName);
					var baos = new ByteArrayOutputStream();
					getPackBlobService().downloadBlob(packBlob.getProject().getId(),
							packBlob.getSha256Hash(), baos);
					try {
						pomElement = new SAXReader().read(new ByteArrayInputStream(baos.toByteArray())).getRootElement();
					} catch (DocumentException e) {
						throw new RuntimeException(e);
					}
					break;
				}
			}

			if (pomElement != null) {
				var packaging = pomElement.elementText("packaging");
				if (packaging == null)
					packaging = "jar";
				var description = "<b>Packaging: " + packaging + "</b>";				
				var descriptionElement = pomElement.element("description");
				if (descriptionElement != null)
					description += "<br><br>" + HtmlEscape.escapeHtml5(descriptionElement.getText());
				artifactFrag.add(new Label("description", description).setEscapeModelStrings(false));
				
				var bindings = new HashMap<String, Object>();
				bindings.put("groupId", substringBefore(getPack().getName(), ":"));
				bindings.put("artifactId", substringAfter(getPack().getName(), ":"));
				bindings.put("version", getPack().getVersion());
				var serverUrl = OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl();
				bindings.put("url", serverUrl + "/" + getPack().getProject().getPath() + "/~" + MavenPackHandler.HANDLER_ID);
				bindings.put("permission", "read");

				if (packaging.equals("jar") || packaging.equals("maven-plugin") || packaging.equals("pom")) {
					var usageFrag = new Fragment("usage", "usageFrag", this);
					
					URL tplUrl;
					if (packaging.equals("jar"))
						tplUrl = Resources.getResource(MavenPackPanel.class, "dependency.tpl");
					else if (packaging.equals("pom"))
						tplUrl = Resources.getResource(MavenPackPanel.class, "parent.tpl");
					else
						tplUrl = Resources.getResource(MavenPackPanel.class, "plugin.tpl");

					try {
						var template = Resources.toString(tplUrl, UTF_8);
						usageFrag.add(new CodeSnippetPanel("pom", Model.of(evalTemplate(template, bindings))));
						usageFrag.add(new CodeSnippetPanel("settings", Model.of(evalTemplate(MavenPackSupport.getServersAndMirrorsTemplate(), bindings))));
						usageFrag.add(new CodeSnippetPanel("jobCommands", Model.of(evalTemplate(MavenPackSupport.getJobCommandsTemplate(), bindings))));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					artifactFrag.add(usageFrag);
				} else {
					artifactFrag.add(new WebMarkupContainer("usage").setVisible(false));
				}
				add(artifactFrag);
			} else {
				artifactFrag.add(new WebMarkupContainer("description").setVisible(false));
				artifactFrag.add(new WebMarkupContainer("usage").setVisible(false));
			}

			List<IColumn<String, Void>> columns = new ArrayList<>();

			columns.add(new AbstractColumn<>(Model.of(_T("Published File"))) {

				@Override
				public void populateItem(Item<ICellPopulator<String>> cellItem,
										 String componentId, IModel<String> rowModel) {
					Fragment fragment = new Fragment(componentId, "fileFrag", MavenPackPanel.this);
					var fileName = rowModel.getObject();

					var link = new Link<Void>("link") {

						@Override
						public void onClick() {
							AbstractResourceStreamWriter stream = new AbstractResourceStreamWriter() {
								@Override
								public String getContentType() {
									return MediaType.APPLICATION_OCTET_STREAM;
								}

								@Override
								public Bytes length() {
									return Bytes.bytes(getPackBlobs().get(fileName).getSize());
								}

								@Override
								public void write(OutputStream os) {
									var packBlob = getPackBlobs().get(fileName);
									getPackBlobService().downloadBlob(packBlob.getProject().getId(),
											packBlob.getSha256Hash(), os);
								}

							};

							ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream, fileName);
							getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
						}
					};
					link.add(new Label("label", fileName));
					fragment.add(link);
					cellItem.add(fragment);
				}

			});

			columns.add(new AbstractColumn<>(Model.of(_T("Size"))) {

				public void populateItem(Item<ICellPopulator<String>> cellItem,
										 String componentId, IModel<String> rowModel) {
					var size = getPackBlobs().get(rowModel.getObject()).getSize();
					cellItem.add(new Label(componentId, FileUtils.byteCountToDisplaySize(size)));
				}

			});

			var dataProvider = new LoadableDetachableDataProvider<String, Void>() {

				@Override
				public Iterator<? extends String> iterator(long first, long count) {
					var files = new ArrayList<>(getPackBlobs().keySet());
					Collections.sort(files);
					return files.subList((int) first, (int) Math.min(files.size(), first + count)).iterator();
				}

				@Override
				public long calcSize() {
					return getPackBlobs().size();
				}

				@Override
				public IModel<String> model(String object) {
					return Model.of(object);
				}

			};

			artifactFrag.add(new DefaultDataTable<>("files", columns, dataProvider,
					WebConstants.PAGE_SIZE, null));				
			
			add(artifactFrag);
		}
	}
	
	private Pack getPack() {
		return getModelObject();
	}
	
	private Map<String, PackBlob> getPackBlobs() {
		if (packBlobs == null) {
			packBlobs = new HashMap<>();
			var pack = getPack();
			MavenData data = (MavenData) pack.getData();
			for (var entry : data.getSha256BlobHashes().entrySet()) {
				var packBlob = pack.getBlobReferences().stream()
						.map(PackBlobReference::getPackBlob)
						.filter(it -> it.getSha256Hash().equals(entry.getValue()))
						.findFirst();
				packBlob.ifPresent(blob -> packBlobs.put(entry.getKey(), blob));
			}
		}
		return packBlobs;
	}

	private PackBlobService getPackBlobService() {
		return OneDev.getInstance(PackBlobService.class);
	}
	
}
