package chaosSimulatorPlotter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener extends Thread {
	private ServerSocket listener;
	private boolean running = true;
	
	public ConnectionListener (ServerSocket listener) {
		this.listener = listener;
	}
	
	public void run () {
		while (running) {
			Socket connection = null;
			try {
				connection = listener.accept();
			} catch (IOException e) {
				if (running) {
					e.printStackTrace();
					System.out.print("oh no error in connection listener");
					continue;
				}
			}
            if (running) {
				ConnectionHandler handler = new ConnectionHandler(connection);
	            handler.start();
            }
		}
	}
	
	public void closeListener() {
		running = false;
		try {
			listener.close();
		} catch (IOException e) {
			System.out.println("listener close error");
		}
	}
}
