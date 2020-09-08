import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main_Script {

    public static void main(String[] args) throws Exception {
        //executeCommand("src/main/resources/export-and-delete.sh");
        //executeCommand("src/main/resources/testing.sh");
        String mongoUri = getMongoUri();
        replace_backend_yml();

    }


    public static void replace_backend_yml() {
        //TODO: use SnakeYAML to modify yml file

    }


    public static String getMongoUri() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("mongodb-information.txt"));
        String mongoUri = "";
        Integer start_letter = 22;
        while((mongoUri = br.readLine()) != null) {
            if (mongoUri.contains("Connection URL:")) {
                System.out.println(mongoUri.substring(start_letter));
            }
        }
        return mongoUri;
    }

    public static void executeCommand(String cmd) {
        try {
            String target = new String(cmd);
            Process proc = Runtime.getRuntime().exec(target);
            proc.waitFor();

            StringBuffer output = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
            System.out.println("### " + output);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}