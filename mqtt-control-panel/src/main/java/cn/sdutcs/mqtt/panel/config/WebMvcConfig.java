package cn.sdutcs.mqtt.panel.config;

import cn.sdutcs.mqtt.panel.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    // 前后端分离项目
    // 配置了跨域后，访问正常，但是配置了拦截器以后，有的访问正常，有的出现跨域问题无法获取header 拿到token
    // 发现出现跨域问题的都是拦截器里面没有放行的请求。
    // 改用过滤器CorsFilter 来配置跨域，由于Filter的位置是在Interceptor之前的，问题得到解决
    // @Bean
    // public CorsFilter corsFilter() {
    //     CorsConfiguration config = new CorsConfiguration();
    //     // 设置允许跨域请求的域名
    //     config.addAllowedOrigin("*");
    //     // 是否允许证书 不再默认开启
    //     // config.setAllowCredentials(true);
    //     // 设置允许的方法
    //     config.addAllowedMethod("*");
    //     // 允许任何头
    //     config.addAllowedHeader("*");
    //     config.addExposedHeader(TokenConstant.HEADER_TOKEN);
    //     UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
    //     configSource.registerCorsConfiguration("/**", config);
    //     return new CorsFilter(configSource);
    // }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则，/**表示拦截所有请求
        // excludePathPatterns 排除拦截规则
        // ****注意前面别掉了斜杠****
        registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/auth/login")
                .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将templates目录下的CSS、JS文件映射为静态资源，防止Spring把这些资源识别成thymeleaf模版
        registry.addResourceHandler("/templates/**.js").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/templates/**.css").addResourceLocations("classpath:/templates/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        // swagger增加url映射
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
