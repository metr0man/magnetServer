package chaosSimulatorPlotter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import simulation.World;

/**
 *  Defines a thread that handles the connection with one
 *  client.
 */
public class ConnectionHandler extends Thread {
    private Socket client; // The connection to the client.
    private InputStream inStream;
    private OutputStream outStream;
    private ObjectOutputStream objOut;
    private ObjectInputStream objIn; 
    
    
    //booleans for opperation
    private boolean genFinished = false;
    private boolean active = true;
    private boolean newPoints = false;
    private boolean generating = false;
    //for communication with main loop
    private double[][] currentPoints;
    private double[][] outputArray;
    
    public ConnectionHandler(Socket socket) {
        this.client = socket;
        this.genFinished = false;
    }
    public void run() {
    	try {
			client.setKeepAlive(true);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    			
		//setup connection
    	String clientAddress = client.getInetAddress().toString();
    	try {
    		 System.out.println("Connection from " + clientAddress );
    		 this.inStream = client.getInputStream();
    		 this.outStream = client.getOutputStream();
    		 objOut = new ObjectOutputStream(this.outStream);
    		 objIn = new ObjectInputStream(this.inStream);
    		 String status = (String) objIn.readObject();
    		 System.out.println(status);
    		 if(status.equalsIgnoreCase("client")) {
    			 Main.addConnection(this);
    			 objOut.writeObject("confirmed as client");
    			 System.out.println("Confirmed as client");
    		 }
    		 else if(status.equalsIgnoreCase("controller")) {
    			 System.out.println("controller confirmation sending");
    			 objOut.writeObject("confirmed as controller");
    			 System.out.println("Confirmed as controller");
    			 ControllerConnectionHandler controller = new ControllerConnectionHandler(client, objIn, objOut);
    			 controller.start();
    			 while(controller.isAlive()) {
    				 Thread.sleep(1000);
    			 }
    		 }
    	}
    	catch(Exception e){
    		System.out.println("error in connection handler thread");
    	}

    	//generation loop
    	while (active) {
    		if (newPoints) {
    			newPoints = false;
    			generating = true;
    			genFinished = false;
    			//start generation
    			System.out.println("sending new batch to "+clientAddress);
    			try {
					startGeneration(currentPoints);
				} catch (IOException e) {
					//failure code here
					System.out.println("generation error");
				}
    			System.out.println("output received from "+clientAddress);
    			generating = false;
    			genFinished = true;
    			
    		}
    		
    		//wait to save resources
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				System.out.println("thread sleep error");
			}
    	}
    	
    	//close connection
    	try {
			client.close();
		} catch (IOException e) {
			System.out.println("client disconnect error");
		}
    	//end of thread
    }
    
    
    public void startGeneration(double[][] points) throws IOException {
    	World world = Main.getCurrentWorld();
    	objOut.writeObject(points);
    	objOut.writeObject(world);
    	
    	try {
			this.outputArray = (double[][]) objIn.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    	this.genFinished = true;
    	
    }
    
    //getters
	public boolean getGenFinished() {return genFinished;}
	public boolean getActive() {return active;}
	public boolean getNewPoints() {return newPoints;}
	public boolean getGenerating() {return generating;}
	public double[][] getOutputArray() {return outputArray;}
	public double[][] getCurrentPoints() {return currentPoints;}
	
	
	//setters
	public void setGenFinished( boolean genFinished) {this.genFinished = genFinished;}
	public void setActive (boolean active ) {this.active = active;}
    public void setNewPoints (boolean newPoints) {this.newPoints = newPoints;}
    public void setCurrentPoints(double[][] currentPoints) {this.currentPoints = currentPoints;}
    
}
