import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;

/* The Server
 * This server accepts multiple client at any point
 * It will not run until a puppet A & B are connected
 */

public class Server {

	public static void main(String[] args) throws IOException {

		final int maxMorePuppetJobs = 4; // The max amount of jobs that one puppet could have more of
		// Job Queues
		Queue<Packet> activeJobs = new LinkedList<Packet>();
		Queue<Packet> puppetAJobs = new LinkedList<Packet>();
		Queue<Packet> puppetBJobs = new LinkedList<Packet>();
		ArrayList<Packet> doneJobs = new ArrayList<Packet>();
		// puppetBJobs.offer(new Packet ( 5, Status.ACTIVE, Type.JOB, JobType.A));
		Object editingActiveJobs = new Object();
		Object editingPuppetAJobs = new Object();
		Object editingPuppetBJobs = new Object();
		Object editingDoneJobs = new Object();

		args = new String[] { "30121", "30122", "30123" };
		if (args.length != 3) {
			System.err.println("Usage: java EchoServer <client port number> <puppet port number>");
			System.exit(1);
		}

		System.out.println("Server started");
		// Connecting client sockets
		ServerSocket clientServerSocket = new ServerSocket(Integer.parseInt(args[0]));
		Thread settingUpNewClients = new SetUpNewClientsThread(clientServerSocket, activeJobs, editingActiveJobs,
				doneJobs, editingDoneJobs);
		settingUpNewClients.start();
		System.out.println("Listening to client socket: " + args[0]);
		
		try (// Puppet A connection
				ServerSocket puppetAServerSocket = new ServerSocket(Integer.parseInt(args[1]));
				Socket puppetASocket = puppetAServerSocket.accept();
				ObjectOutputStream puppetAOut = new ObjectOutputStream(puppetASocket.getOutputStream());
				ObjectInputStream puppetAIn = new ObjectInputStream(puppetASocket.getInputStream());
				// Puppet B connection - this part is repetitive but wanted to 
				// keep the resources in the try
				ServerSocket puppetBServerSocket = new ServerSocket(Integer.parseInt(args[2]));
				Socket puppetBSocket = puppetBServerSocket.accept();
				ObjectOutputStream puppetBOut = new ObjectOutputStream(puppetBSocket.getOutputStream());
				ObjectInputStream puppetBIn = new ObjectInputStream(puppetBSocket.getInputStream());) {

			System.out.println("Listening to puppet A  port: " + args[1]
					+ ", puppet B  port: " + args[2]);

			// Setting up thread to accept & return jobs to/from puppet A
			Thread sendJobsToPuppetAThread = new ObjectOutputThread(puppetASocket, "Puppet A", puppetAOut, puppetAJobs,
					editingPuppetAJobs);
			Thread receiveJobsFromPuppetAThread = new ObjectInputThread(puppetASocket, "Puppet A", puppetAIn, doneJobs,
					editingDoneJobs);
			sendJobsToPuppetAThread.start();
			receiveJobsFromPuppetAThread.start();

			// Setting up thread to accept & return jobs to/from puppet B - this is also
			// repetitive
			Thread sendJobsToPuppetBThread = new ObjectOutputThread(puppetBSocket, "Puppet B", puppetBOut, puppetBJobs,
					editingPuppetBJobs);
			Thread receiveJobsFromPuppetBThread = new ObjectInputThread(puppetBSocket, "Puppet B", puppetBIn, doneJobs,
					editingDoneJobs);
			sendJobsToPuppetBThread.start();
			receiveJobsFromPuppetBThread.start();

			System.out.println("Ready to start sending jobs.");
			// Figure out which puppet to send to
			while (true) {
				Packet currentJob = null; // Current job that analyzing
				boolean puppetA = false; // Boolean if being sent to puppet a (will be sent to puppet b if false)

				// Checks if any jobs need to be assigned
				while (!activeJobs.isEmpty()) {
					currentJob = activeJobs.poll(); // Gets the job at the front of the list
					System.out.println("Deciding where to send Packet" + currentJob.getID());
					// If puppetA has more than 4 packets more than puppetB
					if (puppetAJobs.size() >= puppetBJobs.size() + maxMorePuppetJobs) {
						puppetA = false;
						System.out.println("Job #" + currentJob.getID()
								+ " will be done by a puppet B since puppet A has " + maxMorePuppetJobs + " more to do.");
					}
					// If puppetB has more than 4 packets more than puppetA
					else if (puppetBJobs.size() >= puppetAJobs.size() + maxMorePuppetJobs) {
						puppetA = true;
						System.out.println("Job #" + currentJob.getID()
								+ " will be done by a puppet A since puppet B has " + maxMorePuppetJobs + " more to do.");
					}
					// Otherwise just send to appropriate puppet
					else if (currentJob.getJobType() == JobType.A)
						puppetA = true;
					else
						puppetA = false;

					boolean worked = false;
					// Add to correct list
					if (puppetA) {
						synchronized (editingPuppetAJobs) {
							worked = puppetAJobs.offer(currentJob);
						}
					} else {
						synchronized (editingPuppetBJobs) {
							worked = puppetBJobs.offer(currentJob);
						}
					}
					
					// Check if successfully added to proper list
					if (worked)
						System.out.println("Added to jobs for " + (puppetA ? "puppetA" : "puppetB"));
					else {
						// Add job back to active jobs if could not add to proper list
						synchronized (editingActiveJobs) {
							activeJobs.offer(currentJob);
						}
						System.out.println("Could not add to job list, added back to list.");
					}
				}

				// Sleep at the end of the loop to avoid indefinite postponement
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			// Close connection
			// Eclipse does not let us add unreachable code
			// But this would theoretically be added at the end
			/*puppetAOut.close();
			puppetAIn.close();
			puppetASocket.close();
			puppetBOut.close();
			puppetBIn.close();
			puppetBSocket.close();*/
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}