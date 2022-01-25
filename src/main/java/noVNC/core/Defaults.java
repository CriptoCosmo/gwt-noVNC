package noVNC.core;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;

public class Defaults {
	
	public static Map<String, Object> map = new HashMap<String, Object> ();

	public static Element target;
	public static int connectTimeout = 10;
	public static int disconnectTimeout = 2;
	public static boolean encrypt = false;

	public static boolean true_color = true;
	public static byte[][] colourMap;
	public static boolean shared = true;
	public static float scale = 1.0f;
	public static boolean local_cursor = false;			// Request locally rendered cursor
	public static long fbu_req_rate = 1413;
	public static int check_rate = 217;
	
	public static String logo_str = null;				// Logo to display when cleared
	public static int logo_width = -1;
	public static int logo_height = -1;
	
	public static boolean viewportDrag = false;
	public static boolean viewport = false;				// Use a viewport set with Display.viewportChange(), i.e to full display region

	public static boolean prefer_js = true;
	
	static {
		// colourMap 
	};
}
