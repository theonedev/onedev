package io.onedev.server.plugin.pack.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.util.Pair;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import org.jspecify.annotations.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.apache.commons.lang3.StringUtils.substringAfter;

public class ContainerPackPanel extends Panel {
	
	private final Long projectId;
	
	private final String namespace;
	
	private final String tag;
	
	private final String manifestSha256Hash;
	
	private final IModel<ContainerManifest> manifestIModel = new LoadableDetachableModel<>() {
		@Override
		protected ContainerManifest load() {
			return new ContainerManifest(getPackBlobService().readBlob(projectId, manifestSha256Hash));
		}

	};
	
	public ContainerPackPanel(String id, Long projectId, String namespace, String tag, String manifestSha256Hash) {
		super(id);
		this.projectId = projectId;
		this.namespace = namespace;
		this.tag = tag;
		this.manifestSha256Hash = manifestSha256Hash;
	}
	
	private PackBlobService getPackBlobService() {
		return OneDev.getInstance(PackBlobService.class);
	}

	private String formatJson(JsonNode jsonNode) {
		try {
			return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private JsonNode readJson(byte[] bytes) {
		try {
			return getObjectMapper().readTree(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var digest = "sha256:" + manifestSha256Hash;
		add(new Label("digest", digest));
		
		var data = manifestIModel.getObject();
		if (data.isImageManifest()) {
			add(newImagePanel("content", data.getJson(), null));
		} else if (data.isImageIndex()) {
			var archDigests = new LinkedHashMap<String, String>();
			for (var manifestNode: data.getJson().get("manifests")) {
				var platformNode = manifestNode.get("platform");
				if (platformNode != null) {
					var arch = platformNode.get("os").asText() + "/" + platformNode.get("architecture").asText();
					if (!arch.equals("unknown/unknown")) 
						archDigests.put(arch, manifestNode.get("digest").asText());
				}
			}
			if (archDigests.size() > 1) {
				var fragment = new Fragment("content", "multiArchFrag", this);
				var pullCommand = "docker pull " + namespace + ":" + tag;
				fragment.add(new Label("pullCommand", pullCommand));
				fragment.add(new CopyToClipboardLink("copyPullCommand", Model.of(pullCommand)));
				var tabs = new ArrayList<Tab>();
				for (var entry : archDigests.entrySet()) {
					var archDigest = entry.getValue();
					tabs.add(new AjaxActionTab(Model.of(entry.getKey())) {

						@Override
						protected void onSelect(AjaxRequestTarget target, Component tabLink) {
							Component content = newArchImagePanel("content", archDigest);
							target.add(content);
							fragment.replace(content);
						}

					});
				}
				fragment.add(new Tabbable("tabs", tabs));
				fragment.add(newArchImagePanel("content", archDigests.values().iterator().next()));
				add(fragment);
			} else if (archDigests.size() == 1) {
				var archHash = substringAfter(archDigests.values().iterator().next(), ":");
				var archManifestBytes = getPackBlobService().readBlob(projectId, archHash);
				add(newImagePanel("content", readJson(archManifestBytes), null));
			} else {
				boolean cache = false;
				for (var manifestNode: data.getJson().get("manifests")) {
					if (manifestNode.get("mediaType").asText().startsWith("application/vnd.buildkit.cacheconfig")) {
						cache = true;
						break;
					}
				}	
				
				if (cache) {
					var fragment = new Fragment("content", "cacheFrag", this);
					var cacheFromOption = "--cache-from type=registry,ref=" + namespace + ":" + tag;
					fragment.add(new Label("cacheFromOption", cacheFromOption));
					fragment.add(new CopyToClipboardLink("copyCacheFromOption", Model.of(cacheFromOption)));
					fragment.add(new Label("manifest", formatJson(data.getJson())));
					add(fragment);
				} else {
					var fragment = new Fragment("content", "manifestFrag", this);
					fragment.add(new Label("manifest", formatJson(data.getJson())));
					add(fragment);
				}
			}
		} else {
			var fragment = new Fragment("content", "manifestFrag", this);
			fragment.add(new Label("manifest", formatJson(data.getJson())));
			add(fragment);
		}

		add(new InsecureRegistryNotePanel("insecureRegistryNote"));
	}
	
	private Component newArchImagePanel(String componentId, String archDigest) {
		var archHash = substringAfter(archDigest, ":");
		var archManifestBytes = getPackBlobService().readBlob(projectId, archHash);
		return newImagePanel(componentId, readJson(archManifestBytes), archDigest);
	}
	
	private Component newImagePanel(String componentId, JsonNode manifest, @Nullable String archDigest) {
		var fragment = new Fragment(componentId, "imageFrag", this);
		var pullCommand = "docker pull " + namespace + ":" + tag;
		fragment.add(new Label("pullCommand", pullCommand));
		fragment.add(new CopyToClipboardLink("copyPullCommand", Model.of(pullCommand)));
		
		var configHash = substringAfter(manifest.get("config").get("digest").asText(), ":");
		var config = readJson(getPackBlobService().readBlob(projectId, configHash));
		if (archDigest != null) {
			fragment.add(new WebMarkupContainer("osArch").setVisible(false));
			var pullArchCommand = "docker pull " + namespace + "@" + archDigest;
			fragment.add(new Label("pullArchCommand", pullArchCommand));
			fragment.add(new CopyToClipboardLink("copyPullArchCommand", Model.of(pullArchCommand)));
		} else {
			var osNode = config.get("os");
			var architectureNode = config.get("architecture");
			if (osNode != null && architectureNode != null) 
				fragment.add(new Label("osArch", osNode.asText() + "/" + architectureNode.asText()));
			else
				fragment.add(new WebMarkupContainer("osArch").setVisible(false));
			fragment.add(new WebMarkupContainer("pullArchCommand").setVisible(false));
			fragment.add(new WebMarkupContainer("copyPullArchCommand").setVisible(false));
		}

		var imageSize = 0;
		for (var layerNode: manifest.get("layers")) {
			var sizeNode = layerNode.get("size");
			if (sizeNode != null)
				imageSize += sizeNode.asLong();
		}
		fragment.add(new Label("imageSize", FileUtils.byteCountToDisplaySize(imageSize)));
		
		fragment.add(new Label("manifest", formatJson(manifest)));
		
		var labels = new ArrayList<Pair<String, String>>();
		var labelsNode = config.get("config").get("Labels");
		if (labelsNode != null) {
			for (var it = labelsNode.fields(); it.hasNext();) {
				var entry = it.next();
				labels.add(new Pair<>(entry.getKey(), entry.getValue().asText()));
			}
		}

		fragment.add(new WebMarkupContainer("labels") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new ListView<>("labels", labels) {

					@Override
					protected void populateItem(ListItem<Pair<String, String>> item) {
						var label = item.getModelObject();
						item.add(new Label("name", label.getLeft()));
						item.add(new Label("value", label.getRight()));
					}
				});
			}
		}.setVisible(!labels.isEmpty()));

		fragment.setOutputMarkupId(true);
		
		return fragment;
	}
	
	private ObjectMapper getObjectMapper() {
		return OneDev.getInstance(ObjectMapper.class);
	}

	@Override
	protected void onDetach() {
		manifestIModel.detach();
		super.onDetach();
	}
}
