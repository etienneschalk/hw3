package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import server.model.FileException;
import server.model.UserException;

public interface FileCatalog extends Remote {
	/**
	 * The default URI of the file catalog in the RMI registry
	 */
	 static final String FILE_CATALOG_NAME_IN_REGISTRY = "fileCatalog";

	 void register(String username, String password) throws RemoteException, UserException;

	 String login(String username, String password) throws RemoteException, UserException;

	 List<? extends FileDTO> list(String jwtToken) throws RemoteException, FileException, UserException;

	 FileDTO details(String jwtToken, String fileName) throws RemoteException, FileException, UserException;

	 void upload(String jwtToken, String newName, boolean writePermission) throws RemoteException, FileException, UserException;

	 FileDTO download(String jwtToken, String fileName, String targetDirectory, String newName) throws RemoteException, FileException, UserException;

	 void delete(String jwtToken, String fileName) throws RemoteException, FileException, UserException;

	 void checkLogin(String jwtToken) throws RemoteException, UserException;
	// Observer registration
	// The server is an "observable"
	 void addFileChangeListener(FileChangeListener fcl) throws RemoteException;
	 
	 void removeFileChangeListener(FileChangeListener fcl) throws RemoteException;
}
