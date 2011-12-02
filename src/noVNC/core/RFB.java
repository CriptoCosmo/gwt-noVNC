package noVNC.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import noVNC.core.Display.CleanDirtyResetReturn;
import noVNC.core.WebSocket.MessageEvent;
import noVNC.utils.Point;
import noVNC.utils.Rect;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Timer;

public class RFB {

//	/*global window, Util, Display, Keyboard, Mouse, Websock, Websock_native, Base64, DES */

//	var that           = {},  // Public API methods
//	    conf           = {},  // Configuration attributes
//
//	    // Pre-declare private functions used before definitions (jslint)
//	    init_vars, updateState, fail, handle_message,
//	    init_msg, normal_msg, framebufferUpdate, print_stats,
//
//	    pixelFormat, clientEncodings, fbUpdateRequest, fbUpdateRequests,
//	    keyEvent, pointerEvent, clientCutText,
//
//	    extract_data_uri, scan_tight_imgQ,
//	    keyPress, mouseButton, mouseMove,
//
//	    checkEvents,  // Overridable for testing
//
//
//	    //
//	    // Private RFB namespace variables
//	    //
	    private String rfb_host       = "";
	    private String rfb_port       = "5900";
	    private String rfb_password   = "";
	    private String rfb_path       = "";

	    private String rfb_state      = "disconnected";
	    private double rfb_version    = 0;
	    private double rfb_max_version= 3.8;
	    private int rfb_auth_scheme= 0;

	
		private static class KV {
			public String key;
			public int val;
			public KV(String _key, int _val) {
				this.key = _key;
				this.val = _val;
			}
		};
	    // In preference order
	    private static KV[] encodings      = {
	        new KV("COPYRECT",         0x01 ),
	        new KV("TIGHT_PNG",        -260 ),
	        new KV("HEXTILE",          0x05 ),
	        new KV("RRE",              0x02 ),
	        new KV("RAW",              0x00 ),
	        new KV("DesktopSize",      -223 ),
	        new KV("Cursor",           -239 ),

	        // Psuedo-encoding settings
	        new KV("JPEG_quality_lo",   -32 ),
	        //["JPEG_quality_hi",   -23 ),
	        new KV("compress_lo",      -255 )
	        //["compress_hi",      -247 ]
		};

		public interface EncHandler {
			public boolean run(RFB rfb);
		}

		private static Map<String, EncHandler> encHandlers = new HashMap<String, EncHandler>(encodings.length*2);
	    private static Map<Integer, String>  encNames      = new HashMap<Integer, String>(encodings.length*2); 
//	    private int[][] encStats       = new int[encodings.length][2];     // [rectCnt, rectCntTot]

	    private WebSock ws		= null;   // Websock object
	    private Display display = null;   // Display object
	    private Keyboard keyboard = null; // Keyboard input handler object
	    private Mouse mouse = null; // Mouse input handler object
	    Timer sendTimer      = null;   // Send Queue check timer
	    Timer connTimer      = null;   // connection timer
	    Timer disconnTimer   = null;   // disconnection timer
	    Timer msgTimer       = null;   // queued handle_message timer
//
//	    // Frame buffer update state
	    private static class FBState {
	        public int rects          = 0;
	        public int subrects       = 0;  // RRE
	        public int lines          = 0;  // RAW
	        public int tiles          = 0;  // HEXTILE
	        public int bytes          = 0;
	        public int x              = 0;
	        public int y              = 0;
	        public int width          = 0; 
	        public int height         = 0;
	        public int encoding       = 0;
	        public int subencoding    = -1;
	        public byte[] foreground;
	        public byte[] background  = null;
	        
	        public int lastsubencoding;
	        public byte[] imgQ           = new byte [] {};   // TIGHT_PNG image queue
	        public int tiles_x = 0;
	        public int tiles_y = 0;
	        public int total_tiles = 0;
	    };
	    
	    private FBState FBU = new FBState();

	    private int fb_Bpp         = 4;
	    private int fb_depth       = 3;
	    private int fb_width       = 0;
	    private int fb_height      = 0;
	    private String fb_name        = "";

	    private int scan_imgQ_rate = 40; // 25 times per second or so
	    private long last_req_time  = 0;
	    
	    private static final int rre_chunk_sz   = 100;

	    private static class timingState {
	        public long last_fbu       = 0;
	        public int fbu_total      = 0;
	        public int fbu_total_cnt  = 0;
	        public int full_fbu_total = 0;
	        public int full_fbu_cnt   = 0;

	        public long fbu_rt_start   = 0;
	        public int fbu_rt_total   = 0;
	        public int fbu_rt_cnt     = 0;
	        
	        public int cur_fbu;
	    };
	    
	    private timingState timing = new timingState();
//
	    private boolean test_mode        = false;
//
//	    def_con_timeout  = Websock_native ? 2 : 5,
//
//	    /* Mouse state */
	    private byte mouse_buttonMask = 0;
	    byte[] mouse_arr        = new byte[] {};
	    private boolean viewportDragging = false;
	    Point viewportDragPos = new Point();
	    
		public interface RFBHandler {
			public void onUpdateState(RFB rfb, String state, String oldState, String statusMsg);	    
		}
		
		private RFBHandler rfbHandler = new RFBHandler() {
			@Override
			public void onUpdateState(RFB rfb, String state, String oldState, String statusMsg) {
				Util.Debug(">>> State changed: " + oldState + " --> " + state + " : " + statusMsg);
			}
		};
		public void hook(RFBHandler _rh) {
			rfbHandler = _rh;
		}

//	// Configuration attributes
//	Util.conf_defaults(conf, that, defaults, [
//	    ["target",             "wo", "dom", null, "VNC display rendering Canvas object"],
//	    ["focusContainer",     "wo", "dom", document, "DOM element that captures keyboard input"],
//
//	    ["encrypt",            "rw", "bool", false, "Use TLS/SSL/wss encryption"],
//	    ["true_color",         "rw", "bool", true,  "Request true color pixel data"],
//	    ["local_cursor",       "rw", "bool", false, "Request locally rendered cursor"],
//	    ["shared",             "rw", "bool", true,  "Request shared mode"],
//
//	    ["connectTimeout",     "rw", "int", def_con_timeout, "Time (s) to wait for connection"],
//	    ["disconnectTimeout",  "rw", "int", 3,    "Time (s) to wait for disconnection"],
//
//	    ["viewportDrag",       "rw", "bool", false, "Move the viewport on mouse drags"],
//
//	    ["check_rate",         "rw", "int", 217,  "Timing (ms) of send/receive check"],
//	    ["fbu_req_rate",       "rw", "int", 1413, "Timing (ms) of frameBufferUpdate requests"],
//
//	    // Callback functions
//	    ["onUpdateState",      "rw", "func", function() { },
//	        "onUpdateState(rfb, state, oldstate, statusMsg): RFB state update/change "],
//	    ["onPasswordRequired", "rw", "func", function() { },
//	        "onPasswordRequired(rfb): VNC password is required "],
//	    ["onClipboard",        "rw", "func", function() { },
//	        "onClipboard(rfb, text): RFB clipboard contents received"],
//	    ["onBell",             "rw", "func", function() { },
//	        "onBell(rfb): RFB Bell message received "],
//	    ["onFBUReceive",       "rw", "func", function() { },
//	        "onFBUReceive(rfb, fbu): RFB FBU received but not yet processed "],
//	    ["onFBUComplete",      "rw", "func", function() { },
//	        "onFBUComplete(rfb, fbu): RFB FBU received and processed "],
//
//	    // These callback names are deprecated
//	    ["updateState",        "rw", "func", function() { },
//	        "obsolete, use onUpdateState"],
//	    ["clipboardReceive",   "rw", "func", function() { },
//	        "obsolete, use onClipboard"]
//	    ]);
//
//
//	// Override/add some specific configuration getters/setters
//	that.set_local_cursor = function(cursor) {
//	    if ((!cursor) || (cursor in {"0":1, "no":1, "false":1})) {
//	        conf.local_cursor = false;
//	    } else {
//	        if (display.get_cursor_uri()) {
//	            conf.local_cursor = true;
//	        } else {
//	            Util.Warn("Browser does not support local cursor");
//	        }
//	    }
//	};
//
//	// These are fake configuration getters
//	that.get_display = function() { return display; };
//
//	that.get_keyboard = function() { return keyboard; };
//
//	that.get_mouse = function() { return mouse; };
//
//
//
//	//
//	// Setup routines
//	//
//
//	// Create the public API interface and initialize values that stay
//	// constant across connect/disconnect
	public RFB() {
//	    var rmode;
//	    Util.Debug(">> RFB.constructor");

	    // Create lookup tables based encoding number
	    for (int i=0; i < encodings.length; i+=1) {
	        encHandlers.put(Integer.toString(encodings[i].val), encHandlers.get(encodings[i].key));
	        encNames.put(encodings[i].val, encodings[i].key);
//	        encStats[encodings[i].val][0] = 0;
//	        encStats[encodings[i].val][1] = 0;
	    }
	    
	    // Initialize display, mouse, keyboard, and websock
//	    try {
	        display   = new Display();
//	    } catch (exc) {
//	        Util.Error("Display exception: " + exc);
//	        updateState("fatal", "No working Display");
//	    }

	    keyboard = new Keyboard();
	    keyboard.hook(new Keyboard.KeyboardHandler() {
			@Override
			public void onKeyPress(int keysym, byte down) {
				keyPress(keysym, down);
			}
	    	
		});
	    
		mouse = new Mouse();
		mouse.hook(new Mouse.MouseHandler() {
			@Override
			public void onMouseButton(int x, int y, boolean down, int bmask) {
				mouseButton(x, y, down, bmask);
			}

			@Override
			public void onMouseMove(int x, int y) {
				mouseMove(x, y);
			}
		});

//	    rmode = display.get_render_mode();
//
	    ws = new WebSock();
	    ws.hook(new WebSocket.WebSocketHandler() {
			@Override
			public void onOpen(NativeEvent e) {
			    if (rfb_state.equals("connect")) {
		            updateState("ProtocolVersion", "Starting VNC handshake");
		        } else {
		            fail("Got unexpected WebSockets connection");
		        }
			}
			@Override
			public void onMessage(MessageEvent e) {
	    		handle_message();
			}
			
			@Override
			public void onError(NativeEvent e) {
		        fail("WebSock error: " + e);
			}
			
			@Override
			public void onClose(NativeEvent e) {
		        if (rfb_state.equals("disconnect")) {
		            updateState("disconnected", "VNC disconnected");
		        } else if (rfb_state.equals("ProtocolVersion")) {
		            fail("Failed to connect to server");
		        } else if (rfb_state.equals("failed") || rfb_state.equals("disconnected")) {
		            Util.Error("Received onclose while disconnected");
		        } else  {
		            fail("Server disconnected");
		        }
	    	}
	    });

	    init_vars();
//
//	    /* Check web-socket-js if no builtin WebSocket support */
//	    if (Websock_native) {
//	        Util.Info("Using native WebSockets");
	        updateState("loaded", "noVNC ready: native WebSockets, " /*+ rmode*/);
//	    } else {
//	        Util.Warn("Using web-socket-js bridge. Flash version: " +
//	                  Util.Flash.version);
//	        if ((! Util.Flash) ||
//	            (Util.Flash.version < 9)) {
//	            updateState("fatal", "WebSockets or <a href="http://get.adobe.com/flashplayer">Adobe Flash<\/a> is required");
//	        } else if (document.location.href.substr(0, 7) === "file://") {
//	            updateState("fatal",
//	                    ""file://" URL is incompatible with Adobe Flash");
//	        } else {
//	            updateState("loaded", "noVNC ready: WebSockets emulation, " + rmode);
//	        }
//	    }
//
	    Util.Debug("<< RFB.constructor");
//	    return that;  // Return the public API interface
	}
	

	private void connect() {
	    Util.Debug(">> RFB.connect");

	    String uri = "";
	    if (Defaults.encrypt) {
	        uri = "wss://";
	    } else {
	        uri = "ws://";
	    }
	    uri += rfb_host + ":" + rfb_port + "/" + rfb_path;
	    Util.Info("connecting to " + uri);
	    ws.open(uri);

	    Util.Debug("<< RFB.connect");
	}

	// Initialize variables that are reset before each connection
	private void init_vars() {
	    /* Reset state */
	    ws.init();

	    FBU.rects        = 0;
	    FBU.subrects     = 0;  // RRE and HEXTILE
	    FBU.lines        = 0;  // RAW
	    FBU.tiles        = 0;  // HEXTILE
//	    FBU.imgQ         = []; // TIGHT_PNG image queue
//	    mouse_buttonMask = 0;
//	    mouse_arr        = [];

//	    // Clear the per connection encoding stats
//	    for (int i=0; i < encodings.length; i+=1) {
//	        encStats[encodings[i].val][0] = 0;
//	    }
	};

	// Print statistics
	private void print_stats() {
	    Util.Info("Encoding stats for this connection:");
//	    for (int i=0; i < encodings.length; i+=1) {
//	        int[]s = encStats[encodings[i].val];
//	        if ((s[0] + s[1]) > 0) {
//	            Util.Info("    " + encodings[i].key + ": " + s[0] + " rects");
//	        }
//	    }
	    Util.Info("Encoding stats since page load:");
//	    for (int i=0; i < encodings.length; i+=1) {
//	        int[] s = encStats[encodings[i].val];
//	        if ((s[0] + s[1]) > 0) {
//	            Util.Info("    " + encodings[i].key + ": " +
//	                      s[1] + " rects");
//	        }
//	    }
	};

	//
	// Utility routines
	//
	
	private Set<String> strSet(String... str) {
		Set<String> retVal = new HashSet<String>(str.length);
		for (String s : str) {
			retVal.add(s);
		}
		return retVal;
	}


	/*
	 * Page states:
	 *   loaded       - page load, equivalent to disconnected
	 *   disconnected - idle state
	 *   connect      - starting to connect (to ProtocolVersion)
	 *   normal       - connected
	 *   disconnect   - starting to disconnect
	 *   failed       - abnormal disconnect
	 *   fatal        - failed to load page, or fatal error
	 *
	 * RFB protocol initialization states:
	 *   ProtocolVersion 
	 *   Security
	 *   Authentication
	 *   password     - waiting for password, not part of RFB
	 *   SecurityResult
	 *   ClientInitialization - not triggered by server message
	 *   ServerInitialization (to normal)
	 */
	private void updateState(String state, String statusMsg) {
	    //var func, cmsg, 
	    String oldstate = rfb_state;

	    if (state.equals(oldstate)) {
	        /* Already here, ignore */
	        Util.Debug("Already in state " + state + ", ignoring.");
	        return;
	    }

	    /* 
	     * These are disconnected states. A previous connect may
	     * asynchronously cause a connection so make sure we are closed.
	     */
	    if (strSet("disconnected", "loaded", "connect", "disconnect", "failed", "fatal").contains(state)) {
	        if (sendTimer != null) {
	        	sendTimer.cancel();
	            sendTimer = null;
	        }

	        if (msgTimer != null) {
	            msgTimer.cancel();
	            msgTimer = null;
	        }

	        if (display != null && display.get_context() != null) {
	            keyboard.ungrab();
	            mouse.ungrab();
//	            display.defaultCursor();
//	            if ((Util.get_logging() !== "debug") ||
//	                (state === "loaded")) {
//	                // Show noVNC logo on load and when disconnected if
//	                // debug is off
//	                display.clear();
//	            }
	        }

	        ws.close();
	    }

	    if (oldstate.equals("fatal")) {
	        Util.Error("Fatal error, cannot continue");
	    }

//	    if ((state.equals("failed") || (state.equals("fatal")) {
//	        func = Util.Error;
//	    } else {
//	        func = Util.Warn;
//	    }

	    if (oldstate.equals("failed") && state.equals("disconnected")) {
	        // Do disconnect action, but stay in failed state.
	        rfb_state = "failed";
	    } else {
	        rfb_state = state;
	    }

//	    cmsg = typeof(statusMsg) !== "undefined" ? (" Msg: " + statusMsg) : "";
//	    func("New state "" + rfb_state + "", was "" + oldstate + ""." + cmsg);

	    if (connTimer != null && !rfb_state.equals("connect")) {
	        Util.Debug("Clearing connect timer");
	        connTimer.cancel();
	        connTimer = null;
	    }

	    if (disconnTimer != null && !rfb_state.equals("disconnect")) {
	        Util.Debug("Clearing disconnect timer");
	        disconnTimer.cancel();
	        disconnTimer = null;
	    }

	    /*switch (state) {*/
	    if (state.equals("normal")) {
	    	if (oldstate.equals("disconnected") || oldstate.equals("failed")) {
	    		Util.Error("Invalid transition from 'disconnected' or 'failed' to 'normal'");
	    	}
	    }
	    if (state.equals("connect")) {
	        connTimer = new Timer() {
				@Override
				public void run() {
	                fail("Connect timeout");
				}
			};
			connTimer.schedule(Defaults.connectTimeout * 1000);

			init_vars();
	        connect();
	        // WebSocket.onopen transitions to "ProtocolVersion"
	    }
	    if (state.equals("disconnect")) {
	        if (!test_mode) {
	            disconnTimer = new Timer() {
					public void run() {
						fail("Disconnect timeout");
					}
	            };
	            disconnTimer.schedule(Defaults.disconnectTimeout * 1000);
	        }

	        print_stats();

	        // WebSocket.onclose transitions to "disconnected"
	    }


	    if (state.equals("failed")) {
	        if (oldstate.equals("disconnected")) {
	            Util.Error("Invalid transition from 'disconnected' to 'failed'");
	        }
	        if (oldstate.equals("normal")) {
	            Util.Error("Error while connected.");
	        }
	        if (oldstate.equals("init")) {
	            Util.Error("Error while initializing.");
	        }

	        // Make sure we transition to disconnected
	        Timer disconnectTimeOutTimer = new Timer() {
				@Override
				public void run() {
					updateState("disconnected", null);
				}};
			disconnectTimeOutTimer.schedule(50);
	    }

	    if (oldstate.equals("failed") && state.equals("disconnected")) {
	        // Leave the failed message
	    	rfbHandler.onUpdateState(this, state, oldstate, null);
	    } else {
	    	rfbHandler.onUpdateState(this, state, oldstate, statusMsg);
	    }
	};
	

	private boolean fail(String msg) {
	    updateState("failed", msg);
	    return false;
	};

	private void handle_message() {
	    //Util.Debug(">> handle_message ws.rQlen(): " + ws.rQlen());
	    //Util.Debug("ws.rQslice(0,20): " + ws.rQslice(0,20) + " (" + ws.rQlen() + ")");
	    if (ws.rQlen() == 0) {
	        Util.Warn("handle_message called on empty receive queue");
	        return;
	    }
	    
	    if (rfb_state.equals("disconnected") || rfb_state.equals("failed")) {
	        Util.Error("Got data while disconnected");
	    } else if (rfb_state.equals("normal")) {
	        if (normal_msg() && ws.rQlen() > 0) {
	            // true means we can continue processing
	            // Give other events a chance to run
	        	if (msgTimer == null) {
	        		Util.Debug("More data to process, creating timer");
	        		msgTimer = new Timer() {
						@Override
						public void run() {
							msgTimer = null;
							handle_message();
						}
					};
					msgTimer.schedule(10);
	            } else {
	                Util.Debug("More data to process, existing timer");
	            }
	        }
	    } else {
	        init_msg();
	    }
	};

//	function genDES(password, challenge) {
//	    var i, passwd = [];
//	    for (i=0; i < password.length; i += 1) {
//	        passwd.push(password.charCodeAt(i));
//	    }
//	    return (new DES(passwd)).encrypt(challenge);
//	}
	
	private boolean flushClient() {
	    if (mouse_arr.length > 0) {
	        //send(mouse_arr.concat(fbUpdateRequests()));
	        ws.send(mouse_arr);
	        new Timer() {
				@Override
				public void run() {
		            ws.send(fbUpdateRequests());
				}
			}.schedule(50);

	        mouse_arr = new byte[] {};
	        return true;
	    } else {
	        return false;
	    }
	}
	
	private class CheckEventsTimer extends Timer {
		@Override
		public void run() {
			checkEvents();
		}
	}

	// overridable for testing
	private void checkEvents() {
	    if (rfb_state.equals("normal") && !viewportDragging) {
	        if (! flushClient()) {
	            long now = new Date().getTime();
	            if (now > last_req_time + Defaults.fbu_req_rate) {
	                last_req_time = now;
	                ws.send(fbUpdateRequests());
	            }
	        }
	    }
	    new CheckEventsTimer().schedule(Defaults.check_rate);
	}
	
	public void keyPress(int keysym, byte down) {
	    byte[] arr = new byte[]{};
	    arr = keyEvent(keysym, down);
	    arr = JSUtils.concat(arr, fbUpdateRequests());
	    ws.send(arr);
	}
	
		public void mouseButton (int x, int y, boolean down, int bmask) {
			Util.Debug(">> mouseClick " + x + "," + y);
			 if (down) {
			        mouse_buttonMask |= bmask;
			    } else {
			        mouse_buttonMask ^= bmask;
			    }
			 if (Defaults.viewportDrag) {
			        if (down && !viewportDragging) {
			            viewportDragging = true;
			            viewportDragPos.x = x;
			            viewportDragPos.y = y;
			            // Skip sending mouse events
			            return;
			        } else {
			            viewportDragging = false;
			        }
			   }
		
			 mouse_arr = JSUtils.concat(mouse_arr, pointerEvent(display.absX(x), display.absY(y)));
//			 mouse_arr = mouse_arr.concat(pointerEvent(display.absX(x), display.absY(y)));
			 flushClient();
		}

		public void	mouseMove (int x, int y) {
	    Util.Debug(">> mouseMove " + x + "," + y);
	    int deltaX, deltaY;

	    if (viewportDragging) {
	        //deltaX = x - viewportDragPos.x; // drag viewport
	        deltaX = viewportDragPos.x - x; // drag frame buffer
	        //deltaY = y - viewportDragPos.y; // drag viewport
	        deltaY = viewportDragPos.y - y; // drag frame buffer
	        viewportDragPos.x = x;
            viewportDragPos.y = y;
	        display.viewportChange(deltaX, deltaY);
	        // Skip sending mouse events
	        return;
	    }

	    mouse_arr = JSUtils.concat(mouse_arr, pointerEvent(display.absX(x), display.absY(y)));
	}


	//
	// Server message handlers
	//

	// RFB/VNC initialisation message handler
	private void init_msg() {
	    //Util.Debug(">> init_msg [rfb_state "" + rfb_state + ""]");

//	    var strlen, reason, length, sversion, cversion,
//	        i, types, num_types, challenge, response, bpp, depth,
//	        big_endian, red_max, green_max, blue_max, red_shift,
//	        green_shift, blue_shift, true_color, name_length;

	    //Util.Debug("ws.rQ (" + ws.rQlen() + ") " + ws.rQslice(0));

		//// THE BIG STATE MACHINE
		if (rfb_state.equals("StateXXX")) {
		}
		
		if (rfb_state.equals("ProtocolVersion")) {
	        if (ws.rQlen() < 12) {
	            fail("Incomplete protocol version");
	        }
	        String sversion = ws.rQshiftStr(12).substring(4,11);
	        Util.Info("Server ProtocolVersion: " + sversion);
	        if (sversion.equals("003.003")) {
	        	rfb_version = 3.3;
	        } else if (sversion.equals("003.006")) {
	        	rfb_version = 3.3;  // UltraVNC
	        } else if (sversion.equals("003.007")) {
	        	rfb_version = 3.7;
	        } else if (sversion.equals("003.008")) {
	        	rfb_version = 3.8;
	        } else {
                fail("Invalid server version " + sversion);
	        }

	        if (rfb_version > rfb_max_version) { 
	            rfb_version = rfb_max_version;
	        }

	        if (! test_mode) {
				sendTimer = new Timer() {
					@Override
					public void run() {
						ws.flush();
					}
				};
                // Send updates either at a rate of one update
                // every 50ms, or whatever slower rate the network
                // can handle.
				sendTimer.scheduleRepeating(50);
	        }

	        String cversion = "00" + (int) rfb_version + ".00" + (int) ((rfb_version * 10) % 10);
	        ws.send_string("RFB " + cversion + "\n");
	        updateState("Security", "Sent ProtocolVersion: " + cversion);
		}

		if (rfb_state.equals("Security")) {
	        if (rfb_version >= 3.7) {
	        	throw new IllegalArgumentException("Not Yet Implemented");
//	            // Server sends supported list, client decides 
//	            num_types = ws.rQshift8();
//	            if (ws.rQwait("security type", num_types, 1)) { return false; }
//	            if (num_types === 0) {
//	                strlen = ws.rQshift32();
//	                reason = ws.rQshiftStr(strlen);
//	                return fail("Security failure: " + reason);
//	            }
//	            rfb_auth_scheme = 0;
//	            types = ws.rQshiftBytes(num_types);
//	            Util.Debug("Server security types: " + types);
//	            for (i=0; i < types.length; i+=1) {
//	                if ((types[i] > rfb_auth_scheme) && (types[i] < 3)) {
//	                    rfb_auth_scheme = types[i];
//	                }
//	            }
//	            if (rfb_auth_scheme === 0) {
//	                return fail("Unsupported security types: " + types);
//	            }
//	            
//	            ws.send([rfb_auth_scheme]);
	        } else {
	            // Server decides
	            if (ws.rQwait("security scheme", 4)) { return; }
	            rfb_auth_scheme = ws.rQshift32();
	        }
	        updateState("Authentication",
	                "Authenticating using scheme: " + rfb_auth_scheme);
	        init_msg();  // Recursive fallthrough (workaround JSLint complaint)
		}


	    // Triggered by fallthough, not by server message
		if (rfb_state.equals("Authentication")) {
			//Util.Debug("Security auth scheme: " + rfb_auth_scheme);
	        switch (rfb_auth_scheme) {
	            case 0:  // connection failed
	                if (ws.rQwait("auth reason", 4)) { return; }
	                int strlen = ws.rQshift32();
	                String reason = ws.rQshiftStr(strlen);
	                fail("Auth failure: " + reason);
	                return;
	            case 1:  // no authentication
	                if (rfb_version >= 3.8) {
	                    updateState("SecurityResult", null);
	                    return;
	                }
	                // Fall through to ClientInitialisation
	                break;
	            case 2:  // VNC authentication
	            	throw new IllegalArgumentException("Not yet implemented");
//	                if (rfb_password.length() == 0) {
//	                    // Notify via both callbacks since it is kind of
//	                    // a RFB state change and a UI interface issue.
//	                    updateState("password", "Password Required");
////	                    conf.onPasswordRequired(that);
//	                    return;
//	                }
//	                if (ws.rQwait("auth challenge", 16)) { return; }
//	                byte[] challenge = ws.rQshiftBytes(16);
//	                //Util.Debug("Password: " + rfb_password);
//	                //Util.Debug("Challenge: " + challenge +
//	                //           " (" + challenge.length + ")");
//	                response = genDES(rfb_password, challenge);
//	                //Util.Debug("Response: " + response +
//	                //           " (" + response.length + ")");
//	                
//	                //Util.Debug("Sending DES encrypted auth response");
//	                ws.send(response);
//	                updateState("SecurityResult", null);
	            default:
	                fail("Unsupported auth scheme: " + rfb_auth_scheme);
	                return;
	        }
	        updateState("ClientInitialisation", "No auth required");
	        init_msg();  // Recursive fallthrough (workaround JSLint complaint)
		}
		
		if (rfb_state.equals("Authentication")) {
	        if (ws.rQwait("VNC auth response ", 4)) { return; }
	        switch (ws.rQshift32()) {
	            case 0:  // OK
	                // Fall through to ClientInitialisation
	                break;
	            case 1:  // failed
	                if (rfb_version >= 3.8) {
	                    int length = ws.rQshift32();
	                    if (ws.rQwait("SecurityResult reason", length, 8)) {
	                        return;
	                    }
	                    String reason = ws.rQshiftStr(length);
	                    fail(reason);
	                } else {
	                    fail("Authentication failed");
	                }
	                return;
	            case 2:  // too-many
	                fail("Too many auth attempts");
	                return;
	        }
	        updateState("ClientInitialisation", "Authentication OK");
	        init_msg();  // Recursive fallthrough (workaround JSLint complaint)
		}


	    // Triggered by fallthough, not by server message
		if (rfb_state.equals("ClientInitialisation")) {
	        ws.send(new byte[] {(byte) (Defaults.shared ? 1 : 0)}); // ClientInitialisation
	        updateState("ServerInitialisation", "Authentication OK");
		}

		if (rfb_state.equals("ServerInitialisation")) {

	        if (fb_width == 0) {
	        	if (ws.rQwait("server initialization", 24)) { return; }
	        	
		        /* Screen size */
		        fb_width  = ws.rQshift16();
		        fb_height = ws.rQshift16();
	
		        /* PIXEL_FORMAT */
		        byte bpp            = ws.rQshift8();
		        byte depth          = ws.rQshift8();
		        byte big_endian     = ws.rQshift8();
		        byte true_color     = ws.rQshift8();
	
		        int red_max        = ws.rQshift16();
		        int green_max      = ws.rQshift16();
		        int blue_max       = ws.rQshift16();
		        byte red_shift      = ws.rQshift8();
		        byte green_shift    = ws.rQshift8();
		        byte blue_shift     = ws.rQshift8();
		        ws.rQshiftStr(3); // padding
	
		        Util.Info("Screen: " + fb_width + "x" + fb_height + 
		                  ", bpp: " + bpp + ", depth: " + depth +
		                  ", big_endian: " + big_endian +
		                  ", true_color: " + true_color +
		                  ", red_max: " + red_max +
		                  ", green_max: " + green_max +
		                  ", blue_max: " + blue_max +
		                  ", red_shift: " + red_shift +
		                  ", green_shift: " + green_shift +
		                  ", blue_shift: " + blue_shift);
	
	        }

	        /* Connection name/title */
	        int name_length = ws.rQshift32();
	        if (ws.rQwait("server initialization - name", name_length)) {
	        	ws.rQunshift32(name_length);
	        	return; 
	        }
	        
	        fb_name = ws.rQshiftStr(name_length);

//	        display.set_true_color(Defaults.true_color);
	        display.resize(fb_width, fb_height);
	        keyboard.grab();
	        mouse.grab();

	        if (Defaults.true_color) {
	            fb_Bpp           = 4;
	            fb_depth         = 3;
	        } else {
	            fb_Bpp           = 1;
	            fb_depth         = 1;
	        }

	        byte[] response = pixelFormat();
	        response = JSUtils.concat(response,  clientEncodings());
	        response = JSUtils.concat(response, fbUpdateRequests());
	        timing.fbu_rt_start = (new Date()).getTime();
	        ws.send(response);
	        
	        /* Start pushing/polling */
		    new CheckEventsTimer().schedule(Defaults.check_rate);
		    new Timer() {
				@Override
				public void run() {
					scan_tight_imgQ();
				}
			}.schedule(scan_imgQ_rate);

	        if (Defaults.encrypt) {
	            updateState("normal", "Connected (encrypted) to: " + fb_name);
	        } else {
	            updateState("normal", "Connected (unencrypted) to: " + fb_name);
	        }
		}
	    //Util.Debug("<< init_msg");
	};


	/* Normal RFB/VNC server message handler */
	private boolean normal_msg() {
	    //Util.Debug(">> normal_msg");

//	    var ret = true, msg_type, length, text,
//	        c, first_colour, num_colours, red, green, blue;
		boolean ret = true;
		int msg_type;

	    if (FBU.rects > 0) {
	        msg_type = 0;
	    } else {
	        msg_type = ws.rQshift8();
	    }
	    
	    switch (msg_type) {
	    case 0:  // FramebufferUpdate
	        ret = framebufferUpdate(); // false means need more data
	        break;
	    case 1:  // SetColourMapEntries
//	        Util.Debug("SetColourMapEntries");
//	        ws.rQshift8();  // Padding
//	        first_colour = ws.rQshift16(); // First colour
//	        num_colours = ws.rQshift16();
//	        for (c=0; c < num_colours; c+=1) { 
//	            red = ws.rQshift16();
//	            //Util.Debug("red before: " + red);
//	            red = parseInt(red / 256, 10);
//	            //Util.Debug("red after: " + red);
//	            green = parseInt(ws.rQshift16() / 256, 10);
//	            blue = parseInt(ws.rQshift16() / 256, 10);
//	            display.set_colourMap([red, green, blue], first_colour + c);
//	        }
//	        Util.Debug("colourMap: " + display.get_colourMap());
//	        Util.Info("Registered " + num_colours + " colourMap entries");
//	        //Util.Debug("colourMap: " + display.get_colourMap());
	        break;
	    case 2:  // Bell
//	        Util.Debug("Bell");
//	        conf.onBell(that);
	        break;
	    case 3:  // ServerCutText
//	        Util.Debug("ServerCutText");
//	        if (ws.rQwait("ServerCutText header", 7, 1)) { return false; }
//	        ws.rQshiftBytes(3);  // Padding
//	        length = ws.rQshift32();
//	        if (ws.rQwait("ServerCutText", length, 8)) { return false; }
//
//	        text = ws.rQshiftStr(length);
//	        conf.clipboardReceive(that, text); // Obsolete
//	        conf.onClipboard(that, text);
	        break;
	    default:
//	        fail("Disconnected: illegal server message type " + msg_type);
//	        Util.Debug("ws.rQslice(0,30):" + ws.rQslice(0,30));
	        break;
	    }
	    //Util.Debug("<< normal_msg");
	    return ret;
	};

	private boolean framebufferUpdate() {
//	    var now, hdr, fbu_rt_diff, ret = true;
		long now;

	    if (FBU.rects == 0) {
	        //Util.Debug("New FBU: ws.rQslice(0,20): " + ws.rQslice(0,20));
	        if (ws.rQwait("FBU header", 3)) {
	            ws.rQunshift8(0);  // FBU msg_type
	            return false;
	        }
	        ws.rQshift8();  // padding
	        FBU.rects = ws.rQshift16();
	        //Util.Debug("FramebufferUpdate, rects:" + FBU.rects);
	        FBU.bytes = 0;
	        timing.cur_fbu = 0;
	        if (timing.fbu_rt_start > 0) {
	            now = (new Date()).getTime();
	            Util.Info("First FBU latency: " + (now - timing.fbu_rt_start));
	        }
	    }

	    while (FBU.rects > 0) {
	        if (!rfb_state.equals("normal")) {
	            return false;
	        }
	        if (ws.rQwait("FBU", FBU.bytes)) { return false; }
	        if (FBU.bytes == 0) {
	            if (ws.rQwait("rect header", 12)) { return false; }
	            /* New FramebufferUpdate */

	            byte[] hdr = ws.rQshiftBytes(12);
	            FBU.x      = JSUtils.byte16AsInt(hdr, 0);
	            FBU.y      = JSUtils.byte16AsInt(hdr, 2);
	            FBU.width  = JSUtils.byte16AsInt(hdr, 4);
	            FBU.height = JSUtils.byte16AsInt(hdr, 6);
	            FBU.encoding = JSUtils.byte32AsInt(hdr, 8);
	            

//	            conf.onFBUReceive(that,
//	                    {"x": FBU.x, "y": FBU.y,
//	                     "width": FBU.width, "height": FBU.height,
//	                     "encoding": FBU.encoding,
//	                     "encodingName": encNames[FBU.encoding]});
//
	            if (encNames.containsKey(FBU.encoding)) {
	                // Debug:
	                ///*
	                String msg =  "FramebufferUpdate rects:" + FBU.rects;
	                msg += " x: " + FBU.x + " y: " + FBU.y;
	                msg += " width: " + FBU.width + " height: " + FBU.height;
	                msg += " encoding:" + FBU.encoding;
	                msg += "(" + encNames.get(FBU.encoding) + ")";
	                msg += ", ws.rQlen(): " + ws.rQlen();
	                Util.Debug(msg);
	                //*/
	            } else {
	                fail("Disconnected: unsupported encoding " + FBU.encoding);
	                return false;
	            }
	        }

	        timing.last_fbu = (new Date()).getTime();

	        EncHandler handler = encHandlers.get("" + FBU.encoding + "");
	        boolean ret = handler.run(this);

	        now = (new Date()).getTime();
	        timing.cur_fbu += (now - timing.last_fbu);

	        if (ret) {
//	            encStats[FBU.encoding][0] += 1;
//	            encStats[FBU.encoding][1] += 1;
	        }

	        if (FBU.rects == 0) {
	            if (((FBU.width == fb_width) &&
	                        (FBU.height == fb_height)) ||
	                    (timing.fbu_rt_start > 0)) {
	                timing.full_fbu_total += timing.cur_fbu;
	                timing.full_fbu_cnt += 1;
	                Util.Info("Timing of full FBU, cur: " +
	                          timing.cur_fbu + ", total: " +
	                          timing.full_fbu_total + ", cnt: " +
	                          timing.full_fbu_cnt + ", avg: " +
	                          (timing.full_fbu_total /
	                              timing.full_fbu_cnt));
	            }
	            if (timing.fbu_rt_start > 0) {
	                long fbu_rt_diff = now - timing.fbu_rt_start;
	                timing.fbu_rt_total += fbu_rt_diff;
	                timing.fbu_rt_cnt += 1;
	                Util.Info("full FBU round-trip, cur: " +
	                          fbu_rt_diff + ", total: " +
	                          timing.fbu_rt_total + ", cnt: " +
	                          timing.fbu_rt_cnt + ", avg: " +
	                          (timing.fbu_rt_total /
	                              timing.fbu_rt_cnt));
	                timing.fbu_rt_start = 0;
	            }
	        }
	        if (! ret) {
	            return ret; // false ret means need more data
	        }
	    }
//
//	    conf.onFBUComplete(that,
//	            {"x": FBU.x, "y": FBU.y,
//	                "width": FBU.width, "height": FBU.height,
//	                "encoding": FBU.encoding,
//	                "encodingName": encNames[FBU.encoding]});
//
	    return true; // We finished this FBU
	};

	//
	// FramebufferUpdate encodings
	//
	

	static { 
		encHandlers.put("TEMPLATE", new EncHandler() {
			@Override
			public boolean run(RFB rfb) {
				return false;
			}
		});

		encHandlers.put("RAW", new EncHandler() {
			@Override
			public boolean run(RFB rfb) {
			    //Util.Debug(">> display_raw (" + ws.rQlen() + " bytes)");
				if (rfb.FBU.width == 0) {
			        rfb.FBU.rects -= 1;
			        rfb.FBU.bytes = 0;
					return true;
				}
				
			    int cur_y;
		
			    if (rfb.FBU.lines == 0) {
			    	rfb.FBU.lines = rfb.FBU.height;
			    }
			    rfb.FBU.bytes = rfb.FBU.width * rfb.fb_Bpp; // At least a line
			    if (rfb.ws.rQwait("RAW", rfb.FBU.bytes)) { return false; }
			    cur_y = rfb.FBU.y + (rfb.FBU.height - rfb.FBU.lines);
			    int cur_height = Math.min(rfb.FBU.lines,
			                          (int) Math.floor(rfb.ws.rQlen()/(rfb.FBU.width * rfb.fb_Bpp)));

			    long start = System.currentTimeMillis();
			    System.err.println("Start image: " + start);
			    rfb.display.blitImage(rfb.FBU.x, cur_y, rfb.FBU.width, cur_height, rfb.ws.get_rQ(), rfb.ws.get_rQi());
			    long end = System.currentTimeMillis();
			    System.err.println("End image: " + end + "\nTime(s): " + (end - start)/1000);
			    
			    
			    rfb.ws.rQshiftBytes(rfb.FBU.width * cur_height * rfb.fb_Bpp);
			    rfb.FBU.lines -= cur_height;
		
			    if (rfb.FBU.lines > 0) {
			        rfb.FBU.bytes = rfb.FBU.width * rfb.fb_Bpp; // At least another line
			    } else {
			        rfb.FBU.rects -= 1;
			        rfb.FBU.bytes = 0;
			    }
			    //Util.Debug("<< display_raw (" + ws.rQlen() + " bytes)");
			    return true;
			}
		});

	
		encHandlers.put("COPYRECT", new EncHandler() {
			@Override
			public boolean run(RFB rfb) {
			    //Util.Debug(">> display_copy_rect");
				
			    if (rfb.ws.rQwait("COPYRECT", 4)) { return false; }
			    int old_x = rfb.ws.rQshift16();
			    int old_y = rfb.ws.rQshift16();
			    rfb.display.copyImage(old_x, old_y, rfb.FBU.x, rfb.FBU.y, rfb.FBU.width, rfb.FBU.height);
			    rfb.FBU.rects -= 1;
			    rfb.FBU.bytes = 0;
			    return true;
			}
		});

		encHandlers.put("RRE", new EncHandler() {
			@Override
			public boolean run(RFB rfb) {
			    //Util.Debug(">> display_rre (" + rfb.ws.rQlen() + " bytes)");
				byte[] color = null;
		
			    if (rfb.FBU.subrects == 0) {
			        if (rfb.ws.rQwait("RRE", 4+rfb.fb_Bpp)) { return false; }
			        rfb.FBU.subrects = rfb.ws.rQshift32();
			        color = rfb.ws.rQshiftBytes(rfb.fb_Bpp); // Background
			        rfb.display.fillRect(rfb.FBU.x, rfb.FBU.y, rfb.FBU.width, rfb.FBU.height, color);
			    }
			    while ((rfb.FBU.subrects > 0) && (rfb.ws.rQlen() >= (rfb.fb_Bpp + 8))) {
			        color = rfb.ws.rQshiftBytes(rfb.fb_Bpp);
			        int x = rfb.ws.rQshift16();
			        int y = rfb.ws.rQshift16();
			        int width = rfb.ws.rQshift16();
			        int height = rfb.ws.rQshift16();
			        rfb.display.fillRect(rfb.FBU.x + x, rfb.FBU.y + y, width, height, color);
			        rfb.FBU.subrects -= 1;
			    }
			    //Util.Debug("   display_rre: rects: " + rfb.FBU.rects +
			    //           ", rfb.FBU.subrects: " + rfb.FBU.subrects);
		
			    if (rfb.FBU.subrects > 0) {
			        int chunk = Math.min(rre_chunk_sz, rfb.FBU.subrects);
			        rfb.FBU.bytes = (rfb.fb_Bpp + 8) * chunk;
			    } else {
			        rfb.FBU.rects -= 1;
			        rfb.FBU.bytes = 0;
			    }
			    //Util.Debug("<< display_rre, rfb.FBU.bytes: " + rfb.FBU.bytes);
			    return true;
			}
		});

		encHandlers.put("HEXTILE", new EncHandler() {
			@Override
			public boolean run(RFB rfb) {
			    //Util.Debug(">> display_hextile");
				long start = System.currentTimeMillis();
				int subrects;
//			    var subencoding, subrects, color, cur_tile,
//			        tile_x, x, w, tile_y, y, h, xy, s, sx, sy, wh, sw, sh,
//			        rQ = rfb.ws.get_rfb.rQ(), rQi = rfb.ws.get_rfb.rQi(); 
				int rQi =   rfb.ws.get_rQi();
		
			    if (rfb.FBU.tiles == 0) {
			        rfb.FBU.tiles_x = (int) Math.ceil( rfb.FBU.width/16);
			        rfb.FBU.tiles_y = (int) Math.ceil(rfb.FBU.height/16);
			        rfb.FBU.total_tiles = rfb.FBU.tiles_x * rfb.FBU.tiles_y;
			        rfb.FBU.tiles = rfb.FBU.total_tiles;
			    }
		
			    /* rfb.FBU.bytes comes in as 1, rfb.ws.rQlen() at least 1 */
			    while (rfb.FBU.tiles > 0) {
			        rfb.FBU.bytes = 1;
			        if (rfb.ws.rQwait("HEXTILE subencoding", rfb.FBU.bytes)) { return false; }
			        byte subencoding = rfb.ws.rQ[rQi];  // Peek
			        if (subencoding > 30) { // Raw
			            rfb.fail("Disconnected: illegal hextile subencoding " + subencoding);
			            //Util.Debug("rfb.ws.rQslice(0,30):" + rfb.ws.rQslice(0,30));
			            return false;
			        }
			        subrects = 0;
			        int cur_tile = rfb.FBU.total_tiles - rfb.FBU.tiles;
			        int tile_x = cur_tile % rfb.FBU.tiles_x;
			        int tile_y = (int) Math.floor(cur_tile / rfb.FBU.tiles_x);
			        int x = rfb.FBU.x + tile_x * 16;
			        int y = rfb.FBU.y + tile_y * 16;
			        int w = Math.min(16, (rfb.FBU.x + rfb.FBU.width) - x);
			        int h = Math.min(16, (rfb.FBU.y + rfb.FBU.height) - y);
		
			        /* Figure out how much we are expecting */
			        if ((subencoding & 0x01) != 0) { // Raw
			            //Util.Debug("   Raw subencoding");
			            rfb.FBU.bytes += w * h * rfb.fb_Bpp;
			        } else {
			            if ((subencoding & 0x02) != 0) { // Background
			                rfb.FBU.bytes += rfb.fb_Bpp;
			            }
			            if ((subencoding & 0x04) != 0) { // Foreground
			                rfb.FBU.bytes += rfb.fb_Bpp;
			            }
			            if ((subencoding & 0x08) != 0) { // AnySubrects
			                rfb.FBU.bytes += 1;   // Since we aren"t shifting it off
			                if (rfb.ws.rQwait("hextile subrects header", rfb.FBU.bytes)) { return false; }
			                subrects = JSUtils.b2i(rfb.ws.rQ[rQi + rfb.FBU.bytes-1]); // Peek
			                if ((subencoding & 0x10) != 0) { // SubrectsColoured
			                    rfb.FBU.bytes += subrects * (rfb.fb_Bpp + 2);
			                } else {
			                    rfb.FBU.bytes += subrects * 2;
			                }
			            }
			        }
		
			        /*
			        Util.Debug("   tile:" + cur_tile + "/" + (rfb.FBU.total_tiles - 1) +
			              " (" + tile_x + "," + tile_y + ")" +
			              " [" + x + "," + y + "]@" + w + "x" + h +
			              ", subenc:" + subencoding +
			              "(last: " + rfb.FBU.lastsubencoding + "), subrects:" +
			              subrects +
			              ", rfb.ws.rQlen():" + rfb.ws.rQlen() + ", rfb.FBU.bytes:" + rfb.FBU.bytes +
			              " last:" + rfb.ws.rQslice(rfb.FBU.bytes-10, rfb.FBU.bytes) +
			              " next:" + rfb.ws.rQslice(rfb.FBU.bytes-1, rfb.FBU.bytes+10));
			        */
			        if (rfb.ws.rQwait("hextile", rfb.FBU.bytes)) { return false; }
		
			        /* We know the encoding and have a whole tile */
			        rfb.FBU.subencoding = JSUtils.b2i(rfb.ws.rQ[rQi]);
			        rQi += 1;
			        if (rfb.FBU.subencoding == 0) {
			            if ((rfb.FBU.lastsubencoding & 0x01) != 0) {
			                /* Weird: ignore blanks after RAW */
			                Util.Debug("     Ignoring blank after RAW");
			            } else {
			                rfb.display.fillRect(x, y, w, h, rfb.FBU.background);
			            }
			        } else if ((rfb.FBU.subencoding & 0x01) != 0) { // Raw
			            rfb.display.blitImage(x, y, w, h, rfb.ws.rQ, rQi);
			            rQi += rfb.FBU.bytes - 1;
			        } else {
			            if ((rfb.FBU.subencoding & 0x02)!=0) { // Background
			                rfb.FBU.background = JSUtils.slice(rfb.ws.rQ, rQi, rQi + rfb.fb_Bpp);
			                rQi += rfb.fb_Bpp;
			            }
			            if ((rfb.FBU.subencoding & 0x04)!=0) { // Foreground
			                rfb.FBU.foreground = JSUtils.slice(rfb.ws.rQ, rQi, rQi + rfb.fb_Bpp);
			                rQi += rfb.fb_Bpp;
			            }
		
			            rfb.display.startTile(x, y, w, h, rfb.FBU.background);
			            if ((rfb.FBU.subencoding & 0x08) != 0) { // AnySubrects
			                subrects = JSUtils.b2i(rfb.ws.rQ[rQi]);
			                rQi += 1;
			                for (int s = 0; s < subrects; s += 1) {
			                    byte[] color;
								if ((rfb.FBU.subencoding & 0x10) != 0) { // SubrectsColoured
			                        color = JSUtils.slice(rfb.ws.rQ, rQi, rQi + rfb.fb_Bpp);
			                        rQi += rfb.fb_Bpp;
			                    } else {
			                        color = rfb.FBU.foreground;
			                    }
								int xy = JSUtils.b2i(rfb.ws.rQ[rQi]);
			                    rQi += 1;
			                    int sx = (xy >> 4);
			                    int sy = (xy & 0x0f);
		
			                    int wh = JSUtils.b2i(rfb.ws.rQ[rQi]);
			                    rQi += 1;
			                    int sw = (wh >> 4)   + 1;
			                    int sh = (wh & 0x0f) + 1;
		
			                    rfb.display.subTile(sx, sy, sw, sh, color);
			                }
			            }
			            rfb.display.finishTile();
			        }
			        rfb.ws.set_rQi(rQi);
			        rfb.FBU.lastsubencoding = rfb.FBU.subencoding;
			        rfb.FBU.bytes = 0;
			        rfb.FBU.tiles -= 1;
			    }
		
			    if (rfb.FBU.tiles == 0) {
			        rfb.FBU.rects -= 1;
			    }
		
			    //Util.Debug("<< display_hextile");
			    long end = System.currentTimeMillis();
			    System.err.println("\nTime(s): " + (end - start)/1000);
			    return true;
			}
		});

//	encHandlers.TIGHT_PNG = function display_tight_png() {
//	    //Util.Debug(">> display_tight_png");
//	    var ctl, cmode, clength, getCLength, color, img;
//	    //Util.Debug("   rfb.FBU.rects: " + rfb.FBU.rects);
//	    //Util.Debug("   starting rfb.ws.rQslice(0,20): " + rfb.ws.rQslice(0,20) + " (" + rfb.ws.rQlen() + ")");
//
//	    rfb.FBU.bytes = 1; // compression-control byte
//	    if (rfb.ws.rQwait("TIGHT compression-control", rfb.FBU.bytes)) { return false; }
//
//	    // Get "compact length" header and data size
//	    getCLength = function (arr) {
//	        var header = 1, data = 0;
//	        data += arr[0] & 0x7f;
//	        if (arr[0] & 0x80) {
//	            header += 1;
//	            data += (arr[1] & 0x7f) << 7;
//	            if (arr[1] & 0x80) {
//	                header += 1;
//	                data += arr[2] << 14;
//	            }
//	        }
//	        return [header, data];
//	    };
//
//	    ctl = rfb.ws.rQpeek8();
//	    switch (ctl >> 4) {
//	        case 0x08: cmode = "fill"; break;
//	        case 0x09: cmode = "jpeg"; break;
//	        case 0x0A: cmode = "png";  break;
//	        default:   throw("Illegal basic compression received, ctl: " + ctl);
//	    }
//	    switch (cmode) {
//	        // fill uses fb_depth because TPIXELs drop the padding byte
//	        case "fill": rfb.FBU.bytes += fb_depth; break; // TPIXEL
//	        case "jpeg": rfb.FBU.bytes += 3;            break; // max clength
//	        case "png":  rfb.FBU.bytes += 3;            break; // max clength
//	    }
//
//	    if (rfb.ws.rQwait("TIGHT " + cmode, rfb.FBU.bytes)) { return false; }
//
//	    //Util.Debug("   rfb.ws.rQslice(0,20): " + rfb.ws.rQslice(0,20) + " (" + rfb.ws.rQlen() + ")");
//	    //Util.Debug("   cmode: " + cmode);
//
//	    // Determine rfb.FBU.bytes
//	    switch (cmode) {
//	    case "fill":
//	        rfb.ws.rQshift8(); // shift off ctl
//	        color = rfb.ws.rQshiftBytes(fb_depth);
//	        rfb.FBU.imgQ.push({
//	                "type": "fill",
//	                "img": {"complete": true},
//	                "x": rfb.FBU.x,
//	                "y": rfb.FBU.y,
//	                "width": rfb.FBU.width,
//	                "height": rfb.FBU.height,
//	                "color": color});
//	        break;
//	    case "jpeg":
//	    case "png":
//	        clength = getCLength(rfb.ws.rQslice(1, 4));
//	        rfb.FBU.bytes = 1 + clength[0] + clength[1]; // ctl + clength size + jpeg-data
//	        if (rfb.ws.rQwait("TIGHT " + cmode, rfb.FBU.bytes)) { return false; }
//
//	        // We have everything, render it
//	        //Util.Debug("   png, rfb.ws.rQlen(): " + rfb.ws.rQlen() + ", clength[0]: " + clength[0] + ", clength[1]: " + clength[1]);
//	        rfb.ws.rQshiftBytes(1 + clength[0]); // shift off ctl + compact length
//	        img = new Image();
//	        //img.onload = scan_tight_imgQ;
//	        rfb.FBU.imgQ.push({
//	                "type": "img",
//	                "img": img,
//	                "x": rfb.FBU.x,
//	                "y": rfb.FBU.y});
//	        img.src = "data:image/" + cmode +
//	            extract_data_uri(rfb.ws.rQshiftBytes(clength[1]));
//	        img = null;
//	        break;
//	    }
//	    rfb.FBU.bytes = 0;
//	    rfb.FBU.rects -= 1;
//	    //Util.Debug("   ending rfb.ws.rQslice(0,20): " + rfb.ws.rQslice(0,20) + " (" + rfb.ws.rQlen() + ")");
//	    //Util.Debug("<< display_tight_png");
//	    return true;
//	};
		
		}
	
	
	
	
	
//
//	extract_data_uri = function(arr) {
//	    //var i, stra = [];
//	    //for (i=0; i< arr.length; i += 1) {
//	    //    stra.push(String.fromCharCode(arr[i]));
//	    //}
//	    //return "," + escape(stra.join(""));
//	    return ";base64," + Base64.encode(arr);
//	};
//
	private void scan_tight_imgQ() {
//	    var data, imgQ, ctx;
//	    Context2d ctx = display.get_context();
	    if (rfb_state.equals("normal")) {
	        byte[] imgQ = FBU.imgQ;
	        while ((imgQ.length > 0) /*&& (imgQ[0].img.complete)*/) {
//	            data = imgQ.shift();
//	            if (data["type"] === "fill") {
//	                display.fillRect(data.x, data.y, data.width, data.height, data.color);
//	            } else {
//	                ctx.drawImage(data.img, data.x, data.y);
//	            }
	        }
//	        setTimeout(scan_tight_imgQ, scan_imgQ_rate);
	    }
	}

//	encHandlers.DesktopSize = function set_desktopsize() {
//	    Util.Debug(">> set_desktopsize");
//	    fb_width = FBU.width;
//	    fb_height = FBU.height;
//	    display.resize(fb_width, fb_height);
//	    timing.fbu_rt_start = (new Date()).getTime();
//	    // Send a new non-incremental request
//	    ws.send(fbUpdateRequests());
//
//	    FBU.bytes = 0;
//	    FBU.rects -= 1;
//
//	    Util.Debug("<< set_desktopsize");
//	    return true;
//	};
//
//	encHandlers.Cursor = function set_cursor() {
//	    var x, y, w, h, pixelslength, masklength;
//	    //Util.Debug(">> set_cursor");
//	    x = FBU.x;  // hotspot-x
//	    y = FBU.y;  // hotspot-y
//	    w = FBU.width;
//	    h = FBU.height;
//
//	    pixelslength = w * h * fb_Bpp;
//	    masklength = Math.floor((w + 7) / 8) * h;
//
//	    FBU.bytes = pixelslength + masklength;
//	    if (ws.rQwait("cursor encoding", FBU.bytes)) { return false; }
//
//	    //Util.Debug("   set_cursor, x: " + x + ", y: " + y + ", w: " + w + ", h: " + h);
//
//	    display.changeCursor(ws.rQshiftBytes(pixelslength),
//	                            ws.rQshiftBytes(masklength),
//	                            x, y, w, h);
//
//	    FBU.bytes = 0;
//	    FBU.rects -= 1;
//
//	    //Util.Debug("<< set_cursor");
//	    return true;
//	};
//
//	encHandlers.JPEG_quality_lo = function set_jpeg_quality() {
//	    Util.Error("Server sent jpeg_quality pseudo-encoding");
//	};
//
//	encHandlers.compress_lo = function set_compress_level() {
//	    Util.Error("Server sent compress level pseudo-encoding");
//	};
//
	/*
	 * Client message routines
	 */

	private byte[] pixelFormat() {
	    //Util.Debug(">> pixelFormat");
//	    var arr;
		return new byte[] {
				0, 	// msg-type
				
				0,  // padding
				0,  // padding
				0,  // padding
				
				(byte) (fb_Bpp * 8), // bits-per-pixel
				(byte) (fb_depth * 8), // depth
				0,  // little-endian
				(byte) (Defaults.true_color ? 1 : 0),  // true-color

				0, -1, // red-max (255)
				0, -1, // green-max (255)
				0, -1, // blue-max (255)
			    0,     // red-shift
			    8,     // green-shift
			    16,    // blue-shift

				0,  // padding
				0,  // padding
				0,  // padding
		};
	    //Util.Debug("<< pixelFormat");
	};

	private byte[] clientEncodings() {
	    //Util.Debug(">> clientEncodings");
		
		List<Integer> encList = new ArrayList<Integer>(encodings.length);

	    for (int i=0; i<encodings.length; i += 1) {
	        if ((encodings[i].key.equals("Cursor")) &&
	            (! Defaults.local_cursor)) {
	            Util.Debug("Skipping Cursor pseudo-encoding");
	        } else {
	            //Util.Debug("Adding encoding: " + encodings[i][0]);
	            encList.add(encodings[i].val);
	        }
	    }

		byte arr[] = new byte[] {
			2,      // msg-type
			0,  // padding

			0, (byte) encList.size(), // encoding count
		};
	    for (int i=0; i < encList.size(); i += 1) {
	        arr = JSUtils.concat(arr, JSUtils.intAsByte32(encList.get(i)));
	    }
	    //Util.Debug("<< clientEncodings: " + arr);
	    return arr;
	};

	private byte[] fbUpdateRequest(int incremental, int x, int y, int xw, int yw) {
	    //Util.Debug(">> fbUpdateRequest");
//	    if (typeof(x) === "undefined") { x = 0; }
//	    if (typeof(y) === "undefined") { y = 0; }
//	    if (typeof(xw) === "undefined") { xw = fb_width; }
//	    if (typeof(yw) === "undefined") { yw = fb_height; }
	    byte[] arr = new byte[] {
	    		3,  // msg-type
	    		(byte) incremental,
	    };
	    return JSUtils.concat(arr, 
	    		JSUtils.intAsByte16(x), 
	    		JSUtils.intAsByte16(y), 
	    		JSUtils.intAsByte16(xw), 
	    		JSUtils.intAsByte16(yw)
	    		);
	    //Util.Debug("<< fbUpdateRequest");
	};

	// Based on clean/dirty areas, generate requests to send
	private byte[] fbUpdateRequests() {
		byte[] arr = new byte[] {};

	    CleanDirtyResetReturn cleanDirty = display.getCleanDirtyReset();
	    Rect cb = cleanDirty.cleanBox;
	    if (cb.w > 0 && cb.h > 0) {
	        // Request incremental for clean box
	        arr = JSUtils.concat(arr, fbUpdateRequest(1, cb.x, cb.y, cb.w, cb.h));
	    }
	    for (int i = 0; i < cleanDirty.dirtyBoxes.size(); i++) {
	        Rect db = cleanDirty.dirtyBoxes.get(i);
	        // Force all (non-incremental for dirty box
	        arr = JSUtils.concat(arr, fbUpdateRequest(0, db.x, db.y, db.w, db.h));
	    }
	    return arr;
	};

	public byte[] keyEvent (int keysym, byte down) {
	    //Util.Debug(">> keyEvent, keysym: " + keysym + ", down: " + down);
	    byte[] arr = new byte[] {
	    		4, // msg-type
	    		down
	    		}; 
	    return JSUtils.concat(arr, JSUtils.intAsByte16(0), JSUtils.intAsByte32(keysym));
	    //Util.Debug("<< keyEvent");
	}

	private byte[] pointerEvent(int x, int y) {
	    Util.Debug(">> pointerEvent, x,y: " + x + "," + y +
	               " , mask: " + mouse_buttonMask);
	    byte[] arr = new byte[] {
	    		5,  // msg-type
	    		mouse_buttonMask
	    };
	    //Util.Debug("<< pointerEvent");
	    return JSUtils.concat(arr, JSUtils.intAsByte16(x), JSUtils.intAsByte16(y));
	};
//
//	clientCutText = function(text) {
//	    //Util.Debug(">> clientCutText");
//	    var arr, i, n;
//	    arr = [6];     // msg-type
//	    arr.push8(0);  // padding
//	    arr.push8(0);  // padding
//	    arr.push8(0);  // padding
//	    arr.push32(text.length);
//	    n = text.length;
//	    for (i=0; i < n; i+=1) {
//	        arr.push(text.charCodeAt(i));
//	    }
//	    //Util.Debug("<< clientCutText:" + arr);
//	    return arr;
//	};
//
//
//
//	//
//	// Public API interface functions
//	//
//
	public void connect(String host, String port, String password, String path) {
//	    //Util.Debug(">> connect");
//
	    rfb_host       = host;
	    rfb_port       = port;
//	    rfb_password   = (password !== undefined)   ? password : "";
//	    rfb_path       = (path !== undefined) ? path : "";
	    rfb_password = password;
	    rfb_path = path;

//	    if ((!rfb_host) || (!rfb_port)) {
//	        return fail("Must set host and port");
//	    }

	    updateState("connect", null);
	    //Util.Debug("<< connect");
	}

	public void disconnect() {
	    //Util.Debug(">> disconnect");
	    updateState("disconnect", "Disconnecting");
	    //Util.Debug("<< disconnect");
	}

//	that.sendPassword = function(passwd) {
//	    rfb_password = passwd;
//	    rfb_state = "Authentication";
//	    setTimeout(init_msg, 1);
//	};
//
//	that.sendCtrlAltDel = function() {
//	    if (rfb_state !== "normal") { return false; }
//	    Util.Info("Sending Ctrl-Alt-Del");
//	    var arr = [];
//	    arr = arr.concat(keyEvent(0xFFE3, 1)); // Control
//	    arr = arr.concat(keyEvent(0xFFE9, 1)); // Alt
//	    arr = arr.concat(keyEvent(0xFFFF, 1)); // Delete
//	    arr = arr.concat(keyEvent(0xFFFF, 0)); // Delete
//	    arr = arr.concat(keyEvent(0xFFE9, 0)); // Alt
//	    arr = arr.concat(keyEvent(0xFFE3, 0)); // Control
//	    arr = arr.concat(fbUpdateRequests());
//	    ws.send(arr);
//	};
//
//	// Send a key press. If "down" is not specified then send a down key
//	// followed by an up key.
//	that.sendKey = function(code, down) {
//	    if (rfb_state !== "normal") { return false; }
//	    var arr = [];
//	    if (typeof down !== "undefined") {
//	        Util.Info("Sending key code (" + (down ? "down" : "up") + "): " + code);
//	        arr = arr.concat(keyEvent(code, down ? 1 : 0));
//	    } else {
//	        Util.Info("Sending key code (down + up): " + code);
//	        arr = arr.concat(keyEvent(code, 1));
//	        arr = arr.concat(keyEvent(code, 0));
//	    }
//	    arr = arr.concat(fbUpdateRequests());
//	    ws.send(arr);
//	};
//
//	that.clipboardPasteFrom = function(text) {
//	    if (rfb_state !== "normal") { return; }
//	    //Util.Debug(">> clipboardPasteFrom: " + text.substr(0,40) + "...");
//	    ws.send(clientCutText(text));
//	    //Util.Debug("<< clipboardPasteFrom");
//	};
//
//	// Override internal functions for testing
//	that.testMode = function(override_send) {
//	    test_mode = true;
//	    that.recv_message = ws.testMode(override_send);
//
//	    checkEvents = function () { /* Stub Out */ };
//	    that.connect = function(host, port, password) {
//	            rfb_host = host;
//	            rfb_port = port;
//	            rfb_password = password;
//	            updateState("ProtocolVersion", "Starting VNC handshake");
//	        };
//	};

}
