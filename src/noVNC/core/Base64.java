package noVNC.core;

/*
* Modified from:
* http://lxr.mozilla.org/mozilla/source/extensions/xml-rpc/src/nsXmlRpcClient.js#956
* And then via noVNC/base64.js
*/

/* ***** BEGIN LICENSE BLOCK *****
* Version: MPL 1.1/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Mozilla Public License Version
* 1.1 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS" basis,
* WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
* for the specific language governing rights and limitations under the
* License.
*
* The Original Code is Mozilla XML-RPC Client component.
*
* The Initial Developer of the Original Code is
* Digital Creations 2, Inc.
* Portions created by the Initial Developer are Copyright (C) 2000
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
*   Martijn Pieters <mj@digicool.com> (original author)
*   Samuel Sieb <samuel@sieb.net>
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU General Public License Version 2 or later (the "GPL"), or
* the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the MPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the MPL, the GPL or the LGPL.
*
* ***** END LICENSE BLOCK ***** */


public class Base64 {

	/* Convert data (an array of integers) to a Base64 string. */
	private static final String toBase64Table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	private static final char base64Pad     = '=';
	
	// converts byte to unsigned value
	private static int u(byte b) {
		return b & 0xff;
	}

	public static String encode(byte[] data) {
	    String result = "";
	    char[] chrTable = toBase64Table.toCharArray();
	    int length = data.length;

	    // Convert every three bytes to 4 ascii characters.
	    for (int i = 0; i < (length - 2); i += 3) {
	        result += chrTable[u(data[i]) >> 2];
	        result += chrTable[((data[i] & 0x03) << 4) + (u(data[i+1]) >> 4)];
	        result += chrTable[((data[i+1] & 0x0f) << 2) + (u(data[i+2]) >> 6)];
	        result += chrTable[data[i+2] & 0x3f];
	    }

	    // Convert the remaining 1 or 2 bytes, pad out to 4 characters.
	    if (length%3 != 0) {
	        int i = length - (length%3);
	        result += chrTable[u(data[i]) >> 2];
	        if ((length%3) == 2) {
	            result += chrTable[((data[i] & 0x03) << 4) + (u(data[i+1]) >> 4)];
	            result += chrTable[(data[i+1] & 0x0f) << 2];
	            result += base64Pad;
	        } else {
	            result += chrTable[(data[i] & 0x03) << 4];
	            result += base64Pad + base64Pad;
	        }
	    }

	    return result;
	}

	/* Convert Base64 data to a string */
	private static final byte toBinaryTable[] = {
	    -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
	    -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1,
	    -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,62, -1,-1,-1,63,
	    52,53,54,55, 56,57,58,59, 60,61,-1,-1, -1, 0,-1,-1,
	    -1, 0, 1, 2,  3, 4, 5, 6,  7, 8, 9,10, 11,12,13,14,
	    15,16,17,18, 19,20,21,22, 23,24,25,-1, -1,-1,-1,-1,
	    -1,26,27,28, 29,30,31,32, 33,34,35,36, 37,38,39,40,
	    41,42,43,44, 45,46,47,48, 49,50,51,-1, -1,-1,-1,-1
	};

	public static byte[] decode(String data, int offset) {
		int leftbits = 0; // number of bits decoded, but yet to be appended
	    int leftdata = 0; // bits decoded, but yet to be appended
	    int data_length = data.indexOf('=') - offset;

	    if (data_length < 0) { data_length = data.length() - offset; }

	    /* Every four characters is 3 resulting numbers */
	    int result_length = (int) ((data_length >> 2) * 3 + Math.floor((data_length%4)/1.5));
	    
	    byte[] result = new byte[result_length];

	    // Convert one by one.
	    int idx;
	    int i;
	    for (idx = 0, i = offset; i < data.length(); i++) {
	        int c = toBinaryTable[data.charAt(i) & 0x7f];
	        boolean padding = (data.charAt(i) == base64Pad);
	        // Skip illegal characters and whitespace
	        if (c == -1) {
	            System.err.println("Illegal character '" + data.charAt(i) + "'");
	            continue;
	        }
	        
	        // Collect data into leftdata, update bitcount
	        leftdata = (leftdata << 6) | c;
	        leftbits += 6;

	        // If we have 8 or more bits, append 8 bits to the result
	        if (leftbits >= 8) {
	            leftbits -= 8;
	            // Append if not padding.
	            if (!padding) {
	                result[idx++] = (byte) ((leftdata >> leftbits) & 0xff);
	            }
	            leftdata &= (1 << leftbits) - 1;
	        }
	    }

	    // If there are any bits left, the base64 string was corrupted
	    if (leftbits > 0) {
	    	System.err.println("Base64-Error: Corrupted base64 string");
	    }

	    return result;
	}

}
