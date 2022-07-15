package tools.asuna.manager;

import okhttp3.*;
import org.json.JSONObject;

import org.mindrot.jbcrypt.*;

import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

public class Account {
    private static String at;
    private static String bt;
    private static String passwordHash;

    protected static void login(String username, char[] password) throws Exception {
        if (at == null && bt == null) {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Request request = new Request.Builder().url(Main.endpoint + "/auth").method("POST", RequestBody.create("{\"username\":\"" + username + "\",\"password\":\"" + Base64.getEncoder().encodeToString(new String(password).getBytes()) + "\"}", MediaType.parse("application/json"))).addHeader("Content-Type", "application/json").build();
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                throw new Exception(colorize("Auth Failed!", BRIGHT_RED_TEXT()));
            }
            JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            Account.at = responseJSON.getString("at");
            Account.bt = responseJSON.getString("bt");
            passwordHash = BCrypt.hashpw(new String(password), BCrypt.gensalt(12));

            response.close();
        }
    }

    protected static Object[] getCredentials(){
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        try {
            Request request = new Request.Builder().url(Main.endpoint + "/account").method("GET", null).addHeader("Authorization", "Bearer " + Account.at + Account.bt).build();
            Response response = client.newCall(request).execute();

            if (response.code() != 200) {
                System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + " Failed fetching account info!");
                System.exit(1);
            }

            JSONObject accountInfoJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
            response.close();
            return new Object[]{accountInfoJSON.getString("id"), accountInfoJSON.getString("username"), accountInfoJSON.getString("email"), accountInfoJSON.getBoolean("admin"), Account.at, Account.bt};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean accountMenu() {
        Scanner input = new Scanner(System.in);
        System.out.println("=========" + colorize("Account Menu", CYAN_TEXT(), BOLD()) + "=========");
        System.out.println("1 - Change username (" + colorize(getCredentials()[1].toString(), BRIGHT_BLACK_TEXT(), BOLD()) + ")");
        System.out.println("2 - Change password (" + colorize("Not shown", BRIGHT_BLACK_TEXT(), BOLD()) + ")");
        System.out.println("3 - Change email (" + colorize(getCredentials()[2].toString(), BRIGHT_BLACK_TEXT(), BOLD()) + ")");
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
                try {
                    changeUsername();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 2:
                try {
                    changePassword();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            case 3:
                try {
                    changeEmail();
                } catch (Exception e) {
                    System.out.println(colorize("[ERROR] ", RED_TEXT(), BOLD()) + e.getMessage());
                }
                return true;
            default:
                return false;
        }
    }

    private static void changeUsername() throws Exception {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Scanner input = new Scanner(System.in);
        String newUsername;

        System.out.println("Current username: " + getCredentials()[1]);
        System.out.print("New username: ");
        newUsername = input.nextLine();
        if (Objects.equals(newUsername, getCredentials()[1])) {
            throw new Exception("Same username!");
        }
        if (newUsername.length() > 20) {
            throw new Exception("Username too long!");
        }

        Request postNewUsername = new Request.Builder().url(Main.endpoint + "/account/user/username").method("POST", RequestBody.create("{\"new_username\":\"" + newUsername +"\"}", MediaType.parse("application/json"))).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(postNewUsername).execute();

        if (response.code() != 200) {
            throw new Exception("Request failed!");
        }
        System.out.println(colorize("[OK]", BRIGHT_GREEN_TEXT(), BOLD()) + " Username changed successfully");
    }

    private static void changePassword() throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        char[] actualPassword;
        char[] newPassword;
        String newPasswordHash;

        System.out.print("Actual password: ");
        actualPassword = System.console().readPassword();

        if (!BCrypt.checkpw(new String(actualPassword), passwordHash)) {
            throw new Exception("Actual password not match!");
        }

        System.out.print("New password: ");
        newPassword = System.console().readPassword();
        newPasswordHash = BCrypt.hashpw(new String(newPassword), BCrypt.gensalt(12));
        System.out.print("Again: ");
        if (!BCrypt.checkpw(new String(System.console().readPassword()), newPasswordHash)) {
            throw new Exception("New password not match!");
        }
        Request postNewPassword = new Request.Builder().url(Main.endpoint + "/account/security/password").method("POST", RequestBody.create("{\"new_password\":\"" + Base64.getEncoder().encodeToString(new String(newPassword).getBytes()) + "\",\"new_password_hash\":\"" + newPasswordHash + "\"}", MediaType.parse("application/json"))).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(postNewPassword).execute();
        if (response.code() != 200) {
            throw new Exception("Request failed!");
        }
        System.out.println(colorize("[OK]", BRIGHT_GREEN_TEXT(), BOLD()) + " Password changed successfully");
    }

    private static void changeEmail() throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Scanner input = new Scanner(System.in);
        String newEmail;

        System.out.println("Current email: " + getCredentials()[2]);
        System.out.print("New email: ");
        newEmail = input.nextLine();
        if (!Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}").matcher(newEmail).matches()) {
            throw new Exception(newEmail + "is not a valid email!");
        }
        if (Objects.equals(newEmail, getCredentials()[2])) {
            throw new Exception("Same email!");
        }

        Request postNewEmail = new Request.Builder().url(Main.endpoint + "/account/user/email").method("POST", RequestBody.create("{\"new_email\":\"" + newEmail +"\"}", MediaType.parse("application/json"))).addHeader("Authorization", "Bearer " + getCredentials()[4] + getCredentials()[5]).addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(postNewEmail).execute();

        if (response.code() != 200) {
            throw new Exception("Request failed!");
        }
        System.out.println(colorize("[OK]", BRIGHT_GREEN_TEXT(), BOLD()) + " Email changed successfully");
    }
}
