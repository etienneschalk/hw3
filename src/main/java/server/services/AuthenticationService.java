package server.services;

public interface AuthenticationService {
	void createJWTString();

	boolean isValidJWT(String jwt);

	String getJwtString();
}
