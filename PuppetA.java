import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.*;

/* Puppet A
 * This puppet is optimized for Job Type A
 * When it receives a Job Type A, it "works" for 2 seconds
 * otherwise it "works" for 10 seconds
 */

public class PuppetA {

	public static void main(String[] args) {

		args = new String[] { "127.0.0.1", "30122" };
		// get args for ip address and port
		if (args.length != 2) {
			System.err.println("Usage: java EchoClient <host name> <port number>");
			System.exit(1);
		}
		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		// Connect to server's socket
		try (Socket puppetSocket = new Socket(hostName, portNumber);
				ObjectOutputStream out = new ObjectOutputStream(puppetSocket.getOutputStream()); // writes to server
				ObjectInputStream serverIn = new ObjectInputStream(puppetSocket.getInputStream());// reads from server
		) {

			System.out.println("Connected to IP: " + hostName + ", Port: " + portNumber);
			Queue<Packet> doneList = new LinkedList<Packet>();// list of done packets
			// creates a thread having the done array list of packets and the write to
			// server object
			Thread sendServer = new ObjectOutputThread(puppetSocket, "server", out, doneList, new Object());
			sendServer.start(); // starts the threads
			// Read server request
			Packet serverRequest = null;
			while ((serverRequest = (Packet) serverIn.readObject()) != null) {

				System.out.println("New request: " + serverRequest.toString());

				// if its a job of the type that its meant to do, it will execute then sleep for
				// 2 seconds
				if (serverRequest.getJobType() == JobType.A) {
					System.out.println("Executing my optimized job.");
					TimeUnit.SECONDS.sleep(2);
				}
				// if its the other job, will sleep for 10 seconds
				else {
					System.out.println("Executing my non-optimized job.");
					TimeUnit.SECONDS.sleep(10);
				}
				serverRequest.setStatus(Status.DONE);
				// Send back done
				doneList.add(serverRequest);
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
