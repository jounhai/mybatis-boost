package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MapperUtils;

import java.util.*;

public class DeleteByIds implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @SuppressWarnings("unchecked")
    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> entityType = MapperUtils.getEntityTypeFromMapper
                (mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.')));
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("DELETE FROM ").append(EntityUtils.getTableName(entityType, configuration.getNameAdaptor()));

        Map parameterMap = (Map) boundSql.getParameterObject();
        Object[] parameterArray = (Object[]) parameterMap.get("array");
        List<ParameterMapping> parameterMappings = Collections.emptyList();
        if (parameterArray.length > 0) {
            String idProperty = EntityUtils.getIdProperty(entityType);
            sqlBuilder.append(" WHERE ").append(idProperty).append(" IN (");
            Arrays.stream(parameterArray).forEach(c -> sqlBuilder.append("?, "));
            sqlBuilder.setLength(sqlBuilder.length() - 2);
            sqlBuilder.append(')');

            Map<String, Object> newParameterMap = new HashMap<>(parameterArray.length);
            parameterMappings = new ArrayList<>(parameterArray.length);
            for (int i = 0; i < parameterArray.length; i++) {
                newParameterMap.put(idProperty + i, parameterArray[i]);
                parameterMappings.add(new ParameterMapping.Builder((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"),
                        idProperty + i, Object.class).build());
            }
            parameterMap = newParameterMap;
        } else {
            parameterMap = Collections.emptyMap();
        }
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
        metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);
        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}