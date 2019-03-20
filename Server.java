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


/* This is the passive server. It shows correspondence the clients. It has
 * a GUI text box that displays message between clients as they transit the server. 
 */

//packages
import java.awt.DisplayMode;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.net.*;
 
// Server class
public class Server extends JFrame 
{
 
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();
     
    // counter for clients
    static int i = 0;
    //text area for displaying client response
    static JTextArea displayResponse;
	JScrollPane scroll;
    
    public void initialize() //initialize method for GUI
    {
    	//main frame
    	this.setTitle("Server");																								
		this.setSize(900, 800);																						
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);																			
		getContentPane().setLayout(null);																						

		
		displayResponse = new JTextArea();								
		displayResponse.setBounds(10, 0, 880, 600);					
		displayResponse.setEditable(false);							
		add(displayResponse);
		
		//scroll bar
		scroll = new JScrollPane (displayResponse, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 0, 880, 600);
		add(scroll);
		this.setVisible(true);
    }
 
    
    public static void main(String[] args) throws IOException 
    {
    	Server ser = new Server();
    	ser.initialize();
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1238);
         
        Socket s;
        
        // running infinite loop for getting
        // client request
        while (true) 
        {
            // Accept the incoming request
            s = ss.accept();
 
            //System.out.println("New client request received : " + s);
            displayResponse.append("New client request received : " + s + "\n");
             
            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
             
            //System.out.println("Creating a new handler for this client...");
            displayResponse.append("Creating a new handler for this client...\n");
            
            // Create a new handler object for handling this request.
            ClientHandler mtch = new ClientHandler(s,"client " + i, dis, dos);
 
            // Create a new Thread with this object.
            Thread t = new Thread(mtch);
             
            //System.out.println("Adding this client to active client list");
            displayResponse.append("Adding this client to active client list\n");
            
            // add this client to active clients list
            ar.add(mtch);
 
            // start the thread.
            t.start();
 
            // increment i for new client.
            // i is used for naming only, and can be replaced
            // by any naming scheme
            i++;
 
        }
    }
}
    
    //client thread handler
	class ClientHandler implements Runnable 
    {
        Scanner scn = new Scanner(System.in);
        private String name;
        //input and output streams
        final DataInputStream dis;
        final DataOutputStream dos;
        Socket s;
        boolean isloggedin;
            
        // constructor
        public ClientHandler(Socket s, String name,
                                DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
            this.name = name;
            this.s = s;
            this.isloggedin=true;
        }
     
        @Override
        public void run() {
     
            String received;
            while (true) 
            {
                try
                {
                    // receive the string
                    received = dis.readUTF();
                    
                    Server.displayResponse.append(this.name + " : " + received + "\n");
                     
                    if(received.equals("logout")){
                        this.isloggedin=false;
                        this.s.close();
                        Server.ar.remove(this.name);
                        break;
                    }
                     
                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String MsgToSend = st.nextToken();
                    //String recipient = st.nextToken();
     
                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.ar) 
                    {
                        // if the recipient is found, write on its
                        // output stream
                        //if (mc.name.equals(recipient) && mc.isloggedin==true)
                    	if (!this.name.equals(mc.name) && mc.isloggedin==true) 
                        {
                    		//sending only the message and not the HTTP format 
                    		String copyMsg = MsgToSend;
                    		String[] writeTofile = copyMsg.split("\n");
                    		int len = writeTofile.length;
                            mc.dos.writeUTF(this.name+" : "+writeTofile[len-1]);
                        }
                    }
                } catch (IOException e) {
                    
                    e.printStackTrace();
                }
            }
            try
            {
                // closing resources
                this.dis.close();
                this.dos.close();
                 
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }