package pers.zhixilang.rpc.consumer.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 扫描 -> 实例化bean -> registry
 * 参考MyBatis org.mybatis.spring.mapper.MapperScannerConfigurer
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-04 16:38
 */
@Component
public class RpcScannerConfigurer implements BeanDefinitionRegistryPostProcessor {

    private String basePackage = "pers.zhixilang.rpc.common.service";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        ClassPathRpcScanner classPathRpcScanner = new ClassPathRpcScanner(beanDefinitionRegistry);

        classPathRpcScanner.setAnnotationClass(null);
        classPathRpcScanner.registerFilters();
        // 注册spring容器
        // doScan + register
        classPathRpcScanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
