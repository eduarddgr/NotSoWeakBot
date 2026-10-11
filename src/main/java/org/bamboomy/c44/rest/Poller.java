
/**

$$$License$$$

**/

package org.bamboomy.c44.rest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.bamboomy.c44.Application;
import org.bamboomy.c44.bot.board.Board;
import org.bamboomy.c44.bot.player.Alliance;
import org.bamboomy.c44.bot.player.Color;
import org.bamboomy.c44.bot.player.Player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Poller implements Runnable {

	private String serverUrl;
	private String robotHash;
	private String ownColor;

	private Alliance alliance;

	private Player player;

	private boolean thinking = false;

	private static final boolean OUTPUT_POUNDER = true;

	private ArrayList<String> colorHashes;
	
	private String exceptionHash = null;

	public Poller(String serverUrl, ArrayList<String> colorHashes) {

		this.serverUrl = serverUrl;
		this.colorHashes = colorHashes;
	}

	@Override
	public void run() {

		boolean done = false;

		System.out.println(ownColor);

		for (String hash : colorHashes) {

			sendName(hash);
			
			exceptionHash = hash;
		}

		System.out.print("waiting...");

		Board board = null;

		int index = 0;

		while (!done) {

			System.out.print(".");

			try {

				Thread.sleep(3_000);

			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			StringBuffer robotBoard = getPath(serverUrl, "/getRobotBoard/" + colorHashes.get(index));

			int augment = 0;

			while (robotBoard == null && augment < colorHashes.size()) {

				System.out.println("gotten null response, trying with other color...");

				index = (index + 1) % colorHashes.size();

				augment++;

				robotBoard = getPath(serverUrl, "/getRobotBoard/" + colorHashes.get(index));
			}

			if (robotBoard != null && !robotBoard.toString().isEmpty()) {

				System.out.println("I'm supposed to play now...");

				ownColor = getPath(serverUrl, "/getColor/" + colorHashes.get(index)).toString();

				String allianceColor = getPath(serverUrl, "/getAllianceColor/" + colorHashes.get(index)).toString();

				alliance = new Alliance(Color.getByName(ownColor), Color.getByName(allianceColor));

				System.out.println("(with color: " + ownColor + "; in alliance: " + allianceColor + ")...");

				String numberOfMove = getPath(serverUrl, "/numberOfMove/" + colorHashes.get(index)).toString();

				board = new Board(robotBoard.toString(), alliance, ownColor, this, numberOfMove);

				player = board.getOwnPlayer();

				player.startCalculation();

				thinking = true;
			}

			long start = System.currentTimeMillis();

			int pointz = 0, meta = 0;

			long finish;

			robotHash = colorHashes.get(index);

			while (thinking) {

				if (OUTPUT_POUNDER) {

					System.out.print("Poundering[" + meta + "]...");

					for (int i = 0; i < pointz; i++) {

						System.out.print(".");
					}

					finish = System.currentTimeMillis();

					System.out.println("(" + (finish - start) / 1_000 + "s)");
				}

				try {

					Thread.sleep(7_000);

				} catch (InterruptedException e) {

					e.printStackTrace();
				}

				pointz++;

				if (pointz % 10 == 0) {

					meta++;
					pointz = 0;
				}
			}

			finish = System.currentTimeMillis();

			System.out.println("I poundered for: " + (finish - start) / 1_000 + " secondz...");

			index = (index + 1) % colorHashes.size();

			System.out.println("waiting to play again... -> index: " + index);
		}

		System.out.println("I'm done...");
	}

	public void sendName(String hash) {

		post(Application.BOT_NAME, "text/html; charset=utf-8", "getName", hash);
	}

	private StringBuffer getPath(String serverUrl, String path) {

		StringBuffer result = null;

		URL url;

		try {

			url = new URL(serverUrl + "/react" + path);
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

	public void movePiece(String pieceMd5, String moveMd5) {

		getPath(serverUrl, "/playBot/" + robotHash + "/" + pieceMd5 + "/" + moveMd5);

		System.out.println("moving piece...");

		thinking = false;
	}

	private void post(String output, String contentType, String urlPart, String hash) {

		StringBuffer result = null;

		URL url;

		try {

			String givenHash = robotHash;

			if (hash != null) {

				givenHash = hash;
			}

			url = new URL(serverUrl + "/react/" + urlPart + "/" + givenHash);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", contentType);
			con.connect();

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
			out.write(output);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			result = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
			}

			in.close();

			con.disconnect();

			System.out.print(result.toString() + " -- ");

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void output(String output) {

		post(output, "text/html; charset=utf-8", "updateOutput", null);
	}

	public void sendBoard(Board board) {

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = "doesn't work :(";

		try {

			json = ow.writeValueAsString(board.getGuiArray(ownColor, true));

		} catch (JsonProcessingException e) {

			e.printStackTrace();
		}

		post(json, "application/json", "postBoard", null);
	}

	public void exception(String stack) {

		post(stack, "text/html; charset=utf-8", "exception", exceptionHash);
	}
}
