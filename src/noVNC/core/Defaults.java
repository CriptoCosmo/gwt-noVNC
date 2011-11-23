package noVNC.core;

import java.util.HashMap;
import java.util.Map;

public class Defaults {
	
	public static Map<String, Object> map = new HashMap<String, Object> ();
	public static int connectTimeout = 10;
	public static int disconnectTimeout = 2;
	public static boolean encrypt = false;

	public static boolean true_color = true;
	public static byte[][] colourMap;
	public static boolean shared = true;
	public static float scale = 1.0f;
	public static boolean local_cursor = true;
	public static long fbu_req_rate = 1413;
	public static int check_rate = 217;
	
	static {
		// colourMap 
	};
}
