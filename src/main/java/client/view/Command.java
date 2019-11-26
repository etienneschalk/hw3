package client.view;

/**
 * Commands the user can type in the console. Defines the possibles interactions
 * to the file catalog
 * 
 * @author etis3
 *
 */
public enum Command {
	
	/**
	 * Register to the file catalog users
	 * Example: register username password
	 */
	REGISTER,

	/**
	 * Login to the file catalog
	 * Example: login username password
	 */
	LOGIN,
	
	/**
	 * List all the files of the catalog
	 * Example: list
	 */
	LIST,
	
	/**
	 * View only the meta-information about the file, like size and owner
	 * Example: details file.txt
	 */
	DETAILS,
	
	/**
	 * Uploading a file to the catalog WITH READ ONLY
	 * Examples: 
	 * - upload /my/path/to/file.txt
	 * - upload /my/path/to/file.txt new_name.txt
	 */
	UPR,
	
	/**
	 * Uploading a file to the catalog WITH WRITE PERMISSION
	 * Examples: 
	 * - upload /my/path/to/file.txt
	 * - upload /my/path/to/file.txt new_name.txt
	 */
	UPW,
	
	/**
	 * Downloading a file from the catalog, if available. The user specifies the target folder.
	 * Example: download file.txt C:\Users\etis3\Downloads
	 */
	DOWN,
	
	/**
	 * Delete a file
	 * Example: delete file.txt
	 */
	DELETE,
	
	/**
	 * Logout and erases jwt token
	 * Example: logout
	 */
	LOGOUT,
	
	/**
	 * Quit the session
	 * Example: quit
	 */
	QUIT,
	
	/**
	 * Help, what can I do ?
	 * Example: help
	 */
	HELP,
	
	/**
	 * Not recognized command
	 * Example: djslajdsakldsa
	 */
	UNKNOWN

}
