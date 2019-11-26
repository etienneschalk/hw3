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

	public void login(String username, String password) throws RemoteException, UserException;

	public List<? extends FileDTO> list() throws RemoteException, FileException;

	public FileDTO details(String fileName) throws RemoteException, FileException;

	public void upload(String location) throws RemoteException, FileException, UserException;

	public void upload(String location, String newName) throws RemoteException, FileException, UserException;

	public void download(String fileName) throws RemoteException, FileException;

	public void download(String fileName, String targetDirectory) throws RemoteException, FileException;

	public void download(String fileName, String targetDirectory, String newName) throws RemoteException, FileException;

	public void delete(String fileName) throws RemoteException, FileException, UserException;

}
