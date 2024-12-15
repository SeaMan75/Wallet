package wallet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    @Override
        public String toString() {
            return name; // Возвращаем имя категории
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
        
        Date whenDate = Date.valueOf(LocalDate.now());
        
        // Привязываем расходную операцию к пользователю, если она еще не привязана:
        int budget;
        if (!Helper.isCategoryAssignedToUser(category1, user_id)) {
            if(Helper.getCategoryByCategoryId(category1) == 0) {
                System.out.println("Пожалуйста, введите бюджет: ");
                budget = Helper.readInteger(scanner);
            } else budget = 0;    
            Helper.assignCategoryToUser(category1, user_id, budget);
        }    
        Helper.insertBudgetData(amount, whenDate, user_id, category1);
    
        if (!Helper.isCategoryAssignedToUser(category2, other_user_id)) {
            budget = 0;    
            Helper.assignCategoryToUser(category1, other_user_id, budget);
        }    
        Helper.insertBudgetData(amount, whenDate, other_user_id, category2);
    }
    
    private void usersMenu() {
        System.out.println("====================================");
        System.out.println("Перевод на кошелек другому пользователю");
        System.out.println("====================================");

        ResultSet resultSet = Helper.selectUsersExt(user_id);

        String message = "=== Выберите получателя: ===";
        if (Helper.isAdministrator()) {
            message += " (Код пользователя: " + user_id + ")";
            System.out.println(message);
        }

        try {
            List<Transfer.User> users = Helper.selectItems(resultSet, message, String.valueOf(user_id), Helper.isAdministrator(), (var resultSet1) -> 
            new Transfer.User(resultSet1.getInt("user_id"), resultSet1.getString("user_name"))
            );

            var loop = true;
            int choice;

            while (loop) {
                choice = Helper.readInteger(users.size(), scanner);
                if (choice > 0 && choice <= users.size()) {
                    this.other_user_id = users.get(choice - 1).getId();
                    loop = false;
                }
            }

        } 
        catch (SQLException ex) {
                Logger.getLogger(Wallet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
