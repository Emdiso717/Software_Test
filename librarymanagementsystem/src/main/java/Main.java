import utils.ConnectConfig;
import utils.DatabaseConnector;
import entities.Book;
import queries.BookQueryConditions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import com.sun.net.httpserver.*;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/card", new handle.CardHandler());
            server.createContext("/borrow", new handle.BorrowHandler());
            server.createContext("/book", new handle.BookHandler());
            System.out.println("Server is listening on port 8000");
            server.start();
            System.out.println("Server is listening on port 8000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
