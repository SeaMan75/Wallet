package wallet;

import java.util.Scanner;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;
import static wallet.Constants.WalletMode.EXPENSES;

public class Wallet {

    private Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

// Определение класса Category
    public Wallet() {
        Helper.initData();
    }

    private void subMenu() {

        boolean loop = true;
        int choice = 0;

        while (loop) {

            System.out.println("=== Меню: ===");
            System.out.println("1. Доходы");
            System.out.println("2. Расходы");
            if (Helper.isAdministrator()) {
                System.out.println("3. Список пользователей");
            }
            System.out.println("4. Просмотр бюджета за весь период");
            System.out.println("0. Возврат в главное меню");
            System.out.print("Выберите опцию: ");

            choice = Helper.readInteger(4, scanner);
            switch (choice) {
                case 1 -> {
                    AppendIncomeExpenses budget = new AppendIncomeExpenses(scanner);
                    budget.runBudget();
                }

                case 2 -> {
                    AppendIncomeExpenses budget = new AppendIncomeExpenses(scanner, EXPENSES);
                    budget.runBudget();
                }

                case 3 -> {
                    if (Helper.isAdministrator()) {
                        Helper.selectDataUsers();
                    }
                }
                case 4 -> {Helper.selectDataBudget(Helper.getUserId());}
                
                case 0 -> {
                    Helper.saveDatabaseToFiles();
                    loop = false;
                }

                default ->
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        } //while loop...        
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
