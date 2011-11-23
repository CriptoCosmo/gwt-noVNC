package noVNC.core;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;

public final class WebSocket extends JavaScriptObject {
	
	public interface WebSocketHandler {
		public void onOpen(NativeEvent e);
		public void onClose(NativeEvent e);
		public void onError(NativeEvent e);
		public void onMessage(MessageEvent e);
	}
	
	public static final class MessageEvent extends JavaScriptObject {
		protected MessageEvent(){}
		public final native String getData() /*-{
			return this.data;
		}-*/;
	}

	public static final int CONNECTING = 0;
	public static final int OPEN = 1;
	public static final int CLOSING = 2;
	public static final int CLOSED = 3;
	
	protected WebSocket() {}

	public static native WebSocket create(String url) /*-{
		return new $wnd.WebSocket(url);
	}-*/;
	
	public static native WebSocket create(String url, String protocol) /*-{
		return new $wnd.WebSocket(url, protocol);
    }-*/;
	
	public native void hook(WebSocketHandler handler) /*-{
		this.onopen = $entry(function(e) {
			handler.@noVNC.core.WebSocket.WebSocketHandler::onOpen(Lcom/google/gwt/dom/client/NativeEvent;)(e);
		});
		this.onclose = $entry(function(e) {
			handler.@noVNC.core.WebSocket.WebSocketHandler::onClose(Lcom/google/gwt/dom/client/NativeEvent;)(e);
		});
		this.onerror = $entry(function(e) {
			handler.@noVNC.core.WebSocket.WebSocketHandler::onError(Lcom/google/gwt/dom/client/NativeEvent;)(e);
		});
		this.onmessage = $entry(function(e) {
			handler.@noVNC.core.WebSocket.WebSocketHandler::onMessage(LnoVNC/core/WebSocket$MessageEvent;)(e);
		});
    }-*/;
	

	  public native final int getReadyState() /*-{
	    return this.readyState;
	  }-*/;

	  public native final double getBufferedAmount() /*-{
	    return this.bufferedAmount;
	  }-*/;

	  public native final String getProtocol() /*-{
	    return this.protocol;
	  }-*/;

	  public native final void send(String message) /*-{
	    this.send(message);
	  }-*/;

	  public native final void close() /*-{
	    this.close();
	  }-*/;
	  
}
