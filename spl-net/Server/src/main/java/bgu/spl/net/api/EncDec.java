package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.lang.Integer;

public class EncDec<T> implements MessageEncoderDecoder<T> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;//current word size. we use in the pop string method
    private int counter = 0;
    private int opcodeSize = 0;//indicates the number of bytes there are in the opcode array of bytes
    private short opcode = -1;
    private int numberOfWords = 0;
    private boolean captcha = false;
    private byte endOfLine;
    private boolean end = false;
    @Override
    public T decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if(nextByte == ';'){
            endOfLine = nextByte;
            end = true;
        }
        if (opcodeSize == 2) {
            pushByte(nextByte);//the 3rd byte
            opcode = bytesToShort(new byte[]{bytes[0], bytes[1]});
            System.out.println((int) opcode + " op code");
            numberOfWords += 1;
            opcodeSize++;
        } else {
            if (nextByte == '\0') {
                pushByte(nextByte);
                numberOfWords += 1;
            } else
                pushByte(nextByte);
            opcodeSize++;
        }
        if (opcodeSize > 2 && numberOfWords == 1 && (opcode == 4 || opcode == 9)) {
            byte b = 0;
            pushByte(b);
            numberOfWords += 1;
        }

        if (opcode == 2 && numberOfWords == 3 && nextByte != '\0') captcha = true;
        if (opcodeSize > 2) {
            switch (opcode) {
                case (1)://register
                    if (end) {
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
        }
        return null; //not a line yet
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);

        result += (short) (byteArr[1] & 0xff);

        return result;

    }


    @Override
    public byte[] encode(T message) throws Exception {

        String[] splitBySpace = ((String) message).split(" ");
        String opcode = splitBySpace[0];
        String[] toBytes = new String[6];
        switch (opcode) {
            case "NOTIFICATION": {
                toBytes[0] = "09";//NOTIFICATION
                toBytes[1] = splitBySpace[1];//Public/PM
                toBytes[2] = splitBySpace[2];//PostingUser
                toBytes[3] = "0";
                switch (splitBySpace[1]) {
                    case ("PM"): {
                        toBytes[4] = ((String) message).substring(15);//Content
                    }
                    case ("PUBLIC"): {
                        toBytes[4] = ((String) message).substring(19);//Content
                    }
                }
                toBytes[5] = "0";
                return bytes(toBytes);
            }


            case "ACK":
                toBytes[0] = "10";//ACK
                toBytes[1] = splitBySpace[1];//Message Opcode
                switch (Integer.parseInt(splitBySpace[1])) {
                    case (1 | 2 | 3 | 5 | 6): {//Message Opcode
                        return bytes(toBytes);
                    }
                    case (4): {
                        toBytes[2] = splitBySpace[3];//username;
                        toBytes[3] = "0";//end of byts
                        return bytes(toBytes);
                    }
                    case (7 | 8): {
                        toBytes[2] = splitBySpace[2];//age
                        toBytes[3] = splitBySpace[3];//NumPosts
                        toBytes[4] = splitBySpace[4];//NumFollowers
                        toBytes[5] = splitBySpace[5];//NumFollowing
                        return bytes(toBytes);
                    }
                }

            case "ERROR": {
                toBytes[0] = "10";//ERROR
                toBytes[1] = splitBySpace[1];//Message Opcode
                return bytes(toBytes);
            }
        }
        throw new Exception("illegal");

    }


    private byte[] bytes(String[] messageOutput) {
        int length = 0;
        int counter = 0;
        for (String s : messageOutput) {
            byte[] outputMessageByte = s.getBytes();
            length += outputMessageByte.length;
        }
        byte[] output = new byte[length];
        for (String s : messageOutput) {
            byte[] outputMessageByte = s.getBytes();
            for (int i = 0; i < outputMessageByte.length; i++, counter++) {
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
        String result = "";
        int start = 2;
        for (byte aByte : bytes) System.out.print(" " + aByte);
        for (int i = 2; i < bytes.length; i++) {
            if (bytes[i] == ';') break;
            if (bytes[i] == '\0') {
                String accu = new String(bytes, start, i-start+1, StandardCharsets.UTF_8);
                System.out.println("accu: "+accu);
                result = result + accu + ":";
                start = i+1;
            }
        }
        result = result.substring(0,result.length()-2);
        //String result = new String(bytes, 2, len, StandardCharsets.UTF_8);
        if (opcode <= 9)
            result = "0" + opcode + result;
        else
            result = opcode + result;
        System.out.println(result);
        len = 0;
        captcha = false;
        numberOfWords = 0;
        opcodeSize = 0;
        this.end = false;
        return result;
    }

}
