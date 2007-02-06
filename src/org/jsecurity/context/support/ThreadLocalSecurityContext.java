package org.jsecurity.context.support;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.Authenticator;
import org.jsecurity.authz.AuthorizationException;
import org.jsecurity.authz.NoSuchPrincipalException;
import org.jsecurity.authz.UnauthorizedException;
import org.jsecurity.context.SecurityContext;
import org.jsecurity.session.Session;
import org.jsecurity.util.ThreadContext;

import java.security.Permission;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Retrieves all security context data from the currently executing thread (via the {@link ThreadContext}).  This
 * implementation is most widely used in multi-threaded server environments such as EJB and Servlet containers.
 *
 * @author Les Hazlewood
 * @since 0.1
 */
@SuppressWarnings( {"unchecked"} )
public class ThreadLocalSecurityContext implements SecurityContext {

    private Authenticator authenticator = null;

    public ThreadLocalSecurityContext(){}

    public ThreadLocalSecurityContext( Authenticator authenticator ) {
        setAuthenticator( authenticator );
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator( Authenticator authenticator ) {
        this.authenticator = authenticator;
    }

    public static SecurityContext current() {
        return (SecurityContext)ThreadContext.get( ThreadContext.SECURITY_CONTEXT_KEY );
    }

    public SecurityContext authenticate( AuthenticationToken authenticationToken )
            throws AuthenticationException {
        
        Authenticator authc = getAuthenticator();
        if ( authc != null ) {
            SecurityContext secCtx = authc.authenticate( authenticationToken );
            ThreadContext.put( ThreadContext.SECURITY_CONTEXT_KEY, secCtx );
            return this;
        } else {
            String msg = "underlying Authenticator instance is not set.  The " +
                    getClass().getName() + " class only acts as a delegate to an underlying " +
                    "Authenticator that actually performs the authentication process.  This " +
                    "underlying instance has not been set (it is null) and authenication cannot " +
                    "occur.  Please check your configuration and ensure the delegated " +
                    "Authenticator is available to instances of this class, either via " +
                    "a constructor, or by Dependency Injection.";
            throw new AuthenticationException( msg );
        }
    }


    public boolean isAuthenticated() {
        return getSecurityContext() != null;
    }

    public Principal getPrincipal() throws NoSuchPrincipalException {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            return secCtx.getPrincipal();
        }
        return null;
    }

    public List<Principal> getAllPrincipals() {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            return secCtx.getAllPrincipals();
        }
        return Collections.EMPTY_LIST;
    }

    public Principal getPrincipalByType( Class principalType ) throws NoSuchPrincipalException {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            return secCtx.getPrincipalByType( principalType );
        }
        return null;
    }

    public Collection<Principal> getAllPrincipalsByType( Class principalType ) {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            return secCtx.getAllPrincipalsByType( principalType );
        }
        return Collections.EMPTY_LIST;
    }

    public boolean hasRole( String roleIdentifier ) {
        SecurityContext secCtx = getSecurityContext();
        return secCtx != null && secCtx.hasRole( roleIdentifier );
    }

    public boolean[] hasRoles( List<String> roleIdentifiers ) {
        SecurityContext secCtx = getSecurityContext();
        boolean[] hasRoles;

        if ( secCtx != null ) {
            hasRoles = secCtx.hasRoles( roleIdentifiers );
        } else {
            if ( roleIdentifiers != null ) {
                hasRoles = new boolean[roleIdentifiers.size()];
            } else {
                hasRoles = new boolean[0];
            }
        }

        return hasRoles;
    }

    public boolean hasAllRoles( Collection<String> roleIdentifiers ) {
        SecurityContext secCtx = getSecurityContext();
        return secCtx != null && secCtx.hasAllRoles( roleIdentifiers );
    }

    public boolean implies( Permission permission ) {
        SecurityContext secCtx = getSecurityContext();
        return secCtx != null && secCtx.implies ( permission );
    }

    public boolean[] implies( List<Permission> permissions ) {
        SecurityContext secCtx = getSecurityContext();
        boolean[] implies;

        if ( secCtx != null ) {
            implies = secCtx.implies( permissions );
        } else {
            if ( permissions != null ) {
                implies = new boolean[permissions.size()];
            } else {
                implies = new boolean[0];
            }
        }

        return implies;
    }

    public boolean impliesAll( Collection<Permission> permissions ) {
        SecurityContext secCtx = getSecurityContext();
        return secCtx != null && secCtx.impliesAll( permissions );
    }

    public void checkPermission( Permission permission ) throws AuthorizationException {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            secCtx.checkPermission( permission );
        } else {
            String msg = "No SecurityContext bound to the current thread - user has not " +
                    "authenticated yet?  Permission check failed.";
            throw new UnauthorizedException( msg );
        }
    }

    public void checkPermissions( Collection<Permission> permissions ) throws AuthorizationException {
        SecurityContext secCtx = getSecurityContext();
        if ( secCtx != null ) {
            secCtx.checkPermissions( permissions );
        } else {
            String msg = "No SecurityContext bound to the current thread - user has not " +
                    "authenticated yet?  Permissions check failed.";
            throw new UnauthorizedException( msg );
        }
    }

    protected SecurityContext getSecurityContext() {
        return (SecurityContext) ThreadContext.get( ThreadContext.SECURITY_CONTEXT_KEY );
    }

    public Session getSession() {
        return getSecurityContext().getSession();
    }

    public Session getSession( boolean create ) {
        return getSecurityContext().getSession( create );
    }

    public void invalidate() {
        getSecurityContext().invalidate();
    }

}
