package tk.mybatis.spring.mapper;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.*;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassPathMapperScanner extends org.mybatis.spring.mapper.ClassPathMapperScanner {

    public static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\d+\\]$");

    public static final String MAPPER_PREFIX = "mapper.";

    private MapperHelper mapperHelper = new MapperHelper();

    public ClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        doAfterScan(beanDefinitions);
        return beanDefinitions;
    }

    protected void doAfterScan(Set<BeanDefinitionHolder> beanDefinitions) {
        //如果没有注册过接口，就注册默认的Mapper接口
        this.mapperHelper.ifEmptyRegisterDefaultInterface();
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            if (StringUtil.isNotEmpty(definition.getBeanClassName())
                    && definition.getBeanClassName().equals("org.mybatis.spring.mapper.MapperFactoryBean")) {
                definition.setBeanClass(MapperFactoryBean.class);
                definition.getPropertyValues().add("mapperHelper", this.mapperHelper);
            }
        }
    }

    /**
     * 兼容简单的属性绑定，如 not-empty 和 not_empty，不支持 NOT_EMPTY
     *
     * @param property
     * @return
     */
    private String adaptRelaxBinding(String property) {
        if (property.indexOf("-") != -1 || property.indexOf("_") != -1) {
            property = property.toLowerCase();
            StringBuilder builder = new StringBuilder(property.length());
            boolean uppercase = false;
            for (int i = 0; i < property.length(); i++) {
                char c = property.charAt(i);
                if (c == '-' || c == '_') {
                    uppercase = true;
                    continue;
                }
                if (uppercase) {
                    builder.append(String.valueOf(c).toUpperCase());
                    uppercase = false;
                } else {
                    builder.append(c);
                }
            }
            return builder.toString();
        } else {
            return property;
        }
    }

    /**
     * 从环境变量中获取 mapper 配置信息
     *
     * @param environment
     */
    public void setMapperProperties(Environment environment) {
        if (environment != null) {
            Properties properties = new Properties();
            MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
            Iterator<PropertySource<?>> iterator = propertySources.iterator();
            while (iterator.hasNext()) {
                PropertySource<?> propertySource = iterator.next();
                if (propertySource instanceof EnumerablePropertySource) {
                    EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                    String[] propertyNames = enumerablePropertySource.getPropertyNames();
                    for (String propertyName : propertyNames) {
                        if (propertyName.startsWith(MAPPER_PREFIX)) {
                            String propertyValue = environment.getProperty(propertyName);
                            Matcher matcher = ARRAY_PATTERN.matcher(propertyName);
                            if (matcher.find()) {
                                //去掉数组索引
                                propertyName = matcher.replaceAll("");
                            }
                            //处理relax情况
                            propertyName = adaptRelaxBinding(propertyName);
                            //去掉前缀
                            propertyName = propertyName.substring(propertyName.lastIndexOf(".") + 1);
                            //将数组形式转换为逗号分割形式
                            if (properties.containsKey(propertyName)) {
                                propertyValue = properties.getProperty(propertyName) + "," + propertyValue;
                            }
                            properties.put(propertyName, propertyValue);
                        }
                    }
                }
            }
            mapperHelper.setProperties(properties);
        }
    }
}
