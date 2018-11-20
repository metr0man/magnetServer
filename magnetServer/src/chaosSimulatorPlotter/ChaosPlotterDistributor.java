package chaosSimulatorPlotter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import simulation.World;

public class ChaosPlotterDistributor extends Thread {
	private World world;
	private ArrayList<ConnectionHandler> connections;
	
	public ChaosPlotterDistributor(World world, ArrayList<ConnectionHandler> connections) {
		super();
		this.world = world;
		this.connections = connections;
	}
	
	public void run(){
		
		
		boolean running = true;
		
		
		
		
		
		
		
		//write data
		int lenPoints = 0;
		PrintWriter writer;
		writer = null;
		try {
			writer = new PrintWriter("output.txt","UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < numThreads; i++) {
			for (int j = 0; j < connections.get(i).getOutputArray().length; j++) {
				writer.println(Arrays.toString(connections.get(i).getOutputArray()[j]));
				lenPoints++;
			}
		}
		writer.close();
		System.out.println(lenPoints+" points in output.txt");
	}
}
