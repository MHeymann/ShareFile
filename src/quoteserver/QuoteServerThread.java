package quoteserver;

//SENDER

import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.*;


public class QuoteServerThread extends Thread {

	protected DatagramSocket socket = null;
	protected BufferedReader in = null;
	protected boolean moreQuotes = true;

	public QuoteServerThread() throws IOException {
		this("QuoteServerThread", "one-liners.txt", 8002);
	}

	public QuoteServerThread(String name, String fileName, int port) throws IOException {
		super(name);
		socket = new DatagramSocket(port);

		try {
			in = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.err.printf("Could not open quote file. Serving time instead\n");
		}

	}


	public void run() {
		try {
			go();
		} catch (Exception e) {
			System.out.printf("Exception on running server");
			e.printStackTrace();
			return;
		}
	}

	public void go() {
		System.out.printf("Server Running\n");
		
		while (moreQuotes) {
			try {
				byte[] buf = new byte[256];

				/* receive request */
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				System.out.printf("Received datagram\n");

				/* figure out response */
				String dString = null;
				if (in == null) {
					dString = new Date().toString();
					System.out.printf("Sending date\n");
				} else {
					dString = getNextQuote();
					System.out.printf("Sending quote\n");
				}
				buf = dString.getBytes();

				/* send the response to the client at "address" and 
				 * "port" */
				InetAddress address = packet.getAddress();
				int port = packet.getPort();
				packet = new DatagramPacket(buf, buf.length, address, port);
				socket.send(packet);
				System.out.printf("Sent\n");
			} catch (IOException e) {
				System.err.printf("Something weird in moreQuotes loop\n");
				e.printStackTrace();
				moreQuotes = false;
			}

		}
		socket.close();
	}

	protected String getNextQuote() {
		String returnValue = null;
		try {
			if ((returnValue = in.readLine()) == null) {
				in.close();
				moreQuotes = false;
				returnValue = "No more quotes. Goodbye";
			}
		} catch (IOException e) {
			returnValue = "IOException occured in server.";
		}
		return returnValue;
	}
}
