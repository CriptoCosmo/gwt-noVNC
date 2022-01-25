package noVNC.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Cookies;

public class WebUtil {

/*
* Simple DOM selector by ID
*/
	public static Element D(String id) {
		return Document.get().getElementById(id);
//        if (document.getElementById) {
//        return document.getElementById(id);
//    } else if (document.all) {
//        return document.all[id];
//    } else if (document.layers) {
//        return document.layers[id];
//    }
//    return undefined;
	}
	
//
//
//	/* 
//	 * ------------------------------------------------------
//	 * Namespaced in WebUtil
//	 * ------------------------------------------------------
//	 */
//
//	// init log level reading the logging HTTP param
//	WebUtil.init_logging = function() {
//	    Util._log_level = (document.location.href.match(
//	         /logging=([A-Za-z0-9\._\-]*)/) ||
//	         ['', Util._log_level])[1];
//	    
//	    Util.init_logging()
//	}
//	WebUtil.init_logging();
//
//
//	WebUtil.dirObj = function (obj, depth, parent) {
//	    var i, msg = "", val = "";
//	    if (! depth) { depth=2; }
//	    if (! parent) { parent= ""; }
//
//	    // Print the properties of the passed-in object 
//	    for (i in obj) {
//	        if ((depth > 1) && (typeof obj[i] === "object")) { 
//	            // Recurse attributes that are objects
//	            msg += WebUtil.dirObj(obj[i], depth-1, parent + "." + i);
//	        } else {
//	            //val = new String(obj[i]).replace("\n", " ");
//	            if (typeof(obj[i]) === "undefined") {
//	                val = "undefined";
//	            } else {
//	                val = obj[i].toString().replace("\n", " ");
//	            }
//	            if (val.length > 30) {
//	                val = val.substr(0,30) + "...";
//	            } 
//	            msg += parent + "." + i + ": " + val + "\n";
//	        }
//	    }
//	    return msg;
//	};
//
	public static String getQueryVar(String name) {
		return getQueryVar(name, null);
	}

	/**
	 * Read a query string variable
	 */
	public static native String getQueryVar(String name, String defVal) /*-{
	    var re = new RegExp('[?][^#]*' + name + '=([^&#]*)');
	    //if (typeof defVal === 'undefined') { defVal = null; }
	    return (document.location.href.match(re) || ['',defVal])[1];
    }-*/;


	/*
	 * Cookie handling. Dervied from: http://www.quirksmode.org/js/cookies.html
	 */

	// No days means only for this browser session
	public static void createCookie(String name, String value) {
		createCookie(name, value, -1);
	}
	public static void createCookie(String name, String value, int days) {
	    if (days > 0) {
	    	Date expires = new Date();
	        expires.setTime(expires.getTime()+(days*24*60*60*1000));
	        Cookies.setCookie(name, value, expires);
	    }
	    else {
	        Cookies.setCookie(name, value);
	    }
	};

	public static String readCookie(String name) {
		return readCookie(name, null);
	}
	public static native String readCookie(String name, String defaultValue) /*-{
	    var i, c, nameEQ = name + "=", ca = document.cookie.split(';');
	    for(i=0; i < ca.length; i += 1) {
	        c = ca[i];
	        while (c.charAt(0) === ' ') { c = c.substring(1,c.length); }
	        if (c.indexOf(nameEQ) === 0) { return c.substring(nameEQ.length,c.length); }
	    }
	    return defaultValue;
	    //return (typeof defaultValue !== 'undefined') ? defaultValue : null;
	}-*/;

	public static void eraseCookie(String name) {
	    WebUtil.createCookie(name,"",-1);
	};

	/**
	 * Alternate stylesheet selection
	 */
	public static List<LinkElement> getStylesheets() {
		List<LinkElement> sheets = new ArrayList<LinkElement>();
		NodeList<Element> links = Document.get().getElementsByTagName("link");
		for (int i = 0; i < links.getLength(); i++) {
	        if (links.getItem(i).getTitle() != null && ((LinkElement)links.getItem(i)).getRel().toUpperCase().indexOf("STYLESHEET") > -1)
	        	sheets.add((LinkElement)links.getItem(i));
		}
	    return sheets;
	}
	
	/**
	 * No sheet means try and use value from cookie, null sheet used to clear
	 * all alternates.
	 * @return 
	 */
	public static String selectStylesheet(String sheet) {
	    if (sheet == null) {
	        sheet = "default";
	    }
	    List<LinkElement> sheets = WebUtil.getStylesheets();
	    for (LinkElement link : sheets) {
	        if (link.getTitle().equals(sheet)) {    
		        //Util.Debug("Using stylesheet " + sheet);
	            link.setDisabled(false);
	        } else {
	            //Util.Debug("Skipping stylesheet " + link.title);
	            link.setDisabled(true);
	        }
		}
	    return sheet;
	};
}
