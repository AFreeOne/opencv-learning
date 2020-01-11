/**
 * @date: 2019年12月18日 上午11:34:55
 */
package org.freeatalk.opencv.learning.config;

import com.baomidou.mybatisplus.entity.GlobalConfiguration;
import com.github.pagehelper.PageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * @ClassName: MybatisConfig.java
 * @version: v1.0.0
 * @author lqq
 * @date: 2019年12月18日 上午11:34:55 
 *
 */
@EnableTransactionManagement
@Configuration
public class MybatisConfig {
    
    @Bean
    public GlobalConfiguration globalConfiguration() {
        GlobalConfiguration global = new GlobalConfiguration();
        global.setDbType("mysql");
        return global;
    }
    
    @Bean
    public PageHelper pageHelper(){
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("offsetAsPageNum","true");
        properties.setProperty("rowBoundsWithCount","true");
        properties.setProperty("reasonable","true");
        //配置mysql数据库的方言
        properties.setProperty("dialect","mysql");
        pageHelper.setProperties(properties);
        return pageHelper;
    }

}
