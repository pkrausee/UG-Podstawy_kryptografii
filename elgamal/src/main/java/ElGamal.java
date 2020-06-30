import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ElGamal {
    private static final int RANDOM_EXPONENT_MAX = 100;

    private static final String GENERATOR_FILENAME = "elgamal.txt";
    private static final String PUBLIC_FILENAME = "public.txt";
    private static final String PRIVATE_FILENAME = "private.txt";
    private static final String PLAIN_FILENAME = "plain.txt";
    private static final String CRYPTO_FILENAME = "crypto.txt";
    private static final String DECRYPT_FILENAME = "decrypt.txt";
    private static final String MESSAGE_FILENAME = "message.txt";
    private static final String SIGNATURE_FILENAME = "signature.txt";
    private static final String VERIFY_FILENAME = "verify.txt";

    private static List<String> getContent(String generatorFilename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(generatorFilename));
        lines.replaceAll(e -> e.replace("[\r\n]", ""));

        return lines;
    }

    public static BigInteger getRandomBigInteger(String min, String max) {
        Random rand = new Random();

        BigInteger lowerLimit = new BigInteger(min);
        BigInteger upperLimit = new BigInteger(max);

        BigInteger result;
        do {
            result = new BigInteger(upperLimit.bitLength(), rand);
        } while (result.compareTo(upperLimit) >= 0 || result.compareTo(lowerLimit) <= 0);

        return result;
    }

    private static void generateKeys() throws IOException {
        List<String> lines = getContent(GENERATOR_FILENAME);

        BigInteger prime = new BigInteger(lines.get(0));
        BigInteger generator = new BigInteger(lines.get(1));

        BigInteger aliceKey = BigInteger.valueOf(1 + (int) (Math.random() * ((RANDOM_EXPONENT_MAX - 1) + 1)));
        BigInteger alicePublicKey = generator.modPow(aliceKey, prime);

        Files.write(Paths.get(PRIVATE_FILENAME), Arrays.asList(prime.toString(), generator.toString(), aliceKey.toString()));
        Files.write(Paths.get(PUBLIC_FILENAME), Arrays.asList(prime.toString(), generator.toString(), alicePublicKey.toString()));
    }

    private static void encrypt() throws Exception {
        List<String> publicKeyLines = getContent(PUBLIC_FILENAME);
        List<String> messageLines = getContent(PLAIN_FILENAME);

        BigInteger message = new BigInteger(messageLines.get(0));
        BigInteger prime = new BigInteger(publicKeyLines.get(0));

        if (message.compareTo(prime) >= 0) {
            throw new Exception("The \"m<p\" condition isn't met.");
        }

        BigInteger generator = new BigInteger(publicKeyLines.get(1));
        BigInteger alicePublicKey = new BigInteger(publicKeyLines.get(2));

        BigInteger bobKey = BigInteger.valueOf(1 + (int) (Math.random() * ((RANDOM_EXPONENT_MAX - 1) + 1)));
        BigInteger bobPublicKey = generator.modPow(bobKey, prime);

        BigInteger encryptKey = alicePublicKey.modPow(bobKey, prime);
        BigInteger encryptMsg = (message.multiply(encryptKey)).mod(prime);

        Files.write(Paths.get(CRYPTO_FILENAME), Arrays.asList(bobPublicKey.toString(), encryptMsg.toString()));
    }

    private static void decrypt() throws IOException {
        List<String> privateKeyLines = getContent(PRIVATE_FILENAME);
        List<String> encryptedMessageLines = getContent(CRYPTO_FILENAME);

        BigInteger prime = new BigInteger(privateKeyLines.get(0));
        BigInteger generator = new BigInteger(privateKeyLines.get(1));

        BigInteger bobPublicKey = new BigInteger(encryptedMessageLines.get(0));

        BigInteger bobK = BigInteger.ONE;
        while (!generator.modPow(bobK, prime).equals(bobPublicKey)) {
            bobK = bobK.add(BigInteger.ONE);
        }

        BigInteger aliceK = new BigInteger(privateKeyLines.get(2));
        BigInteger alicePublicKey = generator.modPow(aliceK, prime);

        BigInteger encryptKey = alicePublicKey.modPow(bobK, prime);
        BigInteger encryptMsg = new BigInteger(encryptedMessageLines.get(1));

        BigInteger encryptKeyInv = encryptKey.modPow(prime.subtract(BigInteger.valueOf(2)), prime);
        BigInteger decryptMsg = (encryptMsg.multiply(encryptKeyInv)).mod(prime);

        Files.write(Paths.get(DECRYPT_FILENAME), Collections.singleton(decryptMsg.toString()));
    }

    private static void sign() throws IOException {
        List<String> privateKeyLines = getContent(ElGamal.PRIVATE_FILENAME);
        List<String> messageLines = getContent(ElGamal.MESSAGE_FILENAME);

        BigInteger prime = new BigInteger(privateKeyLines.get(0));
        BigInteger generator = new BigInteger(privateKeyLines.get(1));
        BigInteger aliceK = new BigInteger(privateKeyLines.get(2));
        BigInteger message = new BigInteger(messageLines.get(0));

        BigInteger k;
        do {
            int primeInt = prime.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0 ? prime.intValue() : Integer.MAX_VALUE;
            k = getRandomBigInteger("1", String.valueOf(primeInt));
        } while (!k.gcd(prime.subtract(BigInteger.ONE)).equals(BigInteger.ONE));

        BigInteger mod = prime.subtract(BigInteger.ONE);

        BigInteger r = generator.modPow(k, prime);
        BigInteger x1 = (message.subtract(aliceK.multiply(r))).add(mod.multiply(new BigInteger("10000")));
        BigInteger x = (x1.multiply(multiplicativeInverse(k, prime.subtract(BigInteger.ONE)))).mod(mod);

        Files.write(Paths.get(SIGNATURE_FILENAME), Arrays.asList(r.toString(), x.toString()));
    }

    private static String verifySignature() throws IOException {
        List<String> publicKeyLines = getContent(ElGamal.PUBLIC_FILENAME);
        List<String> messageLines = getContent(ElGamal.MESSAGE_FILENAME);
        List<String> signatureLines = getContent(ElGamal.SIGNATURE_FILENAME);

        BigInteger prime = new BigInteger(publicKeyLines.get(0));
        BigInteger generator = new BigInteger(publicKeyLines.get(1));
        BigInteger publicKey = new BigInteger(publicKeyLines.get(2));

        BigInteger message = new BigInteger(messageLines.get(0));

        BigInteger r = new BigInteger(signatureLines.get(0));
        BigInteger x = new BigInteger(signatureLines.get(1));

        BigInteger left = generator.modPow(message, prime);
        BigInteger right = ((publicKey.modPow(r, prime)).multiply(r.modPow(x, prime))).mod(prime);

        String output;
        if (r.compareTo(BigInteger.ONE) >= 0 && r.compareTo(prime) < 0 && left.equals(right)) {
            output = "Given signature is valid";
        } else {
            output = "Given signature is invalid";
        }

        Files.write(Paths.get(VERIFY_FILENAME), Collections.singleton(output));

        return output;
    }

    private static BigInteger multiplicativeInverse(BigInteger a, BigInteger m) {
        if (m.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }

        BigInteger x0 = BigInteger.ZERO;
        BigInteger x1 = BigInteger.ONE;

        BigInteger m0 = m;

        while (a.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = a.divide(m);
            BigInteger t = m;

            m = a.mod(m);
            a = t;

            t = x0;
            x0 = x1.subtract(q.multiply(x0));
            x1 = t;
        }

        if (x1.compareTo(BigInteger.ZERO) < 0) {
            x1 = x1.add(m0);
        }

        return x1;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("You need to provide action parameter. ");
            return;
        }

        try {
            switch (args[0]) {
                case "-k":
                    generateKeys();
                    break;
                case "-e":
                    encrypt();
                    break;
                case "-d":
                    decrypt();
                    break;
                case "-s":
                    sign();
                    break;
                case "-v":
                    System.out.println(verifySignature());
                    break;
                default:
                    System.out.println("Invalid action parameter. ");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
