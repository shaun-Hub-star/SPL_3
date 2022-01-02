package bgu.spl.net.api;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EncDec<T> implements MessageEncoderDecoder<T> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;//current word size. we use in the pop string method
    private int counter = 0;
    private int opcodeSize = 0;//indicates the number of bytes there are in the opcode array of bytes
    private short opcode = -1;
    private int numberOfWords = 0;
    private final byte SPACE_BYTE = 32;
    private boolean captcha = false;

    @Override
    public T decodeNextByte(byte nextByte) {
        if (opcodeSize == 2) {
            pushByte(nextByte);
            opcode = bytesToShort(new byte[]{bytes[0], bytes[1]});
            numberOfWords += 1;
        } else {
            pushByte(nextByte);
            opcodeSize++;
        }
        if (opcodeSize > 2 && numberOfWords == 1 && (opcode == 4 || opcode == 9)) {
            byte b = 0;
            pushByte(b);// push \0
            numberOfWords += 1;
        }
        if (nextByte == '\0') {
           // pushByte(SPACE_BYTE);
            numberOfWords += 1;
        }
        if (opcode == 2 && numberOfWords == 3 && nextByte != '\0') captcha = true;
        switch (opcode) {
            case (1)://register
                if (numberOfWords == 4) {
                    return (T) popString();
                }
                break;
            case (2)://login
                if (numberOfWords == 3 && captcha) {
                    return (T) popString();
                }
                break;
            case (3 | 7)://logout logstat
                if (numberOfWords == 1)
                    return (T) popString();
                break;
            case (4)://follow
                if (numberOfWords == 3)//opcode follow username \0
                    return (T) popString();
                break;
            case (5 | 8)://post | stat
                if (numberOfWords == 2)
                    return (T) popString();
                break;
            case (6 | 9)://pm
                if (numberOfWords == 4)
                    return (T) popString();
                break;

        }
        return null; //not a line yet
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);

        result += (short) (byteArr[1] & 0xff);

        return result;

    }

/*    public static void main(String[] args) {
        Charset charset = StandardCharsets.UTF_8;
        byte[] byteArrray = charset.encode(" ").array();
        for (byte b : byteArrray)
            System.out.print(b);
        System.out.println();
        byte byteArrray1 = 32;
        System.out.println(Character.toChars(byteArrray1));

    }*/

    public static byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    @Override
    public byte[] encode(T message) throws Exception {//ACK 1 optional
        String messageS = (String) message;
        short opcode = Short.parseShort(messageS.substring(0, messageS.indexOf(" ")));
        byte[] output;
        int length = 1;
        int counter;

        if (opcode == 9) {

            char notificationType = messageS.substring(messageS.indexOf(" ") + length).charAt(0);
            length++;//the notificationType is 0/1 - one step foreword
            String postingUser = messageS.substring(messageS.indexOf(" ") + length);
            byte[] postingUserBytes = postingUser.getBytes();
            length = length + postingUser.length() + 1;
            String Content = messageS.substring(messageS.indexOf(" ") + length);
            byte[] ContentByts = Content.getBytes();
            output = new byte[5 + postingUserBytes.length + ContentByts.length];
            String[] number = new String[]{"0", "9", Character.toString(notificationType)};
            this.counter = 3;
            initializationOpcode(number, output);
            String[] messageOutput = {postingUser, Content};
            bytes(messageOutput, output);
            output[output.length - 1] = 0;
            return output;
        } else if (opcode == 10) {//ACK 1-> 10 1
            char MessageOpcode = messageS.substring(messageS.indexOf(" ") + 1).charAt(1);//TODO cheack if charat 1/0
            String outputMessage = "";
            if (MessageOpcode == '1' | MessageOpcode == '2' | MessageOpcode == '3' | MessageOpcode == '5' | MessageOpcode == '6') {
                if (MessageOpcode == '1')
                    outputMessage = "successful register";
                else if (MessageOpcode == '2')
                    outputMessage = "successful Login";
                else if (MessageOpcode == '3')
                    outputMessage = "LOGOUT";
                else if (MessageOpcode == '5')
                    outputMessage = "successful POST Messages";
                else outputMessage = "successful PM Messages";
                output = toBytesOutput(MessageOpcode, outputMessage);
                return output;
            } else if (MessageOpcode == '4') {
                String[] messageOutput = new String[1];
                messageOutput[0] = messageS.substring(4, messageS.indexOf(" "));
                byte[] mesBackByte = messageOutput[0].getBytes();
                output = new byte[4 + mesBackByte.length];
                String[] number = new String[]{"1", "0", "0", "4"};
                this.counter = 4;
                initializationOpcode(number, output);
                bytes(messageOutput, output);
                output[output.length - 1] = Byte.parseByte("0");
                return output;
            } else if (MessageOpcode == '7' | MessageOpcode == '8') {
                String[] number = new String[4];
                if (MessageOpcode == '7') {
                    number = new String[]{"1", "0", "0", "7"};
                } else number = new String[]{"1", "0", "0", "8"};
                output = new byte[12];
                initializationOpcode(number, output);
                this.counter = 4;
                String[] Messages = new String[4];
                Messages[0] = messageS.substring(4, messageS.indexOf(" "));//age
                length = 4 + Messages[0].length() + 1;
                Messages[1] = messageS.substring(length, messageS.indexOf(" "));//numPost
                length += Messages[1].length() + 1;
                Messages[2] = messageS.substring(length, messageS.indexOf(" "));//NumFollowers
                length += Messages[2].length() + 1;
                Messages[3] = messageS.substring(length, messageS.indexOf(" "));//NumFollowing
                bytes(Messages, output);
                return output;
            }
        } else if (opcode == 11) {
            String mesBack = messageS.substring(messageS.indexOf(" ") + 1);
            byte[] mesBackByte = mesBack.getBytes();
            output = new byte[4 + mesBackByte.length];
            String[] number = new String[]{"1", "0", "1", "1"};
            return getBytes(output, mesBackByte, number);

        } else if (opcode == 12) {
            String[] messageOutput = new String[1];
            messageOutput[0] = messageS.substring(messageS.indexOf(" ") + 1);
            byte[] mesBackByte = messageOutput[0].getBytes();
            output = new byte[4 + mesBackByte.length];
            String[] number = new String[]{"1", "0", "1", "2"};
            return getBytes(output, mesBackByte, number);
        }
        throw new Exception("Illegal");
    }

    private byte[] getBytes(byte[] output, byte[] mesBackByte, String[] number) {
        int counter;
        initializationOpcode(number, output);
        counter = 4;
        for (int i = 0; i < mesBackByte.length; i++, counter++) {
            output[counter] = mesBackByte[i];
        }
        output[counter] = Byte.parseByte("0");
        return output;
    }

    private byte[] toBytesOutput(char MessageOpcode, String messageOutput) {
        byte[] outputMessageByte = messageOutput.getBytes();
        byte[] output = new byte[4 + outputMessageByte.length];
        output[0] = 1;
        output[1] = 0;
        output[2] = 0;
        output[3] = Byte.parseByte(Character.toString(MessageOpcode));
        this.counter = 4;
        for (int i = 0; i < outputMessageByte.length; i++, counter++) {
            output[counter] = outputMessageByte[i];
        }
        return output;
    }

    private byte[] initializationOpcode(String[] number, byte[] output) {
        for (int i = 0; i < number.length; i++) {
            output[i] = Byte.parseByte(number[i]);
        }
        return output;
    }

    private byte[] bytes(String[] messageOutput, byte[] output) {
        for (String s : messageOutput) {
            byte[] outputMessageByte = s.getBytes();
            for (int i = 0; i < outputMessageByte.length; i++, this.counter++) {
                output[counter] = outputMessageByte[i];
            }
        }
        return output;
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        captcha = false;
        numberOfWords = 0;
        return result;
        //return null;
    }
}
