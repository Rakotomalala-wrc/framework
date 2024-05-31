package frameworks;

import util.Mapping;
import util.Methode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    public HashMap<String, Mapping> mapping;
    public List<Class<?>> controllers;
    public List<String> controllersName;
    public Methode methode;
    public String url;

    @Override
    public void init() throws ServletException {
        super.init();
        methode = new Methode();
        String packageName = getControllerPackageName();
        controllers = methode.scanControllers(packageName);
        controllersName = methode.getClassName(controllers);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String urlString = request.getRequestURL().toString();

        url = methode.getUrlAfterSprint1(request);

        mapping = methode.urlMethod(controllers, url);

        String value = methode.execute(methode.getMapping(mapping));

        request.setAttribute("value", value);
        request.setAttribute("mapping", mapping);
        request.setAttribute("url", urlString);
        request.setAttribute("controllers", controllersName);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getControllerPackageName() {
        ServletConfig cg = getServletConfig();
        return cg.getInitParameter("controller-package");
    }

    @Override
    public String getServletInfo() {
        return "FrontController Servlet";
    }
}
