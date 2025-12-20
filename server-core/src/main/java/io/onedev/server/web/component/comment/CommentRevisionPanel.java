package io.onedev.server.web.component.comment;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.base.Splitter;

import io.onedev.server.model.support.CommentRevision;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.asset.textdiff.TextDiffResourceReference;
import io.onedev.server.web.component.diff.text.PlainTextDiffPanel;

abstract class CommentRevisionPanel extends Panel {

    public CommentRevisionPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        var revision = getCommentRevision();

        add(new Label("title", MessageFormat.format(_T("Edited by {0} {1}"), revision.getUser().getDisplayName(), DateUtils.formatAge(revision.getDate()))));

        List<String> oldLines;
        if (revision.getOldContent() != null) 
            oldLines = Splitter.on("\n").splitToList(revision.getOldContent());
        else
            oldLines = new ArrayList<>();
        List<String> newLines;
        if (revision.getNewContent() != null) 
            newLines = Splitter.on("\n").splitToList(revision.getNewContent());
        else
            newLines = new ArrayList<>();

        add(new PlainTextDiffPanel("content", oldLines, newLines));

        add(new AjaxLink<Void>("close") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                onClose(target);
            }
            
        });

        add(new AjaxLink<Void>("ok") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                onClose(target);
            }
            
        });

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(new TextDiffResourceReference()));
    }

    protected abstract CommentRevision getCommentRevision();

    protected abstract void onClose(AjaxRequestTarget target);

}
