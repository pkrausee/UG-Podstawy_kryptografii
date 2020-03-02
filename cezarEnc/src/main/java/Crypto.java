import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Crypto {
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
                        int[] foundKey = affineAnaliseWithPlain();
                        out.println(Arrays.stream(foundKey).mapToObj(String::valueOf).reduce((a, b) -> a.concat(" ").concat(b)).orElse(""));
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
        }catch (NumberFormatException ne) {
            System.err.println("Wrong key. ");
        } catch (IOException ie) {
            System.err.println("Input files not found. ");
        }
    }

    private static String caesarEncrypt() throws IOException {
        String plain = Files.readString(Path.of(PLAIN), StandardCharsets.ISO_8859_1);
        int key = Integer.parseInt(Files.readString(Path.of(KEY), StandardCharsets.ISO_8859_1));

        return caesarEncrypt(plain, key);
    }

    private static String caesarDecrypt() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);
        int key = Integer.parseInt(Files.readString(Path.of(KEY), StandardCharsets.ISO_8859_1));

        return caesarDecrypt(crypto, key);
    }

    private static List<String> caesarAnalise() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);

        return caesarAnalise(crypto);
    }

    private static int caesarAnaliseWithPlain() throws IOException {
        String plain = Files.readString(Path.of(PLAIN), StandardCharsets.ISO_8859_1);
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);

        return caesarAnaliseWithPlain(plain, crypto);
    }

    private static String affineEncrypt() throws IOException {
        String plain = Files.readString(Path.of(PLAIN), StandardCharsets.ISO_8859_1);
        int[] key = Arrays.stream(Files.readString(Path.of(KEY), StandardCharsets.ISO_8859_1).split(" "))
                .mapToInt(Integer::parseInt).toArray();

        return affineEncrypt(plain, key);
    }

    private static String affineDecrypt() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);
        int[] key = Arrays.stream(Files.readString(Path.of(KEY), StandardCharsets.ISO_8859_1).split(" "))
                .mapToInt(Integer::parseInt).toArray();

        return affineDecrypt(crypto, key);
    }

    private static List<String> affineAnalise() throws IOException {
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);

        return affineAnalise(crypto);
    }

    private static int[] affineAnaliseWithPlain() throws IOException {
        String plain = Files.readString(Path.of(PLAIN), StandardCharsets.ISO_8859_1);
        String crypto = Files.readString(Path.of(CRYPTO), StandardCharsets.ISO_8859_1);

        return affineAnaliseWithPlain(plain, crypto);
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
                builder.append((char) (((c - key - BIG_CASE) % CHARS) + BIG_CASE));
            } else if (Character.isLowerCase(c)) {
                builder.append((char) (((c - key - LOWER_CASE) % CHARS) + LOWER_CASE));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
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

    private static int caesarAnaliseWithPlain(String plain, String crypto) {
        // Remove line delimiters.
        plain = plain.replace("\n", "").replace("\r", "");
        crypto = crypto.replace("\n", "").replace("\r", "");

        for (int i = 0; i <= CHARS; i++) {
            String decrypted = caesarDecrypt(crypto, i);

            if (plain.equals(decrypted)) {
                return i;
            }

        }

        return -1;
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

    private static int[] affineAnaliseWithPlain(String plain, String crypto) {
        // Remove line delimiters.
        plain = plain.replace("\n", "").replace("\r", "");
        crypto = crypto.replace("\n", "").replace("\r", "");

        for (int i = 1; i <= CHARS; i++) {
            for (int j = 1; j <= CHARS; j++) {

                if (gcd(i, CHARS) == 1) {
                    String decrypted = affineDecrypt(crypto, new int[]{i, j});

                    if (plain.equals(decrypted)) {
                        return new int[] {i, j};
                    }
                }
            }
        }

        return new int[] {};
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
                    crypto(EXTRA, args[0], args[1]);
                    break;
                default:
                    System.err.println("Wrong arguments");
                    break;
            }
        }
    }
}
