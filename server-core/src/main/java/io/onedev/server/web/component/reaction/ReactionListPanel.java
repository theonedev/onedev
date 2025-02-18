package io.onedev.server.web.component.reaction;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.web.behavior.dropdown.DropdownHoverBehavior;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;

public abstract class ReactionListPanel extends Panel {

    public static final Map<String, String> EMOJIS = new LinkedHashMap<String, String>() {{
        put("thumbs_up", "👍");
        put("thumbs_down", "👎");
        put("smile", "😄");
        put("tada", "🎉");
        put("confused", "😕");
        put("heart", "❤️");
        put("rocket", "🚀");
        put("eyes", "👀");
    }};

    public ReactionListPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new DropdownLink("addReaction") {

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(SecurityUtils.getUser() != null);
            }

            @Override
            protected Component newContent(String id, FloatingPanel dropdown) {
                var fragment = new Fragment(id, "emojiSelectFrag", ReactionListPanel.this);
                var emojisView = new RepeatingView("emojis");
                for (String emoji: EMOJIS.values()) {
                    var link = new AjaxLink<Void>(emojisView.newChildId()) {

                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            onToggleEmoji(target, emoji);
                            target.add(ReactionListPanel.this);
                        }
                    };
                    link.add(new Label("emoji", emoji));
                    emojisView.add(link);
                }
                fragment.add(emojisView);
                return fragment;                
            }

        });
        
        add(new ListView<Pair<String, List<User>>>("reactions", new LoadableDetachableModel<List<Pair<String, List<User>>>>() {

            @Override
            protected List<Pair<String, List<User>>> load() {
                return getReactions().stream().collect(groupingBy(EntityReaction::getEmoji)).entrySet().stream()
                        .map(entry -> new Pair<>(entry.getKey(), entry.getValue().stream().map(reaction->reaction.getUser()).collect(toList())))
                        .collect(toList());
            }

        }) {

            @Override
            protected void populateItem(ListItem<Pair<String, List<User>>> item) {
                var model = item.getModel();
                var emoji = model.getObject().getLeft();
                var link = new AjaxLink<Void>("link") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onToggleEmoji(target, emoji);
                        target.add(ReactionListPanel.this);
                    }

                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setEnabled(SecurityUtils.getUser() != null);
                    }

                };
                link.add(new Label("emoji", emoji));
                link.add(new Label("count", model.getObject().getRight().size()));
                link.add(new DropdownHoverBehavior(AlignPlacement.top(4)) {

                    @Override
                    protected Component newContent(String id) {
                        var fragment = new Fragment(id, "usersFrag", ReactionListPanel.this);
                        var users = model.getObject().getRight();
                        String label;
                        if (users.size() <= 5) {
                            label = users.stream()
                                    .map(User::getDisplayName)
                                    .collect(Collectors.joining(", "));
                        } else {
                            label = users.subList(0, 5).stream()
                                    .map(User::getDisplayName)
                                    .collect(Collectors.joining(", ")) 
                                    + " and " + (users.size()-5) + " more";
                        }
                        fragment.add(new Label("users", label));

                        return fragment;
                    }
                });
                item.add(link);
            }
        });
        setOutputMarkupId(true);
    }

    protected abstract Collection<? extends EntityReaction> getReactions();

    protected abstract void onToggleEmoji(AjaxRequestTarget target, String emoji);

}
