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
        if (nextByte == ';') {
            endOfLine = nextByte;
            end = true;
        }
        if (opcodeSize == 2) {
            pushByte(nextByte);//the 3rd byte
            opcode = bytesToShort(new byte[]{bytes[0], bytes[1]});
            System.out.println((int) opcode + " op code");
            numberOfWords += 1;
        } else {
            if (nextByte == '\0') {
                pushByte(nextByte);
                numberOfWords += 1;
            } else
                pushByte(nextByte);
        }
        opcodeSize++;
        if (opcodeSize > 2 && numberOfWords == 1 && (opcode == 4 || opcode == 9)) {
            byte b = 0;
            pushByte(b);
            numberOfWords += 1;
        }

        if (opcode == 2 && numberOfWords == 3 && nextByte != '\0') captcha = true;
        if (end) {
            return (T)popString();
            /*switch (opcode) {
                case (1)://register
                    if (end) {
                        return (T) popString();
                    }
                    break;
                case (2)://login
                    if (end && captcha) {
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
                    break;*/

            //}
        }
        return null; //not a line yet
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);

        result += (short) (byteArr[1] & 0xff);

        return result;

    }


    @Override
    public byte[] encode(T message) throws Exception {//ACK 1
        System.out.println(message.toString());
        String[] splitBySpace = ((String) message).split(" ");
        System.out.println("**" + splitBySpace[0] + "**" + splitBySpace[1]);
        String opcode = splitBySpace[0];//ack
        String[] toBytes = new String[6];
        switch (opcode) {
            case "NOTIFICATION":
                toBytes[0] = "09";//NOTIFICATION
                toBytes[1] = splitBySpace[1];//Public/PM
                toBytes[2] = splitBySpace[2];//PostingUser
                toBytes[3] = "0";
                switch (splitBySpace[1]) {
                    case ("PM"):
                        toBytes[4] = ((String) message).substring(15);//Content
                        break;

                    case ("PUBLIC"):
                        toBytes[4] = ((String) message).substring(19);//Content
                        break;

                }
                toBytes[5] = "0";
                return bytes(toBytes);

            case "ACK":
                System.out.println("we in ack");
                toBytes[0] = "10";//ACK
                toBytes[1] = splitBySpace[1];//Message Opcode
                System.out.println("in ack"+Integer.parseInt(splitBySpace[1]));
                switch (Integer.parseInt(splitBySpace[1])) {
                    case (1 ): //Message Opcode//| 2 | 3 | 5 | 6
                        System.out.println("we in case one register");
                        return bytes1(toBytes);

                    case (4):
                        toBytes[2] = splitBySpace[3];//username;
                        toBytes[3] = "0";//end of byts
                        return bytes(toBytes);

                    case (7 | 8):
                        toBytes[2] = splitBySpace[2];//age
                        toBytes[3] = splitBySpace[3];//NumPosts
                        toBytes[4] = splitBySpace[4];//NumFollowers
                        toBytes[5] = splitBySpace[5];//NumFollowing
                        return bytes(toBytes);

                }

            case "ERROR": {
                toBytes[0] = "10";//ERROR
                toBytes[1] = splitBySpace[1];//Message Opcode
                return bytes(toBytes);
            }
        }
        throw new Exception("illegal");

    }

    private byte[] bytes1(String[] messageOutput) {
        System.out.println("we in bytes1");
        int length = 0;
        int counter = 0;
        int c = 0;
        byte[] outputMessageByt;
        for (String s : messageOutput) {
            if (s != null) {
                if (c > 1) {
                    byte[] outputMessageByte = s.getBytes();
                    length += outputMessageByte.length;
                } else c++;
            }
        }
        byte[] output = new byte[length + 4];
        System.out.println("output init"+messageOutput[1]);
        if (messageOutput[1].equals("1")) {
            c = 2;
        }
        for (String s : messageOutput) {
            if (s != null) {
                if (c == 0) {
                    System.out.println("check for shaun");
                    outputMessageByt = s.getBytes(StandardCharsets.UTF_8);
                } else {
                    outputMessageByt = shortToBytes((short) Integer.parseInt(s));
                    System.out.println(bytesToShort(outputMessageByt));
                    c--;
                }
                for (int i = 0; i < outputMessageByt.length; i++, counter++) {
                    output[counter] = outputMessageByt[i];
                }
            }
        }
        return output;
    }


    private byte[] bytes(String[] messageOutput) {
        int length = 0;
        int counter = 0;
        for (String s : messageOutput) {
            if (s != null) {
                byte[] outputMessageByte = s.getBytes();
                length += outputMessageByte.length;
            }
        }
        byte[] output = new byte[length];
        for (String s : messageOutput) {
            if (s != null) {
                byte[] outputMessageByte = s.getBytes();
                for (int i = 0; i < outputMessageByte.length; i++, counter++) {
                    output[counter] = outputMessageByte[i];
                }
            }
        }
        return output;
    }

    public byte[] shortToBytes(short num) {

        byte[] bytesArr = new byte[2];

        bytesArr[0] = (byte) ((num >> 8) & 0xFF);

        bytesArr[1] = (byte) (num & 0xFF);

        return bytesArr;

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
                if(i==2 && opcode == 4){
                    result += "0:";
                }else {
                    String accu = new String(bytes, start, i - start + 1, StandardCharsets.UTF_8);
                    System.out.println("accu: " + accu);
                    result = result + accu + ":";
                }
                start = i + 1;
            }
        }
        System.out.println(result);
        if(result.length()>=3)
            result = result.substring(0, result.length() - 2);

        //String result = new String(bytes, 2, len, StandardCharsets.UTF_8);
        if (opcode <= 9)
            result = "0" + opcode + result;
        else
            result = opcode + result;
        System.out.println(result);
        Arrays.fill(bytes, (byte) '\0');
        len = 0;
        captcha = false;
        numberOfWords = 0;
        opcodeSize = 0;
        this.end = false;
        return result;
    }

}
