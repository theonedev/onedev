package io.onedev.server.web.component.comment;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.hibernate.proxy.HibernateProxyHelper;

import io.onedev.server.OneDev;
import io.onedev.server.model.support.CommentRevision;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;

public abstract class CommentHistoryLink extends MenuLink {

    public CommentHistoryLink(String id) {
        super(id);
    }

    @Override
    protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
        var menuItems = new ArrayList<MenuItem>();
        var revisions = new ArrayList<>(getCommentRevisions());
        revisions.sort(new Comparator<CommentRevision>() {
            @Override
            public int compare(CommentRevision o1, CommentRevision o2) {
                return o2.getDate().compareTo(o1.getDate());
            }
        });
        for (var revision : revisions) {
            var revisionClass = HibernateProxyHelper.getClassWithoutInitializingProxy(revision);
            var revisionId = revision.getId();
            menuItems.add(new MenuItem() {

                @Override
                public String getLabel() {
                    @SuppressWarnings("unchecked")
                    var revision = (CommentRevision) getDao().load(revisionClass, revisionId);
                    return MessageFormat.format(_T("{0} edited {1}"), revision.getUser().getDisplayName(), DateUtils.formatAge(revision.getDate()));
                }

                @Override
                public WebMarkupContainer newLink(String id) {
                    return new ModalLink(id) {

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            super.onClick(target);
                            dropdown.close();
                        }

                        @Override
                        protected Component newContent(String id, ModalPanel modal) {
                            return new CommentRevisionPanel(id) {

                                @Override
                                @SuppressWarnings("unchecked")
                                protected CommentRevision getCommentRevision() {
                                    return (CommentRevision) getDao().load(revisionClass, revisionId);
                                }

                                @Override
                                protected void onClose(AjaxRequestTarget target) {
                                    modal.close();
                                }

                            };
                        }

                    };
                }

            });
        }
        return menuItems;
    }

    private Dao getDao() {
        return OneDev.getInstance(Dao.class);
    }

    protected abstract Collection<? extends CommentRevision> getCommentRevisions();

}