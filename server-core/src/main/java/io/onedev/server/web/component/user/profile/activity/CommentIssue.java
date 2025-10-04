package io.onedev.server.web.component.user.profile.activity;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.web.UrlService;

public class CommentIssue extends IssueActivity {

    private final Long commentId;

    public CommentIssue(IssueComment comment) {
        super(comment.getDate());
        this.commentId = comment.getId();
    }

    private IssueComment getComment() {
        return OneDev.getInstance(IssueCommentService.class).load(commentId);
    }
    
    @Override
    public Issue getIssue() {
        return getComment().getIssue();
    }
    
    @Override
    public Component render(String id) {
        var comment = getComment();
        var url = OneDev.getInstance(UrlService.class).urlFor(comment, false);
        var label = MessageFormat.format(_T("Commented on issue \"{0}\" ({1})"), "<a href=\"" + url + "\">" + comment.getIssue().getReference() + "</a>", HtmlEscape.escapeHtml5(comment.getIssue().getTitle()));
        return new Label(id, label).setEscapeModelStrings(false);
    }
}