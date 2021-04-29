import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Scanner;

public class HTTPServer {
    private static BufferedInputStream bis = null;
    private static DataInputStream dis = null;
    private static String req_type;

    public static void main(String[] args) {
        /**
         * It must accept a single command-line argument: port, which is the port that
         * it will listen on for incoming HTTP requests. o If any of the arguments are
         * incorrect, exit after printing an error message of the form ERR - arg x,
         * where x is the argument number. o The only error-checking that needs to be
         * done on the port is to ensure it is a positive integer less than 65536. o
         * Remember that only ports 10000-11000 are open for use.
         */

        Scanner s = new Scanner(System.in);
        print("Enter port number: \n");
        String port = s.nextLine();
        for (Character ch : port.toCharArray()) {
            if (!ch.isDigit(ch)) {
                print("Port should be only number!");
                return;
            }
        }
        int portNumber = Integer.parseInt(port);
        if (portNumber < 65536 && portNumber > 0)
            make(portNumber);
        else
            print("Invalid port number!");
    }

    private static void make(int port) {
        try {
            ServerSocket ss = new ServerSocket(port);

            print("Waiting on port: " + port);
            while (!ss.isClosed()) {
                Socket s = ss.accept();// establishes connection and waits for the client
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
                /**
                 * For each new HTTP request, print the client's IP address, port, and the
                 * request type in the format IP:port:request Example: 172.18.233.83:63307:GET
                 */
                String str = (String) dis.readUTF();
                File file = handleRequest(str);

                print("Client : " + s.getRemoteSocketAddress() + ":" + s.getLocalPort() + ":" + req_type);

                if (file == null) {
                    // invalid request or file not found!
                    dout.writeUTF(errorHeader());
                    dout.flush();
                    dout.close();
                    print("\nResponded to the request.");
                } else {
                    print("\nRequest for page " + file.getName());

                    extractDataFromRequest(str);
                    // check if file exists and extract data

                    /**
                     * Parse the HTTP response header received from the server and print the
                     * following information: § Response code (number) § Server type § If the
                     * response code is in the 200-level: • Last modified date (if included) •
                     * Number of bytes in the response data
                     */

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(file.lastModified());
                    String response = "HTTP:/1.0 200 OK\bl n" + "Content-type: text/html\bl n" + "Content-length: "
                            + file.length() + " \bl n" + "\bl \n Last Modified: " + cal.getTime() + "\n"
                            + readFile(file);
                    dout.writeUTF(response);
                    dout.flush();
                    dout.close();
                    print("Responded to the request.");
                }
            }
            ss.close();
        } catch (Exception ex) {

        }
    }

    static void extractDataFromRequest(String data) {

        if (data.indexOf("?") > 0) {
            String query = data.substring(data.indexOf("?") + 1);
            String[] params = query.split("&");
            print("");
            print("Client Query: ");
            for (String parm : params) {
                print(parm);
            }
            print("");
        }

    }

    static File handleRequest(String msg) {
        // The server should able to handle both HTTP commands: GET and PUT.
        if (msg.contains("PUT")) {
            req_type = "PUT";
            int putIndex = 3;
            int queryIndex = msg.indexOf("?");
            String[] names = msg.substring(putIndex, queryIndex).split("/");

            String fileName = names[names.length - 2];

            print(fileName + " File name \n");
            File file = new File(fileName);
            try {
                if (!file.exists() && file.createNewFile()) {
                    // file created
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(msg);
                    fileWriter.flush();
                    fileWriter.close();
                    return file;
                } else if (file.exists())
                    return file;
                return null;

            } catch (Exception ex) {
                return null;
            }

        } else if (msg.contains("GET")) {
            req_type = "GET";
            String[] paths = msg.split("/");
            File path = new File("index.html");

            for (String file : paths) {
                File resource = new File(file);
                if (resource.exists()) {
                    return path = resource;
                }
            }
            return path;
        } else {
            return null;
        }
    }

    private static void print(String msg) {
        System.out.println(msg);
    }

    static String errorHeader() {
        StringBuilder build = new StringBuilder();
        if (req_type == "GET")
            build.append("HTTP:/1.0 404 Not Found\bl n\n");
        else if (req_type.equalsIgnoreCase("PUT"))
            build.append("HTTP:/1.0 606 FAILED File NOT Created\bl n\n");

        build.append("Content-type: text/html\bl n\n");
        build.append("Content-length: 0 \bl n\n");
        build.append("\bl n\n");
        return build.toString();
    }

    static String readFile(File file) {
        try {
            StringBuilder build = new StringBuilder();
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                build.append(sc.nextLine()).append("\n");
            }

            return build.toString();
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        }
    }
}
