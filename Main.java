import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application. 
 * Le rôle de cette classe est minimal : elle instancie le contrôleur et lance le jeu.
 * 
 * Décomposition – Modèle Vue : Exemple : ClickCounter
•  A la différence du code précédent, Main crée maintenant un contrôleur à qui il passe la fenêtre de
   l’application JavaFX via le constructeur.

    Note : Comme précédemment, la méthode main aurait pu se retrouver dans la vue.
    Il a été choisi
    de créer une classe Main explicite décharger ClickCounterController, 
    et permettre + de lisibilité.

 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Instancie le contrôleur principal en passant la fenêtre principale
        GameController controller = new GameController(primaryStage);
    }

    public static void main(String[] args) {
        // Démarre l'application JavaFX
        launch(args);
    }
}
