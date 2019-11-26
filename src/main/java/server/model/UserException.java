package server.model;

/**
 * Thrown when we have issues with an user: register, login, or modifying a file
 * with not enough permissions
 */
public class UserException extends Exception {
	private static final long serialVersionUID = 778751169294295509L;

	public UserException(String reason) {
		super(reason);
	}

	public UserException(String reason, Throwable rootCause) {
		super(reason, rootCause);
	}
}
