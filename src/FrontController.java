import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "FrontController", urlPatterns = { "/FrontController", "/otherPath" })
public class FrontController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Récupérer l'URL de la requête
        String url = request.getRequestURL().toString();

        // Afficher l'URL
        System.out.println("URL: " + url);

        // Vous pouvez ajouter ici votre logique de traitement des requêtes
        // Par exemple, rediriger vers d'autres servlets en fonction de l'URL

        // Répondre à la requête
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println("URL: " + url);
    }
}
