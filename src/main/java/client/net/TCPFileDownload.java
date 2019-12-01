package client.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import common.TcpFile;
import common.TcpMessage;

public class TCPFileDownload implements Runnable{
	private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    
    private BufferedInputStream bis = null;
    private FileOutputStream fos;
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private BufferedOutputStream bos;
    private String jwtToket;
    private String pathToSaveFile;
    private String fileToDownload;
    private String newFilename;
	private boolean connected = false;
	private boolean autoFlush = true;

	private static final String host = "localhost";
	private static final int port = 5554;
	

	public TCPFileDownload(String jwtToken, String pathToSaveFile, String fileToDownload, String newFilename) {
		this.jwtToket = jwtToken;
		this.pathToSaveFile = pathToSaveFile;
		this.fileToDownload = fileToDownload;
		this.newFilename = newFilename;
		
		initDownload();
	}
	
	private void initDownload() {
		File file = new File(pathToSaveFile);
		if(file.isDirectory()) {
			try {
				connect();
				downloadFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Directory does not exist. Download aborted.");
		}
	}
	
	/**
     * Creates a new instance and connects to the specified server. Also starts a listener thread
     * receiving broadcast messages from server.
     *
     * @param host             Host name or IP address of server.
     * @param port             Server's port number.
     * @param broadcastHandler Called whenever a broadcast is received from server.
     * @throws IOException If failed to connect.
     */
    private void connect() throws
            IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        toServer = new DataOutputStream(socket.getOutputStream());
        fromServer = new DataInputStream(socket.getInputStream());
    }
    
    
    private byte[] getSerializedByteArray(Object objectToSerialize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);

			oos.writeObject(objectToSerialize);
			oos.flush();
			oos.close();

			return baos.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
    
    private void downloadFile() {
    	TcpMessage message = new TcpMessage(fileToDownload);
    	byte[] dataToSend = getSerializedByteArray(message);
    	try {
    		toServer.writeInt(dataToSend.length);
    		toServer.writeChar('#');
			toServer.write(dataToSend, 0, dataToSend.length);
			
			waitForServerResponse();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    //here we wait for server response
    private void waitForServerResponse() {
    	int logCounter = 0;
		System.out.println(logCounter++ + "Reading File Length");
		int messageLength;
		try {
			messageLength = fromServer.readInt();
			char sharp = fromServer.readChar(); 

			byte[] serializedMessage = new byte[messageLength];

			int actuallyReadBytes = 0;
			int totalReadBytes = 0;

			while (actuallyReadBytes != -1 && (totalReadBytes < messageLength - 1)) {
				actuallyReadBytes = fromServer.read(serializedMessage, actuallyReadBytes,
						messageLength - totalReadBytes);
				totalReadBytes += actuallyReadBytes;
			}
			
			if (totalReadBytes == messageLength) {
				ByteArrayInputStream bais = new ByteArrayInputStream(serializedMessage);
				ObjectInputStream ois = new ObjectInputStream(bais);
				try {
					TcpFile file = (TcpFile) ois.readObject();
					
					if(file.getResponseCode() == 200) {
						fos = new FileOutputStream(pathToSaveFile + newFilename);
						bos = new BufferedOutputStream(fos);
						
						bos.write(file.getFileContents(), 0, file.getFileContents().length);
						System.out.println("File downloaded");
					}
					else {
						System.out.println("Requested File does not exist.");
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				finally {
					if(bos != null) {
						bos.close();
					}
					
					if(fos != null) {
						fos.close();
					}
				}
			} else {
				throw new IOException("Data received is not complete.");
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			if(socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
    }
    
	@Override
	public void run() {
		
	}
}
