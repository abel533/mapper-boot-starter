# MyBatis Mapper integration with Spring Boot

Mapper-Spring-Boot-Starter 帮助你集成通用 Mapper 到 Spring Boot。

Mapper-Spring-Boot-Starter will help you use Mapper with Spring Boot.

## How to use
在 pom.xml 中添加如下依赖：

Add the following dependency to your pom.xml: 
```xml
<dependency>
    <groupId>tk.mybatis</groupId>
    <artifactId>mapper-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Special Configurations
一般情况下，你不需要做任何配置。

Normally, you don't need to do any configuration.

如果需要配置，可以使用如下方式进行配置：

You can config PageHelper as the following:

application.properties:
```properties
mapper.propertyName=propertyValue
```

示例:
```properties
mapper.mappers[0]=tk.mybatis.sample.mapper.BaseMapper
mapper.mappers[1]=tk.mybatis.mapper.common.Mapper
```
默认情况下，没有 mappers 配置时，会自动注册 `tk.mybatis.mapper.common.Mapper`

因为通用 Mapper 是固定的属性，所以 IDE 应该可以自动提示：
![自动提示属性](properties.png)

## MyBatis Mapper
>https://github.com/abel533/Mapper
