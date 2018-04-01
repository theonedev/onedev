package io.onedev.server.web.component.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.util.Verification;
import io.onedev.server.util.Verification.Status;
import io.onedev.utils.Pair;

@SuppressWarnings("serial")
public class VerificationDetailPanel extends GenericPanel<Map<String, Verification>> {

	public VerificationDetailPanel(String id, IModel<Map<String, Verification>> model) {
		super(id, model);
		
		add(new ListView<Pair<String, Verification>>("verifications", new LoadableDetachableModel<List<Pair<String, Verification>>>() {

			@Override
			protected List<Pair<String, Verification>> load() {
				Map<String, Verification> map = VerificationDetailPanel.this.getModelObject();
				List<Pair<String, Verification>> list = new ArrayList<>();
				for (Map.Entry<String, Verification> entry: map.entrySet()) {
					list.add(new Pair<String, Verification>(entry.getKey(), entry.getValue()));
				}
				Collections.sort(list, new Comparator<Pair<String, Verification>>() {

					@Override
					public int compare(Pair<String, Verification> o1, Pair<String, Verification> o2) {
						return o1.getSecond().getDate().compareTo(o2.getSecond().getDate());
					}
					
				});
				return list;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Pair<String, Verification>> item) {
				Verification verification = item.getModelObject().getSecond();

				WebMarkupContainer result = new WebMarkupContainer("result");
				item.add(result);
				
				if (verification.getStatus() == Status.SUCCESS) {
					result.add(AttributeAppender.append("class", "success fa fa-check"));
					result.add(AttributeAppender.append("title", "Verification is successful"));
				} else if (verification.getStatus() == Status.ERROR) {
					result.add(AttributeAppender.append("class", "error fa fa-warning"));
					result.add(AttributeAppender.append("title", "Verification is in error"));
				} else if (verification.getStatus() == Status.FAILURE) {
					result.add(AttributeAppender.append("class", "failure fa fa-times"));
					result.add(AttributeAppender.append("title", "Verification is failed"));
				} else {
					result.add(AttributeAppender.append("class", "running fa fa-circle"));
					result.add(AttributeAppender.append("title", "Verification is running"));
				}
				
				item.add(new Label("name", item.getModelObject().getFirst()));
				
				if (verification.getDescription() != null) {
					if (verification.getTargetUrl() != null) {
						Fragment fragment = new Fragment("content", "linkFrag", VerificationDetailPanel.this);
						ExternalLink link = new ExternalLink("link", verification.getTargetUrl());
						link.add(new Label("label", verification.getDescription())); 
						fragment.add(link);
						item.add(fragment);
					} else {
						item.add(new Label("content", verification.getDescription()));
					}
				} else {
					item.add(new WebMarkupContainer("content").setVisible(false));
				}
			}
			
		});
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new VerificationResourceReference()));
	}
	
}
