package wallet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import wallet.Constants.WalletMode;
import static wallet.Helper.readInteger;

public class Transfer {
    
    private Scanner scanner;
    private int user_id;
    private int category_id;
    private String user_name;
    private int other_user_id;

    class User {

        private final int id;
        private final String name;

        public User(int id, String name) {
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

    public Transfer(Scanner scanner){
        this.scanner = scanner;
        this.user_id = Helper.getUserId();
        this.user_name = Helper.getUserName();
    }
    
    public void transfer() {
        usersMenu();
        
        System.out.println("Перевод для " + Helper.getUserNameById(other_user_id));
        
        System.out.println("Пожалуйста, введите сумму перевода:");
        int amount = readInteger(scanner);
        
        int category1 = Helper.getTransferToWalletCategoryId();
        int category2 = Helper.getReceiveFromWalletCategoryId();
        
        Date whenDate = new Date();
        
        // Привязываем расходную операцию к пользователю, если она еще не привязана:
        int budget;
        if (!Helper.isCategoryAssignedToUser(category1, user_id)) {
            if(Helper.getCategoryByCategoryId(category1) == 0) {
                System.out.println("Пожалуйста, введите бюджет: ");
                budget = Helper.readInteger(scanner);
            } else budget = 0;    
            Helper.assignCategoryToUser(category1, user_id, budget);
        }    
        Helper.insertBudgetData(amount, whenDate, user_id, category1, WalletMode.EXPENSES);
    
        if (!Helper.isCategoryAssignedToUser(category2, other_user_id)) {
            budget = 0;    
            Helper.assignCategoryToUser(category1, other_user_id, budget);
        }    
        Helper.insertBudgetData(amount, whenDate, other_user_id, category2, WalletMode.INCOME);
    }
    
    private void usersMenu() {
        System.out.println("====================================");
        System.out.println("Перевод на кошелек другому пользователю");
        System.out.println("====================================");

        ResultSet resultSet = Helper.selectUsersExt(user_id);
        List<User> users = new ArrayList<>();

        int choice = 0;

        String m = "=== Выберите получателя: ===";
        if (Helper.isAdministrator()) {
            m += " (Код пользователя: " + user_id + ")";
        }

        try {
            while (resultSet.next()) {
                users.add(new Transfer.User(resultSet.getInt("user_id"), resultSet.getString("user_name")));
            }
            int index = 1;
            for (Transfer.User _user : users) {
                System.out.println(index + ". " + _user.getName());
                index++;
            }

            boolean loop = true;

            while (loop) {

                choice = Helper.readInteger(users.size(), scanner);
                if (choice > 0 && choice <= users.size()) {
                    this.other_user_id = users.get(choice - 1).getId();
                    loop = false;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
