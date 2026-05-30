package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestReaction;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestReactionService;

@Singleton
public class DefaultPullRequestReactionService extends BaseEntityService<PullRequestReaction>
        implements PullRequestReactionService {

    @Transactional
    @Override
    public void create(PullRequestReaction reaction) {
        Preconditions.checkState(reaction.isNew());
        dao.persist(reaction);
    }

    @Transactional
    @Override
    public void toggleEmoji(User user, PullRequest request, String emoji) {
        var reaction = request.getReactions().stream()
                .filter(r -> r.getUser().equals(user) && r.getEmoji().equals(emoji))
                .findFirst()
                .orElse(null);

        switch (emoji) {
            case "👍":
                request.setThumbsUpCount(request.getThumbsUpCount() + (reaction == null ? 1 : -1));
                break;
            case "👎":
                request.setThumbsDownCount(request.getThumbsDownCount() + (reaction == null ? 1 : -1));
                break;
            case "😄":
                request.setSmileCount(request.getSmileCount() + (reaction == null ? 1 : -1));
                break;
            case "🎉":
                request.setTadaCount(request.getTadaCount() + (reaction == null ? 1 : -1));
                break;
            case "😕":
                request.setConfusedCount(request.getConfusedCount() + (reaction == null ? 1 : -1));
                break;
            case "❤️":
                request.setHeartCount(request.getHeartCount() + (reaction == null ? 1 : -1));
                break;
            case "👀":
                request.setEyesCount(request.getEyesCount() + (reaction == null ? 1 : -1));
                break;
            case "✅":
                request.setTickCount(request.getTickCount() + (reaction == null ? 1 : -1));
                break;
        }
        
        if (reaction == null) {
            reaction = new PullRequestReaction();
            reaction.setUser(user);
            reaction.setRequest(request);
            reaction.setEmoji(emoji);
            create(reaction);
            request.getReactions().add(reaction);
        } else {
            request.getReactions().remove(reaction);
            dao.remove(reaction);
        }
    }
} 