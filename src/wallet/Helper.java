
package wallet;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;
import wallet.Constants.AuthResult;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import org.json.JSONArray;


public class Helper {

    private static final String JDBC_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    private static Connection connection;
    private static Statement statement;
    private static String currentUserName;
    private static int user_role = 0;
    private static int user_id = 0;
    private static AuthResult authResult = AuthResult.USER;

    static {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static int readInteger(int max, Scanner scanner) {
        int choice;

        do {

            while (!scanner.hasNextInt()) {
                System.out.println("Это не число!");
                scanner.next();
            }
            choice = scanner.nextInt();
        } while (choice < 0 || choice > max);
        return choice;
    }

    public static int readInteger(Scanner scanner) {
        int choice;

        do {

            while (!scanner.hasNextInt()) {
                System.out.println("Это не число!");
                scanner.next();
            }
            choice = scanner.nextInt();
        } while (choice < 0);
        return choice;
    }
    
    public static Date readDate(Scanner scanner) {
        LocalDate date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while (date == null) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            String input = scanner.nextLine();

            try {
                date = LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Некорректная дата! Пожалуйста, введите дату в формате yyyy-MM-dd.");
            }
        }
        return java.sql.Date.valueOf(date);
    }

    public static int findCategoryIdByName(String categoryName) {
        int categoryId = -1;

        String selectQuery = "SELECT Category_id FROM tblRefCategories WHERE category_name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                categoryId = rs.getInt("Category_id");
            }
            } catch (SQLException e) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
            }
            return categoryId;
    }
    
    public static String getUserNameById(int user_id) {
        String user_name = "";

        String selectQuery = "SELECT user_name FROM tblUsers WHERE user_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, user_id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user_name = rs.getString("user_name");
            }
            } catch (SQLException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            return user_name;
    }
        
    public static int addCategory(String categoryName, int category) {
        int categoryId = -1;
        String appendQuery = "INSERT INTO tblRefCategories (category_name, category) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(appendQuery
                , Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, categoryName);
                pstmt.setInt(2, category);
            pstmt.executeUpdate();
            ResultSet resultSet = pstmt.getGeneratedKeys();
            if (resultSet.next()) {
                categoryId = resultSet.getInt(1);
            }
        } 
        catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return categoryId;
    }

    public static void assignCategoryToUser(int categoryId, int userId, int budget) {

        String appendQuery = "INSERT INTO tblCategoryByUser (category_id, user_id, budget) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(appendQuery)) {
            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, budget);
            pstmt.executeUpdate();
        } 
        catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static boolean isCategoryAssignedToUser(int categoryId, int userId) {
        boolean assigned = false;
        
        String selectQuery = "SELECT id FROM tblCategoryByUser WHERE category_id = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                assigned = true;
            }
        } 
        catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
        return assigned;
    }

    public static void showAssignedCategories() {

        String selectQuery = """
            SELECT 
                  u.user_name
                , c.category_name
                , cu.budget   
                , c.category             
            FROM tblCategoryByUser cu
            INNER JOIN tblUsers u ON cu.user_id = u.user_id
            INNER JOIN tblRefCategories c ON c.category_id = cu.category_id
            ORDER BY u.user_name, c.category_name;
            """;

            try {

                    ResultSet resultSet = statement.executeQuery(selectQuery);
                    String user_name;
                    String category_name;
                    int budget;
                    int category;

                    while (resultSet.next()) {
                        user_name = resultSet.getString("user_name");
                        category_name = resultSet.getString("category_name");
                        category = resultSet.getInt("category");
                        budget = resultSet.getInt("budget");

                        if(category == 0) {
                        System.out.printf("Пользователь: %s; Категории: %s; Бюджет: %d%n"
                            , user_name, category_name, budget);
                        } else
                        {
                            System.out.printf("Пользователь: %s; Категории: %s%n"
                            , user_name, category_name);
                        }
                    }
            }        

            catch (SQLException e) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
            }    

    }

    public static void addIncome(int user_id, int category_id, Date whenDate, int amount) {
        String sql = "INSERT INTO tblIncomeExpenses (category_id, user_id, when_date, amount) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, category_id);
            pstmt.setInt(2, user_id);
            pstmt.setDate(3, (java.sql.Date) whenDate);
            pstmt.setInt(4, amount);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void showMessage(String message) {

        Console console = System.console();
        if (console == null) {
            System.out.println("Консоль недоступна.");
            return;
        }

        System.out.println(message);
        console.readLine();  
    }

    
    
    public static int getUserId() {
        return user_id;
    }

    public static String getUserName() {
        return currentUserName;
    }

    public static boolean isAuthorized() {
        return authResult == AuthResult.USER || authResult == AuthResult.ADMINISTRATOR;
    }

    public static boolean isAdministrator() {
        return authResult == AuthResult.ADMINISTRATOR;
    }

    private static boolean isFilesExist() {
        String[] fileNames = {"data_tblIncomeExpenses.json",
            "data_tblRefCategories.json",
            "data_tblUsers.json"};

        boolean result = true;

        for (String fileName : fileNames) {
            File file = new File(fileName);
            if (!file.exists()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static void saveDatabaseToFiles() {
        String query_users = """
                             SELECT 
                             user_password, user_login, user_name, user_role
                             FROM tblUsers;""";

        String query_categories = """
                             SELECT 
                             category_name, category
                             FROM tblRefCategories;""";

        String query_budget = """
                             SELECT category_id, user_id, when_date, amount 
                             FROM tblIncomeExpenses;""";
        
        String query_category_by_user = """
                             SELECT category_id, user_id, budget
                             FROM tblCategoryByUser;""";
        
        exportDataToJson(query_users, "data_tblUsers.json");
        exportDataToJson(query_categories, "data_tblRefCategories.json");
        exportDataToJson(query_budget, "data_tblIncomeExpenses.json");
        exportDataToJson(query_category_by_user, "data_tblCategoryByUser.json");
    }

    public static void loadDatabaseFromFile() {
        importDataFromJson("data_tblUsers.json", "tblUsers", "user_password, user_login, user_name, user_role");
        importDataFromJson("data_tblRefCategories.json", "tblRefCategories", "category_name, category");
        importDataFromJson("data_tblIncomeExpenses.json", "tblIncomeExpenses", "category_id, user_id, when_date, amount");
        importDataFromJson("data_tblCategoryByUser.json ", "tblCategoryByUser", "category_id, user_id, budget");
    }

    private static void importDataFromJson(String filePath, String tableName, String fields) {

        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(filePath));
        
            JSONArray jsonArray = new JSONArray(tokener);

            List<String> fieldList = Arrays.asList(fields.split(","));
            String placeholders = String.join(", ", fieldList.stream().map(f -> "?").toArray(String[]::new));
            String sql = "INSERT INTO " + tableName + " (" 
                    + String.join(", ", fieldList) 
                    + ") VALUES (" + placeholders + ")";

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;

                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < fieldList.size(); i++) {
                            String field = fieldList.get(i).trim().toUpperCase();
                            Object value = jsonObject.get(field);

                        switch (value) {
                            case Integer integer -> pstmt.setInt(i + 1, integer);
                            case String string -> pstmt.setString(i + 1, string);
                            default -> pstmt.setObject(i + 1, value);
                        }
                   }
                    pstmt.executeUpdate();
                }
                catch (SQLException e) {
                    Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
        Метод экспортирует данные из всех таблиц в файлы на диске
    */
   
    private static void exportAllTablesToJson(){
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                List<String> fieldList = getTableFields(connection, tableName);
                String filePath = "data_" + tableName + ".json";
                importDataFromJson(filePath, tableName, fieldList);
            }
        }
        catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static List<String> getTableFields(Connection conn, String tableName) {
        
        List<String> fieldList = new ArrayList<>();
        try {
            
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                fieldList.add(columnName);
            }
        }
        catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
        return fieldList;
    }
    
    
    private static void importDataFromJson(String filePath, String tableName, List<String> fieldList) {

        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(filePath));
        
            JSONArray jsonArray = new JSONArray(tokener);
            
            String placeholders = String.join(", ", fieldList.stream().map(f -> "?").toArray(String[]::new));
            String sql = "INSERT INTO " + tableName + " (" 
                    + String.join(", ", fieldList) 
                    + ") VALUES (" + placeholders + ")";

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;

                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < fieldList.size(); i++) {
                            String field = fieldList.get(i).trim().toUpperCase();
                            Object value = jsonObject.get(field);

                        switch (value) {
                            case Integer integer -> pstmt.setInt(i + 1, integer);
                            case String string -> pstmt.setString(i + 1, string);
                            default -> pstmt.setObject(i + 1, value);
                        }
                   }
                    pstmt.executeUpdate();
                }
                catch (SQLException e) {
                    Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void exportDataToJson(String query, String filePath) {

        try (ResultSet resultSet = statement.executeQuery(query)) {

            JSONArray jsonArray = new JSONArray();

            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    jsonObject.put(columnName, value);
                }
                jsonArray.put(jsonObject);
            }

            try (FileWriter file = new FileWriter(filePath)) {
                file.write(jsonArray.toString());
                file.flush();
            }

        } catch (SQLException | IOException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static String Welcome() {
        return switch (authResult) {
            case ADMINISTRATOR ->
                "Добро пожаловать, Администратор " + currentUserName + "!";
            case USER ->
                "Добро пожаловать, " + currentUserName + "!";
            case AUTH_ERROR ->
                "Ошибка аутентификации. Пожалуйста, проверьте логин и пароль.";
            default ->
                "Неизвестный результат аутентификации.";
        };
    }

    public static AuthResult authenticateUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        String query = "SELECT user_id, user_name, user_role FROM tblUsers WHERE user_login = ? AND user_password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                currentUserName = resultSet.getString("user_name");
                user_role = resultSet.getInt("user_role");
                user_id = resultSet.getInt("user_id");
                if (user_role == 1) {
                    authResult = AuthResult.ADMINISTRATOR;
                    //System.out.println("Добро пожаловать, Администратор " + currentUserName + "!");
                } else {
                    authResult = AuthResult.USER;
                    //System.out.println("Добро пожаловать, " + currentUserName + "!");
                }
            } else {
                authResult = AuthResult.AUTH_ERROR;
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
        return authResult;
    }

    private static void createTables() {
        try {
        
            String queryCreateTblCategoryByUser = """
                CREATE TABLE tblCategoryByUser (
                                         id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                         category_id INT,
                                         user_id INT,
                                         budget INT,
                                         FOREIGN KEY (category_id) REFERENCES tblRefCategories(Category_id) ON DELETE CASCADE ON UPDATE CASCADE,
                                         FOREIGN KEY (user_id) REFERENCES tblUsers(user_id) ON DELETE CASCADE ON UPDATE CASCADE
                                         );                 
                """;
            
            String queryCreateTableBudget = """
        CREATE TABLE tblIncomeExpenses (
        id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        category_id INT,
        user_id INT,
        when_date DATE,
        amount INT,
        FOREIGN KEY (category_id) REFERENCES tblRefCategories(Category_id) ON DELETE CASCADE ON UPDATE CASCADE,
        FOREIGN KEY (user_id) REFERENCES tblUsers(user_id) ON DELETE CASCADE ON UPDATE CASCADE
        );
    """;
            String queryCreateTableUsers = """
        CREATE TABLE tblUsers (
        user_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        user_name VARCHAR(50),
        user_login VARCHAR(50),
        user_password VARCHAR(50),
        user_role INT);
    """;
            String queryCreateTableCategories = """
    CREATE TABLE tblRefCategories (
    Category_id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    category_name VARCHAR(50),
    category INT);
                                                        """;
            statement.executeUpdate(queryCreateTableUsers);
            statement.executeUpdate(queryCreateTableCategories);
            statement.executeUpdate(queryCreateTableBudget);
            statement.executeUpdate(queryCreateTblCategoryByUser);
        } catch (SQLException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void initData() {

        createTables();

        try {
            if (!isFilesExist()) {
                
                //showMessage("File not exists");

                String queryInsertUsers = """        
                    INSERT INTO tblUsers (user_name, user_login, user_password, user_role) 
                    VALUES 
                           ('Admin', 'admin', 'admin', 1),
                           ('Olechka90', 'olechka90', 'Ol3chka!90', 0),
                           ('IvanPetrov', 'ivanpetrov', 'Iv@nP3tr0v', 0),
                           ('NinaS', 'ninas', 'N1n@S#2024', 0),
                           ('AlexeyK', 'alexeyk', 'Al3x3yK$', 0),
                           ('MariaL', 'marial', 'M@ri@L2024', 0),
                           ('SergeyT', 'sergeyt', 'S3rg3yT!', 0),
                           ('ElenaM', 'elenam', 'El3n@M#90', 0),
                           ('DmitryB', 'dmitryb', 'Dm1tryB$', 0),
                           ('OlgaV', 'olgav', '0lg@V2024', 0),
                           ('PavelD', 'paveld', 'P@v3lD!', 0),
                           ('TatianaR', 'tatianar', 'T@t1@n@R$', 0),
                           ('AndreyZ', 'andreyz', 'Andr3yZ#', 0);    
                """;

                String queryInsertCategories = """        
                    INSERT INTO tblRefCategories (category_name, category) VALUES 
                           ('Перевод на кошелек', 0), 
                           ('Прием из кошелька', 1), 
                           ('Еда', 0),
                           ('Развлечения', 0),
                           ('Коммунальные услуги', 0),
                           ('Такси', 0),
                           ('Зарплата', 1),
                           ('Бонус', 1);
                """;

                statement.executeUpdate(queryInsertUsers);
                statement.executeUpdate(queryInsertCategories);
                saveDatabaseToFiles();
            } else {
                //showMessage("File exists");
                loadDatabaseFromFile();
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void insertBudgetData(int amount, Date whenDate,
            int user_id, int category_id) {

        String insertQuery = """
                             INSERT INTO tblIncomeExpenses (category_id, user_id, 
                             when_date, amount) 
                             VALUES (?, ?, ?, ?)""";

        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, category_id);
            pstmt.setInt(2, user_id);
            pstmt.setDate(3, (java.sql.Date) whenDate);
            pstmt.setInt(4, amount);
            pstmt.executeUpdate();
        } catch (SQLException e) {
        }
    }

    public static ResultSet selectCategories(int category) {
        String selectQuery = """
                             SELECT *  
                             FROM tblRefCategories 
                             WHERE category = ?  
                             ORDER BY category_name;
                             """;
                
        ResultSet resultSet = null;

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, category);
            resultSet = pstmt.executeQuery();
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return resultSet;    
    }
    
    public static ResultSet selectUsersExt(int user_id) {
        String selectQuery = """
                             SELECT user_id, user_name
                             FROM tblUsers 
                             WHERE user_id <> ?  
                             ORDER BY user_name;
                             """;
                
        ResultSet resultSet = null;

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, user_id);
            resultSet = pstmt.executeQuery();
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return resultSet;    
    }
    
    public static void  showResultSetFormatted(ResultSet resultSet, String format, Object... params) throws SQLException {
        while (resultSet.next()) {
            Object[] formattedParams = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String) {
                    formattedParams[i] = resultSet.getString((String) params[i]);
                } else if (params[i] instanceof Integer) {
                    formattedParams[i] = resultSet.getInt((String) params[i]);
                } else if (params[i] instanceof Double) {
                    formattedParams[i] = resultSet.getDouble((String) params[i]);
                } else if (params[i] instanceof Boolean) {
                    formattedParams[i] = resultSet.getBoolean((String) params[i]);
                } else {
                    throw new IllegalArgumentException("Неподдерживаемый тип параметра: " + params[i].getClass().getName());
                }
            }
            System.out.println(String.format(format, formattedParams));     
        }
        System.out.println("=============");
    }

    public static int getTransferToWalletCategoryId() {
        return findCategoryIdByName("Перевод на кошелек");
    }
    
    public static int getReceiveFromWalletCategoryId() {
        return findCategoryIdByName("Прием из кошелька");
    }
    
    public static int SalaryCategoryId() {
        return findCategoryIdByName("Зарплата");
    }
    
    public static int getCategoryByCategoryId(int category_id) {
        String selectQuery = """
        SELECT category  
        FROM tblRefCategories 
        WHERE Category_id = ?;
        """;

        int category = -1;

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, category_id);

            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                category = resultSet.getInt("category");
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return category;    
    }

    public static ResultSet executeQueryWithParams(String sqlQuery, Object... params) {
        ResultSet resultSet = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer integer) {
                    pstmt.setInt(i + 1, integer);
                } 
                else if (param instanceof String string) {
                    pstmt.setString(i + 1, string);
                } 
                else if (param instanceof Double aDouble) {
                    pstmt.setDouble(i + 1, aDouble);
                } 
                else if (param instanceof Boolean aBoolean) {
                    pstmt.setBoolean(i + 1, aBoolean);
                } 
                else if (param instanceof java.util.Date utilDate) {
                    pstmt.setDate(i + 1, new java.sql.Date(utilDate.getTime()));
                } 
                else {
                    throw new IllegalArgumentException("Неподдерживаемый тип параметра: " 
                               + param.getClass().getName());
                }
            } 
            resultSet = pstmt.executeQuery();
        } 
        catch (SQLException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return resultSet;
    }
    
    public static void selectDataUsers() {

        String selectQuery = "SELECT * FROM tblUsers ORDER BY user_name";

        try {

            ResultSet resultSet = statement.executeQuery(selectQuery);
            // Вывод данных в консоль

            String user_name;
            String userLogin;
            String userPassword;
            int _user_id;
            int _user_role;

            while (resultSet.next()) {
                _user_id = resultSet.getInt("user_id");
                user_name = resultSet.getString("user_name");
                userLogin = resultSet.getString("user_login");
                userPassword = resultSet.getString("user_password");
                _user_role = resultSet.getInt("user_role");

                System.out.printf("ID: %d, Name: %s, Login: %s, Password: %s, Role: %d%n",
                        _user_id, user_name, userLogin, userPassword, _user_role);
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void selectDataBudget(int user_id, Date whenDate) {
        String selectQuery = """
              SELECT b.id, b.category_id, c.category_name, b.amount,
                     CASE WHEN c.category = 1 THEN 'доход' ELSE 'расход' END AS budget_flag
              FROM tblIncomeExpenses b JOIN tblRefCategories c ON b.category_id = c.Category_id 
              WHERE b.user_id = ? AND b.when_date = ?               
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, user_id);
            pstmt.setDate(2, (java.sql.Date) whenDate);

            ResultSet resultSet = pstmt.executeQuery();

            System.out.printf("Дата: %tF%n", whenDate);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String category = resultSet.getString("category_name");
                int amount = resultSet.getInt("amount");
                String budgetFlag = resultSet.getString("budget_flag");

                System.out.printf("ID: %d, Категория: %s, %s: %d%n",
                        id, category, budgetFlag, amount);
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
        public static void selectDataBudget(int user_id) {
        String selectQuery = """
              SELECT b.id, b.category_id, c.category_name, b.amount,
                     CASE WHEN c.category = 1 THEN 'доход' ELSE 'расход' END AS budget_flag
              FROM tblIncomeExpenses b JOIN tblRefCategories c ON b.category_id = c.Category_id 
              WHERE b.user_id = ?;                
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setInt(1, user_id);

            ResultSet resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String category = resultSet.getString("category_name");
                int amount = resultSet.getInt("amount");
                String budgetFlag = resultSet.getString("budget_flag");

                System.out.printf("ID: %d, Категория: %s, %s: %d%n",
                        id, category, budgetFlag, amount);
            }
        } catch (SQLException e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }
        
    public static <T> List<T> selectItems(ResultSet resultSet
                                            , String message
                                            , String userId
                                            , boolean isAdmin
                                            , ItemMapper<T> mapper) throws SQLException {
        List<T> items = new ArrayList<>();

        String m = message;
        if (isAdmin) {
           m += " (Код пользователя: " + userId + ")";
        }
        System.out.println(m);

        while (resultSet.next()) {
            items.add(mapper.map(resultSet));
        }

        int index = 1;
        for (T item : items) {
            System.out.println(index + ". " + item.toString());
            index++;
        }

        return items;
    }

    public interface ItemMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
}    
