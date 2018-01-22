package com.ocean.lib.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkBoundary
{
	public static String getHTML(String urlToRead, String encoding)
	{
		System.out.println(urlToRead);
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		StringBuffer result = new StringBuffer();

		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.2; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			rd = new BufferedReader( new InputStreamReader( conn.getInputStream(), encoding ) );

			while ( (line = rd.readLine() ) != null) {
				result.append(line);
			}

			rd.close();
		} catch (FileNotFoundException e) {
			System.out.println("Download complete...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	public static String getHTML(String urlToRead) {
		return getHTML(urlToRead, "UTF-8");
	}
}
