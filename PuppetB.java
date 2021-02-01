import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

/* Puppet B
 * This puppet accepts jobs 
 * if it is of type b, it will "work" for 2 seconds
 * otherwise itll "work" for 10 seconds
 */

public class PuppetB {
	public static void main(String[] args) {

		args = new String[] { "127.0.0.1", "30123" };

		// get args for ip address and port
		if (args.length != 2) {
			System.err.println("Usage: java EchoClient <host name> <port number>");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);
		System.out.println("Started");
		// Connect to server's socket
		try (Socket puppetSocket = new Socket(hostName, portNumber);
				ObjectOutputStream serverOut = new ObjectOutputStream(puppetSocket.getOutputStream()); // stream to write
																										// text requests
																										// to server
				ObjectInputStream serverIn = new ObjectInputStream(puppetSocket.getInputStream());// stream to read
																									// object response
																									// from server
		) {
			System.out.println("Ready to go...");
			// Set up thread to send done packets to server
			Queue<Packet> doneList = new LinkedList<Packet>();
			Thread sendServer = new ObjectOutputThread(puppetSocket, "Server", serverOut, doneList, new Object());
			sendServer.start();
			// Read server request
			Packet serverRequest;
			System.out.println("Waiting for jobs...");
			while ((serverRequest = (Packet) serverIn.readObject()) != null) {
				System.out.println("New job: " + serverRequest.toString());
				// Spend time based on job type
				if (serverRequest.getJobType() == JobType.B) {
					System.out.println("Executing my optimized job.");
					TimeUnit.SECONDS.sleep(2);
				} else {
					System.out.println("Executing my non-optimized job.");
					TimeUnit.SECONDS.sleep(10);
				}
				// Change status
				serverRequest.setStatus(Status.DONE);
				// Send back done
				doneList.add(serverRequest);
			}
			System.out.println("Finished.");
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
