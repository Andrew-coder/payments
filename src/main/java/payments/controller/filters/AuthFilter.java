package payments.controller.filters;

import org.apache.log4j.Logger;
import payments.model.entity.user.RoleType;
import payments.utils.constants.Attributes;
import payments.utils.constants.PagesPath;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.EnumMap;

/**
 * This class is authorization filter.
 * Filter checks every user request, to find out his permissions.
 * If user don't have permissions, the filter forward user to login page.
 */
public class AuthFilter implements Filter{
    private static final Logger logger = Logger.getLogger(AuthFilter.class);
    private static final String USER_NOT_AUTHORIZED = "User isn't authorized";

    private static EnumMap<RoleType, Authorizer> authorizeByRole = new EnumMap<>(RoleType.class);

    static {
        authorizeByRole.put(RoleType.USER, new UserAuthorizer());
        authorizeByRole.put(RoleType.ADMIN, new AdminAuthorizer());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
        HttpServletRequest req = ((HttpServletRequest) request);
        HttpSession session = req.getSession();
        String uri = req.getRequestURI();
        Object userId = session.getAttribute(Attributes.USER_ID);
        RoleType roleType = (RoleType)session.getAttribute(Attributes.USER_ROLE);
        if(!checkUserPermissions(uri, userId, roleType)){
            req.getRequestDispatcher(PagesPath.LOGIN).forward(request, response);
            logger.info(String.format(USER_NOT_AUTHORIZED));
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * this method check user permissions
     * @param uri
     * @param userId
     * @param roleType
     * @return
     */
    private boolean checkUserPermissions(String uri, Object userId, RoleType roleType){
        if(uri.endsWith(".css")||uri.endsWith(".js")||uri.endsWith(".png")) {
            return true;
        }
        Authorizer authorizer = authorizeByRole.getOrDefault(roleType, new AnonymAuthorizer());
        return authorizer.check(uri, userId);
    }

    private interface Authorizer {
        boolean check(String uri, Object userId);
    }

    private static class UserAuthorizer implements Authorizer {
        public boolean check(String uri, Object userId) {
            return userId!=null && !uri.startsWith(PagesPath.ADMIN);

        }
    }

    private static class AdminAuthorizer implements Authorizer {
        public boolean check(String uri, Object userId) {
            return  userId!=null && (uri.startsWith(PagesPath.ADMIN)||
                    uri.startsWith(PagesPath.LOGIN)||
                    uri.startsWith(PagesPath.REGISTER));
        }
    }

    private class AnonymAuthorizer implements Authorizer {
        public boolean check(String uri, Object userId){
            return  uri.startsWith(PagesPath.LOGIN)||
                    uri.startsWith(PagesPath.REGISTER);
        }
    }

    /**
     * method which perform filter initialization
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    /**
     * method, which will be performed before filter destroy
     */
    @Override
    public void destroy() {

    }
}