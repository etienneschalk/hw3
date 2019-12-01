package server.services;


public interface AuthenticationService {
	void createJWTString(String username);

	boolean isValidJWT(String jwt);

	String getJwtString();
	
	String getUsername(String jwt);
}
