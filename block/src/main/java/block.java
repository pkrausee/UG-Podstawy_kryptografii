import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class block {
    private static final int DELTA = 0x9e3779b9;

    private static final String PLAIN = "plain.bmp";
    private static final String KEY = "key.txt";
    private static final String CRYPTO_ECB = "ecb_crypto.bmp";
    private static final String CRYPTO_CBC = "cbc_crypto.bmp";

    private static final String NEW_LINE_CHAR = "[\r\n]";

    private static int[] getKey() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(KEY))))) {
            String[] line = reader.readLine().replaceAll(NEW_LINE_CHAR, "").split(",");

            System.out.println("Found key: {" + String.join(", ", line) + "}. ");
            return Arrays.stream(line).mapToInt(Integer::parseInt).toArray();
        } catch (IOException e) {
            System.out.println("Program will use default key: {0, 0, 0, 0}. ");
        }

        return new int[]{0, 0, 0, 0};
    }

    private static int[] ecb_encrypt(int[] plain, int[] key) {
        int left = plain[0];
        int right = plain[1];

        return encrypt(left, right, key);
    }

    private static int[] cbc_encrypt(int[] plain, int[] prev, int[] key) {
        int left = plain[0] ^ prev[0];
        int right = plain[1] ^ prev[1];

        return encrypt(left, right, key);
    }

    private static int[] encrypt(int left, int right, int[] key) {
        int sum = 0;
        for (int i = 0; i < 32; i++) {
            sum += DELTA;
            left += ((right << 4) + key[0]) ^ (right + sum) ^ ((right >> 5) + key[1]);
            right += ((left << 4) + key[2]) ^ (left + sum) ^ ((left >> 5) + key[3]);
        }

        return new int[]{left, right};
    }

    private static void ecb(int[] key) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(PLAIN));
             DataOutputStream out = new DataOutputStream(new FileOutputStream(CRYPTO_ECB))) {
            writeImg(in, out);

            int[] img = new int[2];
            boolean eofCheck = true;
            while (in.available() > 0) {
                try {
                    img[0] = in.readInt();
                    eofCheck = true;
                    img[1] = in.readInt();

                    int[] cipher = ecb_encrypt(img, key);
                    out.writeInt(cipher[0]);
                    out.writeInt(cipher[1]);

                    eofCheck = false;
                } catch (EOFException e) {
                    out.writeInt(img[0]);

                    if (!eofCheck) {
                        out.writeInt(img[1]);
                    }
                }
            }
        }
    }

    private static void cbc(int[] key) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(PLAIN));
             DataOutputStream out = new DataOutputStream(new FileOutputStream(CRYPTO_CBC))) {
            writeImg(in, out);

            int[] img = new int[2];
            int[] cipher = new int[2];
            boolean eofCheck = true;
            boolean firstIterCheck = true;
            while (in.available() > 0) {
                try {
                    img[0] = in.readInt();
                    eofCheck = true;
                    img[1] = in.readInt();

                    if (firstIterCheck) {
                        Random rand = new Random();

                        cipher = cbc_encrypt(img, new int[]{rand.nextInt(), rand.nextInt()}, key);
                        firstIterCheck = false;
                    } else {
                        cipher = cbc_encrypt(img, cipher, key);
                    }

                    out.writeInt(cipher[0]);
                    out.writeInt(cipher[1]);

                    eofCheck = false;
                } catch (EOFException e) {
                    out.writeInt(img[0]);

                    if (!eofCheck) {
                        out.writeInt(img[1]);
                    }
                }
            }
        }
    }

    private static void writeImg(DataInputStream in, DataOutputStream out) throws IOException {
        for (int i = 0; i < 10; i++) {
            if (in.available() > 0) {
                out.writeInt(in.readInt());
                out.writeInt(in.readInt());
            }
        }
    }

    public static void main(String[] args) {
        try {
            int[] key = getKey();

            ecb(key);
            cbc(key);

            System.out.println("Encryption complete. ");
        } catch (IOException e) {
            System.out.println("Input file not found. ");
        }
    }
}