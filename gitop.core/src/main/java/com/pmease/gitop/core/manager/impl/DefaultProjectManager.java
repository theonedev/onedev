package com.pmease.gitop.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.hookcallback.GitPostReceiveCallback;
import com.pmease.gitop.core.hookcallback.GitUpdateCallback;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.setting.ServerConfig;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.storage.ProjectStorage;
import com.pmease.gitop.model.storage.StorageManager;

@Singleton
public class DefaultProjectManager extends AbstractGenericDao<Project> implements ProjectManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);

    private final StorageManager storageManager;

    private final BranchManager branchManager;

    private final ServerConfig serverConfig;
    
    private final String gitUpdateHookTemplate;
    
    private final String gitPostReceiveHookTemplate;
    
    @Inject
    public DefaultProjectManager(GeneralDao generalDao, StorageManager storageManager, 
    		BranchManager branchManager, ServerConfig serverConfig) {
        super(generalDao);

        this.storageManager = storageManager;
        this.branchManager = branchManager;
        this.serverConfig = serverConfig;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-update-hook-template")) {
        	Preconditions.checkNotNull(is);
            gitUpdateHookTemplate = StringUtils.join(IOUtils.readLines(is), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-postreceive-hook-template")) {
        	Preconditions.checkNotNull(is);
            gitPostReceiveHookTemplate = StringUtils.join(IOUtils.readLines(is), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    @Override
    public void save(Project project) {
        if (project.isNew()) {
            super.save(project);

            ProjectStorage storage = storageManager.getStorage(project);

            File codeDir = storage.ofCode();
            if (codeDir.exists() && !Project.isCode(new Git(codeDir))) {
            	logger.warn("Deleting existing directory '" + codeDir + "' before initializing project code repo...");
            	FileUtils.deleteDir(codeDir);
            }
            
            if (!codeDir.exists()) {
                FileUtils.createDir(codeDir);
                new Git(codeDir).init(true);
                File hooksDir = new File(codeDir, "hooks");
                String urlRoot;
                if (serverConfig.getHttpPort() != 0)
                    urlRoot = "http://localhost:" + serverConfig.getHttpPort();
                else 
                    urlRoot = "https://localhost:" + serverConfig.getSslConfig().getPort();

                File gitUpdateHook = new File(hooksDir, "update");
                FileUtils.writeFile(gitUpdateHook, 
                        String.format(gitUpdateHookTemplate, urlRoot + GitUpdateCallback.PATH + "/" + project.getId()));
                gitUpdateHook.setExecutable(true);
                
                File gitPostReceiveHook = new File(hooksDir, "post-receive");
                FileUtils.writeFile(gitPostReceiveHook, 
                        String.format(gitPostReceiveHookTemplate, urlRoot + GitPostReceiveCallback.PATH + "/" + project.getId()));
                gitPostReceiveHook.setExecutable(true);
            }
            
            branchManager.syncWithGit(project);
        } else {
            super.save(project);
        }
    }

    @Transactional
    @Override
    public void delete(Project entity) {
        super.delete(entity);

        storageManager.getStorage(entity).delete();
    }

    @Sessional
    @Override
    public Project findBy(String ownerName, String projectName) {
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.eq("name", projectName));
        criteria.createAlias("owner", "owner");
        criteria.add(Restrictions.eq("owner.name", ownerName));

        criteria.setMaxResults(1);
        return (Project) criteria.uniqueResult();
    }

    @Sessional
    @Override
    public Project findBy(User owner, String projectName) {
        return find(Restrictions.eq("owner.id", owner.getId()),
                Restrictions.eq("name", projectName));
    }

}
