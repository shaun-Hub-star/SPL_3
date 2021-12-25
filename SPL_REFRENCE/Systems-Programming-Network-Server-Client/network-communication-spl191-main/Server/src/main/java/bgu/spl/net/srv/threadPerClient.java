package bgu.spl.net.srv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.api.bidi.EncDec;
import bgu.spl.net.api.bidi.clientProtocol;
import bgu.spl.net.impl.echo.EchoProtocol;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;

public class threadPerClient<T>{

    private final int port;
    private final Supplier<clientProtocol> protocolFactory;
    private final Supplier<EncDec> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl<T> connections = new ConnectionsImpl<T>();

    public threadPerClient(
            int port,
            Supplier<clientProtocol> protocolFactory,
            Supplier<EncDec> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.sock = null;
    }


    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");

            this.sock = serverSock;

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler(
                        clientSock,
                        encdecFactory.get(),
                         protocolFactory.get());

                handler.getProtocol().start(connections.addClient(handler), connections);
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }


    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    protected void execute(BlockingConnectionHandler<T>  handler){
        new Thread(handler).start();
    }

}
