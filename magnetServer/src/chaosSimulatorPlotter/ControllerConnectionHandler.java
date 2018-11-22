package chaosSimulatorPlotter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import simulation.World;

public class ControllerConnectionHandler extends Thread{
	private Socket controller;
	private boolean alive;
	private ObjectInputStream objIn;
	private ObjectOutputStream objOut;

	public ControllerConnectionHandler(Socket controller, ObjectInputStream objIn, ObjectOutputStream objOut) {
		super();
		this.controller = controller;
		this.objIn = objIn;
		this.objOut = objOut;
	}
	
	public void run() {
		this.alive = true;
		try {
			World world = (World) objIn.readObject();
			double[][] points = (double[][]) objIn.readObject();
			Main.addWorldAndPointsToQueue(world, points);
			objOut.writeObject("confirm");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
