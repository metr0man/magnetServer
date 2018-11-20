package chaosSimulatorPlotter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
    	//setup world THIS SHOULD BE DONE SOMEWHERE ELSE
    	//WORLD GENERATION
		int width = 800;
		int height = 800;
		int maxTicks = 100000;
		int posArraySize = 1000;
		
		World world = new World(posArraySize);
		
		//set world vars
		world.setMaxForce(1000);
		world.setHomeX(400);
		world.setHomeY(400);
		world.setDefaultCoef(10);
		world.setHomeCoef(10);
		world.setFricition(.95);
		world.setMaxStopDist(15);
		world.setHomeX(400);
		world.setHomeY(400);
		world.setMaxTicks(maxTicks);
    			
		//setup connection
    	String clientAddress = client.getInetAddress().toString();
    	try {
    		 System.out.println("Connection from " + clientAddress );
             Main.addConnection(this);
             this.inStream = client.getInputStream();
             this.outStream = client.getOutputStream();
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
					startGeneration(currentPoints, world);
				} catch (IOException e) {
					System.out.println("send error");
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
    
    
    public void startGeneration(double[][] points, World world) throws IOException {
    	ObjectOutputStream objOut = new ObjectOutputStream(this.outStream);
    	ObjectInputStream objIn = new ObjectInputStream(this.inStream);
    	objOut.writeObject(points);
    	objOut.writeObject(world);
    	
    	try {
    		System.out.println(client.isConnected());
			this.outputArray = (double[][]) objIn.readObject();
			objOut.writeInt(1);
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
