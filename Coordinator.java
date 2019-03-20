//Prashanti Nitin Bhatt
//1001518244

/*code citations
For basic client server chat room: 
'https://www.geeksforgeeks.org/multi-threaded-chat-application-set-1/'
'https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/'
FOR 2PC and other functionality:
Textbook pseudocode
'https://www.mkyong.com/java/how-to-read-file-from-java-bufferedreader-example/'
'https://stackoverflow.com/questions/1053467/how-do-i-save-a-string-to-a-text-file-using-java'
'http://www.baeldung.com/java-timer-and-timertask'
'http://www.javaprogrammingforums.com/java-swing-tutorials/38-java-program-add-scroll-bars-jtextarea-using-jscrollpane.html'
'https://stackoverflow.com/questions/8849063/adding-a-scrollable-jtextarea-java'
'https://stackoverflow.com/questions/7375827/how-to-print-text-to-a-text-area'
*/

import java.io.*;
import java.util.*;
import java.net.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent; 			
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//This is the coordinator class. It deals with the main 2PC logic
public class Coordinator extends JFrame implements ActionListener 
{
	final static int ServerPort = 1238;
	static Timer timer = new Timer();
	static int count = 0;
	static Socket sock;
	static String msg =  "";
	static  DataInputStream dis;
    static DataOutputStream dos;
    static String date=java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.systemDefault())).toString();

	static JTextArea responseArea;
	static JTextArea displayResponse;
	JButton sendBtn;										
	JScrollPane scroll;
	
	public void initializeGUI() //method to initialize GUI
    { 	
    	this.setTitle("Coordinator");																								
		this.setSize(900, 800);																								
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);																			
		getContentPane().setLayout(null);																						

		displayResponse = new JTextArea();								
		displayResponse.setBounds(10, 0, 880, 400);					
		displayResponse.setEditable(false);							
		add(displayResponse);
		
		scroll = new JScrollPane (displayResponse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, 880, 400);
		add(scroll);
		//setVisible (true);
		
		responseArea = new JTextArea();							
		responseArea.setBounds(10, 450, 880, 100);				
		add(responseArea);											

		sendBtn = new JButton("SEND");						
		sendBtn.setBounds(400, 600, 100, 25);				
		sendBtn.addActionListener(this);					
		add(sendBtn);

		this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) // action performed when send is clicked
    {
    	try 
        {
    		if (e.getSource().equals(sendBtn)) 
    		{
    			sendVoteRequest();
            } 
    	}
        catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
	
	void sendVoteRequest() throws IOException //method to send vote request
	{
		
		displayResponse.append(responseArea.getText() + ": VOTE_REQUEST\n");
		int len = responseArea.getText().length() + 14;
		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
				"Content-Length: " + len + "\nUser-Agent: Chat App\n";
		dos.writeUTF(httpMsg + " " + responseArea.getText() + ": VOTE_REQUEST\n");
		responseArea.setText("");
		
		int timerVal = 30;

		timer.scheduleAtFixedRate(new TimerTask()
		{
			int i = timerVal;
			public void run() // method is executed to start timer 
			{
				System.out.println(i--);
				try 
				{		
					if(msg.contains("COMMIT")) //Checks for commit case
					{
						msg = "";
						count++;
					} 
					else if ( msg.contains("ABORT") ||i < 0) //Checks for abort case  
					{	
						displayResponse.append("GLOBAL ABORT\n");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 12\nUser-Agent: Chat App\n";
						dos.writeUTF(httpMsg + " GLOBAL ABORT");
						timer.cancel();
					}		
					
					if(count==3) 	
					{
						displayResponse.append("GLOBAL COMMIT\n");
						String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
								"Content-Length: 13\nUser-Agent: Chat App\n";
						dos.writeUTF(httpMsg + " GLOBAL COMMIT");
						timer.cancel();
					}
				}
				catch (IOException e) 
				{										
					e.printStackTrace();
				}
				}       
		}, 0, 1000);						

	}
	public static void main(String[] args) throws IOException 
	{
		new Coordinator().initializeGUI();
		// TODO Auto-generated method stub
		InetAddress ip = InetAddress.getByName("localhost");
		sock = new Socket(ip, ServerPort);
		dis = new DataInputStream(sock.getInputStream());
		dos = new DataOutputStream(sock.getOutputStream());
				
		while(true)
	    {
	       	msg = dis.readUTF();
	       	displayResponse.append(msg+"\n");
	    }
	}
}

