import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PoShI {

    public static void main(String[] args) throws Exception {
        // Start web server on port 8000 with /date endpoint returning current date
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/date", new PoShIHttpHandler());
        server.setExecutor(null);
        server.start();
    }

    static class PoShIHttpHandler implements HttpHandler {
        static String escapePoSh(String param) {
            // Bad characters list
            String[] chars = { "`", "$", "\"", "\r", "\n", "#", "}", "{", "'" };
            String ret = param;

            // Remove bad characters from user-provided data
            for (String s : chars) {
                if (ret.contains(s)) {
                    ret = ret.replace(s, "");
                }
            }

            return ret;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery(); // HTTP GET query string
            String format = "d"; // Default format for Get-Date cmdlet

            // Naive, oversimplified query string parsing
            // Everything after "format=" will be treated as date format for Get-Date
            // (query string parsing is not relevant for this PoC)
            if(query != null && query.matches("(.*)format=\\w+(.*)")) {
                format = URLDecoder.decode(query.split("format=")[1], StandardCharsets.UTF_8);
            }

            // Start PowerShell, create proper objects for I/O
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoExit", "-Command", "-");
            Process p = pb.start();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream())), true);
            StreamHandler outStream = new StreamHandler(p.getInputStream(), System.out);
            Thread outThread = new Thread(outStream);
            outThread.start();

            // Execute Get-Date cmdlet in PowerShell
            // Format parameter provided by user is escaped using escapePoSh function
            writer.println("Get-Date -Format \"" + escapePoSh(format) + "\"\r\n");
            writer.flush();

            // Close PowerShell
            writer.println("exit");
            writer.flush();

            try {
                p.waitFor(2, TimeUnit.SECONDS);
                p.destroy();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Send HTTP response with command execution result
            t.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");

            String response = "Current date: " + outStream.output;
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

}