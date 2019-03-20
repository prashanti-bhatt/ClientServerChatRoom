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
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
 
public class Client2 extends JFrame implements ActionListener 
{
    final static int ServerPort = 1238;
    static Socket s;
    static  DataInputStream dis;
    static DataOutputStream dos;
    static Scanner scn;
    static String msg =  "";
    static Timer timer = new Timer();
    static boolean coordinatorResp = false;
    static int count = 0;
    static boolean needDecisionSent = false;
    static boolean decisionRecv = false;
    static File logfile;
    static String cordinatorString;
    static String date=java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.systemDefault())).toString();
    
    static JTextArea responseArea;
	static JTextArea displayResponse;							
	JButton sendBtn;										
	JButton abortBtn;											
	JButton commitBtn;
	JScrollPane scroll;
    
    public void initializeGUI() 
    { 	
    	this.setTitle("Client2");																								
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
		sendBtn.setBounds(250, 600, 100, 25);				
		sendBtn.addActionListener(this);					
		add(sendBtn);										
		
		commitBtn=new JButton("COMMIT");					    	
		commitBtn.setBounds(400, 600, 100, 25);			
		commitBtn.addActionListener(this);					
		add(commitBtn);										
		
		abortBtn=new JButton("ABORT");						  
		abortBtn.setBounds(550, 600, 100, 25);				
		abortBtn.addActionListener(this);					
		add(abortBtn);										

		this.setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) 
    {
    	try 
        {
    		if (e.getSource().equals(sendBtn)) 
    		{
            	sendResponse();
            } 
        	else if(e.getSource().equals(commitBtn))
        	{
        		sendCommitResponse();
        	}
        	else if(e.getSource().equals(abortBtn))
        	{
        		sendAbortResponse();
        	}
    	}
        catch (UnknownHostException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    void sendCommitResponse() throws IOException
    {
    	displayResponse.append("COMMIT\n");
    	
    	try 
    	{
            // write on the output stream
        	if(!s.isClosed())
        	{
        		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
						"Content-Length: 6\nUser-Agent: Chat App\n";
        		dos.writeUTF(httpMsg + " COMMIT");
        	}
        	
        	Files.write(Paths.get(logfile.getName()), "COMMIT\n".getBytes(), StandardOpenOption.APPEND);
        	int timerVal = 25;
    		//timer = new Timer();			
    		timer.scheduleAtFixedRate(new TimerTask()
    		{
    			int countdownVal = timerVal;
    			public void run() 
    			{
    				System.out.println(countdownVal--);
    				
    				if(msg.contains("GLOBAL COMMIT")) 
    				{
    					decisionRecv = true;
    					try {
							Files.write(Paths.get(logfile.getName()), cordinatorString.getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    					timer.cancel();
    				}
    				else if( msg.contains("GLOBAL ABORT"))  
    				{
    					decisionRecv = true;
    					timer.cancel();
    				}
    				else if(countdownVal < 0 && decisionRecv == false)
    				{
    					if(!msg.contains("NEED DECISION"))
    					{
    						if(!needDecisionSent)
    						{
    							count = 0;
    							displayResponse.append("NEED DECISION\n");
    							try 
    							{
    								needDecisionSent = true;
    								String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    										"Content-Length: 13\nUser-Agent: Chat App\n";
    								dos.writeUTF(httpMsg + " NEED DECISION");
    							} 
    							catch (IOException e1)
    							{
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    							}
    						}
    						
    						while(true)
    						{
    							try
    							{
    								if(msg.contains("COMMIT")) 
    								{
    									msg = "";
    									count++;
    								}
    								else if(msg.contains("ABORT"))  
    								{
    									msg = "";
    									displayResponse.append("GLOBAL ABORT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 12\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL ABORT");
    									break;
    								}	
							
    								if(count==2) 	
    								{	
    									displayResponse.append("GLOBAL COMMIT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 13\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL COMMIT");
    									Files.write(Paths.get(logfile.getName()), cordinatorString.getBytes(), StandardOpenOption.APPEND);
    									break;
    								}
    							}
    							catch(IOException e) 
								{										
									e.printStackTrace();
								}
    						}
    						timer.cancel();
    					}
    				}
    				else if(countdownVal < 0 && decisionRecv)
    					timer.cancel();
    			}       
    		}, 0, 1000);
        } 
    	catch (IOException e) 
    	{
            e.printStackTrace();
        }
    }
    
    void sendAbortResponse()
    {
    	displayResponse.append("ABORT\n");
    	try 
    	{
            // write on the output stream
        	if(!s.isClosed())
        	{
        		String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
						"Content-Length: 5\nUser-Agent: Chat App\n";
        		dos.writeUTF(httpMsg +" ABORT");
        	}
        	
        	int timerVal = 25;
    		//timer = new Timer();			
    		timer.scheduleAtFixedRate(new TimerTask()
    		{
    			int countdownVal = timerVal;
    			public void run() 
    			{
    				System.out.println(countdownVal--);
    				
    				if(msg.contains("GLOBAL COMMIT")) 
    				{
    					decisionRecv = true;
    					timer.cancel();
    				}
    				else if( msg.contains("GLOBAL ABORT"))  
    				{
    					decisionRecv = true;
    					timer.cancel();
    				}
    				else if(countdownVal < 0 && decisionRecv == false)
    				{
    					if(!msg.contains("NEED DECISION"))
    					{
    						if(!needDecisionSent)
    						{
    							count = 0;
    							displayResponse.append("NEED DECISION\n");
    							try 
    							{
    								needDecisionSent = true;
    								String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    										"Content-Length: 13\nUser-Agent: Chat App\n";
    								dos.writeUTF(httpMsg + " NEED DECISION");
    							} 
    							catch (IOException e1)
    							{
    								// TODO Auto-generated catch block
    								e1.printStackTrace();
    							}
    						}
    						
    						while(true)
    						{
    							try
    							{
    								if(msg.contains("COMMIT")) 
    								{
    									msg = "";
    									count++;
    								}
    								else if(msg.contains("ABORT"))  
    								{
    									msg = "";
    									displayResponse.append("GLOBAL ABORT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 12\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg +" GLOBAL ABORT");
    									break;
    								}	
							
    								if(count==2) 	
    								{	
    									displayResponse.append("GLOBAL COMMIT\n");
    									String httpMsg="\nPOST HTTP/1.1\n"+"Date:"+date+"\n"+"Content-Type:application/x-www-form-urlencoded\n"+ 
    											"Content-Length: 13\nUser-Agent: Chat App\n";
    									dos.writeUTF(httpMsg + " GLOBAL COMMIT");
    									break;
    								}
    							}
    							catch(IOException e) 
								{										
									e.printStackTrace();
								}
    						}
    						timer.cancel();
    					}
    				}
    				else if(countdownVal < 0 && decisionRecv)
    					timer.cancel();
    			}       
    		}, 0, 1000);
        }
    	catch (IOException e) 
    	{
            e.printStackTrace();
        }    	
    }
    
    void sendResponse() throws IOException
    {
    	String msg = responseArea.getText();
    	responseArea.setText("");
    	displayResponse.append(msg+"\n");
    	
        try 
        {
            // write on the output stream
        	if(!s.isClosed())
        		dos.writeUTF(msg);
        	
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }		
    }
    
    public static void fileCreation() throws IOException
    {
    	if(!new File("client2.txt").exists())
    	{
    		logfile = new File("client2.txt");
    		logfile.createNewFile();
    	}
    	else if(new File("client2.txt").exists())
    	{
    		logfile = new File("client2.txt");
    	}
    	   	
    }
 
    public static void main(String args[]) throws UnknownHostException, IOException 
    {
    	Client2 cl = new Client2(); 
    	cl.initializeGUI();

    	fileCreation();
    	
    	if(logfile.exists())
    	{
    		BufferedReader br = null;
    		FileReader fr = null;
		
    		fr = new FileReader(logfile.getName());
    		br = new BufferedReader(fr);

    		String sCurrentLine;
    		while ((sCurrentLine = br.readLine()) != null) 
    		{
    			displayResponse.append(sCurrentLine + "\n");
    		}
    		displayResponse.append("\n");
    	}
    	
        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");
         
        // establish the connection
        s = new Socket(ip, ServerPort);
         
        // obtaining input and out streams
        dis = new DataInputStream(s.getInputStream());
        dos = new DataOutputStream(s.getOutputStream());
        
        while(true)
        {
        	msg = dis.readUTF();
        	displayResponse.append(msg+"\n");
        	String copyMsg = msg;
        	String[] writeTofile = copyMsg.split(":");
        	if(writeTofile.length > 2)
        		cordinatorString = writeTofile[1]+"\n";
        	//Files.write(Paths.get(logfile.getName()), writeTofile[1].getBytes(), StandardOpenOption.APPEND);
        }
    }
}