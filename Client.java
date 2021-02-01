import java.io.*;
import java.net.*;
import java.util.ArrayList;

/* The Client
 * This client connects to the server and
 * sends jobs one at a time
 * waiting for a response before continuing 
 */

public class Client {
	
	public static void main(String[] args) throws IOException {
	        
		// Connect to Server
		args = new String[] {"127.0.0.1", "30121"};
	    	
	    if (args.length != 2) {
	    	System.err.println(
	    			"Usage: java EchoClient <host name> <port number>");
	        System.exit(1);
	    }

	    String hostName = args[0];
	    int portNumber = Integer.parseInt(args[1]);
	    

	    try (
	         Socket clientSocket = new Socket(hostName, portNumber);
	    		ObjectOutputStream requestWriter = new ObjectOutputStream(clientSocket.getOutputStream());		
	       		ObjectInputStream serverResponse = new ObjectInputStream(clientSocket.getInputStream());	
	        ) {
	    	System.out.println("Client writing to IP: " + args[0] +", Port: "+ args[1]);
	    	
	    	
        	//list of requests
			ArrayList<Packet> mypackets = new ArrayList<Packet>();
			Packet request1 =new Packet(1, Status.ACTIVE, Type.JOB, JobType.B);
			mypackets.add(request1);
			Packet request2 =new Packet(2, Status.ACTIVE, Type.JOB, JobType.A);
			mypackets.add(request2);
			Packet request3 =new Packet(3, Status.ACTIVE, Type.JOB, JobType.B);
			mypackets.add(request3);
			Packet request4 =new Packet(4, Status.ACTIVE, Type.JOB, JobType.A);
			mypackets.add(request4);
			Packet request5 =new Packet(5, Status.ACTIVE, Type.JOB, JobType.A);
			mypackets.add(request5);
	
			
			Packet response; 
			//while loop to move through requests
			for(int i=0; i<mypackets.size();i++) {
				requestWriter.writeObject(mypackets.get(i));; // send request to server
				// Send request to server
				System.out.println("Sent Packet(" + mypackets.get(i).toString() + ") to Server");
	            
				// Get server response
				response =  (Packet) serverResponse.readObject();

				// Print response
                System.out.println("SERVER RESPONDS: Job #" + response.getID() + " " + (response.getStatus() == Status.DONE? "Successful" : "Unsuccessful"));	
			}
			
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
}