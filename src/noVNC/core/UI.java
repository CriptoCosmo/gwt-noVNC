package noVNC.core;

import noVNC.core.RFB.RFBHandler;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Display;


public class UI {
	
//	rfb_state : "loaded",
//	settingsOpen : false,
//	connSettingsOpen : true,
//	clipboardOpen: false,
//	keyboardVisible: false,
//
	RFB rfb = null;
	
	/**
	 * Render default UI and initialize settings menu
	 */
	public void load() {
//		load: function() {

//	    var html = "", i, sheet, sheets, llevels;
		
//	    // Stylesheet selection dropdown
//	    String sheet = WebUtil.selectStylesheet(null);
//	    List<LinkElement> sheets = WebUtil.getStylesheets();
//	    for (LinkElement linkElement : sheets) {
//	        UI.addOption(WebUtil.D("noVNC_stylesheet"),linkElement.getTitle(), linkElement.getTitle());
//		}
//
//	    // Logging selection dropdown
//	    String llevels[] = {"error", "warn", "info", "debug"};
//	    for (int i = 0; i < llevels.length; i += 1) {
//	        UI.addOption(WebUtil.D("noVNC_logging"),llevels[i], llevels[i]);
//	    }

	    // Settings with immediate effects
//	    UI.initSetting("logging", "warn");
//	    WebUtil.init_logging(UI.getSetting("logging"));
//
//	    UI.initSetting("stylesheet", "default");
//	    WebUtil.selectStylesheet(null);
//	    // call twice to get around webkit bug
//	    WebUtil.selectStylesheet(UI.getSetting("stylesheet"));
//
//	    /* Populate the controls if defaults are provided in the URL */
//	    UI.initSetting("host", "");
//	    UI.initSetting("port", "");
//	    UI.initSetting("password", "");
//	    UI.initSetting("encrypt", false);
//	    UI.initSetting("true_color", true);
//	    UI.initSetting("cursor", false);
//	    UI.initSetting("shared", true);
//	    UI.initSetting("connectTimeout", 2);
//	    UI.initSetting("path", "");
//

		Element canvasElement = WebUtil.D("noVNC_canvas");
		Defaults.target = canvasElement;
//		this.rfb = new RFB({"target": WebUtil.D("noVNC_canvas")}) {
		this.rfb = new RFB();
		this.rfb.hook(new RFBHandler() {
			@Override
			public void onUpdateState(RFB rfb, String state, String oldState, String statusMsg) {
				UI.this.updateState(rfb, state, oldState, statusMsg);
			}
		});
//		this.rfb = new RFB({"target": WebUtil.D("noVNC_canvas")}) {
//	    	public void onUpdateState() {
//	    		UI.updateState();
//	    	}
//	    	public void onClipboard() {
//	    		UI.clipReceive();
//	    	}
//	    };
	    
//	    UI.updateVisualState();
//
//	    // Unfocus clipboard when over the VNC area
//	    //WebUtil.D("VNC_screen").onmousemove = function () {
//	    //         var keyboard = UI.rfb.get_keyboard();
//	    //        if ((! keyboard) || (! keyboard.get_focused())) {
//	    //            WebUtil.D("VNC_clipboard_text").blur();
//	    //         }
//	    //    };
//
//	    // Show mouse selector buttons on touch screen devices
//	    if ("ontouchstart" in document.documentElement) {
//	        // Show mobile buttons
//	        WebUtil.D("noVNC_mobile_buttons").style.display = "inline";
//	        UI.setMouseButton();
//	        // Remove the address bar
//	        setTimeout(function() { window.scrollTo(0, 1); }, 100);
//	        UI.forceSetting("clip", true);
//	        WebUtil.D("noVNC_clip").disabled = true;
//	    } else {
//	        UI.initSetting("clip", false);
//	    }
//
//	    //iOS Safari does not support CSS position:fixed.
//	    //This detects iOS devices and enables javascript workaround.
//	    if ((navigator.userAgent.match(/iPhone/i)) ||
//	        (navigator.userAgent.match(/iPod/i)) ||
//	        (navigator.userAgent.match(/iPad/i))) {
//	        //UI.setOnscroll();
//	        //UI.setResize();
//	    }
//
//	    WebUtil.D("noVNC_host").focus();
//
//	    UI.setViewClip();
//	    Util.addEvent(window, "resize", UI.setViewClip);
//
//	    Util.addEvent(window, "beforeunload", function () {
//	        if (UI.rfb_state === "normal") {
//	            return "You are currently connected.";
//	        }
//	    } );
//
//	},
//
	}

	// Read form control compatible setting from cookie
	public static String getSetting(String name) {
	    Element ctrl = WebUtil.D("noVNC_" + name);
	    String val = WebUtil.readCookie(name);
	    if (ctrl instanceof InputElement && ((InputElement)ctrl).getType().equals("checkbox")) {
	        if (val.toLowerCase().equals("0") ||
	        		val.toLowerCase().equals("no") ||
	        		val.toLowerCase().equals("false")
	        		)
	            val = "false";
	        else
	            val = "true";
	    }
	    return val;
	}

	// Update cookie and form control setting. If value is not set, then
	// updates from control to current cookie setting.
	public static void updateSetting(String name, String value) {
//	    Element ctrl = WebUtil.D("noVNC_" + name);
	    // Save the cookie for this session
//	    if (typeof value !== "undefined") {
	        WebUtil.createCookie(name, value);
//	    }

	    // Update the settings control
	    value = UI.getSetting(name);

//	    if (ctrl instanceof InputElement && ((InputElement)ctrl).getType().equals("checkbox")) {
//	    	if (value.toLowerCase().equals("true")) 
//	    		((InputElement)ctrl).setChecked(true);
//	    }
	    // need to translate below to java
//	    else if (typeof ctrl.options !== "undefined") {
//	        for (i = 0; i < ctrl.options.length; i += 1) {
//	            if (ctrl.options[i].value === value) {
//	                ctrl.selectedIndex = i;
//	                break;
//	            }
//	        }
//	    } else {
//	        //Weird IE9 error leads to "null" appearring
//	        //in textboxes instead of "".
//	        if (value === null) {
//	            value = "";
//	        }
//	        ctrl.value = value;
//	    }
//	};
	}
	
//	// Save control setting to cookie
//	saveSetting: function(name) {
//	    var val, ctrl = WebUtil.D("noVNC_" + name);
//	    if (ctrl.type === "checkbox") {
//	        val = ctrl.checked;
//	    } else if (typeof ctrl.options !== "undefined") {
//	        val = ctrl.options[ctrl.selectedIndex].value;
//	    } else {
//	        val = ctrl.value;
//	    }
//	    WebUtil.createCookie(name, val);
//	    //Util.Debug("Setting saved "" + name + "=" + val + """);
//	    return val;
//	},
//
	/*
	 * Initial page load read/initialization of settings
	 */
	public static void initSetting(String name, String defVal) {
	    String val;

	    // Check Query string followed by cookie
	    val = WebUtil.getQueryVar(name);
	    if (val == null) {
	        val = WebUtil.readCookie(name, defVal);
	    }
	    UI.updateSetting(name, val);
//	 //Util.Debug("Setting "" + name + "" initialized to "" + val + """);
//	    return val;
	}

//	// Force a setting to be a certain value
//	forceSetting: function(name, val) {
//	    UI.updateSetting(name, val);
//	    return val;
//	},
//
//
//	// Show the clipboard panel
//	toggleClipboardPanel: function() {
//	    //Close settings if open
//	    if (UI.settingsOpen == true) {
//	        UI.settingsApply();
//	        UI.closeSettingsMenu();
//	    }
//	    //Close connection settings if open
//	    if (UI.connSettingsOpen == true) {
//	        UI.toggleConnectPanel();
//	    }
//	    //Toggle Clipboard Panel
//	    if (UI.clipboardOpen == true) {
//	        WebUtil.D("noVNC_clipboard").style.display = "none";
//	        WebUtil.D("clipboardButton").className = "noVNC_status_button";
//	        UI.clipboardOpen = false;
//	    } else {
//	        WebUtil.D("noVNC_clipboard").style.display = "block";
//	        WebUtil.D("clipboardButton").className = "noVNC_status_button_selected";
//	        UI.clipboardOpen = true;
//	    }
//	},
//
//	// Show the connection settings panel/menu
//	toggleConnectPanel: function() {
//	    //Close connection settings if open
//	    if (UI.settingsOpen == true) {
//	        UI.settingsApply();
//	        UI.closeSettingsMenu();
//	        WebUtil.D("connectButton").className = "noVNC_status_button";
//	    }
//	    if (UI.clipboardOpen == true) {
//	        UI.toggleClipboardPanel();
//	    }
//
//	    //Toggle Connection Panel
//	    if (UI.connSettingsOpen == true) {
//	        WebUtil.D("noVNC_controls").style.display = "none";
//	        WebUtil.D("connectButton").className = "noVNC_status_button";
//	        UI.connSettingsOpen = false;
//	    } else {
//	        WebUtil.D("noVNC_controls").style.display = "block";
//	        WebUtil.D("connectButton").className = "noVNC_status_button_selected";
//	        UI.connSettingsOpen = true;
//	        WebUtil.D("noVNC_host").focus();
//	    }
//	},
//
//	// Toggle the settings menu:
//	//   On open, settings are refreshed from saved cookies.
//	//   On close, settings are applied
//	toggleSettingsPanel: function() {
//	    if (UI.settingsOpen) {
//	        UI.settingsApply();
//	        UI.closeSettingsMenu();
//	    } else {
//	        UI.updateSetting("encrypt");
//	        UI.updateSetting("true_color");
//	        if (UI.rfb.get_display().get_cursor_uri()) {
//	            UI.updateSetting("cursor");
//	        } else {
//	            UI.updateSetting("cursor", false);
//	            WebUtil.D("noVNC_cursor").disabled = true;
//	        }
//	        UI.updateSetting("clip");
//	        UI.updateSetting("shared");
//	        UI.updateSetting("connectTimeout");
//	        UI.updateSetting("path");
//	        UI.updateSetting("stylesheet");
//	        UI.updateSetting("logging");
//
//	        UI.openSettingsMenu();
//	    }
//	},
//
//	// Open menu
//	openSettingsMenu: function() {
//	    if (UI.clipboardOpen == true) {
//	        UI.toggleClipboardPanel();
//	    }
//	    //Close connection settings if open
//	    if (UI.connSettingsOpen == true) {
//	        UI.toggleConnectPanel();
//	    }
//	    WebUtil.D("noVNC_settings").style.display = "block";
//	    WebUtil.D("settingsButton").className = "noVNC_status_button_selected";
//	    UI.settingsOpen = true;
//	},
//
//	// Close menu (without applying settings)
//	closeSettingsMenu: function() {
//	    WebUtil.D("noVNC_settings").style.display = "none";
//	    WebUtil.D("settingsButton").className = "noVNC_status_button";
//	    UI.settingsOpen = false;
//	},
//
//	// Save/apply settings when "Apply" button is pressed
//	settingsApply: function() {
//	    //Util.Debug(">> settingsApply");
//	    UI.saveSetting("encrypt");
//	    UI.saveSetting("true_color");
//	    if (UI.rfb.get_display().get_cursor_uri()) {
//	        UI.saveSetting("cursor");
//	    }
//	    UI.saveSetting("clip");
//	    UI.saveSetting("shared");
//	    UI.saveSetting("connectTimeout");
//	    UI.saveSetting("path");
//	    UI.saveSetting("stylesheet");
//	    UI.saveSetting("logging");
//
//	    // Settings with immediate (non-connected related) effect
//	    WebUtil.selectStylesheet(UI.getSetting("stylesheet"));
//	    WebUtil.init_logging(UI.getSetting("logging"));
//	    UI.setViewClip();
//	    UI.setViewDrag(UI.rfb.get_viewportDrag());
//	    //Util.Debug("<< settingsApply");
//	},
//
//
//
//	setPassword: function() {
//	    UI.rfb.sendPassword(WebUtil.D("noVNC_password").value);
//	    //Reset connect button.
//	    WebUtil.D("noVNC_connect_button").value = "Connect";
//	    WebUtil.D("noVNC_connect_button").onclick = UI.Connect;
//	    //Hide connection panel.
//	    UI.toggleConnectPanel();
//	    return false;
//	},
//
//	sendCtrlAltDel: function() {
//	    UI.rfb.sendCtrlAltDel();
//	},
//
//	setMouseButton: function(num) {
//	    var b, blist = [0, 1,2,4], button;
//
//	    if (typeof num === "undefined") {
//	        // Disable mouse buttons
//	        num = -1;
//	    }
//	    if (UI.rfb) {
//	        UI.rfb.get_mouse().set_touchButton(num);
//	    }
//
//	    for (b = 0; b < blist.length; b++) {
//	        button = WebUtil.D("noVNC_mouse_button" + blist[b]);
//	        if (blist[b] === num) {
//	            button.style.display = "";
//	        } else {
//	            button.style.display = "none";
//	            /*
//	            button.style.backgroundColor = "black";
//	            button.style.color = "lightgray";
//	            button.style.backgroundColor = "";
//	            button.style.color = "";
//	            */
//	        }
//	    }
//	},
//
	private void updateState(RFB rfb, String state, String oldState, String msg) {
//	    var s, sb, c, d, cad, vd, klass;
//	    UI.rfb_state = state;
	    Element s = WebUtil.D("noVNC_status");
	    Element sb = WebUtil.D("noVNC_status_bar");
	    
	    String klass = "noVNC_status_normal";
	    
	    if (state.equals("failed") || state.equals("fatal")) {
			klass = "noVNC_status_error";
	    } else if (state.equals("normal") || state.equals("loaded")){
			klass = "noVNC_status_normal";
	    } else if (state.equals("disconnected")){
            WebUtil.D("noVNC_logo").getStyle().setDisplay(Display.BLOCK);
	    } else if (state.equals("password")){
//            UI.toggleConnectPanel();

//            WebUtil.D("noVNC_connect_button").value = "Send Password";
//            WebUtil.D("noVNC_connect_button").onclick = UI.setPassword;
//            WebUtil.D("noVNC_password").focus();

            klass = "noVNC_status_warn";
	    } else {
			klass = "noVNC_status_warn";
	    }

	    if (msg != null) {
	        s.setAttribute("class", klass);
	        sb.setAttribute("class", klass);
	        s.setInnerHTML(msg);
	    }

//	    UI.updateVisualState();
	}

//	// Disable/enable controls depending on connection state
//	updateVisualState: function() {
//	    var connected = UI.rfb_state === "normal" ? true : false;
//
//	    //Util.Debug(">> updateVisualState");
//	    WebUtil.D("noVNC_encrypt").disabled = connected;
//	    WebUtil.D("noVNC_true_color").disabled = connected;
//	    if (UI.rfb && UI.rfb.get_display() &&
//	        UI.rfb.get_display().get_cursor_uri()) {
//	        WebUtil.D("noVNC_cursor").disabled = connected;
//	    } else {
//	        UI.updateSetting("cursor", false);
//	        WebUtil.D("noVNC_cursor").disabled = true;
//	    }
//	    WebUtil.D("noVNC_shared").disabled = connected;
//	    WebUtil.D("noVNC_connectTimeout").disabled = connected;
//	    WebUtil.D("noVNC_path").disabled = connected;
//
//	    if (connected) {
//	        UI.setViewClip();
//	        UI.setMouseButton(1);
//	        WebUtil.D("showKeyboard").style.display = "inline";
//	        WebUtil.D("sendCtrlAltDelButton").style.display = "inline";
//	    } else {
//	        UI.setMouseButton();
//	        WebUtil.D("showKeyboard").style.display = "none";
//	        WebUtil.D("sendCtrlAltDelButton").style.display = "none";
//	    }
//	    // State change disables viewport dragging.
//	    // It is enabled (toggled) by direct click on the button
//	    UI.setViewDrag(false);
//
//	    switch (UI.rfb_state) {
//	        case "fatal":
//	        case "failed":
//	        case "loaded":
//	        case "disconnected":
//	            WebUtil.D("connectButton").style.display = "";
//	            WebUtil.D("disconnectButton").style.display = "none";
//	            break;
//	        default:
//	            WebUtil.D("connectButton").style.display = "none";
//	            WebUtil.D("disconnectButton").style.display = "";
//	            break;
//	    }
//
//	    //Util.Debug("<< updateVisualState");
//	},
//
//
//	clipReceive: function(rfb, text) {
//	    Util.Debug(">> UI.clipReceive: " + text.substr(0,40) + "...");
//	    WebUtil.D("noVNC_clipboard_text").value = text;
//	    Util.Debug("<< UI.clipReceive");
//	},
//
//
	public void connect() {
//	    var host, port, password, path;
//
//	    UI.closeSettingsMenu();
//	    UI.toggleConnectPanel();
//
	    String host = ((InputElement)WebUtil.D("noVNC_host")).getValue();
	    String port = ((InputElement)WebUtil.D("noVNC_port")).getValue();
	    String password = ((InputElement)WebUtil.D("noVNC_password")).getValue();
//	    path = WebUtil.D("noVNC_path").value;
	    String path = "";
	    
//	    if ((!host) || (!port)) {
//	        throw("Must set host and port");
//	    }
//
//	    UI.rfb.set_encrypt(UI.getSetting("encrypt"));
//	    UI.rfb.set_true_color(UI.getSetting("true_color"));
//	    UI.rfb.set_local_cursor(UI.getSetting("cursor"));
//	    UI.rfb.set_shared(UI.getSetting("shared"));
//	    UI.rfb.set_connectTimeout(UI.getSetting("connectTimeout"));
//
	    rfb.connect(host, port, password, path);
//	    //Close dialog.
//	    setTimeout(UI.setBarPosition, 100);
	    WebUtil.D("noVNC_logo").getStyle().setDisplay(Display.NONE);
	};
//
//	disconnect: function() {
//	    UI.closeSettingsMenu();
//	    UI.rfb.disconnect();
//
//	    WebUtil.D("noVNC_logo").style.display = "block";
//	    UI.connSettingsOpen = false;
//	    UI.toggleConnectPanel();
//	},
//
//	displayBlur: function() {
//	    UI.rfb.get_keyboard().set_focused(false);
//	    UI.rfb.get_mouse().set_focused(false);
//	},
//
//	displayFocus: function() {
//	    UI.rfb.get_keyboard().set_focused(true);
//	    UI.rfb.get_mouse().set_focused(true);
//	},
//
//	clipClear: function() {
//	    WebUtil.D("noVNC_clipboard_text").value = "";
//	    UI.rfb.clipboardPasteFrom("");
//	},
//
//	clipSend: function() {
//	    var text = WebUtil.D("noVNC_clipboard_text").value;
//	    Util.Debug(">> UI.clipSend: " + text.substr(0,40) + "...");
//	    UI.rfb.clipboardPasteFrom(text);
//	    Util.Debug("<< UI.clipSend");
//	},
//
//
//	// Enable/disable and configure viewport clipping
//	setViewClip: function(clip) {
//	    var display, cur_clip, pos, new_w, new_h;
//
//	    if (UI.rfb) {
//	        display = UI.rfb.get_display();
//	    } else {
//	        return;
//	    }
//
//	    cur_clip = display.get_viewport();
//
//	    if (typeof(clip) !== "boolean") {
//	        // Use current setting
//	        clip = UI.getSetting("clip");
//	    }
//
//	    if (clip && !cur_clip) {
//	        // Turn clipping on
//	        UI.updateSetting("clip", true);
//	    } else if (!clip && cur_clip) {
//	        // Turn clipping off
//	        UI.updateSetting("clip", false);
//	        display.set_viewport(false);
//	        WebUtil.D("noVNC_canvas").style.position = "static";
//	        display.viewportChange();
//	    }
//	    if (UI.getSetting("clip")) {
//	        // If clipping, update clipping settings
//	        WebUtil.D("noVNC_canvas").style.position = "absolute";
//	        pos = Util.getPosition(WebUtil.D("noVNC_canvas"));
//	        new_w = window.innerWidth - pos.x;
//	        new_h = window.innerHeight - pos.y;
//	        display.set_viewport(true);
//	        display.viewportChange(0, 0, new_w, new_h);
//	    }
//	},
//
//	// Toggle/set/unset the viewport drag/move button
//	setViewDrag: function(drag) {
//	    var vmb = WebUtil.D("noVNC_view_drag_button");
//	    if (!UI.rfb) { return; }
//
//	    if (UI.rfb_state === "normal" &&
//	        UI.rfb.get_display().get_viewport()) {
//	        vmb.style.display = "inline";
//	    } else {
//	        vmb.style.display = "none";
//	    }
//
//	    if (typeof(drag) === "undefined") {
//	        // If not specified, then toggle
//	        drag = !UI.rfb.get_viewportDrag();
//	    }
//	    if (drag) {
//	        vmb.className = "noVNC_status_button_selected";
//	        UI.rfb.set_viewportDrag(true);
//	    } else {
//	        vmb.className = "noVNC_status_button";
//	        UI.rfb.set_viewportDrag(false);
//	    }
//	},
//
//	// On touch devices, show the OS keyboard
//	showKeyboard: function() {
//	    if(UI.keyboardVisible == false) {
//	        WebUtil.D("keyboardinput").focus();
//	        UI.keyboardVisible = true;
//	        WebUtil.D("showKeyboard").className = "noVNC_status_button_selected";
//	    } else if(UI.keyboardVisible == true) {
//	        WebUtil.D("keyboardinput").blur();
//	        WebUtil.D("showKeyboard").className = "noVNC_status_button";
//	        UI.keyboardVisible = false;
//	    }
//	},
//
//	keyInputBlur: function() {
//	    WebUtil.D("showKeyboard").className = "noVNC_status_button";
//	    //Weird bug in iOS if you change keyboardVisible
//	    //here it does not actually occur so next time
//	    //you click keyboard icon it doesnt work.
//	    setTimeout("UI.setKeyboard()",100)
//	},
//
//	setKeyboard: function() {
//	    UI.keyboardVisible = false;
//	},
//
//	// iOS < Version 5 does not support position fixed. Javascript workaround:
//	setOnscroll: function() {
//	    window.onscroll = function() {
//	        UI.setBarPosition();
//	    };
//	},
//
//	setResize: function () {
//	    window.onResize = function() {
//	        UI.setBarPosition();
//	    };
//	},
//
//	//Helper to add options to dropdown.
	public static void addOption(Element selectbox, String text, String value) {
		if (!(selectbox instanceof SelectElement)) {
			System.err.println("Unexpected type"); 
			return;
		}
	    OptionElement optn = Document.get().createOptionElement();
	    optn.setText(text);
	    optn.setValue(value);
	    ((SelectElement)selectbox).add(optn,  null);
	}

//	setBarPosition: function() {
//	    WebUtil.D("noVNC-control-bar").style.top = (window.pageYOffset) + "px";
//	    WebUtil.D("noVNC_mobile_buttons").style.left = (window.pageXOffset) + "px";
//
//	    var vncwidth = WebUtil.D("noVNC_screen").style.offsetWidth;
//	    WebUtil.D("noVNC-control-bar").style.width = vncwidth + "px";
//	}
//
//	};
//
//
//
//
}
