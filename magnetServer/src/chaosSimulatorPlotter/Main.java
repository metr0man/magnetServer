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
	public static final int LISTENING_PORT = 42022;
	public static final String ipAddress = "10.2.22.159";
	public static ChaosPlotterDistributor chaosPlotDistri;
		
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, InterruptedException {
		ServerSocket listener;  // Listens for incoming connections.
        Socket connection;
        listener = new ServerSocket(LISTENING_PORT, 5, InetAddress.getByName(ipAddress));
        System.out.println("Listening on port " + LISTENING_PORT);
		
        //start connection listener
        ConnectionListener connectionListener = new ConnectionListener(listener);
        connectionListener.start();        
        
        System.out.println("waiting for connection");
        while (connections.size() == 0) {
        	Thread.sleep(100);
        }
        
        
		System.out.println("Starting Generation...");
		
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
		
		//start timer
		final long startTime = System.currentTimeMillis();
	
		ArrayList<double[][]> batches = pointBatchSplitter();
		
		//
		boolean running = true;
		while (running == true) {
			
		}
		
		
		
		
		
		
		System.out.println("done with generation");

		//stop timers
		final long endTime = System.currentTimeMillis();
		double execTime = (endTime - startTime)/(double)1000;
		System.out.println("program took: "+execTime+" s");

		
		
		
		//writer   OLD
		/*System.out.println("writing to file");
		PrintWriter writer = new PrintWriter("output.txt","UTF-8");
		for(int i = 0; i < output.length; i++) {
			writer.println("["+output[i][0]+", "+output[i][1]+", "+output[i][2]+", "+output[i][3]+"]");
		}
		writer.close();
		*/
		
		//logWriter.println("program took: "+execTime+" s, "+timePerPoint+" s per point");
		
		//close file
		//writer.close();
		//logWriter.close();
		//System.out.println("data in output.txt");
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
