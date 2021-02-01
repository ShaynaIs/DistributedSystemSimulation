

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.*;

/**
 * This thread listens to a input stream for new packets
 */

public class ObjectInputThread extends Thread {
	private Socket socket; // Socket to check if connected
	private String connectionType; // String for output and debugging
	private List<Packet> list; // List to add received packets to
	private ObjectInputStream in;
	private Object editingList; // Key to edit "list"

	// Constructor
	public ObjectInputThread(Socket socket, String connectionType, ObjectInputStream in, List<Packet> list,
			Object editingList) {
		this.socket = socket;
		this.in = in;
		this.connectionType = connectionType;
		this.list = list;
		this.editingList = editingList;
	}

	@Override
	public void run() {
		System.out.println("New thread created to listen to " + connectionType);
		Packet pack;
		try {
			// Continues while socket is connected
			while (socket.isConnected()) {
				// Check if there a new input
				if ((pack = (Packet) in.readObject()) != null) {
					// Add the packet to the list
					synchronized (editingList) {
						list.add(pack);
					}
					System.out.println("Received Packet(" + pack.toString() + ") from " + connectionType);
				}
			}
			// Close everything
			in.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
