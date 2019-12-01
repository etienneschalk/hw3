package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private final ThreadLocal<Boolean> notificationPresent = new ThreadLocal<>();
	private List<FileChangeListener> fileChangeListeners = new ArrayList<>();

	public Controller() throws RemoteException {
		super();
		fc = new FileCatalogDAO();
		authenticationService = new AuthenticationServiceImpl();
		notificationPresent.set(false);
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
				threadLocalLoggedInUser.set(user); // We remember the information about the successfully logged in user
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
			FileDTO file = fc.findFileByFileName(fileName, true);
			notifyFileChangeListener(file, "DETAILS");
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FileException("Could not retrieve the details of the file " + fileName + ".");
		}
	}

	@Override
	public void upload(String jwtToken, String newName, boolean writePermission) throws FileException, UserException {
		requireAuthentication(jwtToken);
		// We try to delete an eventually existing file
		boolean theFileAlreadyExists = false;
		boolean fileWritePermission = true;
		boolean isOwner = true;
		FileDTO file = null;

		try {
			file = fc.findFileByFileName(newName, true);
			fileWritePermission = file.getPermissionBoolean();
			User loggedInUser = checkAndGetAndRefreshUser();
			isOwner = loggedInUser.getName().equals(file.getOwnerName());
			theFileAlreadyExists = true;
		} catch (Exception e) {
			// The file does not exist, we can upload
			theFileAlreadyExists = false;
		}

		// Check if writable / owner
		boolean overwrite = false;
		try {
			if (theFileAlreadyExists) {
				if (fileWritePermission || isOwner) {
					fc.deleteFile(newName);
					overwrite = true;
				} else {
					throw new UserException("You are not allowed to delete the file " + newName + ".");
				}
			}
		} catch (UserException e) {
			throw new FileException(e.getMessage());
		} catch (Exception e) {
			throw new FileException("Could not delete the file " + newName + ".");
		}
		databaseUpload(newName, writePermission); // Insert file meta data in the DB
		if (overwrite) {
			System.out.println("Notify with OVERWRITE");
			notifyFileChangeListener(file, "UPW");
		}
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
			FileDTO file = fc.findFileByFileName(fileName, true);
			User loggedInUser = checkAndGetAndRefreshUser();
			boolean writePermission = file.getPermissionBoolean();
			boolean isOwner = loggedInUser.getName().equals(file.getOwnerName());

			if (writePermission || isOwner) {
				fc.deleteFile(fileName);

				System.out.println("Notify with DELETE");
				notifyFileChangeListener(file, "DELETE");
			} else {
				throw new UserException("You are not allowed to delete the file " + fileName + ".");
			}
		} catch (UserException e) {
			throw new FileException(e.getMessage());
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
	private void createFileMetaData(String newName, int size, String url, boolean writePermission)
			throws UserException {
		User loggedInUser = checkAndGetAndRefreshUser();

		fc.createFile(new File(newName, size, url, writePermission, loggedInUser));
	}

	/**
	 * Fake upload of file Just insert a new File entry in the database
	 * 
	 * @param newName
	 */
	private void databaseUpload(String newName, boolean writePermission) throws UserException {
		int size = newName.length();
//		String url = "/fakeurl/" + newName;
		String url = newName;
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
	private FileDTO fakeDownload(String userJwtToken, String fileName, String targetDirectory, String newName)
			throws FileException, UserException {

		System.out.println("Notify with DOWNLOAD");
		FileDTO file = fc.findFileByFileName(fileName, true);
		notifyFileChangeListener(file, "DOWN");
		return details(userJwtToken, fileName);
	}

	@Override
	public String waitForNotification(String jwtToken) {
		while (notificationPresent == null || notificationPresent.get() == null || notificationPresent.get() == false) {
			System.out.println("nope");
		}
		notificationPresent.set(false);
		return "Heyheyhey";
	}

	@Override
	public void addFileChangeListener(FileChangeListener fcl) throws RemoteException {
		fileChangeListeners.add(fcl);
	}

	@Override
	public void removeFileChangeListener(FileChangeListener fcl) throws RemoteException {
		fileChangeListeners.remove(fcl);
	}

	/**
	 * When a file is: - read with DETAILS command - downloaded with DOWN command -
	 * uploaded and crunched (TODO) with UPW command (if a file exists we can
	 * rewrite it but cannot lock it with read only, and its owner still the same
	 * 
	 * @param file
	 */
	private void notifyFileChangeListener(FileDTO file, String action) throws UserException {
		if (file == null) {
			return;
		}
		String owner = file.getOwnerName();
		User loggedInUser = checkAndGetAndRefreshUser();

		String accessor = "";
		if (loggedInUser != null) {
			accessor = loggedInUser.getName();
		} else {
//			throw new UserException("No user logged in");
			return;
		}
		String listenerUsername;

		Iterator<FileChangeListener> iteratorFcl = this.fileChangeListeners.iterator();
		synchronized (iteratorFcl) {
			FileChangeListener fcl;
			while (iteratorFcl.hasNext()) {
				fcl = iteratorFcl.next();
				try {
					listenerUsername = fcl.getUsername();
					if (listenerUsername.equals(owner)) {
						fcl.fileChanged(file, accessor, action);
					}
				} catch (RemoteException e) {
					fileChangeListeners.remove(fcl);
				}
			}
		}
	}

	public User checkAndGetAndRefreshUser() throws UserException {
		User loggedInUser = threadLocalLoggedInUser.get();
		if (loggedInUser == null) {
			throw new UserException("Please terminate the program and restart again.");
		}
		threadLocalLoggedInUser.set(loggedInUser); // refresh
		return loggedInUser;
	}

//	public static void verifyJWT(String userJwtToken) throws UserException{
//		try {
//
//		}
//		catch{
//			
//		}
//	}
}
