package com.inxedu.wxos.config;


import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 *  路径匹配配置
 * @author 马颢、乔斌达
 */
@Configuration
public class UrlPathMatchConfig implements WebMvcConfigurer {

    @Value("${project.file.root}")
    private String fileRoot;

    /**
     * 路径匹配配置
     * 如  将该地址 /admin/membertype/status.json 匹配到 /admin/membertype/status
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer){
        //开启路径匹配
        configurer.setUseSuffixPatternMatch(true)
                //设置路径自动匹配
                .setUseTrailingSlashMatch(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //必须配置project.file.root
        if(StrUtil.isNotEmpty(fileRoot)){
            //结尾必须有 File.separator
            if (!fileRoot.endsWith("\\") && !fileRoot.endsWith("/")){
                fileRoot = fileRoot+File.separator;
            }
        } else {
            throw new RuntimeException("config 'project.file.root' not null");
        }

        registry.addResourceHandler("/**")
                .addResourceLocations("file:"+fileRoot)
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/resources/")
                 .addResourceLocations("classpath:/static/");
//                 .addResourceLocations("classpath:/public/")

    }
}

