package util;

import annotations.AnnotationController;
import annotations.Get;
import annotations.Param;
import annotations.Restapi;
import com.google.gson.Gson;
import frameworks.ModelView;
import frameworks.MySession;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Field;
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

            List<String> FormFieldsNames = getFieldsNamesList(request);
            Object[] parameterValues = new Object[method.getParameterCount()];
            System.out.println(FormFieldsNames.size() + " " + method.getParameterCount());
            Employe emp = new Employe();

            boolean empPopulated = false;
            for (int i = 0; i < method.getParameterCount(); i++) {
                Class<?> paramType = method.getParameterTypes()[i];
                if (paramType == MySession.class) {
                    parameterValues[i] = new MySession(request.getSession());
                } else if (i < FormFieldsNames.size()) {
                    //String parameterName = FormFieldsNames.get(i);
                    if (FormFieldsNames.get(i).contains(".")) {
                        while(i < FormFieldsNames.size()) {
                            populateEmploye(request, FormFieldsNames.get(i), emp);
                            empPopulated = true;
                            i++;
                        }
                    } else {
                        parameterValues[i] = request.getParameter(FormFieldsNames.get(i));
                    }
                }
            }

            if (empPopulated) {
                for (int i = 0; i < parameterValues.length; i++) {
                    if (parameterValues[i] == null && method.getParameterTypes()[i] == Employe.class) {
                        parameterValues[i] = emp;
                        break;
                    }
                }
            }

            Object instance = clazz.getDeclaredConstructor().newInstance();
            Object result;

            if (parameterValues.length > 0) {
                result = method.invoke(instance, parameterValues);
            } else {
                result = method.invoke(instance);
            }

            if (method.isAnnotationPresent(Restapi.class)) {
                if (result instanceof ModelView) {
                    ModelView mv = (ModelView) result;
                    return convertToJson(mv.getData());
                } else {
                    return convertToJson(result);
                }
            } else {
                return result;
            }
        } else {
            System.out.println("Mapping not found");
        }
        return null;
    }

    private void populateEmploye(HttpServletRequest request, String parameterName, Employe emp) throws IllegalAccessException {
        Class<?> clazzemp = emp.getClass();
        Field[] fields = clazzemp.getDeclaredFields();
    
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String requestParamName = parameterName.endsWith(fieldName) ? parameterName : parameterName + "." + fieldName;
            String value = request.getParameter(requestParamName);
            
            if (value != null && !value.isEmpty()) {
                try {
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(emp, Integer.parseInt(value));
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        field.set(emp, Long.parseLong(value));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.set(emp, Double.parseDouble(value));
                    } else if (field.getType() == float.class || field.getType() == Float.class) {
                        field.set(emp, Float.parseFloat(value));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(emp, Boolean.parseBoolean(value));
                    } else {
                        field.set(emp, value);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing value for field " + fieldName + ": " + e.getMessage());
                }
            }
        }
    }

    private Method getMethod(Class<?> clazz, String methodName, HttpServletRequest request) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        List<String> parameterNames = getFieldsNamesList(request);

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (paramSize(method, parameterNames)) {
                    Parameter[] parameters = method.getParameters();
                    boolean matches = true;
                    int formParamIndex = 0;

                    for (Parameter param : parameters) {
                        if (param.getType() == MySession.class) {
                            continue;  // Ignorer les paramètres MySession dans la comparaison
                        }

                        Param paramAnnotation = param.getAnnotation(Param.class);
                        if (paramAnnotation != null) {
                            String paramName = paramAnnotation.name();
                            if (formParamIndex >= parameterNames.size() ||
                                    !parameterNames.get(formParamIndex).equals(paramName) &&
                                            !parameterNames.get(formParamIndex).startsWith(paramName + ".")) {
                                matches = false;
                                break;
                            }
                            formParamIndex++;
                        } else {
                            throw new IllegalArgumentException("Parameter annotation @Param not found for method parameter : ETU002604");
                        }
                    }

                    if (matches) {
                        return method;
                    }
                }
            }
        }

        throw new NoSuchMethodException("No such method found with the given name and parameter names.");
    }

    public String getUrlAfterSprint(HttpServletRequest request) {
        // Extract the part after /sprint1
        String contextPath = request.getContextPath(); // This should be "/sprint1"
        String uri = request.getRequestURI(); // This should be "/sprint1/hola"
        return uri.substring(contextPath.length()); // This should be "/hola"
    }

    public List<String> getFieldsNamesList(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();

        List<String> parameterNamesList = new ArrayList<>();

        while (parameterNames.hasMoreElements()) {
            parameterNamesList.add(parameterNames.nextElement());
        }

        return parameterNamesList;
    }
                                            //Liste des champs du formulaire
    public boolean paramSize(Method method, List<String> FormFieldsNames) {
        Parameter[] parameters = method.getParameters();
        int formFieldCount = FormFieldsNames.size();
        int methodParamCount = parameters.length;
        int specialParamCount = 0;

        for (Parameter param : parameters) {
            if (param.getType() == MySession.class) {
                specialParamCount++;
            }
        }

        if (isObject(FormFieldsNames)) {
            // Logique existante pour les objets
            int argumentCount = 0;
            for (Parameter parameter : parameters) {
                if (parameter.getType().isPrimitive() || parameter.getType() == String.class) {
                    argumentCount++;
                    continue;
                }
                if (parameter.getType() == MySession.class) {
                    continue;  // Ne pas compter MySession comme un argument de formulaire
                }
                Class<?> argClass = parameter.getType();
                Field[] fields = argClass.getDeclaredFields();
                argumentCount += fields.length;
            }
            return argumentCount == formFieldCount;
        } else {
            // Comparer le nombre de champs de formulaire au nombre de paramètres
            // de méthode, en excluant les paramètres spéciaux comme MySession
            return formFieldCount == (methodParamCount - specialParamCount);
        }
    }

    public boolean isObject(List<String> FormFieldsNames) {
        boolean isObjet = false;
        for(String paramName : FormFieldsNames) {
            if (paramName.contains(".")) {
                isObjet = true;
                break;
            }
        }
        return isObjet;
    }

    public String convertToJson(Object objet) {
        Gson gson = new Gson();
        return gson.toJson(objet);
    }

    public boolean isJsonResponse(Mapping mapping) throws ClassNotFoundException {
        if (mapping == null) {
            return false;
        }
        Class<?> clazz = Class.forName(mapping.getClassName());
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(mapping.getMethodName())) {
                return method.isAnnotationPresent(Restapi.class);
            }
        }
        return false;
    }

}
