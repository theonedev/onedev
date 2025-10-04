package io.onedev.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.onedev.server.model.IssueDescriptionRevision;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.service.IssueDescriptionRevisionService;

@Singleton
public class DefaultIssueDescriptionRevisionService extends BaseEntityService<IssueDescriptionRevision>
        implements IssueDescriptionRevisionService {

    @Transactional
    @Override
    public void create(IssueDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 