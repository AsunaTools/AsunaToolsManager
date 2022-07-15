package tools.asuna.manager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static tools.asuna.manager.Account.getCredentials;

import com.sun.jna.Function;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;


public class Main {

    public static String endpoint = "https://asuna.tools/api/v1";

    public static void main(String[] args) throws InterruptedException {

        System.out.println("============================================================");
        Thread.sleep(100);
        System.out.println("     ___                          ______            __");
        Thread.sleep(100);
        System.out.println("    /   |  _______  ______  ____ /_  __/___  ____  / /____");
        Thread.sleep(100);
        System.out.println("   / /| | / ___/ / / / __ \\/ __ `// / / __ \\/ __ \\/ / ___/");
        Thread.sleep(100);
        System.out.println("  / ___ |(__  ) /_/ / / / / /_/ // / / /_/ / /_/ / (__  ) ");
        Thread.sleep(100);
        System.out.println(" /_/  |_/____/\\__,_/_/ /_/\\__,_//_/  \\____/\\____/_/____/");
        Thread.sleep(100);
        System.out.println("===========================================================");
        System.out.println();

        if (System.getProperty("os.name").contains("Windows")) {
            enableWindows10AnsiSupport();
        }

        if (pingHost("asuna.tools", 443, 60)) {
            System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " asuna.tools is down!");
            System.exit(0);
        }
        if (pingHost("moe.asuna.tools", 443, 60)) {
            System.out.println(colorize("[ERROR]", RED_TEXT(), BOLD()) + " moe.asuna.tools is down!");
            System.exit(0);
        }

        Scanner input = new Scanner(System.in);
        System.out.print("Username: ");
        String username = input.nextLine();
        System.out.print("Password: ");
        char[] password = System.console().readPassword();
        try {
            Account.login(username, password);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println(colorize("Successfully", BRIGHT_GREEN_TEXT()) + " login as " + username + " (" + getCredentials()[0] + ")\n\r");

        boolean showMenu = true;
        while (showMenu) { showMenu = mainMenu(); }
    }

    public static boolean mainMenu() {

        Scanner input = new Scanner(System.in);
        System.out.println("===========" + colorize("Main Menu", CYAN_TEXT(), BOLD()) + "==========");
        System.out.println("1 - Download Anime");
        System.out.println("2 - Manage Requests");
        System.out.println("3 - Manage Account");
        System.out.println("==============================");
        System.out.println("4 - Quit");
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
                boolean showAnimeMenu = true;
                while (showAnimeMenu) { showAnimeMenu = Anime.animeMenu(); }
                return true;
            case 2:
                boolean showRequestsMenu = true;
                while (showRequestsMenu) { showRequestsMenu = Requests.requestsMenu(); }
                return true;
            case 3:
                boolean showAccountMenu = true;
                while (showAccountMenu) { showAccountMenu = Account.accountMenu(); }
                return true;
            default:
                return false;
        }
    }

    public static boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static void enableWindows10AnsiSupport() {
        Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
        DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
        HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

        DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
        Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
        GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});

        int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
        DWORD dwMode = p_dwMode.getValue();
        dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
        Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
        SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
    }
}
