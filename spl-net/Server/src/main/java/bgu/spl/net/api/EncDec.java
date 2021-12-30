package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EncDec<T> implements MessageEncoderDecoder<T>{
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    int counter;

    @Override
    public T decodeNextByte(byte nextByte) {
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison
        if (nextByte == '\n') {
            return popString();
        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(T message) throws Exception {
        String messageS=(String)message;
        short opcode = Short.parseShort(messageS.substring(0, messageS.indexOf(" ")));
        byte[] output;
        int length=1;
        int counter;

            if(opcode==9) {

                char notificationType = messageS.substring(messageS.indexOf(" ") + length).charAt(0);
                length++;//the notificationType is 0/1 - one step foreword
                String postingUser = messageS.substring(messageS.indexOf(" ") + length);
                byte[] postingUserBytes = postingUser.getBytes();
                length = length + postingUser.length() + 1;
                String Content = messageS.substring(messageS.indexOf(" ") + length);
                byte[] ContentByts = Content.getBytes();
                output = new byte[5 + postingUserBytes.length + ContentByts.length];
                String [] number=new String[]{"0","9",Character.toString(notificationType)};
                this.counter = 3;
                output=initializationOpcode(number,output);
                String[] messageOutput={postingUser,Content};
                output=bytes(messageOutput,output);
                output[output.length-1] = 0;
                return output;
            }


            else if(opcode==10)
            {
                char MessageOpcode=messageS.substring(messageS.indexOf(" ")+1).charAt(1);//TODO chck if charat 1/0
                String outputMessage="";
                if(MessageOpcode=='1'|MessageOpcode=='2'|MessageOpcode=='3'|MessageOpcode=='5'|MessageOpcode=='6'){
                if(MessageOpcode=='1')
                    outputMessage="successful register";
                else if(MessageOpcode=='2')
                    outputMessage="successful Login";
                else if(MessageOpcode=='3')
                    outputMessage="LOGOUT";
                else if(MessageOpcode=='5')
                    outputMessage="successful POST Messages";
                else if(MessageOpcode=='6')
                    outputMessage="successful PM Messages";
                output=toByetsOutput(MessageOpcode,outputMessage);
                return output;}

                else if(MessageOpcode=='4'){
                    String[] messageOutput=new String[1];
                    messageOutput[0]=messageS.substring(4, messageS.indexOf(" "));
                    byte[] mesBackByte = messageOutput[0].getBytes();
                    output=new byte[4+mesBackByte.length];
                    String [] number=new String[]{"1","0","0","4"};
                    this.counter=4;
                    output=initializationOpcode(number,output);
                    output=bytes(messageOutput,output);
                    output[output.length-1]=Byte.parseByte("0");
                    return output;
                }
                else if(MessageOpcode=='7'|MessageOpcode=='8'){
                    String [] number=new String[4];
                    if(MessageOpcode=='7'){
                        number= new String[]{"1", "0", "0", "7"};}
                    else   number = new String[]{"1", "0", "0", "8"};
                    output=new byte[12];
                    output=initializationOpcode(number,output);
                    this.counter=4;
                    String[] Messages= new String[4];
                    Messages[0]=messageS.substring(4, messageS.indexOf(" "));//age
                    length=4+Messages[0].length()+1;
                    Messages[1]=messageS.substring(length, messageS.indexOf(" "));//numPost
                    length+=Messages[1].length()+1;
                    Messages[2]=messageS.substring(length, messageS.indexOf(" "));//NumFollowers
                    length+=Messages[2].length()+1;
                    Messages[3]=messageS.substring(length, messageS.indexOf(" "));//NumFollowing
                    output= bytes(Messages,output);
                    return  output;
                }
            }
            else if(opcode==11){
                String mesBack=messageS.substring(messageS.indexOf(" ")+1);
                byte[] mesBackByte = mesBack.getBytes();
                output=new byte[4+mesBackByte.length];
                String [] number=new String[]{"1","0","1","1"};
                output=initializationOpcode(number,output);
                counter=4;
                for (int i =0;i<mesBackByte.length;i++,counter++)
                {
                    output[counter]=mesBackByte[i];
                }
                output[counter]=Byte.parseByte("0");
                return output;

            }

            else if(opcode==12){
                String[] messageOutput=new String[1];
                messageOutput[0]=messageS.substring(messageS.indexOf(" ")+1);
                byte[] mesBackByte = messageOutput[0].getBytes();
                output=new byte[4+mesBackByte.length];
                String [] number=new String[]{"1","0","1","2"};
                output=initializationOpcode(number,output);
                counter=4;
                for (int i =0;i<mesBackByte.length;i++,counter++)
                {
                    output[counter]=mesBackByte[i];
                }
                output[counter]=Byte.parseByte("0");
                return output;
            }
            throw new Exception("Illegal");
    }

    private byte[] toByetsOutput(char MessageOpcode,String messageOutput) {
        byte[] outputMessageByte = messageOutput.getBytes();
        byte[]output = new byte[4 + outputMessageByte.length];
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

    private byte[] initializationOpcode(String [] number,byte[] output){
        for (int i=0;i<number.length;i++){
            output[i]=Byte.parseByte(number[i]);
        }
        return  output;
    }

    private byte[] bytes(String[] messageOutput,byte[] output)
    {
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

    private T popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        //return result;
        return null;
    }
}
