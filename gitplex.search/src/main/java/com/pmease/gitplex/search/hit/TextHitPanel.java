package com.pmease.gitplex.search.hit;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitplex.search.hit.TextHit.Range;

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
				
				String line = hit.getLine();
				List<TextHit.Range> ranges = hit.getRanges(); 
				segments.add(new Segment(line.substring(0, ranges.get(0).getStart()), false));
				for (int i=0; i<ranges.size(); i++) {
					Range range = ranges.get(i);
					segments.add(new Segment(line.substring(range.getStart(), range.getEnd()), true));
					if (i+1<ranges.size())
						segments.add(new Segment(line.substring(range.getEnd(), ranges.get(i+1).getStart()), false));
					else
						segments.add(new Segment(line.substring(range.getEnd()), false));
				}
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
