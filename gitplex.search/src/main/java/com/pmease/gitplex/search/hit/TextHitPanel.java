package com.pmease.gitplex.search.hit;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.lang.TokenPosition;

@SuppressWarnings("serial")
public class TextHitPanel extends Panel {

	private final TextHit hit;
	
	public TextHitPanel(String id, TextHit hit) {
		super(id);
		
		this.hit = hit;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<Segment>("segments", new LoadableDetachableModel<List<Segment>>() {

			@Override
			protected List<Segment> load() {
				List<Segment> segments = new ArrayList<>();
				
				String lineContent = hit.getLineContent();
				TokenPosition.Range range = hit.getTokenPos().getRange();
				segments.add(new Segment(lineContent.substring(0, range.getStart()), false));
				segments.add(new Segment(lineContent.substring(range.getStart(), range.getEnd()), true));
				segments.add(new Segment(lineContent.substring(range.getEnd()), false));
				return segments;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<Segment> item) {
				item.add(new Label("segment", item.getModelObject().text));
				if (item.getModelObject().matched)
					item.add(AttributeModifier.append("style", "background: #CECCF7;"));
			}
			
		});
	}

	private static class Segment {
		String text;
		
		boolean matched;
		
		Segment(String text, boolean matched) {
			this.text = text;
			this.matched = matched;
		}
	}
}
