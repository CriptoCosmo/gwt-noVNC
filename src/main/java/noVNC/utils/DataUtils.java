package noVNC.utils;

import java.io.PrintStream;

import noVNC.core.Util;

public class DataUtils {

	public static final boolean receiving = true;
	public static final boolean sending = false;

	public static void printData(boolean rcv, String decoded, String raw) { 
		if (decoded.length() == 0) return;
		String msg = "";
		if (rcv) {
			msg += "Receiving: ";
//			System.err.print("Receiving: ");
		} else {
			msg += "Sending: ";
//			System.err.print("Sending: ");
		}
		byte[] decodedBytes = decoded.getBytes();
		print(System.err, decodedBytes);
		
		if (decodedBytes.length>=80) raw = "Large Str - len: " + raw.length();

		if (rcv) {
			msg += " <-- ";
//			System.err.print(" <-- ");
		} else {
			msg += " --> ";
//			System.err.print(" --> ");
		}
		msg += "[" + raw + "]";
//		System.err.println("[" + raw + "]");
		print(msg);
	}
	
	private static void print(String msg) {
		Util.Debug(msg);
	}

	public static void print(PrintStream ps, byte[] b) {
		print(ps, b, 0);
	}
	public static void print(PrintStream ps, byte[] b, int start) {
		print(ps, b, start, b.length);
	}
	public static void print(PrintStream ps, byte[] b, int start, int stop) {
		String msg = "";
		for (int i = start; i<stop; i++) {
			if(i != start) msg += ",";//ps.print(",");
//			ps.print(b[i]);
			msg += b[i];
			if (i-start>=80) {
				msg += ", " + stop + " bytes total";
//				ps.print(", " + stop + " bytes total");
				return;
			}
		}
	}

	// for debugging data going from the server to the client
	public static void printSCData(boolean rcv, String decoded, String raw) {
		printData(rcv, decoded, raw);
	}

	// for debugging data going from the client to the server
	public static void printCSData(boolean rcv, String decoded, String raw) {
		printData(rcv, decoded, raw);
	}

	
}
