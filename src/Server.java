import java.awt.Color;
import java.awt.Font;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.ArrayList;
import java.util.Vector;

public class Server extends JFrame{
	private static final long serialVersionUID = 1L;
	
	ServerSocket server = null;
	Socket client= null;
	int port = 9999;

	String serverinfo="";
	JTextArea serverInfo = new JTextArea();
	JScrollPane sData = new JScrollPane(serverInfo);
	String clientinfo="";
	JTextArea clientInfo = new JTextArea();
	JScrollPane cData = new JScrollPane(clientInfo);
	
	ArrayList<String> clientList = new ArrayList<String>();
	Vector<ClientHandler> users = new Vector<ClientHandler>();
	
	
	public Server() {
		setSize(900,650);
		setVisible(true);
		setLayout(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		serverInfo.setFont(new Font("NSimSun", 1, 16));
		serverInfo.setForeground(Color.BLUE);
		serverInfo.setEditable(false);
		sData.setBounds(0, 0, 450, 600);
		sData.setBorder(BorderFactory.createLineBorder(Color.gray,5));
		add(sData);
		
		clientInfo.setFont(new Font("NSimSun", 1, 16));
		clientInfo.setForeground(Color.BLUE);
		cData.setBounds(450, 0, 450, 600);
		cData.setBorder(BorderFactory.createLineBorder(Color.gray, 5));
		add(cData);
			
	}
	
	void startServer() throws IOException {
		
		server = new ServerSocket(port);
		serverinfo="Server Established at port: "+port+"\n IP: "+InetAddress.getLocalHost();
		serverInfo.setText(serverinfo);
		while(true) {
			client=server.accept();
			ClientHandler thread = new ClientHandler(client);
			thread.start();
			users.add(thread);
		}
		
	}
	
	String getOnlineUsers() {
		String str="Online users are ";
		for(int i=0;i<clientList.size();i++) {
			str+=clientList.get(i)+",";
		}
		return str;
	}
	
	void boardcastToAll(String str) throws IOException {
		for(ClientHandler c : users) {
			c.sendMessage(str);
		}
	}
	
	void boardcastToOne(String str) throws IOException {
		for(ClientHandler c : users) {
			if(str.contains(c.name)) {
				c.sendMessage(str);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new Server().startServer();
	}
	
	class ClientHandler extends Thread{
		
		Socket client;
		String name="";
		DataInputStream dis =null;
		DataOutputStream dos= null;
		
		public ClientHandler(Socket client) throws IOException {
			this.client=client;
			dis = new DataInputStream(client.getInputStream());
			dos=new DataOutputStream(client.getOutputStream());
			name = dis.readUTF();
			clientList.add(name);
			serverinfo = "New client "+name+" connected.";
			serverInfo.append("\n"+serverinfo);
			boardcastToAll(serverinfo);
			if(clientList.size()>1) {
				String str=getOnlineUsers();
				dos.writeUTF(str);
			}
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					String msg = dis.readUTF();
					clientinfo=name+"::"+msg;
					if(msg.contains("@")) {
						boardcastToOne(clientinfo);
					}
					clientInfo.append("\n"+clientinfo);
				}catch(Exception e) {
					serverinfo = "Connection closed by: Client__"+name+".";
					serverInfo.append("\n"+serverinfo);
					clientList.remove(name);
					users.remove(this);
					try {
						boardcastToAll(serverinfo);
						client.close();
					} catch (IOException e1) {
						
					}
					break;
				}
				
				
				
			}
		}
		
		void sendMessage(String str) throws IOException {
				dos.flush();
				dos.writeUTF(str);
				
		}
		
	}
}






















