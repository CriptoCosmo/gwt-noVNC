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
	
	public static int b2i(byte b) {
		return (b & 0xff);
	}
	public static int byte16AsInt(byte[] b, int ndx) {
		return ((b[ndx] & 0xff) << 8) + 
				(b[ndx+1] & 0xff);
	}

	public static int byte32AsInt(byte[] b, int ndx) {
		return  ((b[ndx  ] & 0xff) << 24) +
				((b[ndx+1] & 0xff) << 16) +
				((b[ndx+2] & 0xff) << 8) + 
				 (b[ndx+3] & 0xff);
	}

	public static int[] asIntArr(byte[] color) {
		int[] intArr = new int[color.length];
		for (int i = 0; i < color.length; i++) {
			intArr[i] = b2i(color[i]);
		}
		return intArr;
	}
}
