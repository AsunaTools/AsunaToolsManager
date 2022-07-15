package tools.asuna.manager;

import okhttp3.*;
import org.json.JSONArray;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static tools.asuna.manager.Account.getCredentials;

public class Requests {

    public static boolean requestsMenu() {

        Scanner input = new Scanner(System.in);
        System.out.println("=========" + colorize("Requests Menu", CYAN_TEXT(), BOLD()) + "========");
        System.out.println("1 - List requests");
        System.out.println("2 - Create request");
        System.out.println("3 - Delete request");
        System.out.println("==============================");
        System.out.println("4 - Back");
        System.out.print("Option: ");

        String selection = input.nextLine();
        System.out.println();

        if (!selection.matches("[0-9]+")) { return true; }
        if (selection.length() > 2) {
            System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Too long");
            return true;
        }

        switch (Integer.parseInt(selection)) {
            case 1:
                listRequests();
                return true;
            case 2:
                input = new Scanner(System.in);
                System.out.print("Value: ");
                String value = input.nextLine();
                if (Objects.equals(value, "")) {
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Value cannot be null");
                    return true;
                }
                System.out.println("Valid types: anime, pre_anime, download");
                System.out.print("Type: ");
                String type = input.nextLine();
                if (Objects.equals(type, "")) {
                    System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " Type cannot be null");
                    return true;
                }
                try {
                    postRequest(value, type);
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 3:
                try {
                    deleteRequest();
                } catch (Exception e) {
                   System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            default:
                return false;
        }
    }

    private static void listRequests() {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(Main.endpoint + "/requests").method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
            Response response = client.newCall(request).execute();
            JSONArray responseJSON = new JSONArray(Objects.requireNonNull(response.body()).string());
            for (int i = 0; i < responseJSON.length(); i++) {
                String status;
                switch (responseJSON.getJSONObject(i).getInt("status")) {
                    case 0 -> status = colorize("Pending", YELLOW_TEXT());
                    case 1 -> status = colorize("Approved", GREEN_TEXT());
                    case 2 -> status = colorize("Rejected", RED_TEXT());
                    case 3 -> status = colorize("Delayed", BRIGHT_BLACK_TEXT());
                    default -> status = "Not Parsed!";
                }
                System.out.println("==============================");
                System.out.println(colorize(responseJSON.getJSONObject(i).getString("value"), BRIGHT_RED_TEXT(), BOLD()));
                System.out.println("Type: " + colorize(responseJSON.getJSONObject(i).getString("type"), CYAN_TEXT()));
                System.out.println("By: " + responseJSON.getJSONObject(i).getString("applicant"));
                System.out.println("Status: " + status);
            }
            System.out.println("==============================");
            System.out.println();
        } catch (IOException e) {
            System.out.println("Request failed! " + e);
        }
    }

    private static void postRequest(String value, String type) throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        if (!Objects.equals(type, "anime") && !Objects.equals(type, "pre_anime") && !Objects.equals(type, "download")) {
            throw new Exception("Invalid type!");
        }

        Request postRequest = new Request.Builder().url(Main.endpoint + "/requests").method("POST", RequestBody.create("{\"value\":\"" + value + "\",\"type\":\"" + type + "\"}", MediaType.parse("application/json"))).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(postRequest).execute();

        if (response.code() != 200) {
            throw new Exception("Request failed!");
        }

        System.out.println();
        System.out.println(colorize("[OK]", BRIGHT_GREEN_TEXT(), BOLD()) + " Request posted successfully");
    }

    private static void deleteRequest() throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Scanner input = new Scanner(System.in);
        String[] requests = new String[0];
        int requestNum = 0;

        Request getRequestList = new Request.Builder().url(Main.endpoint + "/requests").method("GET", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
        Response response = client.newCall(getRequestList).execute();
        JSONArray responseJSON = new JSONArray(Objects.requireNonNull(response.body()).string());

        for (int i = 0; i < responseJSON.length(); i++) {
            if (Objects.equals(responseJSON.getJSONObject(i).getString("applicant"), getCredentials()[1])) {
                requests = Arrays.copyOf(requests, requests.length + 1);
                requests[requestNum] = responseJSON.getJSONObject(i).getString("id");
                requestNum++;
                System.out.println(requestNum + " - " + colorize(responseJSON.getJSONObject(i).getString("value"), BRIGHT_RED_TEXT(), BOLD()));
            }
        }

        if (requestNum != 0) {
            System.out.print("Chose request to delete: ");
            String request = input.nextLine();

            if (Objects.equals(request, "")) {
                System.out.println();
                throw new Exception("You have to choose a Request!");
            }

            if (!request.matches("[0-9]+")) {
                System.out.println();
                throw new Exception("Invalid request!");
            }

            if (Integer.parseInt(request) > requestNum) {
                throw new Exception("Invalid request!");
            }
            Request deleteRequest = new Request.Builder().url(Main.endpoint + "/requests/" + requests[Integer.parseInt(request) - 1]).method("DELETE", null).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).build();
            response = client.newCall(deleteRequest).execute();

            if (response.code() != 200) {
                throw new Exception("Cannot delete request!");
            }

            System.out.println(colorize("[OK]", BRIGHT_GREEN_TEXT(), BOLD()) + " Request deleted successfully");
        } else {
            System.out.println();
            System.out.println(colorize("[INFO]", BRIGHT_YELLOW_TEXT(), BOLD()) + " You don't have any requests");
        }
    }
}
