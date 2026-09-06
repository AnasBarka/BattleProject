//dans les model faut pas avoir de logique javafx
/*Le modèle contient la logique de l'application, 
comme la gestion du jeu (états, règles, etc.). */

import java.util.List;

public class GameElement {
    private int x; // Position X
    private int y; // Position Y
    private String imagePath; // Chemin de l'image associée
    private int size;
// Le champ `controller` est défini comme `protected` pour les raisons suivantes :
// 1. Accès direct par les sous-classes : Permet aux sous-classes (comme City) d'accéder au contrôleur sans utiliser de getter,
//    ce qui simplifie le code et améliore la lisibilité.
// 2. Encapsulation partielle : Limite l'accès au champ à la hiérarchie de classes (sous-classes de GameElement)
//    et au même package, tout en empêchant des modifications depuis des classes externes.
// 3. Comportement intentionnel : Le contrôleur est un champ partagé (statique) destiné à être utilisé uniquement
//    par les éléments de jeu. Le rendre `protected` explicite son usage dans le contexte prévu.
//
// Alternatives :
// - `private` avec getter : Garantit une encapsulation maximale mais oblige les sous-classes à appeler un getter, 
//   ce qui alourdit le code si l'accès est fréquent.
// - `public` : Permet un accès direct depuis n'importe où, mais casse l'encapsulation, rendant le champ vulnérable 
//   à des modifications non contrôlées.
//
// Conclusion : `protected` est un compromis équilibré pour ce cas précis, car il allie simplicité et contrôle.

    protected static GameController controller; // Contrôleur commun pour tous les éléments du jeu


    // Constructeur
    public GameElement(int x, int y, int size, String imagePath) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.imagePath = imagePath;

    }

    // Accesseurs

    public Coordinate getPosition(){
        return new Coordinate(x, y);  
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;

    }

    public int getSize() {
        return size;
    }

    public void setSize(int elementSize) {
        this.size = elementSize;
    }

    // Setter pour injecter le contrôleur
    public static void setController(GameController gameController) {
        controller = gameController;
    }
}