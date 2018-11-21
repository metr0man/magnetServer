package chaosSimulatorPlotter;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import simulation.Logic;
import simulation.World;

public class Main{
	private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
	public static final int LISTENING_PORT = 42028;
	public static final String ipAddress = "localhost";
		
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket listener;  // Listens for incoming connections.
        listener = new ServerSocket(LISTENING_PORT, 5, InetAddress.getByName(ipAddress));
        System.out.println("Listening on port " + LISTENING_PORT);
		
        //start connection listener
        ConnectionListener connectionListener = new ConnectionListener(listener);
        connectionListener.start();        
        
        System.out.println("waiting for connection");
        
        
        
		
		//start timer
		final long startTime = System.currentTimeMillis();
	
		//divide batches
		ArrayList<double[][]> batches = pointBatchSplitter();
		
		//main generation loop
		ArrayList<double[][]> finishedBatches = new ArrayList<double[][]>();;
		//ArrayList<double[][]> currentBatches = new ArrayList<double[][]>();;
		ArrayList<double[][]> batchOutputs = new ArrayList<double[][]>();
		int numBatches = batches.size();
		boolean running = true;
		while (running == true) {			
			//loop through connections 
			for (int i = 0; i < connections.size(); i++) {
				if (connections.get(i).getGenerating() == false) { //check if generating
					if (connections.get(i).getGenFinished() == true) {
						batchOutputs.add(connections.get(i).getOutputArray()); //add output
						connections.get(i).setGenFinished(false); //reset genFinished
					}
					//check if new points
					if (batches.size() > 0) {
						//batches available
						connections.get(i).setCurrentPoints(batches.get(0));
						batches.remove(0);
						connections.get(i).setNewPoints(true);
					} else {
						//no more batches
						connections.get(i).setActive(false);
					}
				}
			}
			
			if (batchOutputs.size() >= numBatches) {
				running = false;
				break;
			}
			
			//sleep thread to save resources
			Thread.sleep(100);
		}
		
		//deactivate all connection threads
		connectionListener.closeListener();
		for (int i = 0; i < connections.size(); i++) {
			connections.get(i).setActive(false);
		}
		
		//stop timers
		final long endTime = System.currentTimeMillis();
		double execTime = (endTime - startTime)/(double)1000;
		System.out.println("done with generation");
		System.out.println("program took: "+execTime+" s");

		//writer  
		System.out.println("writing to file");
		PrintWriter writer = new PrintWriter("output.txt","UTF-8");
		for (int j = 0; j < batchOutputs.size(); j++) {
			for(int i = 0; i < batchOutputs.get(j).length; i++) {
				double[][] output = batchOutputs.get(j);
				writer.println("["+output[i][0]+", "+output[i][1]+", "+output[i][2]+", "+output[i][3]+"]");
			}
		}
		writer.close();
		
		
	}
	
	public static void addConnection(ConnectionHandler connection) {
		connections.add(connection);
		
	}
	
	public static ArrayList<double[][]> pointBatchSplitter(){
		int numBatches = 4;
		
		//setup vars
		int minX = 0;
		int maxX = 400;
		int minY = 0;
		int maxY = 400;
		
		int resX = 20;
		int resY = 20;
		
		//define vars for later
		int numPoints = resX*resY;
		double gapX = ((double)maxX - minX)/(resX - 1);
		double gapY = ((double)maxY - minY)/(resY - 1);
		int spaceX = 0; //keep at zero
		int spaceY = 0; //keep at zero
		
		
		
		//generate points
		double totalPoints[][] = new double[numPoints][2];
		for (int i = 0; i < resX; i++) {
			for (int j = 0; j < resY; j++) {
				totalPoints[i*resX+j][0] = i*gapX+minX+spaceX;
				totalPoints[i*resX+j][1] = j*gapY+minY+spaceY;
			}
		}
		
		//divide points
		ArrayList<double[][]> batches = new ArrayList<double[][]>();
		double pointsPerBatch = (double)numPoints/numBatches;
		for(int i = 0; i < numBatches; i++) {
			int startIndex = (int)(pointsPerBatch*i);
			int endIndex = (int)(pointsPerBatch*(i+1));
			batches.add(Arrays.copyOfRange(totalPoints, startIndex, endIndex));
		}
		
		return batches;
	}
}
