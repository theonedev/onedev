package io.onedev.server.ee.pack.container;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PackBlobManager;
import io.onedev.server.util.Pair;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;

import static org.apache.commons.lang3.StringUtils.substringAfter;

public class ContainerPackPanel extends Panel {
	
	private final String serverAndNamespace;
	
	private final String tag;
	
	private final String manifestHash;
	
	private final IModel<ContainerData> dataModel = new LoadableDetachableModel<>() {
		@Override
		protected ContainerData load() {
			return new ContainerData(getPackBlobManager().readBlob(manifestHash));
		}

	};
	
	public ContainerPackPanel(String id, String serverAndNamespace, String tag, String manifestHash) {
		super(id);
		this.serverAndNamespace = serverAndNamespace;
		this.tag = tag;
		this.manifestHash = manifestHash;
	}
	
	private PackBlobManager getPackBlobManager() {
		return OneDev.getInstance(PackBlobManager.class);
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
		
		var digest = "sha256:" + manifestHash;
		add(new Label("digest", digest));
		
		var pullCommand = "docker pull " + serverAndNamespace + ":" + tag;
		add(new Label("pullCommand", pullCommand));
		add(new CopyToClipboardLink("copyPullCommand", Model.of(pullCommand)));
		
		var data = dataModel.getObject();
		if (data.isImageManifest()) {
			add(newImageManifestPanel("content", data.getManifest(), null));
		} else if (data.isImageIndex()) {
			var fragment = new Fragment("content", "imageIndexFrag", this);
			
			String firstArchDigest = null;
			var tabs = new ArrayList<Tab>();
			for (var manifestNode: data.getManifest().get("manifests")) {
				var platformNode = manifestNode.get("platform");
				if (platformNode != null) {
					var arch = platformNode.get("os").asText() + "/" + platformNode.get("architecture").asText();
					if (!arch.equals("unknown/unknown")) {
						var archDigest = manifestNode.get("digest").asText();
						firstArchDigest = archDigest;
						tabs.add(new AjaxActionTab(Model.of(arch)) {

							@Override
							protected void onSelect(AjaxRequestTarget target, Component tabLink) {
								Component content = newArchImageManifestPanel("content", archDigest);
								target.add(content);
								fragment.replace(content);
							}

						});
					}
				}
			}
			fragment.add(new Tabbable("tabs", tabs));
			
			if (firstArchDigest != null)
				fragment.add(newArchImageManifestPanel("content", firstArchDigest));
			else 
				fragment.add(new WebMarkupContainer("content").setVisible(false));
			
			add(fragment);
		} else {
			var fragment = new Fragment("content", "unknownManifestFrag", this);
			fragment.add(new Label("manifest", formatJson(data.getManifest())));
			add(fragment);
		}
		
		add(new InsecureRegistryNotePanel("insecureRegistryNote"));
	}
	
	private Component newArchImageManifestPanel(String componentId, String archDigest) {
		var archHash = substringAfter(archDigest, ":");
		var archManifestBytes = getPackBlobManager().readBlob(archHash);
		return newImageManifestPanel(componentId, readJson(archManifestBytes), archDigest);
	}
	
	private Component newImageManifestPanel(String componentId, JsonNode manifest, @Nullable String archDigest) {
		var fragment = new Fragment(componentId, "imageManifestFrag", this);
		var configHash = substringAfter(manifest.get("config").get("digest").asText(), ":");
		var config = readJson(getPackBlobManager().readBlob(configHash));
		if (archDigest != null) {
			fragment.add(new WebMarkupContainer("osArch").setVisible(false));
			var pullArchCommand = "docker pull " + serverAndNamespace + "@" + archDigest;
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
		dataModel.detach();
		super.onDetach();
	}
}
