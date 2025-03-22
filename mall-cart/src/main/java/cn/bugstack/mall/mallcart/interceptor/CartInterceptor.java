package cn.bugstack.mall.mallcart.interceptor;

import cn.bugstack.common.constant.AuthServerConstant;
import cn.bugstack.common.constant.CartConstant;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.mallcart.vo.UserInfoTO;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author micro, 微信：yykk、
 * @description 在执行目标方法之前，判断用户的登录状态，传递给Controller目标方法
 * @date 2025/3/15 18:28
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfoTO> USERINFO_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 在目标方法执行之前，将请求中的userId封装到ThreadLocal中
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTO userInfoTO = new UserInfoTO();

        HttpSession session = request.getSession();
        MemberResponseVO member = (MemberResponseVO) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            // 用户登录了
            userInfoTO.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTO.setUserKey(cookie.getValue());
                    userInfoTO.setTempUser(true);
                }
            }
        }
        // 如果没有临时用户，一定分配一个临时用户
        if (null == userInfoTO.getUserKey()) {
            String uuid = java.util.UUID.randomUUID().toString();
            userInfoTO.setUserKey(uuid);
        }
        // 目标方法执行之前
        USERINFO_THREAD_LOCAL.set(userInfoTO);
        return true;
    }

    /**
     * 在目标方法执行之后，渲染视图之前，将ThreadLocal中的数据传递给Controller目标方法
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler the handler (or {@link HandlerMethod}) that started asynchronous
     * execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     * (can also be {@code null})
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTO userInfoTO = USERINFO_THREAD_LOCAL.get();
        if (!userInfoTO.isTempUser()) {
            // 持续延长临时用户的过期时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTO.getUserKey());
            cookie.setDomain("mall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
