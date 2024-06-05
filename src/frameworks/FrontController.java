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
    public Object result;
    public Object[] params;
    public String stringParam;
    public String variableName;
    public Object value;
    public String urlDispatcher;

    @Override
    public void init() throws ServletException {
        super.init();
        methode = new Methode();
        String packageName = getControllerPackageName();
        controllers = methode.scanControllers(packageName);
        controllersName = methode.getClassName(controllers);
        stringParam = "Ohatra fotsiny";
        variableName = "tsekijoby";
        value = 69;
        urlDispatcher = "/test.jsp";
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String urlString = request.getRequestURL().toString();

        url = methode.getUrlAfterSprint(request);

        mapping = methode.urlMethod(controllers, url);

        if (url.equals("/hola")) {
            params = new Object[]{stringParam};
        } else if (url.equals("/hole")) {
            params = new Object[]{variableName, value, urlDispatcher};
        }

        result = methode.execute(methode.getMapping(mapping), params);

        if(result instanceof String) {
            request.setAttribute("value", result);
        } else if (result instanceof ModelView) {
            request.setAttribute("data", ((ModelView) result).getData());
            request.getRequestDispatcher(((ModelView) result).getUrl()).forward(request, response);
        } else {
            throw new NoSuchMethodException("No such method found with the given name and parameter count.");
        }

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
