package wallet;

import java.util.Scanner;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class Wallet {

    private Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

// Определение класса Category
    public Wallet() {
        Helper.initData();
    }

    private void subMenu() {

        Menu menu = new Menu(scanner);

        String[] menuItems = {
            "Доходы"
            , "Расходы"
            , "Список пользователей"
            , "Просмотр бюджета за весь период"
            , "Создание категории"    
            , "Просмотр категорий по пользователям"
            , "Общая сумма доходов и расходов и по категориям"
            , "Перевод на другой кошелек"    
            , "Начислить зарплату"    
                
        };

        Consumer<Integer>[] handlers = new Consumer[]{
            choice -> {
                AppendIncomeExpenses a = new AppendIncomeExpenses(scanner, 1);
                a.createIncomeExpense();
            },
            choice -> {
                AppendIncomeExpenses a = new AppendIncomeExpenses(scanner, 0);
                a.createIncomeExpense();
            },

            choice -> {
                if (Helper.isAdministrator()) { //Список пользователей
                    Helper.selectDataUsers();
                }
            },
            choice -> Helper.selectDataBudget(Helper.getUserId()),  //Просмотр бюджета
            
            choice -> {
                CategoryManager manager = new CategoryManager(scanner);
                manager.createCategory();
            },

            choice -> {
                Helper.showAssignedCategories();
            },
            choice -> {
                AppendIncomeExpenses a = new AppendIncomeExpenses(scanner, 0);
                a.showIncomeAndExpenses();
            },

            choice -> {
                Transfer a = new Transfer(scanner);
                a.transfer();
            },
            choice -> {
                AppendIncomeExpenses a = new AppendIncomeExpenses(scanner, 0);
                a.toIssueAsalary(700000);
                a.showBalance();
            },

            
            //===================================    

            choice -> Helper.saveDatabaseToFiles() // Обработчик для выхода
        };

        menu.subMenu(menuItems, handlers);
    }

    private void clearConsole() {

        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void mainMenu() {
        boolean loop = true;
        int choice = 0;

        while (loop) {

            clearConsole();
            System.out.println("=== Меню: ===");
            System.out.println("1. Авторизация");
            System.out.println("0. Выход");
            System.out.print("Выберите опцию: ");

            choice = Helper.readInteger(1, scanner);
            switch (choice) {
                case 1 -> {
                    Helper.authenticateUser();
                    System.out.println(Helper.Welcome());
                    if (Helper.isAuthorized()) {
                        Helper.getUserId();
                        AppendIncomeExpenses a = new AppendIncomeExpenses(scanner, 0);
                        a.showBalance();
                        subMenu();
                    }
                }

                case 0 -> {
                    System.out.println("Завершение работы...");
                    Helper.saveDatabaseToFiles();
                    System.exit(0);
                }

                default ->
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }

        }  //while running...
    }

    public static void main(String[] args) {
        // Установка кодировки UTF-8 для вывода в консоль
        Wallet wallet = new Wallet();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        wallet.mainMenu();
    }
}
