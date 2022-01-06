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

            numberOfWords += 1;
        } else {
            if (nextByte == '\0') {
                pushByte(nextByte);
                numberOfWords += 1;
            } else
                pushByte(nextByte);
        }
        opcodeSize++;
        if (opcodeSize > 2 &&numberOfWords==2&& (opcode == 4 || opcode == 9)) {
            System.out.println(bytes[0]+" "+bytes[1]+" "+bytes[2]+" "+bytes[3]+"******** ");
            byte b = 0;
            pushByte(b);
            numberOfWords += 1;
        }

        if (opcode == 2 && numberOfWords == 3 && nextByte != '\0') captcha = true;
        if (end) {
            return (T) popString();
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
        String opcode = splitBySpace[0];//ack
        String[] toBytes = new String[6];
        switch (opcode) {
            case "NOTIFICATION":
                toBytes[0] = "9"; //NOTIFICATION
                if (splitBySpace[1].equals("PUBLIC")) {
                    toBytes[1] = "1";
                } else {
                    toBytes[1] = "0";
                }
                //toBytes[1] = splitBySpace[1]; //Public/PM
                toBytes[2] = splitBySpace[2]; //PostingUser
                toBytes[3] = "\0";

                String content = "";
                for (int i = 3; i < splitBySpace.length; i++) {
                    content += " " + splitBySpace[i];
                }

                toBytes[4] = content;
                toBytes[5] = "\0";
                return bytes1(toBytes);

            case "ACK":
                toBytes[0] = "10";//ACK
                toBytes[1] = splitBySpace[1];//Message Opcode
                System.out.println(" we in ack" + Integer.parseInt(splitBySpace[1]));
                switch (Integer.parseInt(splitBySpace[1])) {
                    case (1): //Message Opcode//| 2 | 3 | 5 | 6
                        System.out.println("we in case one register");
                        return bytes1(toBytes);
                    case (12): //Message Opcode//| 2 | 3 | 5 | 6
                        System.out.println("we in case block");
                        return bytes1(toBytes);
                    case (2):
                        System.out.println("we in case two login");
                        return bytes1(toBytes);
                    case (3):
                        System.out.println("we in case three logout");
                        return bytes1(toBytes);
                    case (4):
                        // System.out.println("we in case four follow");
                        toBytes[2] = splitBySpace[3];//username;
                        toBytes[3] = "\0";//end of byts
                        return bytes1(toBytes);

                    case (5):
                        System.out.println("we in case five Post");
                        return bytes1(toBytes);
                    case (6):
                        System.out.println("we in case six PM");
                        return bytes1(toBytes);

                    case (7):
                        System.out.println("we in case 7 PM");
                        toBytes[2] = splitBySpace[2];//age
                        toBytes[3] = splitBySpace[3];//NumPosts
                        toBytes[4] = splitBySpace[4];//NumFollowers
                        toBytes[5] = splitBySpace[5];//NumFollowing
                        System.out.println(toBytes[2] + " " + toBytes[3] + " " + toBytes[4] + " " + toBytes[5] + "After case 7 ");
                        return bytes1(toBytes);

                    case (8):
                        System.out.println("we in case 8 PM");
                        toBytes[2] = splitBySpace[2];//age
                        toBytes[3] = splitBySpace[3];//NumPosts
                        toBytes[4] = splitBySpace[4];//NumFollowers
                        toBytes[5] = splitBySpace[5];//NumFollowing
                        return bytes1(toBytes);

                }

            case "ERROR": {
                toBytes[0] = "11";//ERROR
                toBytes[1] = splitBySpace[1];//Message Opcode
                return bytes1(toBytes);
            }
        }
        throw new Exception("illegal");

    }

    private byte[] bytes1(String[] messageOutput) {
        System.out.println("we in bytes1");
        System.out.println("output init" + messageOutput[1] + " output l= " + getOutputLength(messageOutput));
        return getOutput(messageOutput);
    }

    public int getOutputLength(String[] messageOutput) {
        int c;
        if (messageOutput[0].equals("10") || messageOutput[0].equals("11")) {
            c = getC(messageOutput[1]);
        } else {
            c = 2;//9
        }

        int length = c * 2;

        if (messageOutput[1].equals("8") || messageOutput[1].equals("7"))
            return length;
        for (String s : messageOutput) {//take the size
            if (s != null) {
                if (s.equals("\0")) {
                    length = length + 1;
                } else {
                    if (c <= 0) {
                        byte[] outputMessageByte = s.getBytes();
                        //c--;
                        length += outputMessageByte.length;

                    } else {
                        c = c - 1;
                    }
                }
            }
        }

        return length;
    }

    public byte[] getOutput(String[] messageOutput) {
        byte[] output = new byte[getOutputLength(messageOutput)];
        byte[] outputMessageByte;
        for (String ssss : messageOutput) {
            System.out.println(ssss);
        }
        int counter = 0;
        int c;
        if (messageOutput[0].equals("10") || messageOutput[0].equals("11")) {
            c = getC(messageOutput[1]);
        } else {
            c = 2;//9
        }
        for (String s : messageOutput) {
            if (s != null) {
                if (s.equals("\0")) {
                    System.out.println("getOUtput-i am \0");
                    //outputMessageByte = shortToBytes((short) 0);

                    System.out.println("inside the loop for all the \0");
                    output[counter] = '\0';
                    System.out.println("counter " + counter + " " + output[counter]);


                } else {

                    if (c <= 0) {//String
                        outputMessageByte = s.getBytes(StandardCharsets.UTF_8);//if we have messages
                        System.out.println("getOUtput--" + s);

                        c--;
                        /*if (messageOutput[1].equals("4"))
                            c = c + 2;
                        if (messageOutput[1].equals("9") & c == -2)
                            c = c + 3;*/
                    } else {//short
                        System.out.println("getOUtput**" + s);
                        outputMessageByte = shortToBytes((short) Integer.parseInt(s));
                        System.out.println("getOUtput**" + s + " " + outputMessageByte.length);
                        c--;
                    }
                    for (int i = 0; i < outputMessageByte.length; i++, counter++) {
                        output[counter] = outputMessageByte[i];
                        System.out.println("counter " + counter + " " + output[counter]);
                    }
                }
            }
        }
        return output;

    }


    public int getC(String messageOutput) {
        if (messageOutput.equals("1") | messageOutput.equals("2") | messageOutput.equals("3") |
                messageOutput.equals("5") | messageOutput.equals("6") | messageOutput.equals("4") |
                messageOutput.equals("11") | messageOutput.equals("12")) {
            return 2;
        } else if (messageOutput.equals("7") | messageOutput.equals("8"))
            return 6;
        else if (messageOutput.equals("9"))
            return 1;
        return 444444;//TODO

    }

    /*

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
    */
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
                if (i == 2 && opcode == 4) {
                    result += "0:";
                } else {
                    String accu = new String(bytes, start, i - start, StandardCharsets.UTF_8);
                    result = result + accu + ":";
                }
                start = i + 1;
            }
            else if(i==2 && opcode == 4 && bytes[2] == '1'){
                result += "1";
                start = i+1;
            }
        }
        System.out.println(result);
        if (result.length() >= 3)
            result = result.substring(0, result.length() - 1);
        System.out.println(result + "check");
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
