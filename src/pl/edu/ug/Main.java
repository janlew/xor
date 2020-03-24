package pl.edu.ug;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static final String ORIG = "orig.txt";
    public static final String PLAIN = "plain.txt";
    public static final String KEY = "key.txt";
    public static final String CRYPTO = "crypto.txt";
    public static final String DECRYPT = "decrypt.txt";
    public static final int LINE_LENGTH = 64;

    public static void main(String[] args) throws Exception {
        if (args[0].equals("-p")) {
            prepare();
        } else if (args[0].equals("-e")) {
            encrypt();
        } else if (args[0].equals("-k")) {
            analysis();
        } else {
            System.out.println("Wybierz:\n -p przygotowanie tekstu\n -e szyfrowanie\n -k kryptoanaliza");
        }
    }

    public static String readFile(String name) {
        try {
            File file = new File(name);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                scanner.useDelimiter("\\Z");
                String data = scanner.next();
                return data;
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Nie można otworzyć pliku.");
        }
        return null;
    }

    public static void writeFile(String name, String text) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        writer.write(text);
        writer.close();
    }

    public static void prepare() throws Exception {
        String result = "";
        String orig = lower();
        char[] origChars = orig.toCharArray();
        int charsInLine = 0;
        for (int i = 0; i < origChars.length; i++) {
            if (charsInLine == LINE_LENGTH) {
                result += '\n';
                charsInLine = 0;
            }
            if (origChars[i] == '\n') {
                result += ' ';
            } else {
                result += origChars[i];
            }
            charsInLine++;
        }
        while (charsInLine != LINE_LENGTH) {
            result += ' ';
            charsInLine++;
        }
        writeFile(PLAIN, result);
    }

    public static String lower() throws IOException {
        String result = "";
        String input = readFile(ORIG);
        input = input.toLowerCase();
        char[] inputChars = input.toCharArray();

        for (int i = 0; i < inputChars.length; i++) {
            if (inputChars[i] > 96 && inputChars[i] < 123 || inputChars[i] == ' ') {
                result += inputChars[i];
            } else {
                result += "";
            }
        }
        return result;
    }

    public static void encrypt() throws IOException {
        String text = readFile(PLAIN);
        String key = readFile(KEY);

        byte[] textByte = text.getBytes();
        byte[] keyByte = key.getBytes();
        byte[] encryptedByte = new byte[textByte.length];

        int charsInLine = 0;

        for (int i = 0; i < textByte.length; i++) {
            if (charsInLine % LINE_LENGTH == 0) {
                charsInLine = 0;
            }
            encryptedByte[i] = (byte) (keyByte[charsInLine] ^ textByte[i]);
            charsInLine++;
        }
        writeFile(CRYPTO, new String(encryptedByte));
    }

    public static void analysis() throws IOException {
        String text = readFile(CRYPTO);
        byte[] textByte = text.getBytes();
        byte[] foundKeyByte = new byte[LINE_LENGTH];
        byte space = (char) ' ';
        int charsInLine = 0;
        int mask = 64;

        for (int i = 0; i < textByte.length; i++) {
            try {
                if (((textByte[i] ^ textByte[i + 1]) & mask) != 0) {
                    if (((textByte[i] ^ textByte[i + 2]) & mask) != 0 && ((textByte[i + 1] ^ textByte[i + 2]) & mask) == 0) {
                        foundKeyByte[charsInLine] = (byte) (textByte[i] ^ space);
                    } else {
                        if (((textByte[i] ^ textByte[i + 2]) & mask) == 0 && ((textByte[i + 1] ^ textByte[i + 2]) & mask) != 0) {
                            foundKeyByte[charsInLine + 1] = (byte) (textByte[i + 1] ^ space);
                        }
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (((textByte[i] ^ textByte[i + 2]) & mask) != 0) {
                    if (((textByte[i] ^ textByte[i + 1]) & mask) != 0 && ((textByte[i + 1] ^ textByte[i + 2]) & mask) == 0) {
                        foundKeyByte[charsInLine] = (byte) (textByte[i] ^ space);
                    } else {
                        if (((textByte[i] ^ textByte[i + 1]) & mask) == 0 && ((textByte[i + 1] ^ textByte[i + 2]) & mask) != 0) {
                            foundKeyByte[charsInLine + 2] = (byte) (textByte[i + 2] ^ space);
                        }
                    }
                }
            } catch (Exception e) {
            }

            try {
                if (((textByte[i + 1] ^ textByte[i + 2]) & mask) != 0) {
                    if (((textByte[i] ^ textByte[i + 1]) & mask) != 0 && ((textByte[i] ^ textByte[i + 2]) & mask) == 0) {
                        foundKeyByte[charsInLine + 1] = (byte) (textByte[i + 1] ^ space);
                    } else {
                        if (((textByte[i] ^ textByte[i + 1]) & mask) == 0 && ((textByte[i] ^ textByte[i + 2]) & mask) != 0) {
                            foundKeyByte[charsInLine + 2] = (byte) (textByte[i + 2] ^ space);
                        }
                    }
                }
            } catch (Exception e) {
            }
            charsInLine++;
            if (charsInLine % (LINE_LENGTH) == 0) {
                charsInLine = 0;
            }
        }
        String key = new String(foundKeyByte);
        System.out.println("FOUND KEY: " + key);
        byte[] keyByte = key.getBytes();
        byte[] decryptedByte = new byte[textByte.length];

        int charsInLine1 = 0;

        for (int i = 0; i < textByte.length; i++) {
            if (charsInLine1 % LINE_LENGTH == 0) {
                charsInLine1 = 0;
            }
            decryptedByte[i] = (byte) (keyByte[charsInLine1] ^ textByte[i]);
            charsInLine1++;
        }
        writeFile(DECRYPT, new String(decryptedByte));
    }

}
