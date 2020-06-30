import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

public class xor {
    private static final int MAX_LINE_LENGTH = 32;
    private static final int TEXT_LINES = 19;

    private static final String ORIG = "orig.txt";
    private static final String PLAIN = "plain.txt";
    private static final String KEY = "key.txt";
    private static final String CRYPTO = "crypto.txt";
    private static final String DECRYPT = "decrypt.txt";

    private static final String SPECIAL_CHAR = "[,.!?:;'-0123456789]";
    private static final String NEW_LINE_CHAR = "[\r\n]";

    private static void prepare() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ORIG))));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(PLAIN))))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line.replaceAll(SPECIAL_CHAR, "").toLowerCase());
                builder.append('\n');
            }
            String content = builder.toString();

            int currLength = 0;
            builder.setLength(0);
            for (int i = 0; i < content.length(); i++) {
                if (currLength == MAX_LINE_LENGTH) {
                    builder.append('\n');
                    writer.write(builder.toString());

                    builder.setLength(0);
                    currLength = 0;
                }

                if (content.charAt(i) == '\n') {
                    builder.append(' ');
                } else {
                    builder.append(content.charAt(i));
                }

                currLength++;
            }
        }
    }

    private static void encrypt() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(PLAIN))));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(CRYPTO))))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line.replaceAll(SPECIAL_CHAR, "").toLowerCase());
                builder.append('\n');
            }

            byte[] plain = builder.toString().getBytes(StandardCharsets.US_ASCII);
            byte[] key = readKey();

            builder.setLength(0);
            for (int i = 0, j = 0; i <= plain.length - 1; i++) {
                if (j >= key.length - 1) {
                    j = 0;
                }

                int result = plain[i] + key[j];
                j += 1;

                if (result > 122) {
                    result -= 25;
                }

                if (plain[i] == 10) {
                    result = 10;
                    j = 0;
                }

                builder.append((char) result);
            }

            writer.write(builder.toString());
        }
    }

    private static void cryptoAnalise() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CRYPTO))));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(DECRYPT))))) {
            byte[][] plainBytes = new byte[TEXT_LINES][MAX_LINE_LENGTH];

            String line;
            int iter = 0;
            while ((line = reader.readLine()) != null) {
                plainBytes[iter] = line.getBytes();
                iter += 1;
            }

            byte[] bytes = new byte[MAX_LINE_LENGTH];
            int[] keyBytes = new int[MAX_LINE_LENGTH];
            for (int i = 0; i <= TEXT_LINES - 1; i++) {
                for (int j = 0; j <= MAX_LINE_LENGTH - 1; j++) {
                    if (plainBytes[i][j] < 58) {
                        bytes[j] = 32;
                        keyBytes[j] = plainBytes[i][j] - bytes[j];
                    }
                }
            }

            System.out.print("Found key: ");
            IntStream.of(keyBytes).forEach(x -> System.out.print((char) (x + 97)));

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i <= TEXT_LINES - 1; i++) {
                for (int j = 0; j <= MAX_LINE_LENGTH - 1; j++) {
                    plainBytes[i][j] -= keyBytes[j];
                    if (plainBytes[i][j] < 97 && plainBytes[i][j] > 33) {
                        plainBytes[i][j] += 25;
                    }

                    builder.append((char) plainBytes[i][j]);
                }
                builder.append("\n");
            }

            writer.write(builder.toString());
        }
    }

    private static byte[] readKey() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(KEY))))) {
            byte[] key = reader.readLine()
                    .replaceAll(NEW_LINE_CHAR, "")
                    .substring(0, MAX_LINE_LENGTH)
                    .getBytes();

            for (int i = 0; i <= MAX_LINE_LENGTH - 1; i++) {
                key[i] -= 97;
            }

            return key;
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.err.println("Wrong argument");
            } else {
                switch (args[0]) {
                    case "-p":
                        prepare();
                        break;
                    case "-e":
                        encrypt();
                        break;
                    case "-k":
                        cryptoAnalise();
                        break;
                    default:
                        System.err.println("Wrong argument");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Wrong files");
        }
    }
}
