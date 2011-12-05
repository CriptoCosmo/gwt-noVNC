package noVNC.core;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dom.client.NativeEvent;

import noVNC.core.WebSocket.MessageEvent;
import noVNC.core.WebSocket.WebSocketHandler;
import noVNC.utils.DataUtils;

/**
 * Websock is similar to the standard WebSocket object but Websock
 * enables communication with raw TCP sockets (i.e. the binary stream)
 * via websockify. This is accomplished by base64 encoding the data
 * stream between Websock and websockify.
 *
 * Websock has built-in receive queue buffering; the message event
 * does not contain actual data but is simply a notification that
 * there is new data available. Several rQ* methods are available to
 * read binary data off of the receive queue.
 */
public class WebSock {

//	// Load Flash WebSocket emulator if needed
//
//	if (window.WebSocket) {
//	    Websock_native = true;
//	} else if (window.MozWebSocket) {
//	    Websock_native = true;
//	    window.WebSocket = window.MozWebSocket;
//	} else {
//	    /* no builtin WebSocket so load web_socket.js */
//	    Websock_native = false;
//	    (function () {
//	        function get_INCLUDE_URI() {
//	            return (typeof INCLUDE_URI !== "undefined") ?
//	                INCLUDE_URI : "include/";
//	        }
//
//	        var start = "<script src='" + get_INCLUDE_URI(),
//	            end = "'><\/script>", extra = "";
//
//	        WEB_SOCKET_SWF_LOCATION = get_INCLUDE_URI() +
//	                    "web-socket-js/WebSocketMain.swf";
//	        if (Util.Engine.trident) {
//	            Util.Debug("Forcing uncached load of WebSocketMain.swf");
//	            WEB_SOCKET_SWF_LOCATION += "?" + Math.random();
//	        }
//	        extra += start + "web-socket-js/swfobject.js" + end;
//	        extra += start + "web-socket-js/web_socket.js" + end;
//	        document.write(extra);
//	    }());
//	}
//

//	var api = {},         // Public API
	private WebSocket websocket = null; // WebSocket object
    byte[] rQ = new byte[] {};          // Receive queue
    int rQi = 0;          // Receive queue index
    int rQmax = 10000;    // Max receive queue size before compacting
    byte[] sQ = new byte[] {};          // Send queue

    boolean test_mode = false;

//	function Websock() {
//	"use strict";
//
//
//
	public WebSocketHandler eventHandlers = null;
	
	public void hook(WebSocketHandler handler) {
		eventHandlers = handler;
	}

	public static int  maxBufferedAmount = 200;
	
	public WebSock() {
//    // Configuration settings
//
//    // Direct access to send and receive queues
//    api.get_sQ       = get_sQ;
//    api.get_rQ       = get_rQ;
//    api.get_rQi      = get_rQi;
//    api.set_rQi      = set_rQi;
//
//    // Routines to read from the receive queue
//    api.rQlen        = rQlen;
//    api.rQpeek8      = rQpeek8;
//    api.rQshift8     = rQshift8;
//    api.rQunshift8   = rQunshift8;
//    api.rQshift16    = rQshift16;
//    api.rQshift32    = rQshift32;
//    api.rQshiftStr   = rQshiftStr;
//    api.rQshiftBytes = rQshiftBytes;
//    api.rQslice      = rQslice;
//    api.rQwait       = rQwait;
//
//    api.flush        = flush;
//    api.send         = send;
//    api.send_string  = send_string;
//
//    api.on           = on;
//    api.init         = init;
//    api.open         = open;
//    api.close        = close;
//    api.testMode     = testMode;
//
//    return api;
	}

	//
	// Queue public functions
	//

	public byte[] get_sQ() {
	    return sQ;
	}

	public byte[] get_rQ() {
	    return rQ;
	}
	public int get_rQi() {
	    return rQi;
	}
	public void set_rQi(int val) {
	    rQi = val;
	};

	public int rQlen() {
	    return rQ.length - rQi;
	}

	public byte rQpeek8() {
	    return (rQ[rQi]      );
	}
	public byte rQshift8() {
	    return (rQ[rQi++]      );
	}
	
	private static byte[] jsConcat(byte[] orig, byte[] in) {
		return JSUtils.concat(orig, in);
	}
	private static byte[] jsSlice(byte[] orig, int start, int end) {
		return JSUtils.slice(orig, start, end);
	}
	private static byte[] jsSlice(byte[] orig, int start) {
		return JSUtils.slice(orig, start);
	}
	private byte[] jsUnShift(byte[] orig, byte in) {
		return jsConcat(new byte[] {in}, orig);
	}
	public void rQunshift8(int num) {
		rQunshift8((byte)num);
	}
	public void rQunshift8(byte num) {
	    if (rQi == 0) {
	        rQ = jsUnShift(rQ, num);
	    } else {
	        rQi -= 1;
	        rQ[rQi] = num;
	    }
	}
	public void rQunshift32(int num) {
		byte[] bnum = JSUtils.intAsByte32(num);
	    if (rQi == 0) {
	    	rQ = jsConcat(bnum, rQ);
	    } else {
	        rQi -= 4;
	        rQ[rQi] = bnum[0];
	        rQ[rQi+1] = bnum[0+1];
	        rQ[rQi+2] = bnum[0+2];
	        rQ[rQi+3] = bnum[0+3];
	    }
	}
	public int rQshift16() {
		int ret = JSUtils.byte16AsInt(rQ, rQi);
		rQi+=2;
		return ret;
	}
	public int rQshift32() {
		int ret = JSUtils.byte32AsInt(rQ, rQi);
		rQi+=4;
		return ret;
	}
	public String rQshiftStr(int len) {
	    byte[] arr = jsSlice(rQ, rQi, rQi + len);
	    rQi += len;
	    return new String(arr);

	}
	public byte[] rQshiftBytes(int len) {
	    rQi += len;
	    return jsSlice(rQ, rQi-len, rQi);
	}

	public byte[] rQslice(int start, int end) {
	    if (end != -1) {
	        return jsSlice(rQ, rQi + start, rQi + end);
	    } else {
	        return jsSlice(rQ, rQi + start);
	    }
	}

	// Check to see if we must wait for 'num' bytes (default to FBU.bytes)
	// to be available in the receive queue. Return true if we need to
	// wait (and possibly print a debug message), otherwise false.
	public boolean rQwait(String msg, int num) {
		return rQwait(msg, num, -1);
	}
	public boolean rQwait(String msg, int num, int goback) {
	    int rQlen = rQ.length - rQi; // Skip rQlen() function call
	    if (rQlen < num) {
	        if (goback != -1) {
	            if (rQi < goback) {
	                throw new RuntimeException("rQwait cannot backup " + goback + " bytes");
	            }
	            rQi -= goback;
	        }
	        //Util.Debug("   waiting for " + (num-rQlen) +
	        //           " " + msg + " byte(s)");
	        return true;  // true means need more data
	    }
	    return false;
	}

	//
	// Private utility routines
	//

	private String encode_message(byte[] data) {
	    /* base64 encode */
		return Base64.encode(data);
	}

	private void decode_message(String data) {
	    //Util.Debug(">> decode_message: " + data);
	    /* base64 decode */
		DataUtils.printCSData(DataUtils.receiving, new String(Base64.decode(data, 0)), data);
		if (rQlen()==0) {
			// we don't need to concat when everything has been read
			rQ = new byte[] {};	
			rQi = 0;
		}
		rQ = jsConcat(rQ, Base64.decode(data, 0));

		//Util.Debug(">> decode_message, rQ: " + rQ);
	}

	//
	// Public Send functions
	//

	public boolean flush() {
	    if (websocket.getBufferedAmount() != 0) {
	        Util.Debug("bufferedAmount: " + websocket.getBufferedAmount());
	    }
	    if (websocket.getBufferedAmount() < maxBufferedAmount) {
	        //Util.Debug("arr: " + arr);
	        //Util.Debug("sQ: " + sQ);
	        if (sQ.length > 0) {
    			DataUtils.printSCData(DataUtils.sending, new String(sQ), encode_message(sQ));
	            websocket.send(encode_message(sQ));
	        	sQ = new byte[] {};
	        }
	        return true;
	    } else {
	        Util.Info("Delaying send, bufferedAmount: " + websocket.getBufferedAmount());
	        return false;
	    }
	}

	// overridable for testing
	public boolean send(byte[] arr) {
	    //Util.Debug(">> send_array: " + arr);
	    sQ = jsConcat(sQ, arr);
	    return flush();
	}

	public void send_string(String str) {
	    //Util.Debug(">> send_string: " + str);
	    this.send(str.getBytes());
	}

	//
	// Other public functions

	private void recv_message(MessageEvent e) {
	    //Util.Debug(">> recv_message: " + e.data.length);

//	    try {
	        decode_message(e.getData());
	        if (rQlen() > 0) {
	            eventHandlers.onMessage(e);
//	            // Compact the receive queue
	            if (rQ.length > rQmax) {
	                Util.Debug("*** Compacting receive queue");
	                rQ = jsSlice(rQ, rQi);
	                rQi = 0;
	            }
	        } else {
	            Util.Debug("Ignoring empty message");
	        }
//	    } catch (exc) {
//	        if (typeof exc.stack !== 'undefined') {
//	            Util.Warn("recv_message, caught exception: " + exc.stack);
//	        } else if (typeof exc.description !== 'undefined') {
//	            Util.Warn("recv_message, caught exception: " + exc.description);
//	        } else {
//	            Util.Warn("recv_message, caught exception:" + exc);
//	        }
//	        if (typeof exc.name !== 'undefined') {
//	            eventHandlers.error(exc.name + ": " + exc.message);
//	        } else {
//	            eventHandlers.error(exc);
//	        }
//	    }
	    //Util.Debug("<< recv_message");
	}
//
//
//	// Set event handlers
//	function on(evt, handler) { 
//	    eventHandlers[evt] = handler;
//	}
	
//
	public void init() {
	    rQ         = new byte[] {};
	    rQi        = 0;
	    sQ         = new byte[] {};
	    websocket  = null;
	}

	public void open(String uri) {
	    init();

	    if (test_mode) {
	        websocket = null;
	        return;
	    }
	    
	    try {
	    	

	    websocket = WebSocket.create(uri, "base64");
	    // TODO: future native binary support
	    //websocket = new WebSocket(uri, ['binary', 'base64']);
	    } catch (JavaScriptException e) {
	    	e.printStackTrace();
	    }
	    
	    System.err.println(websocket.getProtocol());
	    System.err.println(websocket.getReadyState());

	    websocket.hook(new WebSocketHandler() {
	    	@Override
	    	public void onMessage(MessageEvent event) {
	    		try {
	    			recv_message(event);
	    		} catch (Throwable t) {
	    			System.err.println("Unexpected Exception");
	    			t.printStackTrace(System.err);
	    		}
	    	}
			@Override
			public void onOpen(NativeEvent e) {
		        Util.Debug(">> WebSock.onopen");
	            Util.Info("Server chose sub-protocol: " + websocket.getProtocol());
	            eventHandlers.onOpen(e);
	            Util.Debug("<< WebSock.onopen");
			}
			@Override
			public void onClose(NativeEvent e) {
				Util.Debug(">> WebSock.onclose");
				eventHandlers.onClose(e);
				Util.Debug("<< WebSock.onclose");
			}
			@Override
			public void onError(NativeEvent e) {
				Util.Debug(">> WebSock.onerror: " + e);
				eventHandlers.onError(e);
				Util.Debug("<< WebSock.onerror");
			}
		});
	}

	public void close() {
	    if (websocket != null) {
	        if ((websocket.getReadyState() == WebSocket.OPEN) ||
	            (websocket.getReadyState() == WebSocket.CONNECTING)) {
	            Util.Info("Closing WebSocket connection");
	            websocket.close();
	        }
//	        websocket.onmessage = function (e) { return; };
	    }
	}

//	// Override internal functions for testing
//	// Takes a send function, returns reference to recv function
//	function testMode(override_send) {
//	    test_mode = true;
//	    api.send = override_send;
//	    api.close = function () {};
//	    return recv_message;
//	}
//
}
