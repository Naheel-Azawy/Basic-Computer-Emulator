package app;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Updater.dealWithIt();
        new Console(args, new Scanner(System.in), System.out);
    }

}
