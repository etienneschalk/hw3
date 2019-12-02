package client.view;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

import client.net.FileChangeListenerImpl;
import client.net.TCPFileDownload;
import client.net.TCPFileUpload;
import common.FileCatalog;
import common.FileChangeListener;
import common.FileDTO;
import server.model.FileException;
import server.model.UserException;

/**
 * Non blocking interpreter, largely inspired from previous assignments. A
 * single loops continuously reads user input and delegates commands to a thread
 * pool with completable future.
 * 
 * @author etis3
 *
 */
public class NonBlockingInterpreter implements Runnable {
	private static final String  desktopPath = System.getProperty("user.home") + "/Desktop";
	private static final String  defaultClientFolder = desktopPath + "/hm3client/";
	private static final String  defaultClientDownloadsFolder = desktopPath + "/hm3client/DOWNLOADS/";
	private static final String  defaultServerFolder = desktopPath + "/hm3server/";
	private static final String  nobodyUser = "[ ]";
	
	private static final String PROMPT = "> ";
	private static final Scanner console = new Scanner(System.in);
	private boolean receivingCommands = false;
	private FileCatalog fileCatalog;
	private String jwtToken;
	private String loggedInUser = nobodyUser;

	private FileChangeListener fileChangeListener;
	

	/**
	 * Starts the interpreter, if not starting yet.
	 * 
	 * @param fileCatalog
	 */
	public void start(FileCatalog fileCatalog) {
		this.fileCatalog = fileCatalog;
		if (receivingCommands)
			return;
		jwtToken = null;
		receivingCommands = true;
		new Thread(this).start();
	}

	/**
	 * Loop for receiving commands
	 */
	@Override
	public void run() {
		while (receivingCommands) {
			try {
				CommandHandler commandHandler = new CommandHandler(readNextLine());
				switch (commandHandler.getCommand()) {
				case REGISTER:
					String username = commandHandler.getParam(1);
					String password = commandHandler.getParam(2);
					fileCatalog.register(username, password);
					break;
				case LOGIN:
					
					String username1 = commandHandler.getParam(1);
					String password1 = commandHandler.getParam(2);
					cleanAfterLogout();
					jwtToken = fileCatalog.login(username1, password1);
					try {
						this.fileChangeListener = new FileChangeListenerImpl(username1);
						fileCatalog.addFileChangeListener(this.fileChangeListener);
						loggedInUser = username1;
					} catch (RemoteException e) {
						niceErrorPrint(e);
						return;	
					}
					break;
				case LIST:
					List<? extends FileDTO> files = fileCatalog.list(jwtToken);
					for (FileDTO file : files) {
						printDetails(file);
					}
					break;
				case DETAILS:
					String fileName = commandHandler.getParam(1);
					FileDTO file = fileCatalog.details(jwtToken, fileName);
					printDetails(file);
					break;
				case UPR:
					String pathFileToUploadReadOnly = defaultClientFolder + commandHandler.getParam(1);
					String newFileNameOnServerReadOnly = commandHandler.getParam(2);
					uploadHandler(pathFileToUploadReadOnly, newFileNameOnServerReadOnly, false);
//					if (newFileNameOnServerReadOnly == null || "".equals(newFileNameOnServerReadOnly)) {
//						newFileNameOnServerReadOnly = pathFileToUploadReadOnly
//								.substring(pathFileToUploadReadOnly.lastIndexOf('/') + 1);
//					}
//					new Thread(new TCPFileUpload(jwtToken, pathFileToUploadReadOnly, newFileNameOnServerReadOnly))
//							.start();
//
//					fileCatalog.upload(jwtToken, newFileNameOnServerReadOnly, false);
////					fileCatalog.checkLogin(jwtToken);
					break;
				case UPW:
					String pathFileToUpload = defaultClientFolder + commandHandler.getParam(1);
					String newFileNameOnServer = commandHandler.getParam(2);
					uploadHandler(pathFileToUpload, newFileNameOnServer, true);
					break;
				case DOWN:
					String fileNameToDL = commandHandler.getParam(1);
					String newNameDL = commandHandler.getParam(2);
					String targetDirectory = commandHandler.getParam(3);
					
					// If we do not provide a path, we use the default folder
					if (targetDirectory == null || "".equals(targetDirectory) ) {
						targetDirectory = defaultClientDownloadsFolder;
					}
					
					if (newNameDL == null || "".contentEquals(newNameDL)) {
						newNameDL = fileNameToDL;
					}

					// Don't forget to put a slash at the end
					new Thread(new TCPFileDownload(jwtToken, targetDirectory, fileNameToDL, newNameDL)).start();

					FileDTO filefile = fileCatalog.download(jwtToken, fileNameToDL, targetDirectory, newNameDL);
					safePrintln("(target directory:\n" + targetDirectory + ")");
					printDetails(filefile);
					break;
				case DELETE:
					String fileNameToDelete = commandHandler.getParam(1);
					fileCatalog.delete(jwtToken, fileNameToDelete);
					// TODO
					break;
				case LOGOUT:
					cleanAfterLogout();
					safePrintln("You have been logged out.");
					break;
				case QUIT:
					cleanAfterLogout();
					safePrintln("You have been logged out.");
					receivingCommands = false;
					safePrintln("Good bye!");
					break;
				case HELP:
//					for (Command command : Command.values()) {
//						if (command == Command.UNKNOWN) {
//							continue;
//						}
//						safePrintln(command.toString().toLowerCase());
//					}
					System.out.println("register 	username 		password\r\n" + 
							"login 		username 		password\r\n" + 
							"list \r\n" + 
							"details 	serverFileName\r\n" + 
							"upr 		pathToFile 		newName\r\n" + 
							"upw 		pathToFile 		newName\r\n" + 
							"down 		serverFileName 	 	downloadedFileName    [pathToWantedFolder]\r\n" + 
							"delete 		serverFileName\r\n" + 
							"logout\r\n" + 
							"quit\r\n" + 
							"help");
					break;
				default:
					safePrintln("Unrecognized command.");
				}
			} catch (Exception e) {
				niceErrorPrint(e);
			}
		}
		safePrintln("Program terminated.");
	}

	private String readNextLine() {
		safePrint(loggedInUser+PROMPT);
		return console.nextLine();
	}

	private void safePrintln(String s) {
		synchronized (System.out) {
			System.out.println(s);
		}
	}

	private void safePrint(String s) {
		synchronized (System.out) {
			System.out.print(s);
		}
	}

	private void niceErrorPrint(Exception e) {
		if (e.getClass().equals(FileException.class)) {
			safePrintln("[File Error]");
			safePrintln("  Details: \n  " + e.getMessage());
		}else if (e.getClass().equals(UserException.class)) {
			safePrintln("[User Error]");
			safePrintln("  Details: \n  " + e.getMessage());
		}
		else {
			safePrintln("[" + e.getClass().getName() + "]");
			safePrintln("  Message: " + e.getMessage());
			safePrintln("  Cause: " + e.getCause());
		}
	}

	private void printDetails(FileDTO file) {
//		safePrintln(file.getName() + " - " + file.getPermission() + " - " + file.getOwnerName() + " - "
//				+ file.getSize().toString());
		if (file == null) {
			safePrintln("The file could not be retrieved or does not exist!");
		} else {
			safePrintln("[ " + file.getPermission() + " ] " + file.getSize().toString() + " \t"
					+ normalizeFileNameString(file.getName(), 20) + "\t" + file.getOwnerName());
		}
	}

	private String normalizeFileNameString(String fileName, int newSize) {
		int currentStringSize = fileName.length();
		if (currentStringSize > newSize) {
			String truncatedString = fileName.substring(0, newSize - 3);
			return truncatedString + "...";
		} else {
			String whiteSpaces = " ".repeat(newSize - currentStringSize);
			return fileName + whiteSpaces;
		}
	}

	private void uploadHandler(String pathFileToUpload, String newFileNameOnServer, boolean writePermission) throws RemoteException, FileException, UserException {
		if (newFileNameOnServer == null || "".equals(newFileNameOnServer)) {
			newFileNameOnServer = pathFileToUpload.substring(pathFileToUpload.lastIndexOf('/') + 1);
//			newFileNameOnServer = pathFileToUpload;
		}
		TCPFileUpload uploadWritableFile = new TCPFileUpload(jwtToken, pathFileToUpload, newFileNameOnServer);
		int fileLength = uploadWritableFile.getActualFileLentgth();
		if (fileLength == 0) {
			throw new FileException("The file could not be transferred.");
		}
		fileCatalog.upload(jwtToken, newFileNameOnServer, writePermission, fileLength);
	}
	
	private void cleanAfterLogout() throws RemoteException {
		jwtToken = null;
		loggedInUser = nobodyUser;
		fileCatalog.removeFileChangeListener(this.fileChangeListener);
	}

}
