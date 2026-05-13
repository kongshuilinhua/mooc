package com.elysia.mooc.config;

import com.elysia.mooc.common.enums.StringToBaseEnumConverterFactory;
import com.elysia.mooc.media.config.MediaStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** MVC 工程化配置。 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MediaStorageProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final MediaStorageProperties mediaStorageProperties;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new StringToBaseEnumConverterFactory());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicPrefix = mediaStorageProperties.getPublicPrefix();
        String pattern = publicPrefix.endsWith("/**") ? publicPrefix : publicPrefix + "/**";
        String rootPath = mediaStorageProperties.getRootPath().replace("\\", "/");
        String location = "file:" + (rootPath.endsWith("/") ? rootPath : rootPath + "/");
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}
