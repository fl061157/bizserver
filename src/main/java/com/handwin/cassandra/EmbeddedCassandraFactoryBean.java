package com.handwin.cassandra;


import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * 内嵌式cassandra server，专门用于单元测试
 */
public class EmbeddedCassandraFactoryBean extends AbstractFactoryBean<PersistenceManager> {
    private static PersistenceManager manager;

    static {
        manager = CassandraEmbeddedServerBuilder
                .withEntityPackages("com.handwin")
                .withKeyspaceName("faceshow_test")
                .cleanDataFilesAtStartup(true)
                .withCQLPort(9260).withThriftPort(9340)
                .withDurableWrite(true)
                .buildPersistenceManager();
    }

    @Override
    public Class<?> getObjectType() {
        return PersistenceManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected PersistenceManager createInstance() throws Exception {
        return manager;
    }
}