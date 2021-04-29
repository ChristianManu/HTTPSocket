import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPClient {
    private static Socket socket = null;
    private static DataOutputStream dos = null;

    public static void main(String[] args) {
        //String type = "";
        if(args.length == 1)
        {
            String url = args[0];
            if(isUrlValid(url)){
              String type = "get";
                if (type.equals("get")) {
                String host = getHost(url);
                int port = getPort(url);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        make(host, port, type.toUpperCase(), url);
                    }

                }).start();
            } 
            }
            else {
            // If any of the arguments are incorrect, exit after printing an error message
            // of the form ERR - arg x, where x is the argument number.
            print("ERR - arg 42");
        }

        }
        else if(args.length > 1)
        {
            if(args[0] == "put")
            {
               String type1 = args[0];
                String url1 = args[1];
                if(isUrlValid(url1)){

                     /**
                     * Your client should accept a three command-line argument: PUT command, URL to
                     * where the files should be stored in the server, and the local path/filename
                     * of the file to transmit.
                     */
                    String host = getHost(url1);
                    int port = getPort(url1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            make(host, port, type1.toUpperCase(), url1);
                        }

                    }).start();
                }

            }

        }
        else {
            // If any of the arguments are incorrect, exit after printing an error message
            // of the form ERR - arg x, where x is the argument number.
            print("ERR - arg 76");
        }

    }


    private static void make(String host, int port, String type, String url) {
        try {

            Socket s = new Socket(host, port);
            Calendar cal = Calendar.getInstance();

            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            // Follow the HTTP protocol of GET <path> HTTP/1.0 followed by an empty line

            /**
             * Note: Print the actual string sent to the server. Do not send a request and
             * then re-type the request for output.
             */

            print("Sending request: ");
            String request = type + " " + getPage(url) + " ?Host=" + Inet4Address.getLocalHost().toString() + "&Time="
                    + cal.getTime() + "&User-Agent=VCU-CMSC491" + "&User-name=username" + " HTTP/1.0";
            // Send the HTTP request sent to the server.
            print(request);
            dout.writeUTF(request);
            dout.flush();
            /**
             * Parse the HTTP response header received from the server and print the
             * following information: § Response code (number) § Server type § If the
             * response code is in the 200-level: • Last modified date (if included) •
             * Number of bytes in the response data • Store the received file (for example:
             * index.html from http://www.vcu.edu/) in the current directory. § If the
             * response code is in the 300-level: • The URL where the file is located o
             * Print the HTTP response header. o In case you successfully received and
             * stored a file, open the stored file using any browser to check it • Store the
             * received file (for example: index.html from http://www.vcu.edu/) in the
             * current directory. § If the response code is in the 300-level: • The URL
             * where the file is located
             */

            FileWriter f = new FileWriter("response.txt");
            String rs = din.readUTF();
            print(rs);
            print("Response is saved in a file response.txt");
            f.write(rs);
            f.flush();
            din.close();
            dout.close();
            f.close();
            s.close();
        } catch (Exception ex) {
            print(ex.getLocalizedMessage());
        }
    }

    private static int getPort(String url) {
        // 3-:port - an optional port, if not present, use port 80.
        String regex = "\\d";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        int port = 80;// default
        String digits = "";
        while (matcher.find()) {
            digits += matcher.group();
        }

        try {
            port = Integer.parseInt(digits);
        } catch (Exception ex) {
            // no port found
            port = 80;
        }
        return port;
    }

    private static String getPage(String url) {
        // check if last charachter is not /
        if (url.charAt(url.length() - 1) != '/') {
            return url = url + "/";
        }
        return url;
    }

    private static String getHost(String url) {
        String host = url.split("http://")[1];
        // 2-hostname - the web server's hostname

        if (host.contains(":")) {
            host = host.split(":")[0];
        } else if (host.contains("/")) {
            host = host.split("/")[0];
        }
        return host;
    }

    private static void print(String msg) {
        System.out.println(msg);
    }

    private static boolean isUrlValid(String url) {
        // Treat any URL as valid as long as it starts with http://
        return url.contains("http://");
    }
}
