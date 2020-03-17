import java.io.*;

public class Xor {
    private static final int SPACE_MASK = 64;
    private static final int SPACE_VALUE = ' ';

    private static final int MAX_LINE_LENGTH = 32;

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
                builder.append(line);
                builder.append('\n');
            }

            String plain = builder.toString();
            String key = readKey();

            writer.write(xor(plain, key));
        }
    }

    private static void cryptoAnalise() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CRYPTO))));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(DECRYPT))))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            String crypto = builder.toString();
            String decrypt = findKey(crypto);

            writer.write(decrypt);
        }
    }

    private static String readKey() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(KEY))))) {
            return reader.readLine().replaceAll(NEW_LINE_CHAR, "").substring(0, MAX_LINE_LENGTH);
        }
    }

    private static String xor(String val1, String val2) {
        StringBuilder output = new StringBuilder();

        for (int i = 0, j = 0; i < val1.length(); i++, j++) {
            if (j % MAX_LINE_LENGTH == 0) {
                j = 0;
            }

            output.append((char) (val2.charAt(j) ^ val1.charAt(i)));
        }

        return output.toString();
    }

    private static String findKey(String encrypted) {
        char[] foundKey = new char[MAX_LINE_LENGTH];

        int currLength = 0;
        for (int i = 0; i < encrypted.length() - 2; i++) {
            int c1 = encrypted.charAt(i);
            int c2 = encrypted.charAt(i + 1);
            int c3 = encrypted.charAt(i + 2);

            try {
                if (((c1 ^ c2) & SPACE_MASK) != 0) {
                    if (((c1 ^ c3) & SPACE_MASK) != 0 && ((c2 ^ c3) & SPACE_MASK) == 0) {
                        foundKey[currLength] = (char) (c1 ^ SPACE_VALUE);
                    } else {
                        if (((c1 ^ c3) & SPACE_MASK) == 0 && ((c2 ^ c3) & SPACE_MASK) != 0) {
                            foundKey[currLength + 1] = (char) (c2 ^ SPACE_VALUE);
                        }
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (((c1 ^ c3) & SPACE_MASK) != 0) {
                    if (((c1 ^ c2) & SPACE_MASK) != 0 && ((c2 ^ c3) & SPACE_MASK) == 0) {
                        foundKey[currLength] = (char) (c1 ^ SPACE_VALUE);
                    } else {
                        if (((c1 ^ c2) & SPACE_MASK) == 0 && ((c2 ^ c3) & SPACE_MASK) != 0) {
                            foundKey[currLength + 2] = (char) (c3 ^ SPACE_VALUE);
                        }
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (((c2 ^ c3) & SPACE_MASK) != 0) {
                    if (((c1 ^ c2) & SPACE_MASK) != 0 && ((c1 ^ c3) & SPACE_MASK) == 0) {
                        foundKey[currLength + 1] = (char) (c2 ^ SPACE_VALUE);
                    } else {
                        if (((c1 ^ c2) & SPACE_MASK) == 0 && ((c1 ^ c3) & SPACE_MASK) != 0) {
                            foundKey[currLength + 2] = (char) (c3 ^ SPACE_VALUE);
                        }
                    }
                }
            } catch (Exception e) {
            }

            currLength++;
            if (currLength % (MAX_LINE_LENGTH) == 0) {
                currLength = 0;
            }
        }

        String foundKeyString = new String(foundKey);
        System.out.println("Found key: " + foundKeyString);

        return xor(encrypted, foundKeyString);
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
