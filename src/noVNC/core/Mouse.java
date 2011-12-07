package noVNC.core;

import noVNC.utils.Point;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

public class Mouse {
	//
	// Mouse event handler
	//

//	function Mouse(defaults) {
//	"use strict";
//
//	var that           = {},  // Public API methods
//	    conf           = {};  // Configuration attributes

	// Configuration attributes
//	Util.conf_defaults(conf, that, defaults, [
//	    ['target',         'ro', 'dom',  document, 'DOM element that captures mouse input'],
//	    ['focused',        'rw', 'bool', true, 'Capture and send mouse clicks/movement'],
//	    ['scale',          'rw', 'float', 1.0, 'Viewport scale factor 0.0 - 1.0'],
//
//	    ['onMouseButton',  'rw', 'func', null, 'Handler for mouse button click/release'],
//	    ['onMouseMove',    'rw', 'func', null, 'Handler for mouse movement'],
//	    ['touchButton',    'rw', 'int', 1, 'Button mask (1, 2, 4) for touch devices (0 means ignore clicks)']
//	    ]);
	
	public boolean focused = true; //'Capture and send mouse clicks/movement'
	public float scale = (float) 1.0; //'Viewport scale factor 0.0 - 1.0'
	public interface MouseHandler {
		public void onMouseButton(int x, int y, boolean down, int bmask);
		public void onMouseMove(int x, int y);
	}
	
	private MouseHandler mouseHandler = null;
	public void hook(MouseHandler _mh) {
		mouseHandler = _mh;
	}


	// 
	// Private functions
	//

	private boolean onMouseButton(NativeEvent e, boolean down) {
		int bmask = 0;
		Point pos = new Point(0, 0);
		NativeEvent evt = e;
		
//	    if (! (Boolean)Defaults.map.get("focused")) {
//	        return true;
//	    }
		
//		if (e == null)
////			evt = window.event;
//			evt = Event.getCurrentEvent();
	    
		pos = Util.getEventPosition(e, Defaults.target, scale);
	    
//		if (e.getTouches().length() > 0 || e.getChangedTouches().length() > 0) {
	        // Touch device
//	        bmask = conf.touchButton;
	        // If bmask is set
//	    } else /*if (evt.which) */{
	        /* everything except IE */
//	        bmask = 1 << evt.getButton();
//	    } else {
//	        /* IE including 9 */
	        bmask = ((evt.getButton() & 0x1) +      // Left
	                (evt.getButton() & 0x2) * 2 +  // Right
	                (evt.getButton() & 0x4) / 2);   // Middle
//	    }
	    //Util.Debug("mouse " + pos.x + "," + pos.y + " down: " + down +
	    //           " bmask: " + bmask + "(evt.button: " + evt.button + ")");
	    if (bmask > 0 && mouseHandler != null) {
//	        Util.Debug("onMouseButton " + (down ? "down" : "up") + ", x: " + pos.x + ", y: " + pos.y + ", bmask: " + bmask);
	    	Util.Debug("Handle Mouse Click: " + System.currentTimeMillis());
	        mouseHandler.onMouseButton(pos.x, pos.y, down, bmask);
	    }
	    Util.stopEvent(e);
	    return false;
	}

	private boolean onMouseDown(NativeEvent e) {
	    return onMouseButton(e, true /* was 1*/);
	}

	private boolean onMouseUp(NativeEvent e) {
	    return onMouseButton(e, false /* was 0*/);
	}

//	function onMouseWheel(e) {
//	    var evt, pos, bmask, wheelData;
//	    if (! conf.focused) {
//	        return true;
//	    }
//	    evt = (e ? e : window.event);
//	    pos = Util.getEventPosition(e, conf.target, conf.scale);
//	    wheelData = evt.detail ? evt.detail * -1 : evt.wheelDelta / 40;
//	    if (wheelData > 0) {
//	        bmask = 1 << 3;
//	    } else {
//	        bmask = 1 << 4;
//	    }
//	    //Util.Debug('mouse scroll by ' + wheelData + ':' + pos.x + "," + pos.y);
//	    if (conf.onMouseButton) {
//	        conf.onMouseButton(pos.x, pos.y, 1, bmask);
//	        conf.onMouseButton(pos.x, pos.y, 0, bmask);
//	    }
//	    Util.stopEvent(e);
//	    return false;
//	}

	private boolean onMouseMove(NativeEvent e) {
	    NativeEvent evt; 
	    Point pos = null;
//	   if (! (Boolean)Defaults.map.get("focused")) {
//        return true;
//	   }

	   //	    evt = (e ? e : window.event);
//	    if (e == null)
//			evt = Event.getCurrentEvent();
	    
	    pos = Util.getEventPosition(e, Defaults.target, scale);
	    //Util.Debug('mouse ' + evt.which + '/' + evt.button + ' up:' + pos.x + "," + pos.y);
//	    Util.Debug("Move mouse: " + pos.x + "," + pos.y);
	    if (mouseHandler != null) {
	        mouseHandler.onMouseMove(pos.x, pos.y);
	    }
	    Util.stopEvent(e);
	    return false;
	}
	
//	function onMouseDisable(e) {
//	    var evt, pos;
//	    if (! conf.focused) {
//	        return true;
//	    }
//	    evt = (e ? e : window.event);
//	    pos = Util.getEventPosition(e, conf.target, conf.scale);
//	    /* Stop propagation if inside canvas area */
//	    if ((pos.x >= 0) && (pos.y >= 0) &&
//	        (pos.x < conf.target.offsetWidth) &&
//	        (pos.y < conf.target.offsetHeight)) {
//	        //Util.Debug("mouse event disabled");
//	        Util.stopEvent(e);
//	        return false;
//	    }
//	    //Util.Debug("mouse event not disabled");
//	    return true;
//	}
//
	//
	// Public API interface functions
	//


	public void grab() {
	    //Util.Debug(">> Mouse.grab");
	    Element c = (Element) Defaults.target;
//
//	    if ('ontouchstart' in document.documentElement) {
//	        Util.addEvent(c, 'touchstart', onMouseDown);
//	        Util.addEvent(c, 'touchend', onMouseUp);
//	        Util.addEvent(c, 'touchmove', onMouseMove);
//	    } else {
	        Util.addEvent(c, "mousedown", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseDown(e);
				}
			});

	        Util.addEvent(c, "mouseup", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseUp(e);
				}
			});

	        Util.addEvent(c, "mousemove", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseMove(e);
				}
			});
	        
//	        Util.addEvent(c, (Util.Engine.gecko) ? 'DOMMouseScroll' : 'mousewheel',
//	                onMouseWheel);
//	    }
//
//	    /* Work around right and middle click browser behaviors */
//	    Util.addEvent(document, 'click', onMouseDisable);
//	    Util.addEvent(document.body, 'contextmenu', onMouseDisable);
//
//	    //Util.Debug("<< Mouse.grab");
	}

	public void ungrab() {
//	    //Util.Debug(">> Mouse.ungrab");
		Element c = (Element) Defaults.target;

//	    if ('ontouchstart' in document.documentElement) {
//	        Util.removeEvent(c, 'touchstart', onMouseDown);
//	        Util.removeEvent(c, 'touchend', onMouseUp);
//	        Util.removeEvent(c, 'touchmove', onMouseMove);
//	    } else {
	        Util.removeEvent(c, "mousedown", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseDown(e);
				}
			});
	        
	        Util.removeEvent(c, "mouseup", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseUp(e);
				}
			});

	        Util.removeEvent(c, "mousemove", new Util.NativeEventHandler() {
				@Override
				public boolean run(NativeEvent e) {
					return onMouseMove(e);
				}
			});
//	        Util.removeEvent(c, (Util.Engine.gecko) ? 'DOMMouseScroll' : 'mousewheel',
//	                onMouseWheel);
//	    }
//
//	    /* Work around right and middle click browser behaviors */
//	    Util.removeEvent(document, 'click', onMouseDisable);
//	    Util.removeEvent(document.body, 'contextmenu', onMouseDisable);
//
//	    //Util.Debug(">> Mouse.ungrab");
	}
//
//	return that;  // Return the public API interface
//
//	}  // End of Mouse()
}
