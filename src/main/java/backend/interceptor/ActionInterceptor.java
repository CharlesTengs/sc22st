package backend.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class ActionInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ActionInterceptor.class);


}
