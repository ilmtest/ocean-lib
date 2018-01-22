package com.ocean.lib.text;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;

public class TextUtils
{
	/**
	 * https://stackoverflow.com/questions/18580287/how-could-i-remove-arabic-punctuation-form-a-string-in-java
	 * @param input
	 * @return
	 */
	public static String normalize(String input) {
		return Normalizer.normalize(input, Form.NFKD).replaceAll("\\p{M}", "");
	}
	
	
	public static String htmlToPlainText(Document d)
	{
		final OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
		final Whitelist noWhitelist = Whitelist.none();
		
		d.outputSettings(outputSettings); //makes html() preserve linebreaks and spacing
		d.select("br").append("\\n");
		d.select("p").prepend("\\n\\n");

		return Jsoup.clean(d.html().replaceAll("\\\\n", "\n"), "", noWhitelist, outputSettings);
	}


	/**
	 * 
	 * @param body The original text.
	 * @param start The beginning of the text to match
	 * @param end The end of the text to match.
	 * @return Gets the substrig in the body, between the start and the end. 
	 */
	public static String extractInside(String body, String start, String end)
	{
		int markerStart = body.indexOf(start)+start.length();
		int markerEnd = body.indexOf(end, markerStart);
		return body.substring(markerStart, markerEnd).trim();
	}

	public static String extractInsideBrackets(String input) {
		return input.replaceAll("[\\(\\)]+", "").trim();
	}


	public static String removeQuotes(String input) {
		return input.substring(1, input.length()-1);
	}
}