package server.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.LockModeType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import common.FileDTO;

@NamedQueries({
		@NamedQuery(name = "findFileByFileName", query = "SELECT file FROM File file WHERE file.name = :fileName", lockMode = LockModeType.OPTIMISTIC),
		@NamedQuery(name = "findAllFiles", query = "SELECT file FROM File file", lockMode = LockModeType.OPTIMISTIC),
		@NamedQuery(name = "deleteFileByFileName", query = "DELETE FROM File file WHERE file.name = :fileName") })

@Entity(name = "File")
public class File implements FileDTO {
	private static final long serialVersionUID = -5260105859429222994L;

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long fileId;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "size", nullable = false)
	private int size;

	@Column(name = "url", nullable = false)
	private String url;

	@Column(name = "writePermission", nullable = false)
	private boolean writePermission;

	@ManyToOne
	@JoinColumn(name = "owner", nullable = false)
	private User owner;

	@Version
	@Column(name = "OPTLOCK")
	private int version;

	public File() {
	}

	public File(String name, int size, String url, boolean writePermission, User owner) {
		this.name = name;
		this.size = size;
		this.url = url;
		this.writePermission = writePermission;
		this.owner = owner;
	}

	// FileDTO interface implementation
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPermission() {
		return writePermission ? "W" : "R";
	}

	@Override
	public String getOwnerName() {
		return owner.getName();
	}

	@Override
	public String getSize() {
		return Integer.toString(size);
	}
}