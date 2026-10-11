package org.bamboomy.c44.rest;

/**

$$$License$$$

**/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ColorNegotiator implements Runnable {

	private final String gameHash, serverUrl, identifierHash;

	private ArrayList<String> colorHashes = new ArrayList<String>();

	public ColorNegotiator(String serverUrl, String gameHash, String identifierHash) {

		this.gameHash = gameHash;
		this.serverUrl = serverUrl;
		this.identifierHash = identifierHash;
	}

	@Override
	public void run() {

		String colors = requestColors();

		System.out.println(colors);

		String[] colorsStrings = colors.split(",");

		for (String color : colorsStrings) {

			colorHashes.add(requestHash(color));
		}

		(new Thread(new Poller(serverUrl, colorHashes))).start();
	}

	private String requestColors() {

		return getPath(serverUrl, "/askColors/" + gameHash + "/" + identifierHash).toString();
	}

	private String requestHash(String color) {

		return getPath(serverUrl, "/askHash/" + gameHash + "/" + color + "/" + identifierHash).toString();
	}

	private StringBuffer getPath(String serverUrl, String path) {

		StringBuffer result = null;

		URL url;

		try {

			url = new URL(serverUrl + "/color" + path);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			result = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}
			in.close();

			con.disconnect();

			System.out.print(result.toString());

		} catch (ConnectException e) {

			System.out.println("could not connect :(");

			return null;

		} catch (IOException e) {

			e.printStackTrace();
		}

		return result;
	}

}
