package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileChangeListener extends Remote {

	void fileChanged(FileDTO file, String accessor, String action) throws RemoteException;
	
	String getUsername() throws RemoteException;
}
