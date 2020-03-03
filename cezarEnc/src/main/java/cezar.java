import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class cezar {
    private static final int CHARS = 26;
    private static final int BIG_CASE = 65;
    private static final int LOWER_CASE = 97;

    private static final String PLAIN = "plain.txt";
    private static final String CRYPTO = "crypto.txt";
    private static final String DECRYPT = "decrypt.txt";
    private static final String KEY = "key.txt";
    private static final String EXTRA = "extra.txt";
    private static final String KEY_FOUND = "key-found.txt";

    private static void crypto(String outputFile, String algorithm, String option) {
        try (PrintWriter out = new PrintWriter(outputFile)){
            if (algorithm.equals("-c")) {
                switch (option) {
                    case "-e":
                        out.println(caesarEncrypt());
                        break;
                    case "-d":
                        out.println(caesarDecrypt());
                        break;
                    case "-j":
                        out.println(caesarAnaliseWithPlain());
                        break;
                    case "-k":
                        caesarAnalise().forEach(out::println);
                        break;
                    default:
                        System.err.println("Wrong options. ");
                        break;
                }
            } else if (algorithm.equals("-a")) {
                switch (option) {
                    case "-e":
                        out.println(affineEncrypt());
                        break;
                    case "-d":
                        out.println(affineDecrypt());
                        break;
                    case "-j":
                        out.println(affineAnaliseWithPlain());
                        break;
                    case "-k":
                        affineAnalise().forEach(out::println);
                        break;
                    default:
                        System.err.println("Wrong options. ");
                        break;
                }
            } else {
                System.err.println("Wrong options. ");
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ne) {
            System.err.println("Wrong key. ");
        } catch (IOException ie) {
            System.err.println("Input files not found. ");
        }
    }

    private static String caesarEncrypt() throws IOException {
        int key = Integer.parseInt(Files.readString(Path.of(KEY)));
        String plain = Files.readString(Path.of(PLAIN));

        return caesarEncrypt(plain, key);
    }

    private static String caesarDecrypt() throws IOException {
        int key = Integer.parseInt(Files.readString(Path.of(KEY)));
        String crypto = Files.readString(Path.of(CRYPTO));

        return caesarDecrypt(crypto, key);
    }

    private static String caesarAnaliseWithPlain() throws IOException {
        String extra = Files.readString(Path.of(EXTRA));
        String crypto = Files.readString(Path.of(CRYPTO));

        return caesarAnaliseWithPlain(extra, crypto);
    }
    
    private static List<String> caesarAnalise() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO));

        return caesarAnalise(crypto);
    }

    private static String affineEncrypt() throws IOException {
        int[] key = Arrays.stream(Files.readString(Path.of(KEY)).split(" "))
                .mapToInt(Integer::parseInt).toArray();

        String plain = Files.readString(Path.of(PLAIN));

        return affineEncrypt(plain, key);
    }

    private static String affineDecrypt() throws IOException {
        int[] key = Arrays.stream(Files.readString(Path.of(KEY)).split(" "))
                .mapToInt(Integer::parseInt).toArray();

        String crypto = Files.readString(Path.of(CRYPTO));

        return affineDecrypt(crypto, key);
    }

    private static List<String> affineAnalise() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO));

        return affineAnalise(crypto);
    }

    private static String affineAnaliseWithPlain() throws IOException {
        String extra = Files.readString(Path.of(EXTRA));
        String crypto = Files.readString(Path.of(CRYPTO));

        return affineAnaliseWithPlain(extra, crypto);
    }

    private static String caesarEncrypt(String text, int key) {
        // Remove line delimiters.
        text = text.replace("\n", "").replace("\r", "");

        if(key < 0 || key > 26) {
            throw new NumberFormatException();
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                builder.append((char) (((c + key - BIG_CASE) % CHARS) + BIG_CASE));
            } else if (Character.isLowerCase(c)) {
                builder.append((char) (((c + key - LOWER_CASE) % CHARS) + LOWER_CASE));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String caesarDecrypt(String text, int key) {
        // Remove line delimiters.
        text = text.replace("\n", "").replace("\r", "");

        if(key < 0 || key > 26) {
            throw new NumberFormatException();
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                builder.append((char) (((c - key - BIG_CASE + CHARS) % CHARS) + BIG_CASE));
            } else if (Character.isLowerCase(c)) {
                builder.append((char) (((c - key - LOWER_CASE + CHARS) % CHARS) + LOWER_CASE));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String caesarAnaliseWithPlain(String extra, String crypto) {
        // Remove line delimiters.
        extra = extra.replace("\n", "").replace("\r", "");
        crypto = crypto.replace("\n", "").replace("\r", "");

        for (int i = 0; i <= CHARS; i++) {
            String extraCrypto = caesarEncrypt(extra, i);

            if (crypto.contains(extraCrypto)) {
                return i + " " + caesarDecrypt(crypto, i);
            }

        }

        return "";
    }

    private static List<String> caesarAnalise(String crypto) {
        // Remove line delimiters.
        crypto = crypto.replace("\n", "").replace("\r", "");

        List<String> analise = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i <= CHARS; i++) {
            builder.append(i)
                    .append(" ")
                    .append(caesarDecrypt(crypto, i));

            analise.add(builder.toString());
            builder.setLength(0);
        }

        return analise;
    }

    private static String affineEncrypt(String text, int[] key) {
        // Remove line delimiters.
        text = text.replace("\n", "").replace("\r", "");

        if (gcd(key[0], CHARS) != 1) {
            throw new NumberFormatException();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                builder.append((char) (((key[0] * (c - BIG_CASE) + key[1]) % CHARS) + BIG_CASE));
            } else if (Character.isLowerCase(c)) {
                builder.append((char) (((key[0] * (c - LOWER_CASE) + key[1]) % CHARS) + LOWER_CASE));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String affineDecrypt(String text, int[] key) {
        // Remove line delimiters.
        text = text.replace("\n", "").replace("\r", "");

        int a_inv = aInv(key);

        if ((a_inv == 0 && key[0] != 0 && key[1] != 0) || gcd(key[0], CHARS) != 1) {
            throw new NumberFormatException();
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isUpperCase(c)) {
                builder.append((char) (Mod((a_inv * (c - BIG_CASE - key[1])), CHARS) + BIG_CASE));
            } else if (Character.isLowerCase(c)) {
                builder.append((char) (Mod((a_inv * (c - LOWER_CASE - key[1])), CHARS) + LOWER_CASE));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private static String affineAnaliseWithPlain(String extra, String crypto) {
        // Remove line delimiters.
        extra = extra.replace("\n", "").replace("\r", "");
        crypto = crypto.replace("\n", "").replace("\r", "");

        for (int i = 1; i <= CHARS; i++) {
            for (int j = 1; j <= CHARS; j++) {

                if (gcd(i, CHARS) == 1) {
                    String extraCrypto = affineEncrypt(extra, new int[]{i, j});

                    if (crypto.contains(extraCrypto)) {
                        return i + " " + j + " " + affineDecrypt(crypto, new int[] {i, j});
                    }
                }
            }
        }

        return "";
    }

    private static List<String> affineAnalise(String crypto) {
        // Remove line delimiters.
        crypto = crypto.replace("\n", "").replace("\r", "");

        List<String> analise = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= CHARS; i++) {
            for (int j = 1; j <= CHARS; j++) {

                if (gcd(i, CHARS) == 1) {
                    builder.append(i)
                            .append(" ")
                            .append(j)
                            .append(" ")
                            .append(affineDecrypt(crypto, new int[]{i, j}));

                    analise.add(builder.toString());
                    builder.setLength(0);
                }
            }
        }

        return analise;
    }

    private static int aInv(int[] key) {
        for (int i = 0; i < CHARS; i++) {
            if (((key[0] * i) % CHARS) == 1) {
                return i;
            }
        }

        return 0;
    }

    private static int Mod(int i, int mod) {
        if (i < 0) {
            while (i < 0) {
                i += mod;
            }

            return i;
        }

        return i % mod;
    }

    private static int gcd(int n1, int n2) {
        int gcd = 1;

        for (int i = 1; i <= n1 && i <= n2; i++) {
            if (n1 % i == 0 && n2 % i == 0) {
                gcd = i;
            }
        }

        return gcd;
    }

    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Wrong arguments");
        } else {
            switch (args[1]) {
                case "-e":
                    crypto(CRYPTO, args[0], args[1]);
                    break;
                case "-d":
                    crypto(DECRYPT, args[0], args[1]);
                    break;
                case "-j":
                    crypto(KEY_FOUND, args[0], args[1]);
                    break;
                case "-k":
                    crypto(PLAIN, args[0], args[1]);
                    break;
                default:
                    System.err.println("Wrong arguments");
                    break;
            }
        }
    }
}
