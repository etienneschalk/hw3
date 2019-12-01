package tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
	private int id;
	private FileServerUpload server;
	private FileServerDownload downloadServer;
    private final Socket clientSocket;
    private boolean connected;
    private InputStream fromClient;
    private OutputStream toClient;
    private FileInputStream fis;
    private BufferedOutputStream bos;
    private BufferedInputStream bis;
    private FileOutputStream fos;
    private int bytesRead;
    private int current = 0;
    private String clientIntent;
	private String clientJwtString;
	private String providedFileName;
	private final String serverDirectory = "C:/Users/Gibson/Desktop/hm3server/";
	private final String okStatusCode = "200";
	private final String fileNotFoundStatusCode = "404";

	
	public ClientHandler(FileServerUpload server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		connected = true;
		this.clientIntent = "upload";
		System.out.println("Client connected for file uploading");
	}
	
	public ClientHandler(FileServerDownload server, Socket clientSocket) {
		this.downloadServer = server;
		this.clientSocket = clientSocket;
		connected = true;
		this.clientIntent = "download";
		System.out.println("Client connected for file downloading");
	}

	/**
     * The run loop handling all communication with the connected client.
     */
    @Override
    public void run() {
        if(clientIntent == "download") {
        	sendFileToClient();
        }
        else if(clientIntent == "upload") {
        	receiveFileFromClient();
        }
    }
    
    private void sendFileToClient() {
    	try {
			fromClient = clientSocket.getInputStream();
			
			byte[] fileNamerByteArray  = new byte [1024];
            bytesRead = fromClient.read(fileNamerByteArray,0,fileNamerByteArray.length);
            current = bytesRead;
            
            System.out.println(bytesRead);
            do {
                bytesRead =
                   fromClient.read(fileNamerByteArray, current, (fileNamerByteArray.length-current));
                System.out.println(bytesRead);
                if(bytesRead >= 0) current += bytesRead;
             } while(bytesRead > -1);
            
            System.out.println(serverDirectory + new String(fileNamerByteArray));
            File requestedFile = new File(serverDirectory + new String(fileNamerByteArray));
            
            if(requestedFile.exists() && !requestedFile.isDirectory()) {
            	int fileSize = (int) requestedFile.length();
            	byte[] fileByteArray = new byte[fileSize];
            	byte[] okStatusByteArray = okStatusCode.getBytes();
            	
            	
            	fis = new FileInputStream(requestedFile);
            	bis = new BufferedInputStream(fis);

            	bis.read(fileByteArray,0,fileByteArray.length);
				
            	byte[] byteArrayToSend = new byte[okStatusByteArray.length + fileByteArray.length];
				System.arraycopy(okStatusByteArray, 0, byteArrayToSend, 0, okStatusByteArray.length);
				System.arraycopy(fileByteArray, 0, byteArrayToSend, okStatusByteArray.length, fileByteArray.length);
				
				System.out.println(new String(byteArrayToSend));
				toClient = clientSocket.getOutputStream();
				toClient.write(byteArrayToSend,0,byteArrayToSend.length);
				toClient.flush();
            }
            else {
            	
            	byte[] notFoundStatusCode = fileNotFoundStatusCode.getBytes();
            	toClient = clientSocket.getOutputStream();
				toClient.write(notFoundStatusCode,0,notFoundStatusCode.length);
				toClient.flush();
            }
           
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally {
    		try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		try {
				bis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		try {
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    }
    
    private void receiveFileFromClient() {
    	try {
            fromClient = clientSocket.getInputStream();

            List<Byte> jwtStringByteList = new ArrayList<Byte>();
            List<Byte> fileNameByteList = new ArrayList<Byte>();
            
            byte[] fileByteArray  = new byte [6022386];
            bytesRead = fromClient.read(fileByteArray,0,fileByteArray.length);
            current = bytesRead;
             
            do {
                bytesRead =
                   fromClient.read(fileByteArray, current, (fileByteArray.length-current));
                if(bytesRead >= 0) current += bytesRead;
             } while(bytesRead > -1);
            

            String del = " ";
            byte[] delimeterArray = del.getBytes(Charset.forName("UTF-8"));
            byte delimeter = delimeterArray[0];
            //extract filename and jwt from the byte array received
            int fileStartIndex = 0;
            
            int jwtIndexEnd = 0;
            for(int i = 0; i < fileByteArray.length; i++) {
            	byte b = fileByteArray[i];
            	if(b == delimeter) {
            		jwtIndexEnd = i + 1;
            		break;
            	}
            	jwtStringByteList.add(b);
            }
            
      
            for(int i = jwtIndexEnd; i < fileByteArray.length; i++) {
            	byte b = fileByteArray[i];
            	if(b == delimeter) {
            		fileStartIndex = i + 1;
            		break;
            	}
            	fileNameByteList.add(b);
            }

          //convert list of bytes of jwt to byte array
            byte[] jwtArray = new byte[jwtStringByteList.size()];
            for (int index = 0; index < jwtStringByteList.size(); index++) {
            	jwtArray[index] = jwtStringByteList.get(index);
            }
            
            //convert list of bytes of file name to byte array
            byte[] fileNameArray = new byte[fileNameByteList.size()];
            for (int index = 0; index < fileNameByteList.size(); index++) {
            	fileNameArray[index] = fileNameByteList.get(index);
            }
            
            //convert byte arrays to strings
            clientJwtString = new String(jwtArray);
            providedFileName = new String(fileNameArray);
            //directory
            fos = new FileOutputStream(serverDirectory  + providedFileName);
            bos = new BufferedOutputStream(fos);
            
            bos.write(fileByteArray, fileStartIndex , current);
            bos.flush();
            
            System.out.println("File uploaded");
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        finally {
        	try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	try {
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }    	
    }
}
