package controllers;

import annotations.AnnotationController;
import annotations.Get;
import frameworks.ModelView;

@AnnotationController
public class MyController {
    @Get(value = "/hola")
    public String hola(String value) {
        return value;
    }

    @Get(value = "/hole")
    public ModelView hole(String variableName, Object value, String url) {
        ModelView modelView = new ModelView(url);
        modelView.addObject(variableName, value);
        return modelView;
    }
}
