import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.Image;
import javafx.scene.Group;
import javafx.stage.Stage;

import java.util.List;
import java.util.Set;
/*La vue est responsable de l'affichage graphique. 
Elle doit contenir tout le code qui configure le Canvas, 
les éléments graphiques, et la gestion du dessin via GraphicsContext */
public class GameView {

    private static final int WIDTH = 800;
    private static final int HEIGHT = WIDTH;
    private static int SQUARE_SIZE;

    private Canvas canvas;
    private GraphicsContext gc;

    private GameController controller;

    public GameView(GameController controller) {
        this.controller = controller;
    }

    public void initialize(Stage primaryStage, List<GameElement> gameElements, Coordinate GRID_XY) {
        Group root = new Group();
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake Map Only");
        primaryStage.show();

        this.SQUARE_SIZE = WIDTH / GRID_XY.y;
        // Dessiner les éléments du jeu initialement
        drawGameElements(gameElements);

        // Ajouter la gestion des raccourcis clavier
        scene.setOnKeyPressed(event -> controller.handleKeyPress(event.getCode()));    }

    public void drawBackground(int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                gc.setFill((i + j) % 2 == 0 ? Color.web("FFFFFF") : Color.web("647687"));
                gc.fillRect(i * SQUARE_SIZE, j * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }


    public void drawGameElements(List<GameElement> gameElements) {
        
        for (GameElement element : gameElements) {
            Image elementImage = new Image(element.getImagePath());
            gc.drawImage(elementImage,
                element.getX() * SQUARE_SIZE,
                element.getY() * SQUARE_SIZE,
                SQUARE_SIZE * element.getSize(),
                SQUARE_SIZE * element.getSize());       
        }
    }
    

    public void drawWoodCarried(int woodCollected, int cityX, int cityY) {
        // Assurez-vous que vous avez un objet Graphics (gc) pour dessiner sur l'écran.
        // Calculez les coordonnées de la ville sur la grille
        // Chaque case de la grille a une taille de SQUARE_SIZE
        int gridX = cityX * SQUARE_SIZE;  // Position X de la ville dans la grille
        int gridY = cityY * SQUARE_SIZE;  // Position Y de la ville dans la grille

        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Digital-7", 20));

        // Affiche le compteur de bois collecté à côté de la ville
        // Vous pouvez ajuster l'offset selon votre besoin pour que le texte soit bien positionné
        gc.fillText(""+woodCollected, (gridX + SQUARE_SIZE / 3)+10, (gridY + SQUARE_SIZE / 8) + 20);    
    }

}
