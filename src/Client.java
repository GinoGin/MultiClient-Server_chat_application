import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Client extends JFrame implements ActionListener, MouseListener{

	private static final long serialVersionUID = 1L;
	
	Socket client= null;
	DataInputStream dis= null;
	DataOutputStream dos = null;
	
	int port =9999;
	static String name="";
	static String ip="";
	int updateCount=0;
	String info="";
	
	JTextArea msg = new JTextArea();
	JScrollPane mData= new JScrollPane(msg);
	
	JButton btn = new JButton();
	
	
	DefaultListModel<String> clientModel = new DefaultListModel<String>();
	JList<String> clientList = new JList<String>(clientModel);
	JScrollPane cData = new JScrollPane(clientList);
	
	HashMap<String,NewTextArea> textArea = new HashMap<String,NewTextArea>();
	
	JScrollPane chatPane = new JScrollPane();

	public Client() {
		setSize(610,610);
		setVisible(true);
		setLayout(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		clientList.addMouseListener(this);
		cData.setBounds(0, 0, 250, 500);
		cData.setBorder(BorderFactory.createLineBorder(Color.gray,5));
		add(cData);
		
		mData.setBounds(1, 500, 525, 73);
		add(mData);
		
		btn.setBounds(535, 505, 70, 70);
		btn.setText("Send");
		btn.addActionListener(this);
		add(btn);
		
		chatPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		chatPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		chatPane.setBounds(250, 0, 350, 500);
		add(chatPane);
		
		
	}
	
	String[] getUserName(String str) {
		str=str.replace("Online users are ", "").replace(name, "");
		String users[]=null;
		String name="";
		int count=0,j=0;
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i)==',')
				count++;
		}
		users =new String[count];
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i)!=' ')
				name+=str.charAt(i);
			if(str.charAt(i)==',') {
				String uname=name.replace(",", "");
				users[j++]=uname;
				name="";	
			}
			
		}
		return users;
		
	}
	
	void initializeInfo(String  str) {
		String users[]=getUserName(str);
		for(int i=0;i<users.length-1;i++) {
			NewTextArea userM = new NewTextArea();
			clientModel.addElement(users[i]);
			textArea.put(users[i],userM);
		}
		
	}
	
	void addElement(String str) {
		String name = str.replace("New client","").replace("connected.","").replace(" ","");
		NewTextArea userM = new NewTextArea();
		clientModel.addElement(name);
		textArea.put(name, userM);
		
	}
	
	String stringBefore(String str, char c) {
		String uname="";
		for(int i=0;i<str.length();i++) {
			if(str.charAt(i)==c)
				return uname;
			uname+=str.charAt(i);
		}
		return null;
	}
	
	String replaceBefore(String str,char c) {
		String data = str.replace(name+"::@", "");
		for(int i=0; i<data.length(); i++)
		{
			if(data.charAt(i)==c)
			{
				return ("YOU: "+data.substring(i+1, data.length()));
			}
		}
		return null;
	}
	
	void setTextIntoField(String str) {
		String cName = stringBefore(str,':');
		if(str.contains("::") && cName.equals(name)) {
			textArea.get(clientList.getSelectedValue()).insertSelf(replaceBefore(str, ':')+"\n");
		}
		else {
			textArea.get(cName).insertOpponent(str.replace("::@"+name, "")+"\n");
		}
	}
	
	void removeInfo(String str) {
		String name ="";
		name =str.replace("Connection closed by: Client__", "").replace(".", "").replace(" ", "");
		for(int i=0; i<clientModel.size(); i++)
		{
			if(clientModel.get(i).equals(name))
			{
				try {
					clientModel.remove(i);
					textArea.remove(name);
				}catch(ArrayIndexOutOfBoundsException ex) {}
			}
		}
	}
	
	void run(String name) throws UnknownHostException, IOException {
		
		client=new Socket(ip,port);
		setTitle(name);
		dis = new DataInputStream(client.getInputStream());
		dos = new DataOutputStream(client.getOutputStream());
		dos.writeUTF(name);
		while(true) {
			try {
				String str=dis.readUTF();
				info=str;
				if(info.contains("New client")){
					addElement(info);
				}
				else if(info.contains("Online users") && updateCount==0) {
					initializeInfo(info);
					updateCount=1;
				}
				else if(info.contains("Connection closed")) {
					removeInfo(info);
				}
				else {
					setTextIntoField(info);
				}
			}catch(SocketException e) {
				JOptionPane.showMessageDialog(this, "Server Has Stopped/Closed.");
				break;
			}
		}
		
	}
	
	public static void main(String[] args) {
		try {
			name=JOptionPane.showInputDialog("Enter your name");
			ip = JOptionPane.showInputDialog("Enter the server IP address");
			if(name.isEmpty() || ip.isEmpty()) {
				System.exit(EXIT_ON_CLOSE);
			}
			try {
				new Client().run(name);
			}catch(Exception e) {
				JOptionPane.showMessageDialog(null, "There is no server to establish");
			}
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
	}
	
	void send(String str) throws IOException {
		dos.flush();
		String data=msg.getText();
		data="@"+str+": "+data;
		dos.writeUTF(data);
		msg.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
			if(e.getSource()==btn && msg.getText().length()!=0)  {
				try {
					send(clientList.getSelectedValue());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource()==clientList) {
			try {
				chatPane.getViewport().add(textArea.get(clientList.getSelectedValue()));
			}catch(IndexOutOfBoundsException ex) {}
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class NewTextArea extends JTextPane{
	
	private static final long serialVersionUID = 1L;
	
	StyledDocument doc =getStyledDocument();
	SimpleAttributeSet self = new SimpleAttributeSet();
	SimpleAttributeSet opponent = new SimpleAttributeSet();
	
	public NewTextArea() {
		StyleConstants.setForeground(self, Color.blue);
		StyleConstants.setAlignment(self, StyleConstants.ALIGN_LEFT);
		
		StyleConstants.setForeground(opponent, Color.red);
		StyleConstants.setAlignment(opponent, StyleConstants.ALIGN_LEFT);
	}
	
	void insertSelf(String str) {
		try	
	    {
	        doc.insertString(doc.getLength(), str, self );
	        doc.setParagraphAttributes(doc.getLength(), 1, self, false);
	    }
	    catch(Exception e) { }
	}
	public void insertOpponent(String str)
    {
    	try {
	        doc.insertString(doc.getLength(), str, opponent );
	        doc.setParagraphAttributes(doc.getLength(), 1, opponent, false);
    	}catch(Exception e) {}
    }
	
}











