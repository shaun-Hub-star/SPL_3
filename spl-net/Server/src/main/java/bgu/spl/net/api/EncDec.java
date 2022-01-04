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

    @Override
    public T decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
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
            pushByte(b);
            numberOfWords += 1;
        }
        if (nextByte == '\0') {
            //pushByte(SPACE_BYTE);
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


    @Override
    public byte[] encode(T message) throws Exception {

        String[] splitBySpace = ((String) message).split(" ");
        String opcode = splitBySpace[0];
        byte[] output;
        int length = 1;
        int counter;
        String[] tobytes = new String[6];


        switch (opcode) {
            case "NOTIFICATION": {
                tobytes[0] = "09";//NOTIFICATION
                tobytes[1] = splitBySpace[1];//Public/PM
                tobytes[2] = splitBySpace[2];//PostingUser
                tobytes[3] = "0";
                switch (splitBySpace[1]) {
                    case ("PM"): {

                        tobytes[4] = ((String) message).substring(15);//Content
                    }
                    case ("PUBLIC"): {
                        tobytes[4] = ((String) message).substring(19);//Content

                    }

                }
                tobytes[5] = "0";
                return bytes(tobytes);
            }


            case "ACK":
                tobytes[0] = "10";//ACK
                tobytes[1] = splitBySpace[1];//Message Opcode
                switch (Integer.parseInt(splitBySpace[1])) {
                    case (1 | 2 | 3 | 5 | 6): {//Message Opcode
                        return bytes(tobytes);
                    }
                    case (4): {
                        tobytes[2] = splitBySpace[3];//username;
                        tobytes[3] = "0";//end of byts
                        return bytes(tobytes);
                    }
                    case (7 | 8): {
                        tobytes[2] = splitBySpace[2];//age
                        tobytes[3] = splitBySpace[3];//NumPosts
                        tobytes[4] = splitBySpace[4];//NumFollowers
                        tobytes[5] = splitBySpace[5];//NumFollowing
                        return bytes(tobytes);
                    }
                }

             case "ERROR": {
                 tobytes[0] = "10";//ERROR
                 tobytes[1]=splitBySpace[1];//Message Opcode
                 return bytes(tobytes);
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
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        captcha = false;
        numberOfWords = 0;
        return result;
    }

}
