package wallet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static wallet.Helper.readInteger;

public class AppendIncomeExpenses {

    private final Scanner scanner;
    private final int user_id;
    private int category_id;
    private final String user_name;
    private final int category;

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
        @Override
        public String toString() {
            return name; // Возвращаем имя категории
    
        }
    }

    public AppendIncomeExpenses(Scanner scanner, int category) {
        this.scanner = scanner;
        this.user_id = Helper.getUserId();
        this.user_name = Helper.getUserName();
        this.category = category;
    }

    private void showData(Date whenDate) {
        System.out.println("Пользователь: " + user_name);
        System.out.println("====================================");
        Helper.selectDataBudget(user_id, whenDate);
        System.out.println("====================================");
    }
    
    public void createIncomeExpense() {
        categoryMenu();
        Date whenDate = Date.valueOf(LocalDate.now());

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

        Helper.insertBudgetData(amount, whenDate, user_id, category_id);
        showData(whenDate);
    }

    private void categoryMenu() {

        System.out.println("====================================");
        System.out.println("Доходы/расходы в зависимости от выбранной категории");
        System.out.println("====================================");

        ResultSet resultSet = Helper.selectCategories(this.category);
        
        String message = "=== Выберите категорию: ===";
        if (Helper.isAdministrator()) {
            message += " (Код пользователя: " + user_id + ")";
        }
        
        try {
            List<Category> categories = 
            Helper.selectItems(resultSet, message, String.valueOf(user_id)
                    , Helper.isAdministrator()
                    , (var resultSet1) 
                            -> new Category(resultSet1.getInt("category_id")
                                    , resultSet1.getString("category_name")));

                var loop = true;
                int choice;

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
                    SELECT SUM(ie.amount) AS amount
                    FROM tblIncomeExpenses ie
                    JOIN tblRefCategories rc ON ie.category_id = rc.Category_id
                    WHERE ie.user_id = ? AND rc.category = 1;
                    """, user_id);
            showAmount(resultSet);

            System.out.print("Общий расход: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT SUM(ie.amount) AS amount
                    FROM tblIncomeExpenses ie
                    JOIN tblRefCategories rc ON ie.category_id = rc.Category_id
                    WHERE ie.user_id = ? AND rc.category = 0;
                    """, user_id);
            showAmount(resultSet);
            
            System.out.print("Доход по категориям: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT c.category_name, sub.amount
                        FROM (
                            SELECT b.category_id, SUM(b.amount) amount
                            FROM tblIncomeExpenses b
                            WHERE b.user_id = ?
                            GROUP BY b.category_id
                        ) sub
                        JOIN tblRefCategories c ON c.category_id = sub.category_id
                    WHERE c.category = 1;
                    """, user_id);
            Helper.showResultSetFormatted(resultSet, "Категория: %s, Сумма: %s", "category_name", "amount");

            System.out.print("Расход по категориям: ");
            resultSet = Helper.executeQueryWithParams(
                    """
                    SELECT c.category_name, sub.amount
                    FROM (
                        SELECT b.category_id, SUM(b.amount) amount
                        FROM tblIncomeExpenses b
                        WHERE b.user_id = ?
                        GROUP BY b.category_id
                    ) sub
                    JOIN tblRefCategories c ON c.category_id = sub.category_id
                    WHERE c.category = 0;
                    """, user_id);
            Helper.showResultSetFormatted(resultSet, "Категория: %s, Сумма: %s", "category_name", "amount");
            
            System.out.print("Контроль бюджета: ");
            resultSet = Helper.executeQueryWithParams(
            """
            SELECT c.category_name, sub.amount, cu.budget,
            CASE
                WHEN sub.amount < cu.budget * 0.75 THEN 'в пределах бюджета'
                WHEN sub.amount >= cu.budget * 0.75 AND sub.amount < cu.budget THEN 'бюджет на исходе'
            ELSE 'за пределами бюджета'
            END AS budget_status
            FROM (
                SELECT b.category_id, SUM(b.amount) amount
                FROM tblIncomeExpenses b
                WHERE b.user_id = ?
                GROUP BY b.category_id
            ) sub
            JOIN tblRefCategories c ON c.category_id = sub.category_id
            JOIN tblCategoryByUser cu ON cu.category_id = sub.category_id AND cu.user_id = ?
            WHERE c.category = 0;
            """, user_id, user_id);

            Helper.showResultSetFormatted(resultSet, "Категория: %s, Сумма: %s"
                    + ", Бюджет: %s, Статус: %s"
                    , "category_name"
                    , "amount"
                    , "budget"
                    , "budget_status");
            
        } catch (SQLException ex) {
            Logger.getLogger(AppendIncomeExpenses.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public void showBalance(){
        
        ResultSet resultSet;
        String selectQuery = """
	SELECT 
            SUM(CASE WHEN c.category = 1 THEN e.amount ELSE 0 END) total_income,
            SUM(CASE WHEN c.category = 0 THEN e.amount ELSE 0 END) total_expenses,
            SUM(CASE WHEN c.category = 1 THEN e.amount ELSE -e.amount END) balance
        FROM 
            tblIncomeExpenses e
        JOIN 
            tblRefCategories c ON e.category_id = c.Category_id
        WHERE 
            e.when_date <= ? AND user_id = ?;
        """;
        Date now = Date.valueOf(LocalDate.now());
        try {

            
            System.out.println("Владелец кошелька: " + this.user_name);
            resultSet = Helper.executeQueryWithParams(selectQuery, now, user_id);
            Helper.showResultSetFormatted(resultSet, "Баланс: %s", "balance");
        } catch (SQLException ex) {
            Logger.getLogger(AppendIncomeExpenses.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        System.out.println("=============");
    }
    
    public void toIssueAsalary(int salary) {
        int SalaryCategoryId = Helper.SalaryCategoryId();
        
        if (!Helper.isCategoryAssignedToUser(SalaryCategoryId, this.user_id)) {
            Helper.assignCategoryToUser(SalaryCategoryId, this.user_id, 0);
        }    
        Date whenDate = Date.valueOf(LocalDate.now());
        Helper.insertBudgetData(salary, whenDate, this.user_id, SalaryCategoryId);
    }
}




