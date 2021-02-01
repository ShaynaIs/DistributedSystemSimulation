

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This thread listens for new connections and sets them up with the correct
 * threads and lists
 */

public class SetUpNewClientsThread extends Thread {
	private ServerSocket clientServerSocket; // Socket for clients
	private Queue<Packet> activeJobs; // List of all active jobs
	private Object edittingActiveJobs; // Key to edit activeJobs
	private ArrayList<Packet> doneJobs; // List of all done jobs
	private Object edittingDoneJobs; // Key to edit doneJobs
	private int id = 1; // Counter for client ids

	// Constructor
	public SetUpNewClientsThread(ServerSocket clientServerSocket, Queue<Packet> activeJobs, Object edittingActiveJobs,
			ArrayList<Packet> doneJobs, Object edittingDoneJobs) {
		this.clientServerSocket = clientServerSocket;
		this.activeJobs = activeJobs;
		this.edittingActiveJobs = edittingActiveJobs;
		this.doneJobs = doneJobs;
		this.edittingDoneJobs = edittingDoneJobs;
	}

	public void run() {
		System.out.println("Waiting for a client...");
		while (true) {
			Socket clientSocket = null;
			try {
				// Listens for new connections
				while ((clientSocket = clientServerSocket.accept()) != null) {
					System.out.println("Client trying to connect.");
					try {
						// Setting up thread to accept & return jobs to/from client
						ArrayList<Integer> client1Jobs = new ArrayList<Integer>();
						Object edittingClient1Jobs = new Object();
						Queue<Packet> client1DoneJobs = new LinkedList<Packet>();
						Object edittingClient1DoneJobs = new LinkedList<Packet>();
						Thread intakeJobsThread = new ClientObjectInputThread(id, clientSocket,
								new ObjectInputStream(clientSocket.getInputStream()), client1Jobs, edittingClient1Jobs,
								activeJobs, edittingActiveJobs);
						Thread checkIfJobIsDone = new CollectJobsThread(id, doneJobs, edittingDoneJobs, client1Jobs,
								edittingClient1Jobs, client1DoneJobs, edittingClient1DoneJobs);
						Thread returnJobsThread = new ObjectOutputThread(clientSocket, "client " + id,
								new ObjectOutputStream(clientSocket.getOutputStream()), client1DoneJobs,
								edittingClient1DoneJobs);
						intakeJobsThread.start();
						returnJobsThread.start();
						checkIfJobIsDone.start();
					} catch (IOException e) {
						System.out.println(
								"Exception caught when trying to listen on client port or listening for a connection");
						System.out.println(e.getMessage());
					}
					id++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
