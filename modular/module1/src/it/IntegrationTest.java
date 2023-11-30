import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegrationTest {

    final List<String> iprofs = new ArrayList<>();

    void test(final String[] executablePath, final String directory, final String iprofOutputFile) throws Exception {
        String iprof = Paths.get(directory, iprofOutputFile).toString();
        List<String> list = new ArrayList<>(Arrays.stream(executablePath).toList());
        list.add("-XX:ProfilesDumpFile=" + iprof);
        final Process process = Runtime.getRuntime().exec(list.toArray(new String[0]));
        final InputStream inputStream = process.getInputStream();
        final String actual = readAllLines(new BufferedReader(new InputStreamReader(inputStream)));

        iprofs.add(iprof);
        System.out.println(actual);
    }

    void execute(final String[] args) throws Exception {
        final String directory = args[0];
        final String iprofResult = args[1];
        final String[] executablePath = Arrays.copyOfRange(args, 2, args.length);

        // Create directory if not exists
        Files.createDirectories(Paths.get(directory));

        test(executablePath, directory, "1.iprof");
        test(executablePath, directory, "2.iprof");

        try(PrintWriter writer = new PrintWriter(Paths.get(directory, iprofResult).toString(), StandardCharsets.UTF_8)) {
            writer.print(String.join(",", iprofs));
        }
    }

    String readAllLines(BufferedReader reader) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            content.append(line);
            content.append(System.lineSeparator());
        }

        return content.toString();
    }

    public static void main(final String[] args) throws Exception {
        new IntegrationTest().execute(args);
    }
}