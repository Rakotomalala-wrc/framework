package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String urlString = request.getRequestURL().toString();

        request.setAttribute("url", urlString);

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    public static List<String> scanControllers(String packageName) {
        List<String> controllersName = new ArrayList<>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File dir = new File(resource.getFile());
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        String className = file.getName().replaceAll(".class$", "");
                        Class<?> clazz = Class.forName(packageName + "." + className);
                        if (clazz.isAnnotationPresent(AnnotationController.class)) {
                            controllersName.add(clazz.getName());
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return controllersName;
    }

    private String getControllerPackageName() {
        return getServletContext().getInitParameter("controller-package");
    }    

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String packageName = getControllerPackageName();
        List<String> controllers = scanControllers(packageName);
        // Utilisez la liste de contrôleurs comme vous le souhaitez
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String packageName = getControllerPackageName();
        List<String> controllers = scanControllers(packageName);
        // Utilisez la liste de contrôleurs comme vous le souhaitez
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "FrontController Servlet";
    }
}
