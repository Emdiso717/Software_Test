import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sun.net.httpserver.*;
import java.lang.String;

import entities.Book;
import entities.Borrow;
import entities.Book.SortColumn;
import entities.Card;
import entities.Card.CardType;
import utils.ConnectConfig;
import queries.ApiResult;
import queries.BookQueryConditions;
import queries.BorrowHistories;
import queries.CardList;
import queries.*;
import utils.DatabaseConnector;

public class handle {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static DatabaseConnector connector;
    private static LibraryManagementSystem library;
    private static ConnectConfig connectConfig = null;

    static public void connect() {
        try {
            connectConfig = new ConnectConfig();
            log.info("Success to parse connect config. " + connectConfig.toString());
            // connect to database
            connector = new DatabaseConnector(connectConfig);
            boolean connStatus = connector.connect();
            if (!connStatus) {
                log.severe("Failed to connect database.");
                System.exit(1);
            }
            library = new LibraryManagementSystemImpl(connector);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void release_connect() {
        if (connector.release()) {
            log.info("Success to release connection.");
        } else {
            log.warning("Failed to release connection.");
        }
    }

    static class BookHandler implements HttpHandler {
        private void handleDeleteRequest(HttpExchange exchange) throws IOException {
            String k = exchange.getRequestURI().toString();
            int bookid = Integer.parseInt(k.substring(k.indexOf("=") + 1));
            System.out.println(bookid);
            connect();
            ApiResult result = library.removeBook(bookid);
            release_connect();
            if (result.ok) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write("success".getBytes());
                outputStream.close();
            } else {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.message.getBytes());
                outputStream.close();
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            String k = exchange.getRequestURI().toString();
            if (k.charAt(6) == 'q') {
                BookQueryConditions qb = new BookQueryConditions();
                if (k.indexOf("category=") + "category=".length() != k.indexOf("&title=")) {
                    qb.setCategory((k.substring(k.indexOf("category=") + "category=".length(),
                            k.indexOf("&title=")).replace('+', ' ')));
                }
                if (k.indexOf("title=") + "title=".length() != k.indexOf("&press=")) {
                    qb.setTitle((k.substring(k.indexOf("title=") + "title=".length(), k.indexOf("&press=")).replace('+',
                            ' ')));
                }
                if (k.indexOf("press=") + "press=".length() != k.indexOf("&minpubilshyear=")) {
                    qb.setPress((k.substring(k.indexOf("press=") + "press=".length(), k.indexOf("&minpubilshyear="))
                            .replace('+',
                                    ' ')));
                }
                if (k.indexOf("minpubilshyear=") + "minpubilshyear=".length() != k.indexOf("&maxpubilshyear=")) {
                    qb.setMinPublishYear(1 + Integer
                            .parseInt(k.substring(k.indexOf("minpubilshyear=") + "minpubilshyear=".length(),
                                    k.indexOf("minpubilshyear=") + "minpubilshyear=".length() + 4)));
                }
                if (k.indexOf("maxpubilshyear=") + "maxpubilshyear=".length() != k.indexOf("&author=")) {
                    qb.setMaxPublishYear(1 + Integer
                            .parseInt(k.substring(k.indexOf("maxpubilshyear=") + "maxpubilshyear=".length(),
                                    k.indexOf("maxpubilshyear=") + "maxpubilshyear=".length() + 4)));
                }
                if (k.indexOf("author=") + "author=".length() != k.indexOf("&minprice=")) {
                    qb.setAuthor((k.substring(k.indexOf("author=") + "author=".length(), k.indexOf("&minprice="))
                            .replace('+',
                                    ' ')));
                }
                if (k.indexOf("minprice=") + "minprice=".length() != k.indexOf("&maxprice=")) {
                    qb.setMinPrice(Double.parseDouble(
                            k.substring(k.indexOf("minprice=") + "minprice=".length(), k.indexOf("&maxprice="))));
                }
                if (k.indexOf("maxprice=") + "maxprice=".length() != k.indexOf("&sortby=")) {
                    qb.setMaxPrice(Double.parseDouble(
                            k.substring(k.indexOf("maxprice=") + "maxprice=".length(), k.indexOf("&sortby="))));
                }
                if (k.indexOf("sortby=") + "sortby=".length() != k.indexOf("&sortorder=")) {
                    for (SortColumn scl : SortColumn.values()) {
                        if (scl.getValue().equals(
                                k.substring(k.indexOf("sortby=") + "sortby=".length(), k.indexOf("&sortorder=")))) {
                            qb.setSortBy(scl);
                        }
                    }

                }
                if (k.indexOf("&sortorder=") + "&sortorder=".length() != k.length()) {
                    for (queries.SortOrder so : queries.SortOrder.values()) {
                        if (so.getValue().equals(k.substring(k.indexOf("&sortorder=") + "&sortorder=".length()))) {
                            qb.setSortOrder(so);
                        }
                    }
                }
                System.out.println(qb.getSortOrder());
                connect();
                ApiResult result = library.queryBook(qb);
                BookQueryResults bqr = (BookQueryResults) result.payload;
                String respones = "[";
                if (bqr.getCount() > 0) {
                    respones = respones + "{\"id\": \"" + Integer.toString(bqr.getResults().get(0).getBookId())
                            + "\", \"category\": \"" + bqr.getResults().get(0).getCategory()
                            + "\", \"title\": \"" + bqr.getResults().get(0).getTitle()
                            + "\", \"press\": \"" + bqr.getResults().get(0).getPress()
                            + "\", \"publishyear\": \"" + Integer.toString(bqr.getResults().get(0).getPublishYear())
                            + "\", \"author\": \"" + bqr.getResults().get(0).getAuthor()
                            + "\", \"price\": \"" + Double.toString(bqr.getResults().get(0).getPrice())
                            + "\", \"stock\": \"" + Integer.toString(bqr.getResults().get(0).getStock()) + "\"}";
                }
                for (int i = 1; i < bqr.getCount(); i++) {
                    respones = respones + ",{\"id\": \"" + Integer.toString(bqr.getResults().get(i).getBookId())
                            + "\", \"category\": \"" + bqr.getResults().get(i).getCategory()
                            + "\", \"title\": \"" + bqr.getResults().get(i).getTitle()
                            + "\", \"press\": \"" + bqr.getResults().get(i).getPress()
                            + "\", \"publishyear\": \"" + Integer.toString(bqr.getResults().get(i).getPublishYear())
                            + "\", \"author\": \"" + bqr.getResults().get(i).getAuthor()
                            + "\", \"price\": \"" + Double.toString(bqr.getResults().get(i).getPrice())
                            + "\", \"stock\": \"" + Integer.toString(bqr.getResults().get(i).getStock()) + "\"}";
                }
                respones = respones + "]";
                release_connect();
                outputStream.write(respones.getBytes());
                outputStream.close();
            }
        }

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // 读取POST请求体
            InputStream requestBody = exchange.getRequestBody();
            // 用这个请求体（输入流）构造个buffered reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            // 拼字符串的
            StringBuilder requestBodyBuilder = new StringBuilder();
            // 用来读的
            String line;
            String chose = exchange.getRequestURI().toString();
            // 没读完，一直读，拼到string builder里
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String k = requestBodyBuilder.toString();
            System.out.println(k);
            if (chose.charAt(6) == 'm') {
                Book modifBook = new Book();
                modifBook.setBookId(Integer.parseInt(k.substring(7, k.indexOf("\",\"category\":"))));
                modifBook.setCategory(k.substring(k.indexOf(",\"category\":\"") + ",\"category\":\"".length(),
                        k.indexOf("\",\"title\":")));
                modifBook.setTitle(k.substring(k.indexOf(",\"title\":\"") + ",\"title\":\"".length(),
                        k.indexOf("\",\"press\":")));
                modifBook.setPress(k.substring(k.indexOf(",\"press\":\"") + ",\"press\":\"".length(),
                        k.indexOf("\",\"publishyear\":")));
                modifBook.setPublishYear(
                        Integer.parseInt(k.substring(k.indexOf(",\"publishyear\":\"") + ",\"publishyear\":\"".length(),
                                k.indexOf("\",\"author\":"))));
                modifBook.setAuthor(k.substring(k.indexOf(",\"author\":\"") + ",\"author\":\"".length(),
                        k.indexOf("\",\"price\":")));
                modifBook.setPrice(Double
                        .parseDouble(
                                k.substring(k.indexOf(",\"price\":") + ",\"price\":".length(), k.length() - 1)));
                connect();
                // 响应头
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                ApiResult result = library.modifyBookInfo(modifBook);
                if (result.ok)
                    outputStream.write("success".getBytes());
                else
                    outputStream.write(result.message.getBytes());
                release_connect();
                outputStream.close();
            } else if (chose.charAt(6) == 's') {
                int bookid = Integer.parseInt(k.substring(k.indexOf("\"id\":\"") + "\"id\":\"".length(),
                        k.indexOf("\",\"stock\":")));
                int stock = Integer.parseInt(k.substring(k.indexOf(",\"stock\":") +
                        ",\"stock\":".length(),
                        k.indexOf(",\"order\":")));
                int order = Integer.parseInt(k.substring(k.indexOf(",\"order\":\"") +
                        ",\"order\":\"".length(),
                        k.length() - 2));
                if (order == 2)
                    stock = -stock;
                connect();
                // 响应头
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                ApiResult result = library.incBookStock(bookid, stock);
                if (result.ok)
                    outputStream.write("success".getBytes());
                else
                    outputStream.write(result.message.getBytes());
                release_connect();
                outputStream.close();
            } else if (chose.charAt(6) == 'b') {
                Borrow bb = new Borrow();
                bb.setCardId(Integer.parseInt(k.substring(k.indexOf("\"cardid\":\"") + "\"cardid\":\"".length(),
                        k.indexOf("\",\"bookid\":"))));
                bb.setBookId(Integer.parseInt(k.substring(k.indexOf("\"bookid\":\"") + "\"bookid\":\"".length(),
                        k.indexOf("\",\"borrowtime\":"))));
                bb.setBorrowTime(
                        Long.parseLong(k.substring(k.indexOf("\"borrowtime\":") + "\"borrowtime\":".length(),
                                k.length() - 1)));
                connect();
                // 响应头
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                ApiResult result = library.borrowBook(bb);
                if (result.ok)
                    outputStream.write("success".getBytes());
                else
                    outputStream.write(result.message.getBytes());
                release_connect();
                outputStream.close();
            } else if (chose.charAt(6) == 'r') {
                Borrow bb = new Borrow();
                bb.setCardId(Integer.parseInt(k.substring(k.indexOf("\"cardid\":\"") + "\"cardid\":\"".length(),
                        k.indexOf("\",\"bookid\":"))));
                bb.setBookId(Integer.parseInt(k.substring(k.indexOf("\"bookid\":\"") + "\"bookid\":\"".length(),
                        k.indexOf("\",\"returntime\":"))));
                bb.setReturnTime(
                        Long.parseLong(k.substring(k.indexOf("\"returntime\":") + "\"returntime\":".length(),
                                k.length() - 1)));
                connect();
                // 响应头
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                ApiResult result = library.returnBook(bb);
                if (result.ok)
                    outputStream.write("success".getBytes());
                else
                    outputStream.write(result.message.getBytes());
                release_connect();
                outputStream.close();
            } else if (chose.charAt(6) == 'n') {
                String[] js = k.split("\\},\\{");
                List<Book> books = new ArrayList<>();
                for (int i = 0; i < js.length - 1; i++) {
                    Book book = new Book();
                    book.setCategory(js[i].substring(js[i].indexOf("\"category\":\"") + "\"category\":\"".length(),
                            js[i].indexOf("\",\"title\":")));
                    book.setTitle(js[i].substring(js[i].indexOf(",\"title\":\"") + ",\"title\":\"".length(),
                            js[i].indexOf("\",\"press\":")));
                    book.setPress(js[i].substring(js[i].indexOf(",\"press\":\"") + ",\"press\":\"".length(),
                            js[i].indexOf("\",\"publishyear\":")));
                    book.setPublishYear(
                            Integer.parseInt(
                                    js[i].substring(
                                            js[i].indexOf(",\"publishyear\":\"") + ",\"publishyear\":\"".length(),
                                            js[i].indexOf("\",\"author\":"))));
                    book.setAuthor(js[i].substring(js[i].indexOf(",\"author\":\"") + ",\"author\":\"".length(),
                            js[i].indexOf("\",\"price\":")));
                    book.setPrice(Double
                            .parseDouble(
                                    js[i].substring(js[i].indexOf(",\"price\":\"") + ",\"price\":\"".length(),
                                            js[i].indexOf("\",\"stock\":"))));
                    book.setStock(
                            Integer.parseInt(
                                    js[i].substring(js[i].indexOf(",\"stock\":\"") + ",\"stock\":\"".length(),
                                            js[i].length() - 1)));
                    books.add(book);
                }
                Book book = new Book();
                int i = js.length - 1;
                book.setCategory(js[i].substring(js[i].indexOf("\"category\":\"") + "\"category\":\"".length(),
                        js[i].indexOf("\",\"title\":")));
                book.setTitle(js[i].substring(js[i].indexOf(",\"title\":\"") + ",\"title\":\"".length(),
                        js[i].indexOf("\",\"press\":")));
                book.setPress(js[i].substring(js[i].indexOf(",\"press\":\"") + ",\"press\":\"".length(),
                        js[i].indexOf("\",\"publishyear\":")));
                book.setPublishYear(
                        Integer.parseInt(
                                js[i].substring(
                                        js[i].indexOf(",\"publishyear\":\"") + ",\"publishyear\":\"".length(),
                                        js[i].indexOf("\",\"author\":"))));
                book.setAuthor(js[i].substring(js[i].indexOf(",\"author\":\"") + ",\"author\":\"".length(),
                        js[i].indexOf("\",\"price\":")));
                book.setPrice(Double
                        .parseDouble(
                                js[i].substring(js[i].indexOf(",\"price\":\"") + ",\"price\":\"".length(),
                                        js[i].indexOf("\",\"stock\":"))));
                book.setStock(
                        Integer.parseInt(
                                js[i].substring(js[i].indexOf(",\"stock\":\"") + ",\"stock\":\"".length(),
                                        js[i].length() - 3)));
                books.add(book);
                connect();
                // 响应头
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                ApiResult result;
                if (js.length == 1) {
                    result = library.storeBook(book);
                } else {
                    result = library.storeBook(books);
                }
                if (result.ok)
                    outputStream.write("success".getBytes());
                else
                    outputStream.write(result.message.getBytes());
                release_connect();
                outputStream.close();
            } else if (chose.charAt(6) == 'u') {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // 响应状态码200
                exchange.sendResponseHeaders(200, 0);
                // 剩下三个和GET一样
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write("success".getBytes());
                outputStream.close();
            }

        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS,DELETE");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            System.out.println(requestMethod);
            // 注意判断要用equals方法而不是==啊，java的小坑（
            if (requestMethod.equals("GET")) {
                // 处理GET
                handleGetRequest(exchange);
            } else if (requestMethod.equals("POST")) {
                // 处理POST
                handlePostRequest(exchange);
            } else if (requestMethod.equals("OPTIONS")) {
                // 处理OPTIONS
                handleOptionsRequest(exchange);
            } else if (requestMethod.equals("DELETE")) {
                handleDeleteRequest(exchange);
            } else {
                // 其他请求返回405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }

    }

    static class CardHandler implements HttpHandler {
        private void handleDeleteRequest(HttpExchange exchange) throws IOException {
            String k = exchange.getRequestURI().toString();
            int cardid = Integer.parseInt(k.substring(k.indexOf("=") + 1));
            System.out.println(cardid);
            connect();
            ApiResult result = library.removeCard(cardid);
            release_connect();
            if (result.ok) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write("Card deleted successfully".getBytes());
                outputStream.close();
            } else {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, 0);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(result.message.getBytes());
                outputStream.close();
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // 响应头，因为是JSON通信
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            // 状态码为200，也就是status ok
            exchange.sendResponseHeaders(200, 0);
            // 获取输出流，java用流对象来进行io操作
            OutputStream outputStream = exchange.getResponseBody();
            // 构建JSON响应数据，这里简化为字符串
            // 这里写的一个固定的JSON，实际可以查表获取数据，然后再拼出想要的JSON
            connect();
            ApiResult result = library.showCards();
            CardList resCardList = (CardList) result.payload;
            String response = "[";
            if (resCardList.getCount() >= 1)
                response = response + "{\"id\": " + Integer.toString(resCardList.getCards().get(0).getCardId())
                        + ", \"name\": \"" + resCardList.getCards().get(0).getName()
                        + "\", \"department\": \"" + resCardList.getCards().get(0).getDepartment()
                        + "\", \"type\": \"" + resCardList.getCards().get(0).getType() + "\"}";
            for (int i = 1; i < resCardList.getCount(); i++) {
                response = response
                        + ",{\"id\": " + Integer.toString(resCardList.getCards().get(i).getCardId())
                        + ", \"name\": \"" + resCardList.getCards().get(i).getName()
                        + "\", \"department\": \"" + resCardList.getCards().get(i).getDepartment()
                        + "\", \"type\": \"" + resCardList.getCards().get(i).getType() + "\"}";
            }
            response = response + "]";
            release_connect();
            // 写
            outputStream.write(response.getBytes());
            // 流一定要close！！！小心泄漏
            outputStream.close();
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // 读取POST请求体
            InputStream requestBody = exchange.getRequestBody();
            // 用这个请求体（输入流）构造个buffered reader
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            // 拼字符串的
            StringBuilder requestBodyBuilder = new StringBuilder();
            // 用来读的
            String line;
            // 没读完，一直读，拼到string builder里
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String k = requestBodyBuilder.toString();
            int num_id = k.indexOf("\"id\"");
            int num_a = k.indexOf("\"name\":");
            int num_b = k.indexOf("\"department\":");
            int num_c = k.indexOf("\"type\":");
            Card new_card = new Card();
            new_card.setName(k.substring(num_a + 8, num_b - 2));
            new_card.setDepartment(k.substring(num_b + 14, num_c - 2));
            String ty = k.substring(num_c + 8, k.length() - 2);
            if (ty.equals("学生")) {
                new_card.setType(CardType.values("S"));
            } else {
                new_card.setType(CardType.values("T"));
            }
            connect();
            // 响应头
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            // 响应状态码200
            exchange.sendResponseHeaders(200, 0);
            // 剩下三个和GET一样
            OutputStream outputStream = exchange.getResponseBody();
            if (num_id != 1) {
                ApiResult result = library.registerCard(new_card);
                if (result.ok)
                    outputStream.write("Card created successfully".getBytes());
                else
                    outputStream.write(result.message.getBytes());
            } else {
                new_card.setCardId(Integer.parseInt(k.substring(num_id + 5, num_a - 1)));
                System.out.println(new_card.getCardId());
                ApiResult result = library.modifyCard(new_card);
                if (result.ok)
                    outputStream.write("Card modified sucessfully".getBytes());
                else
                    outputStream.write(result.message.getBytes());
            }

            release_connect();
            outputStream.close();
        }

        private void handleOptionsRequest(HttpExchange exchange) throws IOException {

            exchange.sendResponseHeaders(204, -1);

        }

        // 关键重写handle方法
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 允许所有域的请求，cors处理
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS,DELETE");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            // 解析请求的方法，看GET还是POST
            String requestMethod = exchange.getRequestMethod();
            System.out.println(requestMethod);
            // 注意判断要用equals方法而不是==啊，java的小坑（
            if (requestMethod.equals("GET")) {
                // 处理GET
                handleGetRequest(exchange);
            } else if (requestMethod.equals("POST")) {
                // 处理POST
                handlePostRequest(exchange);
            } else if (requestMethod.equals("OPTIONS")) {
                // 处理OPTIONS
                handleOptionsRequest(exchange);
            } else if (requestMethod.equals("DELETE")) {
                handleDeleteRequest(exchange);
            } else {
                // 其他请求返回405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class BorrowHandler implements HttpHandler {
        private void handleGetRequest(HttpExchange exchange, int id) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            OutputStream outputStream = exchange.getResponseBody();
            connect();
            ApiResult result = library.showBorrowHistory(id);
            BorrowHistories bh = (BorrowHistories) result.payload;
            String response = "[";
            if (bh.getCount() > 0) {
                response = response + "{\"cardID\": " + Integer.toString(bh.getItems().get(0).getCardId())
                        + ", \"bookID\": " + Integer.toString(bh.getItems().get(0).getBookId())
                        + ", \"borrowTime\": " + Long.toString(bh.getItems().get(0).getBorrowTime())
                        + ", \"returnTime\": " + Long.toString(bh.getItems().get(0).getReturnTime()) + "}";
            }
            for (int i = 1; i < bh.getCount(); i++) {
                response = response + ",{\"cardID\": " + Integer.toString(bh.getItems().get(i).getCardId())
                        + ", \"bookID\": " + Integer.toString(bh.getItems().get(i).getBookId())
                        + ", \"borrowTime\": " + Long.toString(bh.getItems().get(i).getBorrowTime())
                        + ", \"returnTime\": " + Long.toString(bh.getItems().get(i).getReturnTime()) + "}";
            }
            response = response + "]";
            release_connect();
            outputStream.write(response.getBytes());
            outputStream.close();

        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            String requestMethod = exchange.getRequestMethod();
            String k = exchange.getRequestURI().toString();
            System.out.println(k);
            int id = Integer.parseInt(k.substring(k.indexOf("=") + 1));
            if (requestMethod.equals("GET")) {
                handleGetRequest(exchange, id);
            }
        }
    }

}
