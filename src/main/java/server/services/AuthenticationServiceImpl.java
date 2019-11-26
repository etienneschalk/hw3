package server.services;

import java.security.Key;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import server.services.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService {
	private final static String subject = "ID1212";
	private final static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	private String jwtString = null;

	@Override
	public void createJWTString() {
		jwtString = Jwts.builder().setSubject(subject).signWith(key).compact();
	}

	@Override
	public boolean isValidJWT(String jwt) {
		try {
			Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}
	
	@Override 
	public String getJwtString() {
		return jwtString;
	}
}
