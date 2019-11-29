package common;

import java.io.Serializable;

public interface FileDTO extends Serializable {
	public String getName();

	public String getPermission();
	
	public boolean getPermissionBoolean();

	public String getOwnerName();

	public String getSize();
}
