package com.handwin.cassandra;

import static info.archinnov.achilles.configuration.ConfigurationParameters.BEAN_VALIDATION_ENABLE;
import static info.archinnov.achilles.configuration.ConfigurationParameters.BEAN_VALIDATION_VALIDATOR;
import static info.archinnov.achilles.configuration.ConfigurationParameters.CONSISTENCY_LEVEL_READ_DEFAULT;
import static info.archinnov.achilles.configuration.ConfigurationParameters.CONSISTENCY_LEVEL_READ_MAP;
import static info.archinnov.achilles.configuration.ConfigurationParameters.CONSISTENCY_LEVEL_WRITE_DEFAULT;
import static info.archinnov.achilles.configuration.ConfigurationParameters.CONSISTENCY_LEVEL_WRITE_MAP;
import static info.archinnov.achilles.configuration.ConfigurationParameters.ENTITIES_LIST;
import static info.archinnov.achilles.configuration.ConfigurationParameters.ENTITY_PACKAGES;
import static info.archinnov.achilles.configuration.ConfigurationParameters.FORCE_TABLE_CREATION;
import static info.archinnov.achilles.configuration.ConfigurationParameters.GLOBAL_INSERT_STRATEGY;
import static info.archinnov.achilles.configuration.ConfigurationParameters.GLOBAL_NAMING_STRATEGY;
import static info.archinnov.achilles.configuration.ConfigurationParameters.KEYSPACE_NAME;
import static info.archinnov.achilles.configuration.ConfigurationParameters.NATIVE_SESSION;
import static info.archinnov.achilles.configuration.ConfigurationParameters.JACKSON_MAPPER;
import static info.archinnov.achilles.configuration.ConfigurationParameters.JACKSON_MAPPER_FACTORY;
import static info.archinnov.achilles.configuration.ConfigurationParameters.OSGI_CLASS_LOADER;
import static info.archinnov.achilles.configuration.ConfigurationParameters.PREPARED_STATEMENTS_CACHE_SIZE;
import static info.archinnov.achilles.configuration.ConfigurationParameters.PROXIES_WARM_UP_DISABLED;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Validator;

import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import info.archinnov.achilles.type.NamingStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import info.archinnov.achilles.configuration.ConfigurationParameters;
import info.archinnov.achilles.json.JacksonMapperFactory;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.persistence.PersistenceManagerFactory;
import info.archinnov.achilles.persistence.PersistenceManagerFactory.PersistenceManagerFactoryBuilder;
import info.archinnov.achilles.type.ConsistencyLevel;
import info.archinnov.achilles.type.InsertStrategy;

public class PersistenceManagerFactoryBean extends AbstractFactoryBean<PersistenceManager> {
    private PersistenceManager manager;

    private String contactPoints;
    private String compression;
    private LoadBalancingPolicy loadBalancingPolicy;
    private int port;
    private String entityPackages;
    private List<Class<?>> entityList;

    private Session session;
    private String keyspaceName;

    private JacksonMapperFactory jacksonMapperFactory;
    private ObjectMapper objectMapper;

    private ConsistencyLevel consistencyLevelReadDefault;
    private ConsistencyLevel consistencyLevelWriteDefault;
    private Map<String, ConsistencyLevel> consistencyLevelReadMap;
    private Map<String, ConsistencyLevel> consistencyLevelWriteMap;

    private boolean forceTableCreation = false;

    private boolean enableSchemaUpdate = false;

    private Map<String,Boolean> enableSchemaUpdateForTables;

    private boolean enableBeanValidation = false;

    private Validator beanValidator;

    private Integer preparedStatementCacheSize;

    private boolean disableProxiesWarmUp = true;

    private InsertStrategy globalInsertStrategy = InsertStrategy.ALL_FIELDS;

    private NamingStrategy globalNamingStrategy = NamingStrategy.LOWER_CASE;

    private ClassLoader osgiClassLoader;

    private com.fasterxml.jackson.databind.ObjectMapper _mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    protected void initialize() {
        Map<ConfigurationParameters, Object> configMap = new HashMap<>();

        fillEntityPackages(configMap);

        fillEntityList(configMap);

        if (session != null) {
            configMap.put(NATIVE_SESSION, session);
        }

        fillKeyspaceName(configMap);
        _mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        _mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        this._mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        fillObjectMapper(configMap);

        fillConsistencyLevels(configMap);

        configMap.put(FORCE_TABLE_CREATION, forceTableCreation);

        configMap.put(ConfigurationParameters.ENABLE_SCHEMA_UPDATE, enableSchemaUpdate);

        if (enableSchemaUpdateForTables!=null) {
            configMap.put(ConfigurationParameters.ENABLE_SCHEMA_UPDATE_FOR_TABLES, enableSchemaUpdateForTables);
        }

        fillBeanValidation(configMap);

        if (preparedStatementCacheSize != null) {
            configMap.put(PREPARED_STATEMENTS_CACHE_SIZE, preparedStatementCacheSize);
        }
        configMap.put(PROXIES_WARM_UP_DISABLED, disableProxiesWarmUp);
        configMap.put(GLOBAL_INSERT_STRATEGY, globalInsertStrategy);
        configMap.put(GLOBAL_NAMING_STRATEGY, globalNamingStrategy);

        if (osgiClassLoader != null) {
            configMap.put(OSGI_CLASS_LOADER, osgiClassLoader);
        }

        Cluster cluster = Cluster.builder().addContactPoints(contactPoints.split(",")).withLoadBalancingPolicy(loadBalancingPolicy).build();
        PersistenceManagerFactory pmf = PersistenceManagerFactoryBuilder.build(cluster, configMap);
        manager = pmf.createPersistenceManager();
    }

    private void fillBeanValidation(Map<ConfigurationParameters, Object> configMap) {
        configMap.put(BEAN_VALIDATION_ENABLE, enableBeanValidation);
        if (beanValidator != null) {
            configMap.put(BEAN_VALIDATION_VALIDATOR, beanValidator);
        }
    }

    private void fillEntityPackages(Map<ConfigurationParameters, Object> configMap) {
        if (isNotBlank(entityPackages)) {
            configMap.put(ENTITY_PACKAGES, entityPackages);
        }
    }

    private void fillEntityList(Map<ConfigurationParameters, Object> configMap) {
        if (CollectionUtils.isNotEmpty(entityList)) {
            configMap.put(ENTITIES_LIST, entityList);
        }
    }

    private void fillKeyspaceName(Map<ConfigurationParameters, Object> configMap) {
        if (StringUtils.isNotBlank(keyspaceName)) {
            configMap.put(KEYSPACE_NAME, keyspaceName);
        }
    }


    private void fillObjectMapper(Map<ConfigurationParameters, Object> configMap) {
        if (jacksonMapperFactory != null) {
            configMap.put(JACKSON_MAPPER_FACTORY, jacksonMapperFactory);
        }
        configMap.put(JACKSON_MAPPER, _mapper);
    }

    private void fillConsistencyLevels(Map<ConfigurationParameters, Object> configMap) {
        if (consistencyLevelReadDefault != null) {
            configMap.put(CONSISTENCY_LEVEL_READ_DEFAULT, consistencyLevelReadDefault);
        }
        if (consistencyLevelWriteDefault != null) {
            configMap.put(CONSISTENCY_LEVEL_WRITE_DEFAULT, consistencyLevelWriteDefault);
        }

        if (consistencyLevelReadMap != null) {
            configMap.put(CONSISTENCY_LEVEL_READ_MAP, consistencyLevelReadMap);
        }
        if (consistencyLevelWriteMap != null) {
            configMap.put(CONSISTENCY_LEVEL_WRITE_MAP, consistencyLevelWriteMap);
        }
    }


    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }



    public void setEntityPackages(String entityPackages) {
        this.entityPackages = entityPackages;
    }

    public void setEntityList(List<Class<?>> entityList) {
        this.entityList = entityList;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setEnableBeanValidation(boolean enableBeanValidation) {
        this.enableBeanValidation = enableBeanValidation;
    }

    public void setBeanValidator(Validator beanValidator) {
        this.beanValidator = beanValidator;
    }

    public void setPreparedStatementCacheSize(Integer preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
    }

    public void setDisableProxiesWarmUp(boolean disableProxiesWarmUp) {
        this.disableProxiesWarmUp = disableProxiesWarmUp;
    }

    public void setGlobalInsertStrategy(InsertStrategy globalInsertStrategy) {
        this.globalInsertStrategy = globalInsertStrategy;
    }

    public void setForceTableCreation(boolean forceTableCreation) {
        this.forceTableCreation = forceTableCreation;
    }

    public void setEnableSchemaUpdate(boolean enableSchemaUpdate) {
        this.enableSchemaUpdate = enableSchemaUpdate;
    }

    public void setEnableSchemaUpdateForTables(Map<String, Boolean> enableSchemaUpdateForTables) {
        this.enableSchemaUpdateForTables = enableSchemaUpdateForTables;
    }

    public void setJacksonMapperFactory(JacksonMapperFactory jacksonMapperFactory) {
        this.jacksonMapperFactory = jacksonMapperFactory;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setConsistencyLevelReadDefault(ConsistencyLevel consistencyLevelReadDefault) {
        this.consistencyLevelReadDefault = consistencyLevelReadDefault;
    }

    public void setConsistencyLevelWriteDefault(ConsistencyLevel consistencyLevelWriteDefault) {
        this.consistencyLevelWriteDefault = consistencyLevelWriteDefault;
    }

    public void setConsistencyLevelReadMap(Map<String, ConsistencyLevel> consistencyLevelReadMap) {
        this.consistencyLevelReadMap = consistencyLevelReadMap;
    }

    public void setConsistencyLevelWriteMap(Map<String, ConsistencyLevel> consistencyLevelWriteMap) {
        this.consistencyLevelWriteMap = consistencyLevelWriteMap;
    }

    public void setOsgiClassLoader(ClassLoader osgiClassLoader) {
        this.osgiClassLoader = osgiClassLoader;
    }

    public void setContactPoints(String contactPoints) {
        this.contactPoints = contactPoints;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public void setLoadBalancingPolicy(LoadBalancingPolicy loadBalancingPolicy) {
        this.loadBalancingPolicy = loadBalancingPolicy;
    }

    public void setPort(int port) {
        this.port = port;
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
        synchronized (this) {
            if (manager == null) {
                initialize();
            }
        }
        return manager;
    }

}
