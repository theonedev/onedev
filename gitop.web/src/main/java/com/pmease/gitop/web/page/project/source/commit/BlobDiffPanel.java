package com.pmease.gitop.web.page.project.source.commit;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.diff.DiffEntry.Side;

import com.pmease.gitop.web.page.project.source.commit.patch.FileHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader.AnnotatedLine;
import com.pmease.gitop.web.page.project.source.commit.patch.HunkHeader.LineType;

@SuppressWarnings("serial")
public class BlobDiffPanel extends Panel {

	public BlobDiffPanel(String id, IModel<FileHeader> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<HunkHeader>("hunk", new LoadableDetachableModel<List<? extends HunkHeader>>() {

			@Override
			protected List<? extends HunkHeader> load() {
				return getFile().getHunks();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<HunkHeader> item) {
				HunkHeader hunk = item.getModelObject();
				item.add(new Label("head", 
						"@@ -" + hunk.getOldImage().getStartLine() 
						+ "," + hunk.getOldImage().getLineCount() 
						+ " +" + hunk.getNewStartLine() 
						+ ", " + hunk.getNewLineCount() + " @@"));
				
				item.add(createLines("lines", item.getModel()));
			}
		});
		
		add(new Label("path", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getFile().getPath(Side.NEW);
			}
		}));
	}
	
	private Component createLines(String id, final IModel<HunkHeader> hunkModel) {
		
		return new ListView<AnnotatedLine>(id, new LoadableDetachableModel<List<AnnotatedLine>>() {

			@Override
			protected List<AnnotatedLine> load() {
				return hunkModel.getObject().getAnnotatedLines();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<AnnotatedLine> item) {
				AnnotatedLine line = item.getModelObject();
				String oldLineNo, newLineNo;
				oldLineNo = line.getOldLineNo() > 0 ? "" + line.getOldLineNo() : "";
				newLineNo = line.getNewLineNo() > 0 ? "" + line.getNewLineNo() : "";
				
				if (line.getLineType() == LineType.OLD) {
					oldLineNo = "- " + oldLineNo;
				} else if (line.getLineType() == LineType.NEW) {
					newLineNo = "+ " + newLineNo;
				}
				
				item.add(new Label("oldLineNo", oldLineNo));
				item.add(new Label("newLineNo", newLineNo));
				item.add(new Label("code", line.getText()));
				item.add(AttributeAppender.append("class", line.getLineType().name().toLowerCase()));
			}
		};
	}
	
	private FileHeader getFile() {
		return (FileHeader) getDefaultModelObject();
	}
}
