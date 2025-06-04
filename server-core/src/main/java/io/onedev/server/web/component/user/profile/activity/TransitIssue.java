package io.onedev.server.web.component.user.profile.activity;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.model.Issue;
import io.onedev.server.web.UrlManager;

public class TransitIssue extends IssueActivity {

    private final Long issueId;

    private final String state;

    public TransitIssue(Date date, Issue issue, String state) {
        super(date);
        this.issueId = issue.getId();
        this.state = state;
    }

    @Override
    public Issue getIssue() {
        return OneDev.getInstance(IssueManager.class).load(issueId);
    }
    
    public String getState() {
        return state;
    }
    
    @Override
    public Component render(String id) {
        var issue = getIssue();
        var url = OneDev.getInstance(UrlManager.class).urlFor(issue, false);
        var label = MessageFormat.format(_T("Transited state of issue \"{0}\" to \"{1}\" ({2})"), "<a href=\"" + url + "\">" + issue.getReference() + "</a>", state, HtmlEscape.escapeHtml5(issue.getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }
}