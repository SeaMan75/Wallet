package wallet;

import java.util.Scanner;

public class CategoryManager {
    
    private Scanner scanner;
    private int user_id;
    private String user_name;
   
    public CategoryManager(Scanner scanner) {
        this.scanner = scanner;
    }
    
    public void createCategory() {
        this.user_id = Helper.getUserId();
        this.user_name = Helper.getUserName();
        int budget = 0;
        int category = 0;
        
        Helper.showAssignedCategories();
        
        System.out.println("Пожалуйста, введите название категории, " + this.user_name + ": ");
        if (scanner.hasNextLine()) {
            scanner.nextLine(); // Очистка буфера
        }
        String categoryName = scanner.nextLine();
        int category_id = Helper.findCategoryIdByName(categoryName);

        if (category_id == -1) {
            System.out.println("Пожалуйста, выберите статью: 1 - доходы, 0 - расходы");
            category = Helper.readInteger(1, scanner);
            category_id = Helper.addCategory(categoryName, category);
        }
        
        if (!Helper.isCategoryAssignedToUser(category_id, user_id)) {
            if(category == 0) {
                System.out.println("Пожалуйста, введите бюджет, " + this.user_name + ": ");
                budget = Helper.readInteger(scanner);
            }    
            Helper.assignCategoryToUser(category_id, user_id, budget);
        }    
        Helper.showAssignedCategories();
    }
}
