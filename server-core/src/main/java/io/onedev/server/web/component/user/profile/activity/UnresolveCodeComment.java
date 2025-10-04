package io.onedev.server.web.component.user.profile.activity;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.web.UrlService;

public class UnresolveCodeComment extends CodeCommentActivity {

    private final Long commentId;

    public UnresolveCodeComment(Date date, CodeComment comment) {
        super(date);
        this.commentId = comment.getId();
    }

    @Override
    public CodeComment getComment() {
        return OneDev.getInstance(CodeCommentService.class).load(commentId);
    }
    
    @Override
    public Component render(String id) {
        var comment = getComment();
        var url = OneDev.getInstance(UrlService.class).urlFor(comment, false);
        var label = MessageFormat.format(_T("Unresolved comment on file \"{0}\" in project \"{1}\""), "<a href=\"" + url + "\">" + HtmlEscape.escapeHtml5(comment.getMark().getPath()) + "</a>", comment.getProject().getPath());
        return new Label(id, label).setEscapeModelStrings(false);
    }

}
