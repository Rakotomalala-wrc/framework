package util;

import annotations.AnnotationController;
import annotations.Get;
import annotations.Param;
import frameworks.ModelView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class Methode {

    public List<Class<?>> scanControllers(String packageName) {
        List<Class<?>> controllers = new ArrayList<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);
            if (resource == null) {
                System.err.println("No resource found for path: " + path);
                return controllers;
            }

            File directory = new File(resource.getFile());
            if (!directory.exists()) {
                System.err.println("Directory does not exist: " + directory.getAbsolutePath());
                return controllers;
            }

            File[] files = directory.listFiles();
            if (files == null) {
                System.err.println("No files found in directory: " + directory.getAbsolutePath());
                return controllers;
            }

            for (File file : files) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(AnnotationController.class)) {
                        controllers.add(clazz);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error scanning controllers: " + e.getMessage());
        }

        return controllers;
    }

    public List<String> getClassName(List<Class<?>> controllers) {
        List<String> controllersName = new ArrayList<>();
        for (Class<?> clazz : controllers) {
            if (clazz != null) {
                controllersName.add(clazz.getSimpleName());
            }
        }
        return controllersName;
    }

    public HashMap<String, Mapping> urlMethod(List<Class<?>> controllers, String url) {
        HashMap<String, Mapping> hashMap = new HashMap<>();
        for (Class<?> controller : controllers) {
            Method[] declaredMethods = controller.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    if(getAnnotation.value().equals(url)) {
                        Mapping mapping = new Mapping(controller.getName(), method.getName());
                        hashMap.put(getAnnotation.value(), mapping);
                    }
                }
            }
        }
        return hashMap;
    }

    public Mapping getMapping(HashMap<String, Mapping> hashMap) {
        for (Map.Entry<String, Mapping> entry : hashMap.entrySet()) {
            return entry.getValue();
        }
        return null;
    }

    public Object execute(Mapping mapping, HttpServletRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (mapping != null) {
            String className = mapping.getClassName();
            Class<?> clazz = Class.forName(className);

            // Find the method that matches the name and parameters
            Method method = getMethod(clazz, mapping.getMethodName(), request);

            List<String> parameterNames = getParameterNamesList(request);
            String[] parameterValues = new String[parameterNames.size()];

            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.size(); i++) {
                    parameterValues[i] = request.getParameter(parameterNames.get(i));
                }
            }

            Object instance = clazz.getDeclaredConstructor().newInstance();
            Object result;

            if (parameterValues.length > 0) {
                result = method.invoke(instance, (Object[]) parameterValues);
            } else {
                result = method.invoke(instance);
            }

            if (result instanceof String) {
                return result;
            } else if (result instanceof ModelView) {
                return result;
            } else {
                System.out.println("Le type de retour n'existe pas");
            }
        } else {
            System.out.println("Mapping not found");
        }
        return null;
    }

    private Method getMethod(Class<?> clazz, String methodName, HttpServletRequest request) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        Method targetMethod = null;

        // Obtenez la liste des noms de paramètres de la requête
        List<String> parameterNames = getParameterNamesList(request);

        for (Method method : methods) {
            // Vérifiez si le nom de la méthode correspond
            if (method.getName().equals(methodName)) {
                System.out.println(method.getName());
                Parameter[] parameters = method.getParameters();
                if (parameters.length == parameterNames.size()) {
                    boolean matches = true;
                    for(int i = 0; i < parameters.length; i++) {
                        Param paramAnnotation = parameters[i].getAnnotation(Param.class);
                        if (paramAnnotation != null) {
                            String paramName = paramAnnotation.name();
                            System.out.println(paramName);
                            System.out.println(parameterNames.get(i));
                            if (!parameterNames.get(i).equals(paramName)) {
                                matches = false;
                                break;
                            }
                        } else {
                            throw new IllegalArgumentException("Parameter annotation @Param not found for method parameter");
                        }
                    }
                    if (matches) {
                        targetMethod = method;
                        break;
                    }
                }
            }
        }

        if (targetMethod == null) {
            throw new NoSuchMethodException("No such method found with the given name and parameter names.");
        }
        return targetMethod;
    }

    public String getUrlAfterSprint(HttpServletRequest request) {
        // Extract the part after /sprint1
        String contextPath = request.getContextPath(); // This should be "/sprint1"
        String uri = request.getRequestURI(); // This should be "/sprint1/hola"
        return uri.substring(contextPath.length()); // This should be "/hola"
    }

    public List<String> getParameterNamesList(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();

        List<String> parameterNamesList = new ArrayList<>();

        while (parameterNames.hasMoreElements()) {
            parameterNamesList.add(parameterNames.nextElement());
        }

        return parameterNamesList;
    }

}
