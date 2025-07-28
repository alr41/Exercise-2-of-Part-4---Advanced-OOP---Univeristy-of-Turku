package fi.utu.tech.ooj.exercise4.exercise1;

import java.io.IOException;

public class Exercise1 {
    public Exercise1() {
        System.out.println("Exercise 1");

        try (var zipper = new TestZipper("books.zip")) {
            zipper.run();
        } catch (IOException e) {
            System.err.println("Execution failed!");
            e.printStackTrace();
        }
    }
}
