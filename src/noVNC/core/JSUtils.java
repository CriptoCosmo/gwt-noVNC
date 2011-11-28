package noVNC.core;

public class JSUtils {

	public static byte[] concat(byte[] orig, byte[] in) {
		if (in.length == 0) return orig;
		if (orig.length == 0) return in;
		byte[] result = new byte[orig.length + in.length];
		System.arraycopy(orig, 0, result, 0, orig.length);
		System.arraycopy(in, 0, result, orig.length, in.length);
		return result;
	}
	
	public static byte[] concat(byte[] orig, byte[] in1, byte[] in2) {
		byte[] ret;
		ret = concat(orig, in1);
		ret = concat(ret, in2);
		return ret;
	}
	
	public static byte[] concat(byte[] orig, byte[] in1, byte[] in2, byte[] in3, byte[] in4) {
		byte[] ret;
		ret = concat(orig, in1);
		ret = concat(ret, in2);
		ret = concat(ret, in3);
		ret = concat(ret, in4);
		return ret;
	}
	public static byte[] slice(byte[] orig, int start, int end) {
		byte[] result = new byte[end-start];
		System.arraycopy(orig, start, result, 0, end-start);
		return result;
	}
	public static byte[] slice(byte[] orig, int start) {
		return slice(orig, start, orig.length);
	}
	
	public static byte[] intAsByte32(int in) {
		return new byte[] {
				(byte) ((in >>> 24) & 0xFF),
				(byte) ((in >>> 16) & 0xFF),
				(byte) ((in >>>  8) & 0xFF),
				(byte) ((in >>>  0) & 0xFF),
		};
	}

	public static byte[] intAsByte16(int in) {
		return new byte[] {
				(byte) ((in >>>  8) & 0xFF),
				(byte) ((in >>>  0) & 0xFF),
		};
	}
}
