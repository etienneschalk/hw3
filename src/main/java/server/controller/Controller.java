package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import javax.persistence.EntityManager;

import common.FileCatalog;
import common.FileChangeListener;
import common.FileDTO;
import server.integration.FileCatalogDAO;
import server.model.File;
import server.model.FileException;
import server.model.User;
import server.model.UserException;
import server.services.AuthenticationService;
import server.services.AuthenticationServiceImpl;

/**
 * Implementation of the file catalog remote methods. This server class will be
 * called remotely by a client.
 * 
 * @author etis3
 *
 */
public class Controller extends UnicastRemoteObject implements FileCatalog {
	private static final long serialVersionUID = -2027287211344704385L;
	private FileCatalogDAO fc;
	private AuthenticationService authenticationService;
	private final ThreadLocal<User> threadLocalLoggedInUser = new ThreadLocal<>();

	public Controller() throws RemoteException {
		super();
		fc = new FileCatalogDAO();
		authenticationService = new AuthenticationServiceImpl();
	}

	@Override
	public void register(String username, String password) throws UserException {
		if (fc.findUserByName(username, true) != null) {
			throw new UserException(
					"A user with the username " + username + " is already registered. Please choose another username.");
		}
		fc.createUser(username, password);
	}

	@Override
	public String login(String username, String password) throws UserException {
		User user = fc.findUserByName(username, true);
		if (user != null) {
			if (password == null || !password.equals(user.getPassword()))
				throw new UserException("Incorrect password!");
			else {
				// Successful connection
				// TODO handle the connection ( JWT ? )
				authenticationService.createJWTString();
				String jwtString = authenticationService.getJwtString();
				if (jwtString == null) {
					throw new UserException("Unexpected error during authentication.");
				}
				threadLocalLoggedInUser.set(user);	// We remember the information about the successfully logged in user
				return jwtString;
			}
		} else {
			throw new UserException("Not user with username " + username + " was found.");
		}
	}

	@Override
	public List<? extends FileDTO> list(String jwtToken) throws FileException, UserException {
		requireAuthentication(jwtToken);
		try {
			return fc.findAllFiles();
		} catch (Exception e) {
			throw new FileException("Unable to list files.", e);
		}
	}

	@Override
	public FileDTO details(String jwtToken, String fileName) throws FileException, UserException {
		requireAuthentication(jwtToken);
		try {
			return fc.findFileByFileName(fileName, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FileException("Could not retrieve the details of the file " + fileName + ".");
		}
	}

	@Override
	public void upload(String jwtToken, String newName, boolean writePermission) throws FileException, UserException {
		// TODO Auto-generated method stub
		requireAuthentication(jwtToken);
		fakeUpload(newName, writePermission); // Just insert fake metadata into the db
	}

	@Override
	public FileDTO download(String jwtToken, String fileName, String targetDirectory, String newName)
			throws FileException, UserException {
		requireAuthentication(jwtToken);
		return fakeDownload(jwtToken, fileName, targetDirectory, newName); // Just retrieve the details from db
	}

	@Override
	public void delete(String jwtToken, String fileName) throws FileException, UserException {
		requireAuthentication(jwtToken);
		try {
			fc.deleteFile(fileName);
		} catch (Exception e) {
			throw new FileException("Could not delete the file " + fileName + ".");
		}
	}

	/**
	 * Put this method where authentication is needed
	 * 
	 * @param userJwtToken
	 * @return
	 * @throws UserException
	 */
	private void requireAuthentication(String userJwtToken) throws UserException {
		if (authenticationService == null) {
			throw new UserException("Authentication service has problems.");
		}
		String jwtStringServer = authenticationService.getJwtString();
		if (jwtStringServer == null) {
			throw new UserException("Server does not remember you.");
		}
		if (!jwtStringServer.equals(userJwtToken)) {
			throw new UserException("Invalid token.");
		}
	}

	/**
	 * Add the file entry to the database. Holds the metadata.
	 * 
	 * @param newName
	 * @param size
	 * @param url
	 * @param writePermission
	 */
	private void createFileMetaData(String newName, int size, String url, boolean writePermission) {
		fc.createFile(new File(newName, size, url, writePermission, threadLocalLoggedInUser.get()));
	}

	/**
	 * Fake upload of file Just insert a new File entry in the database
	 * 
	 * @param newName
	 */
	private void fakeUpload(String newName, boolean writePermission) {
		int size = newName.length();
		String url = "/fakeurl/" + newName;
		createFileMetaData(newName, size, url, writePermission);
	}

	/**
	 * Fake download of a file Just retrieve the details
	 * 
	 * @param fileName
	 * @param targetDirectory
	 * @param newName
	 * @throws UserException 
	 * @throws FileException 
	 */
	private FileDTO fakeDownload(String userJwtToken, String fileName, String targetDirectory, String newName) throws FileException, UserException {
		return details(userJwtToken, fileName);
	}

	@Override
	public String waitForNotification(String jwtToken) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkLogin(String jwtToken) throws RemoteException, UserException {
		requireAuthentication(jwtToken);
	}

	@Override
	public void addFileChangeListener(FileChangeListener fcl) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFileChangeListener(FileChangeListener fcl) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
