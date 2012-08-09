package org.openhealthtools.mdht.mdmi.editor.common;

import java.util.UUID;

public class UniqueID {

	/** get a UUID string */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
    
    /** Does this string have the characteristics of a UUID */
    public static boolean isUUID(String string) {
    	// 8-4-4-4-12 for a total of 36 characters (32 digits and four hyphens).
    	if (string == null || string.length() != 36) {
    		return false;
    	}
    	
    	int [] pattern = {8, 4, 4, 4, 12};
    	int hyphenIdx = -1;
    	
    	for (int i=0; i<pattern.length; i++) {
    		int nChars = pattern[i];
    		
    		if (i == pattern.length-1) {
    			hyphenIdx = string.length();
    		} else {
    			hyphenIdx = string.indexOf('-');
            	string = string.substring(nChars+1, string.length());
    		}
    		
        	if (hyphenIdx != nChars) {
        		return false;
        	}
        	
    	}
    	
    	
    	return true;
    }

}
