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
import com.pmease.gitop.core.hookcallback.PostReceiveServlet;
import com.pmease.gitop.core.hookcallback.PreReceiveServlet;
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
    
    private final ServerConfig serverConfig;
    
    private final String hookTemplate;
    
    @Inject
    public DefaultProjectManager(GeneralDao generalDao, StorageManager storageManager, ServerConfig serverConfig) {
        super(generalDao);

        this.storageManager = storageManager;
        this.serverConfig = serverConfig;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-hook-template")) {
        	Preconditions.checkNotNull(is);
            hookTemplate = StringUtils.join(IOUtils.readLines(is), "\n");
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

                File preReceiveHook = new File(hooksDir, "pre-receive");
                FileUtils.writeFile(preReceiveHook, 
                        String.format(hookTemplate, urlRoot + PreReceiveServlet.PATH + "/" + project.getId()));
                preReceiveHook.setExecutable(true);
                
                File postReceiveHook = new File(hooksDir, "post-receive");
                FileUtils.writeFile(postReceiveHook, 
                        String.format(hookTemplate, urlRoot + PostReceiveServlet.PATH + "/" + project.getId()));
                postReceiveHook.setExecutable(true);
            }
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
