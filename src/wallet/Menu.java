package wallet;

import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Menu {

    private final Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void subMenu(String[] menuItems, Consumer<Integer>[] handlers) {
        boolean loop = true;

        while (loop) {
            System.out.println("=== Меню: ===");
            IntStream.range(0, menuItems.length).forEach(i
                    -> System.out.println((i + 1) + ". " + menuItems[i])
            );
            System.out.println("0. Возврат в главное меню");
            System.out.print("Выберите опцию: ");

            int choice = Helper.readInteger(menuItems.length, scanner);
            if (choice == 0) {
                handlers[menuItems.length].accept(choice); // Обработчик для выхода
                loop = false;
            } else if (choice > 0 && choice <= menuItems.length) {
                handlers[choice - 1].accept(choice);
            } else {
                System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }
    }
}
