package noVNC.core;


public class Keyboard {
//	function Keyboard(defaults) {
//		"use strict";
//
//		var that           = {},  // Public API methods
//		    conf           = {},  // Configuration attributes
//
//		    keyDownList    = [];         // List of depressed keys 
//		                                 // (even if they are happy)
//
//		// Configuration attributes
//		Util.conf_defaults(conf, that, defaults, [
//		    ['target',      'wo', 'dom',  document, 'DOM element that captures keyboard input'],
//		    ['focused',     'rw', 'bool', true, 'Capture and send key events'],
//
//		    ['onKeyPress',  'rw', 'func', null, 'Handler for key press/release']
//		    ]);
//
//
//		// 
//		// Private functions
//		//

	public interface KeyboardHandler {
		public void onKeyPress(int keysym, byte down);
	}
	
	private KeyboardHandler keyboardHandler = null;
	public void hook(KeyboardHandler _kh) {
		keyboardHandler = _kh;
	}
//		// From the event keyCode return the keysym value for keys that need
//		// to be suppressed otherwise they may trigger unintended browser
//		// actions
//		function getKeysymSpecial(evt) {
//		    var keysym = null;
//
//		    switch ( evt.keyCode ) {
//		        // These generate a keyDown and keyPress in Firefox and Opera
//		        case 8         : keysym = 0xFF08; break; // BACKSPACE
//		        case 13        : keysym = 0xFF0D; break; // ENTER
//
//		        // This generates a keyDown and keyPress in Opera
//		        case 9         : keysym = 0xFF09; break; // TAB
//		        default        :                  break;
//		    }
//
//		    if (evt.type === 'keydown') {
//		        switch ( evt.keyCode ) {
//		            case 27        : keysym = 0xFF1B; break; // ESCAPE
//		            case 46        : keysym = 0xFFFF; break; // DELETE
//
//		            case 36        : keysym = 0xFF50; break; // HOME
//		            case 35        : keysym = 0xFF57; break; // END
//		            case 33        : keysym = 0xFF55; break; // PAGE_UP
//		            case 34        : keysym = 0xFF56; break; // PAGE_DOWN
//		            case 45        : keysym = 0xFF63; break; // INSERT
//		                                                     // '-' during keyPress
//		            case 37        : keysym = 0xFF51; break; // LEFT
//		            case 38        : keysym = 0xFF52; break; // UP
//		            case 39        : keysym = 0xFF53; break; // RIGHT
//		            case 40        : keysym = 0xFF54; break; // DOWN
//		            case 16        : keysym = 0xFFE1; break; // SHIFT
//		            case 17        : keysym = 0xFFE3; break; // CONTROL
//		            //case 18        : keysym = 0xFFE7; break; // Left Meta (Mac Option)
//		            case 18        : keysym = 0xFFE9; break; // Left ALT (Mac Command)
//
//		            case 112       : keysym = 0xFFBE; break; // F1
//		            case 113       : keysym = 0xFFBF; break; // F2
//		            case 114       : keysym = 0xFFC0; break; // F3
//		            case 115       : keysym = 0xFFC1; break; // F4
//		            case 116       : keysym = 0xFFC2; break; // F5
//		            case 117       : keysym = 0xFFC3; break; // F6
//		            case 118       : keysym = 0xFFC4; break; // F7
//		            case 119       : keysym = 0xFFC5; break; // F8
//		            case 120       : keysym = 0xFFC6; break; // F9
//		            case 121       : keysym = 0xFFC7; break; // F10
//		            case 122       : keysym = 0xFFC8; break; // F11
//		            case 123       : keysym = 0xFFC9; break; // F12
//
//		            default        :                  break;
//		        }
//		    }
//
//		    if ((!keysym) && (evt.ctrlKey || evt.altKey)) {
//		        if ((typeof(evt.which) !== "undefined") && (evt.which > 0)) {
//		            keysym = evt.which;
//		        } else {
//		            // IE9 always
//		            // Firefox and Opera when ctrl/alt + special
//		            Util.Warn("which not set, using keyCode");
//		            keysym = evt.keyCode;
//		        }
//
//		        /* Remap symbols */
//		        switch (keysym) {
//		            case 186       : keysym = 59; break; // ;  (IE)
//		            case 187       : keysym = 61; break; // =  (IE)
//		            case 188       : keysym = 44; break; // ,  (Mozilla, IE)
//		            case 109       :                     // -  (Mozilla, Opera)
//		                if (Util.Engine.gecko || Util.Engine.presto) {
//		                            keysym = 45; }
//		                                        break;
//		            case 189       : keysym = 45; break; // -  (IE)
//		            case 190       : keysym = 46; break; // .  (Mozilla, IE)
//		            case 191       : keysym = 47; break; // /  (Mozilla, IE)
//		            case 192       : keysym = 96; break; // `  (Mozilla, IE)
//		            case 219       : keysym = 91; break; // [  (Mozilla, IE)
//		            case 220       : keysym = 92; break; // \  (Mozilla, IE)
//		            case 221       : keysym = 93; break; // ]  (Mozilla, IE)
//		            case 222       : keysym = 39; break; // '  (Mozilla, IE)
//		        }
//		        
//		        /* Remap shifted and unshifted keys */
//		        if (!!evt.shiftKey) {
//		            switch (keysym) {
//		                case 48        : keysym = 41 ; break; // )  (shifted 0)
//		                case 49        : keysym = 33 ; break; // !  (shifted 1)
//		                case 50        : keysym = 64 ; break; // @  (shifted 2)
//		                case 51        : keysym = 35 ; break; // #  (shifted 3)
//		                case 52        : keysym = 36 ; break; // $  (shifted 4)
//		                case 53        : keysym = 37 ; break; // %  (shifted 5)
//		                case 54        : keysym = 94 ; break; // ^  (shifted 6)
//		                case 55        : keysym = 38 ; break; // &  (shifted 7)
//		                case 56        : keysym = 42 ; break; // *  (shifted 8)
//		                case 57        : keysym = 40 ; break; // (  (shifted 9)
//
//		                case 59        : keysym = 58 ; break; // :  (shifted `)
//		                case 61        : keysym = 43 ; break; // +  (shifted ;)
//		                case 44        : keysym = 60 ; break; // <  (shifted ,)
//		                case 45        : keysym = 95 ; break; // _  (shifted -)
//		                case 46        : keysym = 62 ; break; // >  (shifted .)
//		                case 47        : keysym = 63 ; break; // ?  (shifted /)
//		                case 96        : keysym = 126; break; // ~  (shifted `)
//		                case 91        : keysym = 123; break; // {  (shifted [)
//		                case 92        : keysym = 124; break; // |  (shifted \)
//		                case 93        : keysym = 125; break; // }  (shifted ])
//		                case 39        : keysym = 34 ; break; // "  (shifted ')
//		            }
//		        } else if ((keysym >= 65) && (keysym <=90)) {
//		            /* Remap unshifted A-Z */
//		            keysym += 32;
//		        } else if (evt.keyLocation === 3) {
//		            // numpad keys
//		            switch (keysym) {
//		                case 96 : keysym = 48; break; // 0
//		                case 97 : keysym = 49; break; // 1
//		                case 98 : keysym = 50; break; // 2
//		                case 99 : keysym = 51; break; // 3
//		                case 100: keysym = 52; break; // 4
//		                case 101: keysym = 53; break; // 5
//		                case 102: keysym = 54; break; // 6
//		                case 103: keysym = 55; break; // 7
//		                case 104: keysym = 56; break; // 8
//		                case 105: keysym = 57; break; // 9
//		                case 109: keysym = 45; break; // -
//		                case 110: keysym = 46; break; // .
//		                case 111: keysym = 47; break; // /
//		            }
//		        }
//		    }
//
//		    return keysym;
//		}
//
//		/* Translate DOM keyPress event to keysym value */
//		function getKeysym(evt) {
//		    var keysym, msg;
//
//		    if (typeof(evt.which) !== "undefined") {
//		        // WebKit, Firefox, Opera
//		        keysym = evt.which;
//		    } else {
//		        // IE9
//		        Util.Warn("which not set, using keyCode");
//		        keysym = evt.keyCode;
//		    }
//
//		    if ((keysym > 255) && (keysym < 0xFF00)) {
//		        msg = "Mapping character code " + keysym;
//		        // Map Unicode outside Latin 1 to X11 keysyms
//		        keysym = unicodeTable[keysym];
//		        if (typeof(keysym) === 'undefined') {
//		           keysym = 0; 
//		        }
//		        Util.Debug(msg + " to " + keysym);
//		    }
//
//		    return keysym;
//		}
//
//		function show_keyDownList(kind) {
//		    var c;
//		    var msg = "keyDownList (" + kind + "):\n";
//		    for (c = 0; c < keyDownList.length; c++) {
//		        msg = msg + "    " + c + " - keyCode: " + keyDownList[c].keyCode +
//		              " - which: " + keyDownList[c].which + "\n";
//		    }
//		    Util.Debug(msg);
//		}
//
//		function copyKeyEvent(evt) {
//		    var members = ['type', 'keyCode', 'charCode', 'which',
//		                   'altKey', 'ctrlKey', 'shiftKey',
//		                   'keyLocation', 'keyIdentifier'], i, obj = {};
//		    for (i = 0; i < members.length; i++) {
//		        if (typeof(evt[members[i]]) !== "undefined") {
//		            obj[members[i]] = evt[members[i]];
//		        }
//		    }
//		    return obj;
//		}
//
//		function pushKeyEvent(fevt) {
//		    keyDownList.push(fevt);
//		}
//
//		function getKeyEvent(keyCode, pop) {
//		    var i, fevt = null;
//		    for (i = keyDownList.length-1; i >= 0; i--) {
//		        if (keyDownList[i].keyCode === keyCode) {
//		            if ((typeof(pop) !== "undefined") && (pop)) {
//		                fevt = keyDownList.splice(i, 1)[0];
//		            } else {
//		                fevt = keyDownList[i];
//		            }
//		            break;
//		        }
//		    }
//		    return fevt;
//		}
//
//		function ignoreKeyEvent(evt) {
//		    // Blarg. Some keys have a different keyCode on keyDown vs keyUp
//		    if (evt.keyCode === 229) {
//		        // French AZERTY keyboard dead key.
//		        // Lame thing is that the respective keyUp is 219 so we can't
//		        // properly ignore the keyUp event
//		        return true;
//		    }
//		    return false;
//		}
//
//
//		//
//		// Key Event Handling:
//		//
//		// There are several challenges when dealing with key events:
//		//   - The meaning and use of keyCode, charCode and which depends on
////		     both the browser and the event type (keyDown/Up vs keyPress).
//		//   - We cannot automatically determine the keyboard layout
//		//   - The keyDown and keyUp events have a keyCode value that has not
////		     been translated by modifier keys.
//		//   - The keyPress event has a translated (for layout and modifiers)
////		     character code but the attribute containing it differs. keyCode
////		     contains the translated value in WebKit (Chrome/Safari), Opera
////		     11 and IE9. charCode contains the value in WebKit and Firefox.
////		     The which attribute contains the value on WebKit, Firefox and
////		     Opera 11.
//		//   - The keyDown/Up keyCode value indicates (sort of) the physical
////		     key was pressed but only for standard US layout. On a US
////		     keyboard, the '-' and '_' characters are on the same key and
////		     generate a keyCode value of 189. But on an AZERTY keyboard even
////		     though they are different physical keys they both still
////		     generate a keyCode of 189!
//		//   - To prevent a key event from propagating to the browser and
////		     causing unwanted default actions (such as closing a tab,
////		     opening a menu, shifting focus, etc) we must suppress this
////		     event in both keyDown and keyPress because not all key strokes
////		     generate on a keyPress event. Also, in WebKit and IE9
////		     suppressing the keyDown prevents a keyPress but other browsers
////		     still generated a keyPress even if keyDown is suppressed.
//		//
//		// For safe key events, we wait until the keyPress event before
//		// reporting a key down event. For unsafe key events, we report a key
//		// down event when the keyDown event fires and we suppress any further
//		// actions (including keyPress).
//		//
//		// In order to report a key up event that matches what we reported
//		// for the key down event, we keep a list of keys that are currently
//		// down. When the keyDown event happens, we add the key event to the
//		// list. If it is a safe key event, then we update the which attribute
//		// in the most recent item on the list when we received a keyPress
//		// event (keyPress should immediately follow keyDown). When we
//		// received a keyUp event we search for the event on the list with
//		// a matching keyCode and we report the character code using the value
//		// in the 'which' attribute that was stored with that key.
//		//
//
//		function onKeyDown(e) {
//		    if (! conf.focused) {
//		        return true;
//		    }
//		    var fevt = null, evt = (e ? e : window.event),
//		        keysym = null, suppress = false;
//		    //Util.Debug("onKeyDown kC:" + evt.keyCode + " cC:" + evt.charCode + " w:" + evt.which);
//
//		    fevt = copyKeyEvent(evt);
//
//		    keysym = getKeysymSpecial(evt);
//		    // Save keysym decoding for use in keyUp
//		    fevt.keysym = keysym;
//		    if (keysym) {
//		        // If it is a key or key combination that might trigger
//		        // browser behaviors or it has no corresponding keyPress
//		        // event, then send it immediately
//		        if (conf.onKeyPress && !ignoreKeyEvent(evt)) {
//		            Util.Debug("onKeyPress down, keysym: " + keysym +
//		                   " (onKeyDown key: " + evt.keyCode +
//		                   ", which: " + evt.which + ")");
//		            conf.onKeyPress(keysym, 1, evt);
//		        }
//		        suppress = true;
//		    }
//
//		    if (! ignoreKeyEvent(evt)) {
//		        // Add it to the list of depressed keys
//		        pushKeyEvent(fevt);
//		        //show_keyDownList('down');
//		    }
//
//		    if (suppress) {
//		        // Suppress bubbling/default actions
//		        Util.stopEvent(e);
//		        return false;
//		    } else {
//		        // Allow the event to bubble and become a keyPress event which
//		        // will have the character code translated
//		        return true;
//		    }
//		}
//
//		function onKeyPress(e) {
//		    if (! conf.focused) {
//		        return true;
//		    }
//		    var evt = (e ? e : window.event),
//		        kdlen = keyDownList.length, keysym = null;
//		    //Util.Debug("onKeyPress kC:" + evt.keyCode + " cC:" + evt.charCode + " w:" + evt.which);
//		    
//		    if (((evt.which !== "undefined") && (evt.which === 0)) ||
//		        (getKeysymSpecial(evt))) {
//		        // Firefox and Opera generate a keyPress event even if keyDown
//		        // is suppressed. But the keys we want to suppress will have
//		        // either:
//		        //     - the which attribute set to 0
//		        //     - getKeysymSpecial() will identify it
//		        Util.Debug("Ignoring special key in keyPress");
//		        Util.stopEvent(e);
//		        return false;
//		    }
//
//		    keysym = getKeysym(evt);
//
//		    // Modify the the which attribute in the depressed keys list so
//		    // that the keyUp event will be able to have the character code
//		    // translation available.
//		    if (kdlen > 0) {
//		        keyDownList[kdlen-1].keysym = keysym;
//		    } else {
//		        Util.Warn("keyDownList empty when keyPress triggered");
//		    }
//
//		    //show_keyDownList('press');
//		    
//		    // Send the translated keysym
//		    if (conf.onKeyPress && (keysym > 0)) {
//		        Util.Debug("onKeyPress down, keysym: " + keysym +
//		                   " (onKeyPress key: " + evt.keyCode +
//		                   ", which: " + evt.which + ")");
//		        conf.onKeyPress(keysym, 1, evt);
//		    }
//
//		    // Stop keypress events just in case
//		    Util.stopEvent(e);
//		    return false;
//		}
//
//		function onKeyUp(e) {
//		    if (! conf.focused) {
//		        return true;
//		    }
//		    var fevt = null, evt = (e ? e : window.event), keysym;
//		    //Util.Debug("onKeyUp   kC:" + evt.keyCode + " cC:" + evt.charCode + " w:" + evt.which);
//
//		    fevt = getKeyEvent(evt.keyCode, true);
//		    
//		    if (fevt) {
//		        keysym = fevt.keysym;
//		    } else {
//		        Util.Warn("Key event (keyCode = " + evt.keyCode +
//		                ") not found on keyDownList");
//		        keysym = 0;
//		    }
//
//		    //show_keyDownList('up');
//
//		    if (conf.onKeyPress && (keysym > 0)) {
//		        //Util.Debug("keyPress up,   keysym: " + keysym +
//		        //        " (key: " + evt.keyCode + ", which: " + evt.which + ")");
//		        Util.Debug("onKeyPress up, keysym: " + keysym +
//		                   " (onKeyPress key: " + evt.keyCode +
//		                   ", which: " + evt.which + ")");
//		        conf.onKeyPress(keysym, 0, evt);
//		    }
//		    Util.stopEvent(e);
//		    return false;
//		}
//
//		//
//		// Public API interface functions
//		//
//
		public void grab() {
//		    //Util.Debug(">> Keyboard.grab");
//		    var c = conf.target;
//
//		    Util.addEvent(c, 'keydown', onKeyDown);
//		    Util.addEvent(c, 'keyup', onKeyUp);
//		    Util.addEvent(c, 'keypress', onKeyPress);
//
//		    //Util.Debug("<< Keyboard.grab");
		}

		public void ungrab() {
//		    //Util.Debug(">> Keyboard.ungrab");
//		    var c = conf.target;
//
//		    Util.removeEvent(c, 'keydown', onKeyDown);
//		    Util.removeEvent(c, 'keyup', onKeyUp);
//		    Util.removeEvent(c, 'keypress', onKeyPress);
//
//		    //Util.Debug(">> Keyboard.ungrab");
		}

//		return that;  // Return the public API interface
//
//		}  // End of Keyboard()

}
