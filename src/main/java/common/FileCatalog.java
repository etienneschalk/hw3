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
	public static final String FILE_CATALOG_NAME_IN_REGISTRY = "fileCatalog";

	public void register(String username, String password) throws RemoteException, UserException;

	public String login(String username, String password) throws RemoteException, UserException;

	public List<? extends FileDTO> list(String jwtToken) throws RemoteException, FileException, UserException;

	public FileDTO details(String jwtToken, String fileName) throws RemoteException, FileException, UserException;

	public void upload(String jwtToken, String newName, boolean writePermission) throws RemoteException, FileException, UserException;

	public FileDTO download(String jwtToken, String fileName, String targetDirectory, String newName) throws RemoteException, FileException, UserException;

	public void delete(String jwtToken, String fileName) throws RemoteException, FileException, UserException;

}
