package client.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPFileUpload implements Runnable{
	private static final int TIMEOUT_HALF_HOUR = 1800000;
    private static final int TIMEOUT_HALF_MINUTE = 30000;
    private Socket socket;
    private BufferedInputStream bis = null;
    private OutputStream os = null;
    private String jwtToket;
    private String pathFileToUpload;
    private String newFileNameOnServer;
	private boolean connected = false;
	
	private static final String host = "localhost";
	private static final int port = 5553;
	

	public TCPFileUpload(String jwtToken, String pathFileToUpload, String newFileNameOnServer) {
		this.jwtToket = jwtToken;
		this.pathFileToUpload = pathFileToUpload;
		this.newFileNameOnServer = newFileNameOnServer;
		try {
			connect();
			uploadFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    }

	public void uploadFile(){
		File userFile = new File(pathFileToUpload);
		int fileSize = (int) userFile.length();
		FileInputStream fis;
		try {
			fis = new FileInputStream(userFile);
			
			String jwtToketPlusDelimeter = jwtToket + " " + newFileNameOnServer + " ";
			byte[] userMetadataByteArray = jwtToketPlusDelimeter.getBytes(Charset.forName("UTF-8"));
			
			byte [] fileByteArray  = new byte [fileSize];
			
			bis = new BufferedInputStream(fis);
			try {
				bis.read(fileByteArray,0,fileByteArray.length);
				
				os = socket.getOutputStream();
				
				byte[] byteArrayToSend = new byte[userMetadataByteArray.length + fileByteArray.length];
				System.arraycopy(userMetadataByteArray, 0, byteArrayToSend, 0, userMetadataByteArray.length);
				System.arraycopy(fileByteArray, 0, byteArrayToSend, userMetadataByteArray.length, fileByteArray.length);
				
				os.write(byteArrayToSend,0,byteArrayToSend.length);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				bis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
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
