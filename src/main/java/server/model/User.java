package server.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

@NamedQueries({
		@NamedQuery(name = "findUserByName", query = "SELECT user FROM User user WHERE user.name = :name", lockMode = LockModeType.OPTIMISTIC),
		@NamedQuery(name = "findAllUsers", query = "SELECT user FROM User user", lockMode = LockModeType.OPTIMISTIC),
		@NamedQuery(name = "deleteUserByName", query = "DELETE FROM User user WHERE user.name = :name") })
@Entity(name = "User")
public class User implements Serializable {
	private static final long serialVersionUID = 5483057371442193943L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long userId;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "password", nullable = false)
	private String password;

	@Version
	@Column(name = "OPTLOCK")
	private int version;

	public User() {
		this(null, null);
	}
	
	public User(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
}