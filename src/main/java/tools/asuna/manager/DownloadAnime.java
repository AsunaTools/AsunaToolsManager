package tools.asuna.manager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.math.BigInteger;
import java.util.Objects;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static tools.asuna.manager.Account.getCredentials;

public class DownloadAnime extends Download{

    public static void downloadVideo(BigInteger id, String path, String info) throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request getVideoUrlRequets = new Request.Builder().url(Main.endpoint + "/video/" + id).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
        Response response = client.newCall(getVideoUrlRequets).execute();

        JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
        response.close();
        downloadFile("https://moe.asuna.tools/" + responseJSON.getString("video_link"), path, info);
    }

    public static void downloadSeason(String animeID, int season) throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request seasonInfoRequest = new Request.Builder().url(Main.endpoint + "/anime/serie/" + animeID + "/season/" + season).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
        Response response = client.newCall(seasonInfoRequest).execute();

        if (response.code() == 404) {
            throw new Exception("The anime or season does not exist!");
        }

        if (response.code() != 200) {
            throw new Exception("Failed Fetching data: HTTP " + response.code());
        }

        JSONArray seasonInfoArray = new JSONArray(Objects.requireNonNull(response.body()).string());

        Request animeInfoRequest = new Request.Builder().url(Main.endpoint + "/anime/serie/" + animeID).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
        response = client.newCall(animeInfoRequest).execute();

        JSONObject animeInfoJSON = new JSONObject(Objects.requireNonNull(response.body()).string());

        response.close();

        File path = new File(animeInfoJSON.getString("title") + "/" + season);

        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new Exception("Failed creating folder!");
            }
        }

        for (int i = 0; i < seasonInfoArray.length(); i++) {
            BigInteger id = seasonInfoArray.getJSONObject(i).getBigInteger("id");
            StringBuilder idHex = new StringBuilder(id.toString(16));
            while (idHex.length() < 8) {
                idHex.insert(0, "0");
            }
            downloadVideo(id, animeInfoJSON.getString("title") + "/" + season + "/" + Anime.getEpisodeInfo(seasonInfoArray.getJSONObject(i).getBigInteger("id"))[5] + " [" + idHex.toString().toUpperCase() + "].mp4", Anime.getEpisodeInfo(seasonInfoArray.getJSONObject(i).getBigInteger("id"))[5]);
        }
    }

    public static void downloadFullAnime(String animeID) throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request animeInfoRequest = new Request.Builder().url(Main.endpoint + "/anime/serie/" + animeID).method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
        Response response = client.newCall(animeInfoRequest).execute();

        if (response.code() == 404) {
            throw new Exception("The anime does not exist!");
        }

        if (response.code() != 200) {
            throw new Exception("Failed Fetching data: HTTP " + response.code());
        }

        JSONObject animeInfoJSON = new JSONObject(Objects.requireNonNull(response.body()).string());

        response.close();

        if (animeInfoJSON.getInt("state") != 0 && !(boolean)getCredentials()[3]) {
            throw new Exception("Anime not available");
        }

        File path = new File(animeInfoJSON.getString("title"));

        if (!path.exists()) {
            if (!path.mkdirs()) {
                throw new Exception("Failed creating folder!");
            }
        }

        if (animeInfoJSON.getBoolean("seasons")) {
            JSONArray seasonsArray = new JSONArray(animeInfoJSON.getJSONArray("season_list"));

            for (int i = 0; i < seasonsArray.length(); i++) {
                downloadSeason(animeID, seasonsArray.getJSONObject(i).getInt("season"));
            }
        } else {
            JSONArray episodesArray = new JSONArray(animeInfoJSON.getJSONArray("episode_list"));

            for (int i = 0; i < episodesArray.length(); i++) {
                BigInteger id = episodesArray.getJSONObject(i).getBigInteger("id");
                StringBuilder idHex = new StringBuilder(id.toString(16));
                while (idHex.length() < 8) {
                    idHex.insert(0, "0");
                }
                downloadVideo(id, animeInfoJSON.getString("title") + "/" + Anime.getEpisodeInfo(episodesArray.getJSONObject(i).getBigInteger("id"))[5] + " [" + idHex.toString().toUpperCase() + "].mp4", Anime.getEpisodeInfo(episodesArray.getJSONObject(i).getBigInteger("id"))[5]);
            }
        }
        System.out.println(colorize("Done!", BRIGHT_GREEN_TEXT()));
    }
}
