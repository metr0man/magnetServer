package chaosSimulatorPlotter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import simulation.World;

/**
 *  Defines a thread that handles the connection with one
 *  client.
 */
public class ConnectionHandler extends Thread {
    private Socket client; // The connection to the client.
    private InputStream inStream;
    private OutputStream outStream;
    
    //booleans for operation
    private boolean genFinished = false;
    private boolean active = true;
    private boolean newPoints = false;
    private boolean generating = false;
    private boolean disconnect = false;
    private boolean threadDead = false;
    
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
		//int width = 800;
		//int height = 800;
		int maxTicks = 100000;
		int posArraySize = 1000;
		
		World world = new World(posArraySize);
		
		//set world vars
		world.setMaxForce(1000);
		world.setHomeX(400);
		world.setHomeY(400);
		world.setDefaultCoef(10);
		world.setHomeCoef(10);
		world.setFriction(.95);
		world.setMaxStopDist(15);
		world.setHomeX(400);
		world.setHomeY(400);
		world.setMaxTicks(maxTicks);
		world.setDefaultMagnets(Main.getDefaultMagnets());
    			
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
    		if (!disconnect) {
	    		if (newPoints) {
	    			newPoints = false;
	    			generating = true;
	    			genFinished = false;
	    			//start generation
	    			System.out.println("sending new batch to "+clientAddress);
	    			try {
						startGeneration(currentPoints, world);
						System.out.println("output received from "+clientAddress);
		    			generating = false;
		    			genFinished = true;
	    			
	    			} catch (IOException e) {
						//failure code here
						System.out.println("generation error at "+clientAddress);
						System.err.println(e);
						disconnect = true;
						continue;
						
					}
	    			
	    			
	    		}
    		}
    		//wait to save resources
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
    	}
    	
    	//close connection
    	try {
			client.close();
			System.out.println("disconnected "+clientAddress);
		} catch (IOException e) {
			System.out.println("client disconnect error");
		}
    	//end of thread
    	threadDead = true;
    	
    }
    
    
    public void startGeneration(double[][] points, World world) throws IOException {
    	ObjectOutputStream objOut = new ObjectOutputStream(this.outStream);
    	ObjectInputStream objIn = new ObjectInputStream(this.inStream);
    	objOut.writeObject(points);
    	objOut.writeObject(world);
    	
    	try {
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
	public boolean getDisconnect() {return disconnect;}
	public boolean getThreadDead() {return threadDead;}
	public double[][] getOutputArray() {return outputArray;}
	public double[][] getCurrentPoints() {return currentPoints;}
	
	
	//setters
	public void setGenFinished( boolean genFinished) {this.genFinished = genFinished;}
	public void setActive (boolean active ) {this.active = active;}
    public void setNewPoints (boolean newPoints) {this.newPoints = newPoints;}
    public void setCurrentPoints(double[][] currentPoints) {this.currentPoints = currentPoints;}
    
}
