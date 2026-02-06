import java.sql.*;
public class DatabaseHelper {
    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String URL = "jdbc:mysql://" + DB_HOST + ":3306/javaChat";
    private static final String USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "nit37";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean userExists(String username){
        String sql = "SELECT 1 FROM USER WHERE username = ?";
        try(Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static String verifyUser(String username, String password){
        String sql = "SELECT password FROM USER WHERE username = ?";
        try(Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if(!rs.next()) return "NEW";
            return rs.getString("password").equals(password) ? "OK" : "WRONG_PASSWORD";
        }
        catch(SQLException e){
            e.printStackTrace();
        }
        return "ERROR";
    }

    public static void createUser(String username, String password){
        String sql = "INSERT INTO USER(username, password) VALUES (?, ?)";
        try(Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private static String[] normalizeUsers(String user1, String user2){
        if(user1.compareToIgnoreCase(user2)<0){
            return new String[]{user1,user2};
        }
        
        return new String[]{user2,user1};
    }

    public static void saveChat(String user1, String user2, String sessionLog){
        String users[] = normalizeUsers(user1, user2);
        String sql = "insert into USER_CHATS (user1, user2, chat_log) values (?, ?, ?) " +
         "on duplicate key update chat_log = ?, last_updated = CURRENT_TIMESTAMP";

         try (Connection conn = getConnection();
           PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, users[0]);
          stmt.setString(2, users[1]);
          stmt.setString(3, sessionLog);
          stmt.setString(4, sessionLog);
          stmt.executeUpdate();
      } catch (SQLException e) {
          e.printStackTrace();
      }
    }

    public static String getChatHistory(String userA, String userB) {
      String[] users = normalizeUsers(userA, userB);
      String sql = "SELECT chat_log FROM USER_CHATS WHERE user1 = ? AND user2 = ?";

      try (Connection conn = getConnection();
           PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, users[0]);
          stmt.setString(2, users[1]);
          ResultSet rs = stmt.executeQuery();
          if (rs.next()) {
              return rs.getString("chat_log");
          }
      } catch (SQLException e) {
          e.printStackTrace();
      }
      return null;
  }
}
