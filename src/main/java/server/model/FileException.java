package server.model;

/**
 * Thrown when we have issues with a file: the file name does not exist, etc *
 */
public class FileException extends Exception {
	private static final long serialVersionUID = -1383509514873635849L;

	public FileException(String reason) {
		super(reason);
	}

	public FileException(String reason, Throwable rootCause) {
		super(reason, rootCause);
	}
}
