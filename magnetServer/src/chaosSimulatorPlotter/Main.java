package chaosSimulatorPlotter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulation.Magnet;

public class Main{
	private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
	public static int LISTENING_PORT = 42020;
	public static String ipAddress = "localhost";
	
	private static ArrayList<Magnet> defaultMagnets = new ArrayList<Magnet>();

	public static void main(String[] args) throws IOException, InterruptedException {
		try {
			File file = new File("config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			ipAddress = doc.getElementsByTagName("ip").item(0).getTextContent();
			LISTENING_PORT = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent());
		} catch (Exception e) {
			System.out.println("file read error");
		}
		
		try {
			File file = new File("magnets.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			
			NodeList nList = doc.getElementsByTagName("magnet");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				
				if(nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					int xPos = Integer.parseInt(eElement.getElementsByTagName("xPos").item(0).getTextContent());
					int yPos = Integer.parseInt(eElement.getElementsByTagName("yPos").item(0).getTextContent());
					int coef = Integer.parseInt(eElement.getElementsByTagName("coef").item(0).getTextContent());
					int magNum = i+1;
				
					System.out.println("magnet "+magNum+": "+xPos+", "+yPos+", "+coef);
					defaultMagnets.add(new Magnet(xPos, yPos, coef));
				}
			}
			
			System.out.println("found "+nList.getLength()+" magnets in magnets.xml");
			
		} catch (Exception e) {
			System.out.println("magnet file read error");
		}
		
		
		
		
		
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
		ArrayList<double[][]> batchOutputs = new ArrayList<double[][]>();
		int numBatches = batches.size();
		boolean running = true;
		while (running) {			
			//loop through connections 
			for (int i = 0; i < connections.size(); i++) {
				if (connections.get(i).getThreadDead()) {
					connections.remove(i);
					continue;
				}
			}
					
			for (int i = 0; i < connections.size(); i++) {
				if(connections.get(i).getActive()) {
					if (connections.get(i).getGenerating() == false) { //check if generating
						if (connections.get(i).getGenFinished() == true) {
							batchOutputs.add(connections.get(i).getOutputArray()); //add output
							finishedBatches.add(connections.get(i).getCurrentPoints());
							connections.get(i).setGenFinished(false); //reset genFinished
							System.out.println("finished "+finishedBatches.size()+"/"+numBatches+" batches, "+batches.size()+" remain, "+connections.size()+" clients running");
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
				//check if disconnect
				if (connections.get(i).getDisconnect()) {
					if (!connections.get(i).getCurrentPoints().equals(batches.get(batches.size()-1))) {
						batches.add(connections.get(i).getCurrentPoints());
					}
					connections.get(i).setActive(false);
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
		int maxX = 800;
		int minY = 0;
		int maxY = 800;
		
		int resX = 40;
		int resY = 40;
		
		try {
			File file = new File("config.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			
			numBatches = Integer.parseInt(doc.getElementsByTagName("numBatches").item(0).getTextContent());
			minX = Integer.parseInt(doc.getElementsByTagName("minX").item(0).getTextContent());
			maxX = Integer.parseInt(doc.getElementsByTagName("maxX").item(0).getTextContent());
			minY = Integer.parseInt(doc.getElementsByTagName("minY").item(0).getTextContent());
			maxY = Integer.parseInt(doc.getElementsByTagName("maxY").item(0).getTextContent());
			resX = Integer.parseInt(doc.getElementsByTagName("resX").item(0).getTextContent());
			resY = Integer.parseInt(doc.getElementsByTagName("resY").item(0).getTextContent());
			
			System.out.println("found setup in config.xml");
			
			System.out.println("numBatches "+numBatches);
			System.out.println("minX "+minX);
			System.out.println("maxX "+maxX);
			System.out.println("minY "+minX);
			System.out.println("maxY "+maxY);
			System.out.println("resX "+resX);
			System.out.println("resY "+resY);

		} catch (Exception e) {
			System.out.println("file read error");
		}
		
		
		
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
	
	//getter
	public static ArrayList<Magnet> getDefaultMagnets() {return defaultMagnets;}
}
