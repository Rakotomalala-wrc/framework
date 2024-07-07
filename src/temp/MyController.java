package controllers;

import annotations.AnnotationController;
import annotations.Get;
import annotations.Param;
import frameworks.ModelView;
import util.Employe;

@AnnotationController
public class MyController {
    @Get(value = "/hola")
    public String hola() {
        return "Ohatra fotsiny";
    }

    @Get(value = "/hole")
    public ModelView hole() {
        String url = "/test.jsp";
        String variableName = "Mika&Davis";
        Object value = "tsekijoby";
        ModelView modelView = new ModelView(url);
        modelView.addObject(variableName, value);
        return modelView;
    }

    @Get(value = "/form")
    public String login(@Param(name = "username") String username, @Param(name = "password") String password) {
        return "Votre pseudo: " + username + ", votre mot de passe: " + password;
    }

    @Get(value = "/liste")
    public String liste(@Param(name = "emp") Employe emp) {
        return "Nom de l'employe: " + emp.getNom() + " et Age de l'employe: " + emp.getAge();
    }

}
