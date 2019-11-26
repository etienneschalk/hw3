package server.controller;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.FileCatalog;
import common.FileDTO;
import server.integration.FileCatalogDAO;
import server.model.FileException;
import server.model.User;
import server.model.UserException;

/**
 * Implementation of the file catalog remote methods. This server class will be
 * called remotely by a client.
 * 
 * @author etis3
 *
 */
public class Controller extends UnicastRemoteObject implements FileCatalog {
	private FileCatalogDAO fc;

	public Controller() throws RemoteException {
		super();
		fc = new FileCatalogDAO();
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
	public void login(String username, String password) throws UserException {
		User user = fc.findUserByName(username, true);
		if (user != null) {
			if (password == null || ! password.equals(user.getPassword()))
					throw new UserException("Incorrect password!");
			else {
				// TODO handle the connection ( JWT ? )
			}
		} else {
			throw new UserException("Not user with username " + username + " was found.");
		}
	}

	@Override
	public List<? extends FileDTO> list() throws FileException {
		try {
			return fc.findAllFiles();
		} catch(Exception e) {
			throw new FileException("Unable to list files.", e);
		}
	}

	@Override
	public FileDTO details(String fileName) throws FileException {
		try {
			return fc.findFileByFileName(fileName, true);
		} catch(Exception e) {
			throw new FileException("Could not retrieve the details of the file " + fileName + ".");
		}
	}

	@Override
	public void upload(String location) throws FileException, UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public void upload(String location, String newName) throws FileException, UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public void download(String fileName) throws FileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void download(String fileName, String targetDirectory) throws FileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void download(String fileName, String targetDirectory, String newName)
			throws FileException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(String fileName) throws FileException, UserException {
		try {
			fc.deleteFile(fileName);
		} catch(Exception e) {
			throw new FileException("Could not delete the file " + fileName + ".");
		} 
	}
}
