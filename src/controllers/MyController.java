package controllers;

import annotations.AnnotationController;
import annotations.Get;

@AnnotationController
public class MyController {
    @Get(value = "/hola")
    public String hola(String value) {
        System.out.println("Value: " + value);
        return value;
    }

    public static void main (String[] args) {
        System.out.println("This is my controller");
    }
}
