package com.youzi.teaChain.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object arg2, Exception arg3)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object arg2, ModelAndView arg3)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("------LoginInterceptor-------");

        // 获取请求的URL
        String url = request.getRequestURI();
        // URL:login.jsp是公开的;这个demo是除了login.jsp是可以公开访问的，其它的URL都进行拦截控制
        // 注意：一些静态文件不能拦截，否则会死循环，知道内存耗尽
        if (checkUrl(url)) {
            return true;
        }
        // 获取Session
        HttpSession session = request.getSession();
        Object user = session.getAttribute("user");

        if (user != null) {
            return true;
        }
        // 不符合条件的，跳转到登录界面
//         request.getRequestDispatcher("test.html").forward(request, response);
        response.sendRedirect("test.html");

        return false;
    }

    /**
     * 校验非不拦截地址
     * @param url
     * @return
     */
    private Boolean checkUrl(String url) {
        String [] aa = {"login","common","toRequest","error","getWXAccess","selectUserInfo","MP_verify_BzuwGCPjCBIRqaaT"};
        for (int i=0;i<aa.length;i++) {
            if (url.indexOf(aa[i]) >= 0) {
                return true;
            }
        }
        return false;
    }

}
