/*******************************************************************************
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Jeff Klann - initial API and implementation
*
* Author:
*     Jeff Klann, PhD
*
* This contributes to a reimaining of Dates in MDMI. Also see DateWrapper,
* DateTimeConverter, ToStringConverter, and ToDateTimeConverter. Currently allows values that are
* shorter than the specified format and uses either HL7 or ISO as default,
* depending on the input string. Does not allow values that are *longer*
* than the input format, optional timezones with shorter strings, or retained
* precision of output. Also does not validate that the parsed string makes sense -
* this seems to only affect the year, which can be very strange if a bad value is passed
* for certain date formats.
*
*
* @author jklann
*
*******************************************************************************/
package org.openhealthtools.mdht.mdmi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhealthtools.mdht.mdmi.engine.converter.DateWrapper;


public class DateUtil {
	// Date formats
	public static final String fmtISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String fmtHL7 = "yyyyMMddHHmmss.SSSZ"; //"YYYYMMDDHHMMSS.UUUU[+|-ZZzz]";

	private static final Pattern bracketMatcher = Pattern.compile("\\[([^\\]]+)");

	private static HashMap<String,SimpleDateFormat> dateCache = new HashMap<String,SimpleDateFormat>();

	/***
	 * Global DateFormat cache. Note this is not thread safe, but
	 * MDMI is not in general currently anyway.
	 */
	public static SimpleDateFormat getDateFormatCached(String format) {
		if (dateCache.containsKey(format)) return dateCache.get(format);
		SimpleDateFormat sdf = null;
		try {
			sdf = new SimpleDateFormat(format);
		} catch(IllegalArgumentException e) { return null; }
		dateCache.put(format, sdf);
		return sdf;
	}

	public static String formatDate(String format, Date value) {
		if (value==null) return "";
		String myFormat = format;
		if (format==null || format.equals("") || format.equals("DATE"))
			myFormat = fmtISO;
		SimpleDateFormat sdf = getDateFormatCached(myFormat);
		String dateString = sdf.format(value);

		// I've preserved a weird colon insertion in the timezone from XmlUtil.
		if (format==null || format.equals("") || format.equals("DATE"))
			return dateString.substring(0, 26) + ":" + dateString.substring(26);
		else return dateString;
	}

	/***
	 * Simple method to pick a date format.
	 * If format is empty or null, chooses either standard ISO
	 * or HL7 ISO.
	 * Then truncates the format string to be the length of the value string
	 * sans quotes. This works because each character in the format string
	 * represents at least one character in the value string. The other way around
	 * is not possible without complex logic.
	 * TODO: Truncation could create an invalid substring. I don't think
	 * that it matters if used in conjunction with parseDateImplicitOptional,
	 * but should be tested further.
	 */
	public static String pickDateFormat(String format, String value) {
		if (value==null) return "";
		if (format==null || format.equals("")) {
			if (value.contains("-")) format=fmtISO;
			else format=fmtHL7;
		}
		int fmtLen = format.replace("\'", "").length();
		if (fmtLen>value.length()) {
			return format.substring(0, value.length());
		}
		else return format;
	}
	/***
	 * Compatibility method to handle the old semicolon split-string issue.
	 * Finds the longest string among the semicolon separated strings and returns that.
	 *
	 * @param format
	 * @return
	 */
	public static String getLongestWithoutSemiColons(String format) {
        String[] formatStrings = format.split(";");
        String outFormat = "";
        for (String formatString : formatStrings) {
        	if (formatString.length()>outFormat.length()) outFormat=formatString;
        }
		return outFormat;
	}

	/**
	 * Parse a Java SimpleDateFormat with optional sections, i.e. yyyy[MM][dd].
	 * Not presently used.
	 *
	 * @param format
	 * @param value
	 * @return
	 */
	public static DateWrapper parseDateWithOptional(String format, String value) {
		ArrayList<String> fmts = new ArrayList<String>();
		StringBuilder fmtSB = new StringBuilder();
		int firstBracket = format.indexOf('[');
		fmtSB.append(format.substring(0,firstBracket==-1?format.length():firstBracket));
		fmts.add(fmtSB.toString());
		Matcher matcher = bracketMatcher.matcher(format);

	    int pos = -1;
	    while (matcher.find(pos+1)){
	        pos = matcher.start();
	        fmtSB.append(matcher.group(1));
	        fmts.add(fmtSB.toString());
	    }

	    Date output = null;
	    String originalFormat = null;
	    for (int i=fmts.size()-1;i>=0;i--) {
	    	SimpleDateFormat sdf = new SimpleDateFormat(fmts.get(i));
	    	try {
	    		output = sdf.parse(value);
	    		originalFormat = fmts.get(i);
	    		break;
	    	}
	    	catch(ParseException e) {
	    		// Ignore, just keep going
	    	}
	    }
	    return new DateWrapper(output, originalFormat, null);
	}

	/**
	 * Parse a date string with the given format (of SimpleDateFormat format).
	 * All components are optional - the longest format string that can
	 * parse the given value string is used. Separators, literals, and all format elements
	 * are supported.
	 * TODO: Preserve optional timezones in shorter string
	 * TODO: Validate that date is logical?
	 *
	 * @param format
	 * @param value
	 * @return
	 */
	public static DateWrapper parseDateImplicitOptional(String format, String value) {
		format = pickDateFormat(format,value);
		Date output = null;
		//System.out.println("Date format is"+format);

		// Optimization: in the average case, the format chosen by pickDateFormat will work
    	SimpleDateFormat sdf = getDateFormatCached(format);
    	sdf.setLenient(false);
    	// Otherwise crazy dates crop up if the string is longer than the format
    	try {
    		output = sdf.parse(value);
    		// It worked!
    		return new DateWrapper(output, format,value);
    	}
    	catch(ParseException e) {
    		// Ignore, just keep going
    	}

    	// All the complex logic to try subparts of the dateformat, in chunks.
		int vLen = value.length();
		int outLen = 0;
		ArrayList<String> dateParts = new ArrayList<String>();
		StringBuilder datePart = new StringBuilder();
		char charAt = '~';
		boolean inQuote = false,buffering=true;
		for (int i=0;i<format.length();i++) {
			// Optimization: stop if the value string is shorter than the current format strings (sans quotes)
			// Note that you still have to try multiple formats because some characters in the
			// format string represent multiple characters in the value string.
			if (vLen<outLen) break;
			char currChar = format.charAt(i);
			// If we've encountered a new character, we need to do something...
			if(currChar!=charAt) {
				// If we've just finished buffering, output dateformat to the list
				if (datePart.length()>0 && buffering) {
					dateParts.add(datePart.toString());
					buffering=false;
				}
				// Buffer
				datePart.append(currChar);
				outLen++;
				boolean nonAlpha = (""+currChar).matches("^.*[^a-zA-Z0-9 ].*$");
				// If not alphanumeric or inside a single-quoted string, buffer but don't set buffering flag...
				if (nonAlpha || inQuote) {
					if (currChar=='\'') {
						outLen--;
						if(inQuote==false) inQuote=true;
						else if(inQuote==true) inQuote=false;
					}
					currChar='~';
					continue;
				}
				charAt=currChar;
				buffering=true;
			} else {
				// Otherwise just keep buffering...
				datePart.append(currChar);
				outLen++;
			}
		}
		if (vLen>=outLen) dateParts.add(datePart.toString());
		//System.out.println(dateParts);

		// Loop through our list of formats, from longest to shortest...
	    output = null;
	    String originalFormat = null;
	    for (int i=dateParts.size()-1;i>=0;i--) {
	    	sdf = getDateFormatCached(dateParts.get(i));
	    	sdf.setLenient(false);
	    	 // Otherwise crazy dates crop up if the string is longer than the format
	    	try {
	    		output = sdf.parse(value);
	    		originalFormat = dateParts.get(i);
	    		break;
	    	}
	    	catch(ParseException e) {
	    		// Ignore, just keep going
	    	}
	    }

	    //System.out.println("Slower date parsing:"+output+","+originalFormat);

	    return new DateWrapper(output, originalFormat,value);
	}
}
