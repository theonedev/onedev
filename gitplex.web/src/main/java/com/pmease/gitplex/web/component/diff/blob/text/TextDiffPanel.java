package com.pmease.gitplex.web.component.diff.blob.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.git.BlobChange;
import com.pmease.commons.lang.diff.DiffBlock;
import com.pmease.commons.lang.diff.DiffMatchPatch.Operation;
import com.pmease.commons.lang.tokenizers.CmToken;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.diff.diffstat.DiffStatBar;
import com.pmease.gitplex.web.component.diff.difftitle.BlobDiffTitle;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class TextDiffPanel extends Panel {

	public static final int MAX_DISPLAY_SIZE = 5000;
	
	private static final int DEFAULT_CONTEXT_SIZE = 5;
	
	private final IModel<Repository> repoModel;
	
	private final BlobChange change;
	
	private final Map<Integer, Integer> contextSizes = new HashMap<>();
	
	private final boolean unified;
	
	private final InlineCommentSupport commentSupport;
	
	public TextDiffPanel(String id, IModel<Repository> repoModel, BlobChange change, boolean unified, 
			@Nullable InlineCommentSupport commentSupport) {
		super(id);
		
		this.repoModel = repoModel;
		this.change = change;
		this.unified = unified;
		this.commentSupport = commentSupport;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new DiffStatBar("diffStat", change.getAdditions(), change.getDeletions(), true));
		add(new BlobDiffTitle("title", change));
		
		PageParameters params = RepoFilePage.paramsOf(repoModel.getObject(), change.getBlobIdent());
		add(new BookmarkablePageLink<Void>("viewFile", RepoFilePage.class, params)
				.add(AttributeAppender.append("title", "View file at commit " + change.getBlobIdent().revision)));
		
		add(new Label("diffLines", renderDiffs()));
	}
	
	private String renderDiffs() {
		int contextSize = DEFAULT_CONTEXT_SIZE;
		StringBuilder builder = new StringBuilder("<tr class='diff-line'>");
		for (int i=0; i<change.getDiffBlocks().size(); i++) {
			DiffBlock block = change.getDiffBlocks().get(i);
			if (block.getOperation() == Operation.EQUAL) {
				if (i == 0) {
					int start = block.getLines().size()-contextSize;
					if (start < 0)
						start=0;
					for (int j=start; j<block.getLines().size(); j++) 
						appendEqual(builder, block.getLines().get(j));
				} else if (i == change.getDiffBlocks().size()-1) {
					int end = block.getLines().size();
					if (end > contextSize)
						end = contextSize;
					for (int j=0; j<end; j++)
						appendEqual(builder, block.getLines().get(j));
				} else if (2*contextSize < block.getLines().size()) {
					for (int j=0; j<contextSize; j++)
						appendEqual(builder, block.getLines().get(j));
					for (int j=block.getLines().size()-1; j>=block.getLines().size()-contextSize; j--)
						appendEqual(builder, block.getLines().get(j));
				} else {
					for (int j=0; j<block.getLines().size(); j++)
						appendEqual(builder, block.getLines().get(j));
				}
			} else if (block.getOperation() == Operation.INSERT) {
				for (int j=0; j<block.getLines().size(); j++) {
					
				}
			} else {
				for (int j=0; j<block.getLines().size(); j++) {
					
				}
			}
		}
		builder.append("</tr>");
		return builder.toString();
	}

	private void appendEqual(StringBuilder builder, List<CmToken> line) {
		
	}
	
	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}
