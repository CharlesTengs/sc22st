package backend.interceptor;

import backend.resp.UserLoginResp;
import backend.util.LoginUserContext;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(LoginInterceptor.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOG.info("------------- LoginInterceptor Start -------------");
        long startTime = System.currentTimeMillis();
        request.setAttribute("requestStartTime", startTime);


        if(request.getMethod().toUpperCase().equals("OPTIONS")){
            return true;
        }

        String path = request.getRequestURL().toString();
        LOG.info("Interface Login Interception: path：{}", path);

        //get token in header
        String token = request.getHeader("token");
        LOG.info("Login verification begins, token: {}", token);
        if (token == null || token.isEmpty()) {
            LOG.info( "Empty token, request intercepted" );
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        // Validate token in Redis
        Object object = redisTemplate.opsForValue().get(token);
        if (object == null) {
            LOG.warn( "Invalid token, request intercepted" );
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        } else {
            LOG.info("Logged in: {}", object);
            LoginUserContext.setUser(JSON.parseObject((String) object, UserLoginResp.class));
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("requestStartTime");
        LOG.info("------ LoginInterceptor finished, time taken: {} ms -----", System.currentTimeMillis() - startTime);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        LOG.info("LogInterceptor finish");
    }
}
