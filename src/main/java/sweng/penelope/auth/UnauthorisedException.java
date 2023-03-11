package sweng.penelope.auth;

import org.springframework.security.core.AuthenticationException;

/**
 * <code>UnauthorisedException</code> is the exception to be thrown when an user
 * does not have access to a resource.
 */
public class UnauthorisedException extends AuthenticationException {

    public UnauthorisedException() {
        super("User does not have access to this resource");
    }

}
