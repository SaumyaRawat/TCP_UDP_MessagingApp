import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.*;
import java.util.*;
import java.net.*;

public class FileServer extends Thread {
	
	private ServerSocket ss;
	
	public FileServer(int port) {
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			try {
				Socket clientSock = ss.accept();
                DataInputStream dis = new DataInputStream(clientSock.getInputStream());
                DataOutputStream dos = new DataOutputStream(clientSock.getOutputStream());
                String message = dis.readUTF();
                System.out.println("Client : " + message);
                // Integer sendComplete=0;
                String[] message_recvd = message.split(" ");
                if(message_recvd[0].equals("Sending")){
                	if(message_recvd[2].equals("TCP")){
                		saveFileTCP(clientSock,message_recvd[1]);
                	}
                	else if(message_recvd[2].equals("UDP")){
                		saveFileUDP(message_recvd[1]);
                	}
                	System.out.println("Received file");
                }
                else if(message_recvd[0].equals("quit")){
                    ss.close();
                    System.exit(0);
                }
 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveFileTCP(Socket clientSock, String filename) throws IOException {
		
		System.out.println("Receiving " + filename);
		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream("Received/" + filename);
		byte[] buffer = new byte[4096];
		
		int filesize = 15123; // Send file size in separate msg
		int read = 0;
		int totalRead = 0;
		int remaining = filesize;
		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;
			System.out.println("read " + totalRead + " bytes.");
			fos.write(buffer, 0, read);
		}
		
		fos.close();
		dis.close();
	}
	
	private void saveFileUDP(String filename)  throws IOException {
		byte b[]=new byte[1024];
        DatagramSocket dsoc=new DatagramSocket(1988);
        FileOutputStream fos=new FileOutputStream("Received/" + filename);
        while(true)
        {
        	DatagramPacket dp=new DatagramPacket(b,b.length);
            dsoc.receive(dp);
            if(dp.getData() == null){
              	break;
            }
            //System.out.println(new String(dp.getData(),0,dp.getLength()));
            //System.out.println("read " + totalRead + " bytes.");
			fos.write(dp.getData(),0,dp.getLength());                             
        }
        fos.close();
		dsoc.close();
	}

	public static void main(String[] args) {
		FileServer fs = new FileServer(1988);
		fs.start();
	}

}
class FileClient {
	
	private Socket s;
	
	public FileClient(String host, int port) {
		while (true)
		{
			try {
				s = new Socket(host, port);
	            Scanner scn = new Scanner(System.in);
	            String send_message;
	            DataInputStream dis_client = new DataInputStream(s.getInputStream());
	            DataOutputStream dos_client = new DataOutputStream(s.getOutputStream());
	            send_message = scn.nextLine();
	            dos_client.writeUTF(send_message);
	            String[] message_sent = send_message.split(" ");
                if(message_sent[0].equals("Sending")){
                	String file = message_sent[1];
                	if(message_sent[2].equals("TCP")){
						sendFileTCP(file);
                	}
                	else if(message_sent[2].equals("UDP")){
						sendFileUDP(file);
                	}
                	System.out.println("Sent file");
                }
                else if(message_sent[0].equals("quit")){
                    s.close();
                    System.exit(0);
                }
				
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
	}
	
	public void sendFileTCP(String file) throws IOException {
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		
		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}
		
		fis.close();
		dos.close();	
	}

	public void sendFileUDP(String file) throws IOException {
	    byte[] b=new byte[1024];
        FileInputStream f=new FileInputStream(file);
        DatagramSocket dsoc=new DatagramSocket(2000);
        int i=0;
        while(f.available()!=0)
        {
        	f.read(b);
        	dsoc.send(new DatagramPacket(b,b.length,InetAddress.getLocalHost(),1988));
        	i++;
        }                     
        f.close();
        dsoc.close();
 	}
 		
	public static void main(String[] args) {
		FileClient fc = new FileClient("localhost", 1988);
	}
}


