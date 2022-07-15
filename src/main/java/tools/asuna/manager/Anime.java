package tools.asuna.manager;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Scanner;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static tools.asuna.manager.Account.getCredentials;

public class Anime {
    public static boolean animeMenu() {
        String id;
        String animeID;

        Scanner input = new Scanner(System.in);
        System.out.println("==========" + colorize("Anime Menu", CYAN_TEXT(), BOLD()) + "==========");
        System.out.println("1 - Download Episode");
        System.out.println("2 - Download Season");
        System.out.println("3 - Download full Anime");
        System.out.println("4 - Download film");
        System.out.println("==============================");
        System.out.println("5 - Back");
        System.out.print("Option: ");

        String selection = input.nextLine();
        System.out.println();

        if (!selection.matches("[0-9]+")) { return true; }
        if (selection.length() > 2) {
            System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Too long");
            return true;
        }

        switch (Integer.parseInt(selection)){
            case 1:
                input = new Scanner(System.in);
                System.out.print("Episode id: ");
                id = input.nextLine();
                if (Objects.equals(id, "")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " ID cannot be null!");
                    return true;
                }
                if (!id.matches("[0-9]+")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Invalid ID!");
                    return true;
                }
                try {
                    StringBuilder idHex = new StringBuilder(new BigInteger(id).toString(16));
                    while (idHex.length() < 8) {
                        idHex.insert(0, "0");
                    }
                    System.out.println();
                    DownloadAnime.downloadVideo(new BigInteger(id), getEpisodeInfo(new BigInteger(id))[5] + " [" + idHex.toString().toUpperCase() + "].mp4", getEpisodeInfo(new BigInteger(id))[5]);
                    System.out.println(colorize("Done!", BRIGHT_GREEN_TEXT()));
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 2:
                input = new Scanner(System.in);
                System.out.print("Anime id: ");
                animeID = input.nextLine();
                if (Objects.equals(animeID, "")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " ID cannot be null!");
                    return true;
                }
                System.out.print("Season: ");
                String season = input.nextLine();
                if (Objects.equals(season, "")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Season cannot be null!");
                    return true;
                }
                if (!season.matches("[0-9]+")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Invalid Season!");
                    return true;
                }
                try {
                    DownloadAnime.downloadSeason(animeID, Integer.parseInt(season));
                    System.out.println(colorize("Done!", BRIGHT_GREEN_TEXT()));
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 3:
                input = new Scanner(System.in);
                System.out.print("Anime id: ");
                animeID = input.nextLine();
                if (Objects.equals(animeID, "")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " ID cannot be null!");
                    return true;
                }
                try {
                    DownloadAnime.downloadFullAnime(animeID);
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 4:
                input = new Scanner(System.in);
                System.out.print("Film id: ");
                id = input.nextLine();
                if (Objects.equals(id, "")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " ID cannot be null!");
                    return true;
                }
                if (!id.matches("[0-9]+")) {
                    System.out.println();
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Invalid ID!");
                    return true;
                }
                try {
                    StringBuilder idHex = new StringBuilder(new BigInteger(id).toString(16));
                    while (idHex.length() < 8) {
                        idHex.insert(0, "0");
                    }
                    System.out.println();
                    DownloadAnime.downloadVideo(new BigInteger(id), getFilmInfo(new BigInteger(id))[0] + " [" + idHex.toString().toUpperCase() + "].mp4", getFilmInfo(new BigInteger(id))[3]);
                    System.out.println();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            default:
                return false;
        }
    }

    public static String[] getEpisodeInfo(BigInteger id) throws Exception {
        String[] responseInfo = new String[6];

        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(Main.endpoint + "/anime/episode/" + id).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
            Response response = client.newCall(request).execute();
            if (response.code() == 404) {
                throw new Exception("The episode does not exist!");
            }
            if (response.code() != 200) {
                throw new Exception("Failed Fetching data: HTTP " + response.code());
            }
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            responseInfo[0] = responseJSON.getString("serie");
            responseInfo[1] = responseJSON.optString("season");
            responseInfo[2] = responseJSON.getString("episode");
            responseInfo[3] = responseJSON.getString("orden");
            responseInfo[4] = responseJSON.optString("custom");
            responseInfo[5] = responseJSON.getString("path");
            return responseInfo;

        } catch (IOException e) {
            throw new Exception("Request failed! " + e);
        }
    }

    public static String[] getFilmInfo(BigInteger id) throws Exception {
        String[] responseInfo = new String[4];

        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(Main.endpoint + "/anime/film/" + id).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
            Response response = client.newCall(request).execute();
            if (response.code() == 404) {
                throw new Exception("The film does not exist!");
            }
            if (response.code() != 200) {
                throw new Exception("Failed Fetching data: HTTP " + response.code());
            }
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            responseInfo[0] = responseJSON.getString("title");
            responseInfo[1] = responseJSON.getString("serie");
            responseInfo[2] = responseJSON.optString("orden");
            responseInfo[3] = responseJSON.getString("path");
            return responseInfo;

        } catch (IOException e) {
            throw new Exception("Request failed! " + e);
        }
    }
}
