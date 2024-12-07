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
import static wallet.Constants.WalletMode.BUDGET;
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
        this(scanner, Constants.WalletMode.EXPENSES);
    }

    private void showData(Date whenDate) {
        System.out.println("Пользователь: " + user_name);
        System.out.println("====================================");
        Helper.selectDataBudget(user_id, whenDate);
        System.out.println("====================================");
    }
    
    public void runBudget() {
        categoryMenu();
        System.out.println("Пожалуйста, введите дату в формате YYYY-mm-dd:");
        Date whenDate = readDate(scanner);

        System.out.println("Пожалуйста, введите сумму:");
        int amount = readInteger(scanner);

        Helper.insertBudgetData(amount, whenDate, user_id, category_id, walletMode);
        
        showData(whenDate);
    }

    private void categoryMenu() {

        String s = walletMode == BUDGET ? "* БЮДЖЕТ * " : "* РАСХОДЫ *";

        System.out.println("====================================");
        System.out.println(s);
        System.out.println("====================================");

        ResultSet resultSet = Helper.selectCategories();
        List<Category> categories = new ArrayList<>();

        int choice = 0;

        String m = "=== Выберите категорию: ===";
        if (Helper.isAdministrator()) {
            m += " (Код пользователя: " + user_id + ")";
        }
        System.out.println(m);

        try {
            while (resultSet.next()) {
                categories.add(new Category(resultSet.getInt("category_id"), resultSet.getString("category_name")));
            }
            int index = 1;
            for (Category category : categories) {
                System.out.println(index + ". " + category.getName());
                index++;
            }

            boolean loop = true;

            while (loop) {

                choice = Helper.readInteger(categories.size(), scanner);
                if (choice > 0 && choice < categories.size()) {
                    this.category_id = categories.get(choice - 1).getId();
                    loop = false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Wallet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
