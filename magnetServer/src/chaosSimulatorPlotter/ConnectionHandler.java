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
    private boolean genFinished;
    private double[][] outputArray;
    public ConnectionHandler(Socket socket) {
        this.client = socket;
        this.genFinished = false;
    }
    public void run() {
    	String clientAddress = client.getInetAddress().toString();
    	try {
    		 System.out.println("Connection from " + clientAddress );
             Main.addConnection(this);
             this.inStream = client.getInputStream();
             this.outStream = client.getOutputStream();
    	}
    	catch(Exception e){
    		
    	}
    	
    	
    	
    	// (code copied from the original DateServer program)
//        String clientAddress = client.getInetAddress().toString();
//        try {
//            System.out.println("Connection from " + clientAddress );
//            BufferedInputStream incoming = new BufferedInputStream(client.getInputStream());
//            PrintWriter outgoing;   // Stream for sending data.
//            outgoing = new PrintWriter( client.getOutputStream() );
//            OutputStream outStream = client.getOutputStream();
//            outStream.write(arg0);
//            outgoing.flush();  // Make sure the data is actually sent!
//            client.close();
//        }
//        catch (Exception e){
//            System.out.println("Error on connection with: " 
//                    + clientAddress + ": " + e);
//        }
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	this.genFinished = true;
    	
    }
	public boolean isGenFinished() {
		return genFinished;
	}
	public double[][] getOutputArray() {
		return outputArray;
	}
	
    
    
}
