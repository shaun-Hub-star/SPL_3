package bgu.spl.net.api.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;

import javax.xml.bind.SchemaOutputResolver;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EncDec implements MessageEncoderDecoder<String> {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private int counter = 0;
  private short ops = 0;
  private short num=0;
    @Override
    public String decodeNextByte(byte nextByte) {

        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts
        //this allow us to do the following comparison

        System.out.println(nextByte+" DEC");

        if (nextByte == '\0') {
            counter++;
        }


        if (len==2){
            byte[] b= new byte[2];
            b[0]=bytes[0];
            b[1]=bytes[1];
           ops=bytesToShort(b);
        }

        if(ops==1) {
            if (counter == 3) {
                String s = popString();
                return s;
            }
        }
        if(ops==2){
            if (counter == 3) {
                String s = popString();
                return s;
            }
        }

        if(ops==4){
            num=0;
            if(len>=5) {
                byte[] b5 = new byte[2];

                b5[0] = bytes[3];
                b5[1] = bytes[4];
                num = bytesToShort(b5);

                if (bytes[2] == 0)
                    if (counter > 0 & counter == num + 3) {
                        String s = popFollowString();
                        return s;
                    }
                if (bytes[2] == 1)
                    if (counter > 0 & counter == num + 2) {
                        String s = popFollowString();
                        return s;
                    }
            }
        }
        if(ops==5){
            if(counter==2){
                String s = popString();
                return s;
            }
        }
        if(ops==6){
            if(counter==3){
                String s = popString();
                return s;
            }
        }

        if(ops==8){
            if(counter==2){
                String s = popString();
                return s;
            }
        }




        pushByte(nextByte);
        if(len>1) {
            if (bytes[1] == 3) {
                if (counter == 1) {
                    String s = popString();
                    return "3";
                }
            }
            if (bytes[1] == 7) {
                if (counter == 1) {
                    String s = popString();

                    return "7";
                }
            }
        }
        return null; //not a line yet
    }

    @Override
    public byte[] encode(String message) {

        byte[] errBytes;
        short sh=0;
        byte[] toReturn=null;
        byte[] numOfUsers;
        short j = Short.parseShort(message.substring(0, message.indexOf(" ")));
        byte[] towBytes = shortToBytes(j);
        byte[] listofusers ;
        byte[] numoffollweres=null;
        byte[] numoffollowing = null;
        if(j==11){
            toReturn=new byte[4];
            sh=Short.parseShort(message.substring(message.indexOf(" ")+1));
            errBytes=shortToBytes(sh);
            toReturn[0]=towBytes[0];
            toReturn[1]=towBytes[1];
            toReturn[2]=errBytes[0];
            toReturn[3]=errBytes[1];
        }

        else {
            if (j == 10) {

                String s=message.substring(message.indexOf(" ")+1);

                if(s.charAt(0)=='4'){
                    towBytes = shortToBytes(j);//ack opcode
                    sh=Short.parseShort(s.substring(0,s.indexOf(" ")));//follow upcode
                    errBytes=shortToBytes(sh);//bytes of the follow opcode
                    s = s.substring(s.indexOf(' ')+1);
                    short numberofusers = Short.parseShort(s.substring(0,s.indexOf(' ')));
                    numOfUsers=shortToBytes(numberofusers);
                    s= s.substring(s.indexOf(' ')+1);
                    listofusers = s.getBytes();
                    toReturn=new byte[6+listofusers.length];
                    toReturn[0]=towBytes[0];
                    toReturn[1]=towBytes[1];
                    toReturn[2]=errBytes[0];
                    toReturn[3]=errBytes[1];
                    toReturn[4]=numOfUsers[0];
                    toReturn[5]=numOfUsers[1];
                    for(int i=6;i<toReturn.length;i++)
                        toReturn[i]=listofusers[i-6];
                }
                else if(s.charAt(0)=='7'){
                    towBytes = shortToBytes(j);//ack opcode
                    sh=Short.parseShort(s.substring(0,s.indexOf(" ")));//userlistopcode upcode
                    errBytes=shortToBytes(sh);//bytes of the userlistopcode opcode
                    s=s.substring(s.indexOf(" ")+1);
                    short numberofusers = Short.parseShort(s.substring(0,s.indexOf(' ')));
                    numOfUsers=shortToBytes(numberofusers);
                    s= s.substring(s.indexOf(' ')+1);
                    listofusers = s.getBytes();
                    toReturn=new byte[6+listofusers.length];
                    toReturn[0]=towBytes[0];
                    toReturn[1]=towBytes[1];
                    toReturn[2]=errBytes[0];
                    toReturn[3]=errBytes[1];
                    toReturn[4]=numOfUsers[0];
                    toReturn[5]=numOfUsers[1];
                    for(int i=6;i<toReturn.length;i++)
                        toReturn[i]=listofusers[i-6];
                }
                else if(s.charAt(0)=='8'){
                    towBytes = shortToBytes(j);//ack opcode
                    sh=Short.parseShort(s.substring(0,s.indexOf(" ")));//stat opcode
                    errBytes=shortToBytes(sh);//bytes of the stat opcode
                    s = s.substring(s.indexOf(' ')+1);
                    System.out.println(s+" hereg");
                    short numofposts = Short.parseShort(s.substring(0,s.indexOf(' ')));
                    numOfUsers=shortToBytes(numofposts);//number of posts in bytes
                    s = s.substring(s.indexOf(' ')+1);
                    short numoffollowers = Short.parseShort(s.substring(0,s.indexOf(' ')));
                    numoffollweres =shortToBytes(numoffollowers);//number of followers in bytes
                    s = s.substring(s.indexOf(' ')+1);
                    short numoffollowings = Short.parseShort(s);
                    System.out.println(numoffollowings+" numoffollowings");
                    numoffollowing = shortToBytes(numoffollowings);//num of following
                    toReturn=new byte[10];
                    toReturn[0]=towBytes[0];
                    toReturn[1]=towBytes[1];
                    toReturn[2]=errBytes[0];
                    toReturn[3]=errBytes[1];
                    toReturn[4]=numOfUsers[0];
                    toReturn[5]=numOfUsers[1];
                    toReturn[6]=numoffollweres[0];
                    toReturn[7]=numoffollweres[1];
                    toReturn[8]=numoffollowing[0];
                    toReturn[9]=numoffollowing[1];



                }
                else if(s.charAt(0)=='1' || s.charAt(0)=='2' || s.charAt(0)=='3' ||s.charAt(0)=='5'||s.charAt(0)=='6'){
                    sh=Short.parseShort(message.substring(message.indexOf(" ")+1));
                    errBytes=shortToBytes(sh);
                    toReturn=new byte[4];
                    toReturn[0]=towBytes[0];
                    toReturn[1]=towBytes[1];
                    toReturn[2]=errBytes[0];
                    toReturn[3]=errBytes[1];
                }
            }
        }


        if(j==9){
            String s=message.substring(message.indexOf(" ")+1);
            short type = Short.parseShort(s.substring(0,1));
            towBytes = shortToBytes(j);//ack opcode
            byte typeofnoti;
            if(type==0){
                typeofnoti=0;
            }else {
                    typeofnoti=1;
            }
            s=s.substring(s.indexOf(" ")+1);
            String postingUser=s.substring(0,s.indexOf(" "));
            System.out.println(postingUser+" hhhhhhhhhhhhhhhhhhhhhhhhh postin g user");
            byte[] postUser=postingUser.getBytes();
            s=s.substring(s.indexOf(" "),s.length());
            String content=s;
            System.out.println(content+" hhhhhhhhhhhhhhhhhhhhhhhhh postin g constent");
            byte[] contents=content.getBytes();
            toReturn= new byte[5+postingUser.length()+contents.length];
            toReturn[0]=towBytes[0];
            toReturn[1]=towBytes[1];
            toReturn[2]=typeofnoti;
            for(int i=0;i<postUser.length;i++) {
                toReturn[i + 3] = postUser[i];

            }
            toReturn[postUser.length+3]=0;
            int f=postUser.length+4;
            for(int i=0;i<contents.length;i++)
                toReturn[i+f]=contents[i];

            toReturn[toReturn.length-1]=0;
        }


        return toReturn; //uses utf8 by default
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popFollowString(){
        byte[] b= new byte[2];
        String result= "" ;
        byte[] b1;
        b[0]=bytes[0];
        b[1]=bytes[1];
        short s=bytesToShort(b);
        char c=(char) bytes[2];
        b[0]=bytes[3];
        b[1]=bytes[4];
        short num=bytesToShort(b);
        b1 = Arrays.copyOfRange(bytes,5,bytes.length);
        result =   new String(b1, 0, len-5, StandardCharsets.UTF_8);

        len = 0;
        counter=0;
        return s+" "+bytes[2]+" "+num+" "+result;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        byte[] b= new byte[2];
        String result= "" ;
        byte[] b1= new byte[bytes.length-2];
        b[0]=bytes[0];
        b[1]=bytes[1];
        short s=bytesToShort(b);
        b1 = Arrays.copyOfRange(bytes,2,bytes.length);

        result =   new String(b1, 0, len-1, StandardCharsets.UTF_8);

        len = 0;
        counter=0;
        return s+" "+result;
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);

        result += (short)(byteArr[1] & 0xff);

        return result;

    }

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }


}
