import entities.Book;
import entities.Borrow;
import entities.Card;
import entities.Card.CardType;
import queries.*;
import queries.BorrowHistories.Item;
import utils.DBInitializer;
import utils.DatabaseConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryManagementSystemImpl implements LibraryManagementSystem {

    private final DatabaseConnector connector;

    public LibraryManagementSystemImpl(DatabaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public ApiResult storeBook(Book book) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement ps = conn
                    .prepareStatement(
                            "INSERT INTO book (category,title,press,publish_year,author,price,stock) VALUES (?,?,?,?,?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getCategory());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getPress());
            ps.setInt(4, book.getPublishYear());
            ps.setString(5, book.getAuthor());
            if (book.getPrice() < 0) {
                throw new Exception("价格必须大于0");
            }
            ps.setDouble(6, book.getPrice());
            ps.setInt(7, book.getStock());
            ps.executeUpdate();
            commit(conn);
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                book.setBookId(rs.getInt(1));
            }
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult incBookStock(int bookId, int deltaStock) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT book_id,stock FROM book WHERE book_id=?;");
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new Exception("没有对应id!");
            else {
                if (rs.getInt(2) + deltaStock < 0)
                    throw new Exception("库存小于0!");
            }

            ps = conn.prepareStatement("UPDATE book SET stock=stock+? WHERE book_id=? AND stock+?>=0;");
            ps.setInt(1, deltaStock);
            ps.setInt(2, bookId);
            ps.setInt(3, deltaStock);
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult storeBook(List<Book> books) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO book (category,title,press,publish_year,author,price,stock) VALUES (?,?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS);
            for (Book book : books) {
                ps.setString(1, book.getCategory());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getPress());
                ps.setInt(4, book.getPublishYear());
                ps.setString(5, book.getAuthor());
                if (book.getPrice() < 0) {
                    throw new Exception("价格必须大于0");
                }
                ps.setDouble(6, book.getPrice());
                ps.setInt(7, book.getStock());
                ps.addBatch();
            }
            ps.executeBatch();
            commit(conn);
            ResultSet rs = ps.getGeneratedKeys();
            for (Book book : books) {
                if (rs.next()) {
                    book.setBookId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeBook(int bookId) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT * FROM book WHERE book_id=?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍不存在！");
            }
            sql = "SELECT * FROM borrow WHERE book_id=? AND return_time=0;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            rs = ps.executeQuery();
            if (rs.next()) {
                throw new Exception("还有用户未归还此书!");
            }
            sql = "DELETE FROM book WHERE book_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, bookId);
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult modifyBookInfo(Book book) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT * FROM book WHERE book_id=?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, book.getBookId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍不存在!");
            }
            sql = "UPDATE book SET category=?,title=?,press=?,publish_year=?,author=?,price=? WHERE book_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setString(1, book.getCategory());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getPress());
            ps.setInt(4, book.getPublishYear());
            ps.setString(5, book.getAuthor());
            if (book.getPrice() < 0) {
                throw new Exception("价格必须大于0");
            }
            ps.setDouble(6, book.getPrice());
            ps.setInt(7, book.getBookId());
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult queryBook(BookQueryConditions conditions) {
        Connection conn = connector.getConn();
        BookQueryResults bqr;
        try {
            String sql = "SELECT * FROM book";
            Integer in = 0;
            if (conditions.getCategory() != null) {
                if (in == 0) {
                    sql = sql + " WHERE category=?";
                    in = 1;
                } else {
                    sql = sql + " AND category=?";
                }
            }
            if (conditions.getTitle() != null) {
                if (in == 0) {
                    sql = sql + " WHERE title LIke ?";
                    in = 1;
                } else {
                    sql = sql + " AND title LIke ?";
                }
            }
            if (conditions.getPress() != null) {
                if (in == 0) {
                    sql = sql + " WHERE press LIke ?";
                    in = 1;
                } else {
                    sql = sql + " AND press LIke ?";
                }
            }
            if (conditions.getMinPublishYear() != null) {
                if (in == 0) {
                    sql = sql + " WHERE publish_year>=?";
                    in = 1;
                } else {
                    sql = sql + " AND publish_year>=?";
                }
            }
            if (conditions.getMaxPublishYear() != null) {
                if (in == 0) {
                    sql = sql + " WHERE publish_year<=?";
                    in = 1;
                } else {
                    sql = sql + " AND publish_year<=?";
                }
            }
            if (conditions.getAuthor() != null) {
                if (in == 0) {
                    sql = sql + " WHERE author LIke ?";
                    in = 1;
                } else {
                    sql = sql + " AND author LIke ?";
                }
            }
            if (conditions.getMinPrice() != null) {
                if (in == 0) {
                    sql = sql + " WHERE price>=?";
                    in = 1;
                } else {
                    sql = sql + " AND price>=?";
                }
            }
            if (conditions.getMaxPrice() != null) {
                if (in == 0) {
                    sql = sql + " WHERE price<=?";
                    in = 1;
                } else {
                    sql = sql + " AND price<=?";
                }
            }
            sql = sql + " ORDER BY " + conditions.getSortBy().getValue() + " " + conditions.getSortOrder().getValue()
                    + " ,book_id ASC;";
            PreparedStatement ps = conn.prepareStatement(sql);
            Integer order = 1;
            if (conditions.getCategory() != null) {
                ps.setString(order, conditions.getCategory());
                order++;
            }
            if (conditions.getTitle() != null) {
                ps.setString(order, "%" + conditions.getTitle() + "%");
                order++;
            }
            if (conditions.getPress() != null) {
                ps.setString(order, "%" + conditions.getPress() + "%");
                order++;
            }
            if (conditions.getMinPublishYear() != null) {
                ps.setInt(order, conditions.getMinPublishYear());
                order++;
            }
            if (conditions.getMaxPublishYear() != null) {
                ps.setInt(order, conditions.getMaxPublishYear());
                order++;
            }
            if (conditions.getAuthor() != null) {
                ps.setString(order, "%" + conditions.getAuthor() + "%");
                order++;
            }
            if (conditions.getMinPrice() != null) {
                ps.setDouble(order, conditions.getMinPrice());
                order++;
            }
            if (conditions.getMaxPrice() != null) {
                ps.setDouble(order, conditions.getMaxPrice());
                order++;
            }
            ResultSet rs = ps.executeQuery();
            List<Book> result = new ArrayList<>();
            while (rs.next()) {
                Book temp = new Book();
                temp.setBookId(rs.getInt(1));
                temp.setCategory(rs.getString(2));
                temp.setTitle(rs.getString(3));
                temp.setPress(rs.getString(4));
                temp.setPublishYear(rs.getInt(5));
                temp.setAuthor(rs.getString(6));
                temp.setPrice(rs.getDouble(7));
                temp.setStock(rs.getInt(8));
                result.add(temp);
            }
            bqr = new BookQueryResults(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, bqr);
    }

    @Override
    public ApiResult borrowBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            // conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            conn.setAutoCommit(false);
            String sql = "SELECT stock FROM book WHERE book_id=? for update;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getBookId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍不存在!");
            } else {
                if (rs.getInt(1) <= 0)
                    throw new Exception("没有额外的库存!");
            }
            sql = "SELECT * FROM card WHERE card_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getCardId());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("借书证不存在！");
            }
            sql = "SELECT * FROM borrow WHERE card_id=? AND book_id=? AND return_time=0 for update;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getCardId());
            ps.setInt(2, borrow.getBookId());
            rs = ps.executeQuery();
            if (rs.next()) {
                throw new Exception("用户借阅此书但未归还！");
            }
            sql = "INSERT INTO borrow (card_id,book_id,borrow_time) VALUES (?,?,?);";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getCardId());
            ps.setInt(2, borrow.getBookId());
            ps.setLong(3, borrow.getBorrowTime());
            ps.executeUpdate();
            sql = "UPDATE book SET stock=stock-1 WHERE book_id=? ;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getBookId());
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        } finally {
            try {
                if (!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult returnBook(Borrow borrow) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT stock FROM book WHERE book_id=? for update;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getBookId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍不存在！");
            }
            sql = "SELECT * FROM card WHERE card_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getCardId());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("借书证不存在！");
            }
            sql = "SELECT borrow_time FROM borrow WHERE card_id=? AND book_id=? AND return_time=0 for update;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getCardId());
            ps.setInt(2, borrow.getBookId());
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍重复归还！");
            } else {
                if (rs.getLong(1) >= borrow.getReturnTime())
                    throw new Exception("归还时间错误");
            }
            conn.setAutoCommit(false);
            sql = "UPDATE borrow SET return_time=? WHERE card_id=? AND book_id=? AND return_time=0;";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, borrow.getReturnTime());
            ps.setInt(2, borrow.getCardId());
            ps.setInt(3, borrow.getBookId());
            ps.executeUpdate();
            sql = "UPDATE book SET stock=stock+1 WHERE book_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, borrow.getBookId());
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        } finally {
            try {
                if (!conn.getAutoCommit())
                    conn.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showBorrowHistory(int cardId) {
        Connection conn = connector.getConn();
        BorrowHistories bh;
        try {
            String sql = "SELECT * FROM card WHERE card_id=?; ";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cardId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("借书证不存在！");
            }
            sql = "SELECT card_id,book.book_id,category,title,press,publish_year,author,price,borrow_time,return_time"
                    + " FROM book,borrow WHERE book.book_id=borrow.book_id AND card_id=? ORDER BY borrow_time DESC,book.book_id ASC;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cardId);
            rs = ps.executeQuery();
            List<Item> result = new ArrayList<>();
            while (rs.next()) {
                Item temp = new Item();
                temp.setCardId(rs.getInt(1));
                temp.setBookId(rs.getInt(2));
                temp.setCategory(rs.getString(3));
                temp.setTitle(rs.getString(4));
                temp.setPress(rs.getString(5));
                temp.setPublishYear(rs.getInt(6));
                temp.setAuthor(rs.getString(7));
                temp.setPrice(rs.getDouble(8));
                temp.setBorrowTime(rs.getLong(9));
                temp.setReturnTime(rs.getLong(10));
                result.add(temp);
            }
            bh = new BorrowHistories(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, bh);
    }

    @Override
    public ApiResult registerCard(Card card) {
        Connection conn = connector.getConn();
        try {
            PreparedStatement ps = conn
                    .prepareStatement(
                            "INSERT INTO card (name,department,type) VALUES (?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, card.getName());
            ps.setString(2, card.getDepartment());
            ps.setString(3, card.getType().getStr());
            ps.executeUpdate();
            commit(conn);
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                card.setCardId(rs.getInt(1));
            }
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult removeCard(int cardId) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT * FROM card WHERE card_id=?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, cardId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("书籍不存在!");
            }
            sql = "SELECT * FROM borrow WHERE card_id=? AND return_time=0;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cardId);
            rs = ps.executeQuery();
            if (rs.next()) {
                throw new Exception("用户仍然有未归还的书籍!");
            }
            sql = "DELETE FROM card WHERE card_id=?;";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cardId);
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult modifyCard(Card card) {
        Connection conn = connector.getConn();
        try {
            String sql = "SELECT * FROM card WHERE card_id=?;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, card.getCardId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("ID不存在");
            }
            sql = "UPDATE card SET name=?,department=?,type=? WHERE card_id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, card.getName());
            ps.setString(2, card.getDepartment());
            ps.setString(3, card.getType().getStr());
            ps.setInt(4, card.getCardId());
            ps.executeUpdate();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    @Override
    public ApiResult showCards() {
        Connection conn = connector.getConn();
        CardList cl;
        try {
            String sql = "SELECT * FROM card ORDER BY card_id;";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            List<Card> result = new ArrayList<>();
            while (rs.next()) {
                Card temp = new Card();
                temp.setCardId(rs.getInt(1));
                temp.setName(rs.getString(2));
                temp.setDepartment(rs.getString(3));
                temp.setType(CardType.values(rs.getString(4)));
                result.add(temp);
            }
            cl = new CardList(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, cl);
    }

    @Override
    public ApiResult resetDatabase() {
        Connection conn = connector.getConn();
        try {
            Statement stmt = conn.createStatement();
            DBInitializer initializer = connector.getConf().getType().getDbInitializer();
            stmt.addBatch(initializer.sqlDropBorrow());
            stmt.addBatch(initializer.sqlDropBook());
            stmt.addBatch(initializer.sqlDropCard());
            stmt.addBatch(initializer.sqlCreateCard());
            stmt.addBatch(initializer.sqlCreateBook());
            stmt.addBatch(initializer.sqlCreateBorrow());
            stmt.executeBatch();
            commit(conn);
        } catch (Exception e) {
            rollback(conn);
            return new ApiResult(false, e.getMessage());
        }
        return new ApiResult(true, null);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
