package noVNC.utils;

public class DataUtils {
	
	public static final boolean receiving = true;
	public static final boolean sending = false;

	public static void printData(boolean rcv, String decoded, String raw) { 
		if (rcv) {
			System.err.print("Receiving: ");
		} else {
			System.err.print("Sending: ");
		}
		byte[] decodedBytes = decoded.getBytes();
		boolean first = true;
		for (byte b : decodedBytes) {
			if(!first)
				System.err.print(",");
			System.err.print(b);
			first = false;
		}
		if (rcv) {
			System.err.print(" <-- ");
		} else {
			System.err.print(" --> ");
		}
		System.err.println("[" + raw + "]");
	}

	// for debugging data going from the server to the client
	public static void printSCData(boolean rcv, String decoded, String raw) {
		//printData(rcv, decoded, raw);
	}

	// for debugging data going from the client to the server
	public static void printCSData(boolean rcv, String decoded, String raw) {
		printData(rcv, decoded, raw);
	}
}
