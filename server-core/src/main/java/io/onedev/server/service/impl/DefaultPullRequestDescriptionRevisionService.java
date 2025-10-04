package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.PullRequestDescriptionRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.PullRequestDescriptionRevisionService;

@Singleton
public class DefaultPullRequestDescriptionRevisionService extends BaseEntityService<PullRequestDescriptionRevision>
        implements PullRequestDescriptionRevisionService {

    @Transactional
    @Override
    public void create(PullRequestDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 