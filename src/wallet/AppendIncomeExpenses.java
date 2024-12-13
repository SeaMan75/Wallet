package wallet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import wallet.Constants.WalletMode;
import static wallet.Constants.WalletMode.INCOME;
import static wallet.Helper.readDate;
import static wallet.Helper.readInteger;

public class AppendIncomeExpenses {

    private Scanner scanner;
    private int user_id;
    private int category_id;
    private String user_name;
    WalletMode walletMode;

    class Category {

        private final int id;
        private final String name;

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public AppendIncomeExpenses(Scanner scanner, WalletMode walletMode) {
        this.scanner = scanner;
        this.user_id = Helper.getUserId();
        this.user_name = Helper.getUserName();
        this.walletMode = walletMode;
    }

    public AppendIncomeExpenses(Scanner scanner) {
        this(scanner, Constants.WalletMode.INCOME);
    }

    private void showData(Date whenDate) {
        System.out.println("Пользователь: " + user_name);
        System.out.println("====================================");
        Helper.selectDataBudget(user_id, whenDate);
        System.out.println("====================================");
    }
    
    public void createIncomeExpense() {
        categoryMenu();
        System.out.println("Пожалуйста, введите дату в формате YYYY-mm-dd:");
        Date whenDate = readDate(scanner);

        System.out.println("Пожалуйста, введите сумму:");
        int amount = readInteger(scanner);
        
        int budget;
        if (!Helper.isCategoryAssignedToUser(category_id, user_id)) {
            if(Helper.getCategoryByCategoryId(category_id) == 0) {
                System.out.println("Пожалуйста, введите бюджет: ");
                budget = Helper.readInteger(scanner);
            } else budget = 0;    
            Helper.assignCategoryToUser(category_id, user_id, budget);
        }    

        Helper.insertBudgetData(amount, whenDate, user_id, category_id, walletMode);
        showData(whenDate);
    }

    private void categoryMenu() {

        String s = walletMode == INCOME ? "* (ДОХОДЫ) * " : "* (РАСХОДЫ) *";
        int category = walletMode == INCOME ? 1 : 0;

        System.out.println("====================================");
        System.out.println(s);
        System.out.println("====================================");

        ResultSet resultSet = Helper.selectCategories(category);
        List<Category> categories = new ArrayList<>();

        int choice = 0;

        String m = "=== Выберите категорию: ===";
        if (Helper.isAdministrator()) {
            m += " (Код пользователя: " + user_id + ")";
        }

        try {
            while (resultSet.next()) {
                categories.add(new Category(resultSet.getInt("category_id"), resultSet.getString("category_name")));
            }
            int index = 1;
            for (Category _category : categories) {
                System.out.println(index + ". " + _category.getName());
                index++;
            }

            boolean loop = true;

            while (loop) {

                choice = Helper.readInteger(categories.size(), scanner);
                if (choice > 0 && choice <= categories.size()) {
                    this.category_id = categories.get(choice - 1).getId();
                    loop = false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Wallet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void showFormatted(ResultSet resultSet, String format, Object... params) throws SQLException {
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
    
    private void showAmount(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            System.out.println(resultSet.getInt("amount"));
        }
        System.out.println("=============");
    }
    
    public void showIncomeAndExpenses() {
        try {
            System.out.println("Пользователь: " + user_name);
            System.out.print("Общий доход: ");
            ResultSet resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT SUM(amount) amount FROM tblIncomeExpenses
                    WHERE user_id = ? AND budget_flag = 1;
                    """, user_id);
            showAmount(resultSet);

            System.out.print("Общий расход: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT SUM(amount) amount FROM tblIncomeExpenses
                    WHERE user_id = ? AND budget_flag = 0;
                    """, user_id);
            showAmount(resultSet);
            
            System.out.print("Доход по категориям: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT c.category_name, SUM(amount) amount FROM tblIncomeExpenses b
                    JOIN tblRefCategories c ON c.category_id = b.category_id
                    WHERE b.user_id = ? AND b.budget_flag = 1
                    GROUP BY c.category_name
                    """, user_id);
            showFormatted(resultSet, "Категория: %s, Сумма: %s", "category_name", "amount");

            System.out.print("Расход по категориям: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT c.category_name, SUM(amount) amount FROM tblIncomeExpenses b
                    JOIN tblRefCategories c ON c.category_id = b.category_id
                    WHERE b.user_id = ? AND b.budget_flag = 0
                    GROUP BY c.category_name
                    """, user_id);
            showFormatted(resultSet, "Категория: %s, Сумма: %s", "category_name", "amount");
            
            
            
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(AppendIncomeExpenses.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
