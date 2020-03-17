package com.confusinguser.sbgods;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Start {

	public static LeaderboardUpdater updater;
	public static SBGods sbgods;

	private static final int PORT = 65000;  // random large port number
	@SuppressWarnings("unused")
	private static ServerSocket s;

	public static void main(String[] args) {
		System.out.println("Starting the bot to run on the server: Skyblock Gods");

		try {
			s = new ServerSocket(PORT, 10, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// Shouldn't happen for localhost
		} catch (IOException e) {
			// Port taken, so app is already running
			System.out.print("Application is already running, terminating");
			System.exit(0);
		}

		sbgods = new SBGods();
		updater = new LeaderboardUpdater();
		Thread updaterThread = new Thread(updater);
		updaterThread.start();
	}


}
