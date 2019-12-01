package client.net;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import common.TcpFile;

public class TCPFileUpload implements Runnable{
	private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private BufferedInputStream bis = null;
    private DataOutputStream toServer = null;
    private String jwtToket;
    private String pathFileToUpload;
    private String newFileNameOnServer;
	private boolean connected = false;
	private final String delimeter = " ";
	private File userFile;
	
	
	private static final String host = "localhost";
	private static final int port = 5553;
	

	public TCPFileUpload(String jwtToken, String pathFileToUpload, String newFileNameOnServer) {
		this.jwtToket = jwtToken;
		this.pathFileToUpload = pathFileToUpload;
		this.newFileNameOnServer = newFileNameOnServer;
		startUpload();
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
	
	private void startUpload() {
		try {
			if(isValidFile()) {
				connect();
				uploadFile();
			}
			else {
				System.out.println("Server connection aborted");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isValidFile() {
		this.userFile = new File(pathFileToUpload);
		if(userFile.exists() && !userFile.isDirectory()) {
			return true;
		}
		else {
			System.out.println("File not found");
		}
		return false;
	}

    private void connect() throws
            IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_HALF_MINUTE);
        socket.setSoTimeout(TIMEOUT_HALF_HOUR);
        connected = true;
        toServer = new DataOutputStream(socket.getOutputStream());
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

	public void uploadFile(){
		int fileSize = (int) userFile.length();
		FileInputStream fis;

		try {
			fis = new FileInputStream(userFile);
			
			TcpFile tcpFile = new TcpFile();
			tcpFile.setOwner("gibson");
			tcpFile.setFilename(newFileNameOnServer);
			tcpFile.setFileSize(fileSize);
			
			byte [] fileByteArray  = new byte [fileSize];
			
			bis = new BufferedInputStream(fis);
			try {
				bis.read(fileByteArray,0,fileByteArray.length);
				
				tcpFile.setFileContents(fileByteArray);

				byte[] dataToSend = getSerializedByteArray(tcpFile);
				toServer.writeInt(dataToSend.length);
				toServer.writeChar('#');
				toServer.write(dataToSend,0,dataToSend.length);
				toServer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if(bis != null)
					try {
						bis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				if(fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if(socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	@Override
	public void run() {
	}

}
