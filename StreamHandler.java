import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class StreamHandler implements Runnable {
    public String output;
    private BufferedReader reader;

    StreamHandler(InputStream i, PrintStream o) {
        this.reader = new BufferedReader(new InputStreamReader(i));
        this.output = "";
    }

    public void run() {
        try {
            String line;
            while (null != (line = this.reader.readLine())) {
                this.output += line;
            }

            this.reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
