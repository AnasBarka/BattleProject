import java.util.List;

public abstract class Collectable extends GameElement {

    public boolean isAdjacent(Collectable item, Unit unit) {
        int deltaX = Math.abs(item.getX() - unit.getX());
        int deltaY = Math.abs(item.getY() - unit.getY());
        return (deltaX <= 1 && deltaY <= 1); // Adjacence horizontale, verticale ou diagonale
    }
    
    // Constructeur
    public Collectable(int x, int y, int size, String imagePath) {
        super(x, y, size, imagePath); // Appelle le constructeur de GameElement
    }

    // Action effectuée lorsqu'une unité interagit avec le collectable
    public abstract void onCollect(Unit unitWhoCollect, List<Unit> allUnits);
}
