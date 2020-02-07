package sbgods;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Start {

	private static final int PORT = 65000;  // random large port number
	@SuppressWarnings("unused")
	private static ServerSocket s;

	public static void main(String[] args) {
		try {
			s = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// shouldn't happen for localhost
		} catch (IOException e) {
			// port taken, so app is already running
			System.out.print("Application is already running, terminating");
			System.exit(0);
		}
		@SuppressWarnings("unused")
		SBGods sbgods = new SBGods();
	}
}
