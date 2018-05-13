package io.onedev.server.web.page.project.issues.issuedetail.changes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Splitter;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.page.project.issues.issuedetail.IssueDetailPage;

@SuppressWarnings("serial")
public class IssueChangesPage extends IssueDetailPage {

	public IssueChangesPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<IssueChange>("changes", new LoadableDetachableModel<List<IssueChange>>() {

			@Override
			protected List<IssueChange> load() {
				List<IssueChange> changes = new ArrayList<>(getIssue().getChanges());
				Collections.sort(changes, new Comparator<IssueChange>() {

					@Override
					public int compare(IssueChange o1, IssueChange o2) {
						return o1.getDate().compareTo(o2.getDate());
					}
					
				});
				return changes;
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<IssueChange> item) {
				IssueChange change = item.getModelObject();
				item.add(new AvatarLink("avatar", User.getForDisplay(change.getUser(), change.getUserName())));
				
				item.add(new UserLink("user", User.getForDisplay(change.getUser(), change.getUserName())));
				item.add(new Label("property", change.getProperty()));
				item.add(new Label("age", DateUtils.formatAge(change.getDate())));
				
				List<String> prevLines;
				if (change.getPrevContent() != null)
					prevLines = Splitter.on("\n").splitToList(change.getPrevContent());
				else
					prevLines = new ArrayList<>();
				
				List<String> lines;
				if (change.getContent() != null)
					lines = Splitter.on("\n").splitToList(change.getContent());
				else
					lines = new ArrayList<>();
				item.add(new PlainDiffPanel("body", prevLines, lines));
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueChangesResourceReference()));
	}

}
