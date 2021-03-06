package com.stone.common.filter;

import com.stone.common.util.UserInfoUtil;
import com.stone.core.model.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by 石头 on 2017/6/20.
 */
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //web应用程序启动时，web服务器将创建Filter的实例对象，并调用其init方法，完成对象的初始化
        //filter对象只会创建一次，init方法也只会执行一次
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        User user = UserInfoUtil.getUserFromRedis(request);

        if(user == null && !path.contains("/login.html")){
            if (path.contains("/register.html")) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            response.sendRedirect("/login.html");
        } else if(user != null && path.contains("/login.html")) {
            response.sendRedirect("/html/index.html");
        } else {
            //将请求转发给过滤器链上下一个对象,没有下一个filter就转向请求的资源
            //System.out.println("请求执行前")
            filterChain.doFilter(servletRequest, servletResponse);
            //System.out.println("请求执行后")
        }
    }

    @Override
    public void destroy() {
        //web容器调用destroy方法销毁Filter。此方法在Filter生命周期中仅执行一次。在destroy方法中，可以释放过滤器使用的资源。
    }
}
