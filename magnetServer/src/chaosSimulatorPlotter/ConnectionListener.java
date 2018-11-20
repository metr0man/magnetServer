package chaosSimulatorPlotter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionListener extends Thread {
	private ServerSocket listener;
	
	public ConnectionListener (ServerSocket listener) {
		this.listener = listener;
	}
	
	public void run () {
		while (true) {
			Socket connection;
			try {
				connection = listener.accept();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.print("oh no error in connection listener");
				continue;
			}
            ConnectionHandler handler = new ConnectionHandler(connection);
            handler.start();
            System.out.println("new connection");
		}
	}
}
