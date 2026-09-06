import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Tree extends GameElement {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final String TREE_LIGHT_IMAGE = "/img/treeLight.png";
    private static final String TREE_NORMAL_IMAGE = "/img/treeNormal.png";
    private static final String TREE_DARK_IMAGE = "/img/treeDark.png";

    private static final String TREE_TRANSPARENT_IMAGE = "/img/transparent.png";
    private static final int DEFAULT_WOOD_THRESHOLD = 60;

    private static final int MAX_WOOD = 100; // Quantité maximale de bois
    private static final int MIN_WOOD = 0;   // Quantité minimale de bois
    private static final int RESPAWN_TIME = 30;

    private int startWood;
    private int wood;
    private boolean isDispo; // Indique si l'arbre est disponible

    private static int treeSize = 1;

    // Constructeur par défaut
    public Tree(int x, int y) {
        super(x, y, treeSize, TREE_DARK_IMAGE);
        this.wood = controller.getRandomBetween(MIN_WOOD + 1, MAX_WOOD + 1);
        this.startWood = wood;
        this.isDispo = wood <= 0 ? false : true; // Initialement disponible
        updateImage();
    }

    // Met à jour l'image en fonction de la quantité de bois
    private void updateImage() {
        if (wood >= DEFAULT_WOOD_THRESHOLD) {
            setImagePath(TREE_DARK_IMAGE);
        } 
        else if(wood >= DEFAULT_WOOD_THRESHOLD/2){
            setImagePath(TREE_NORMAL_IMAGE);
        }
        else{
            setImagePath(TREE_LIGHT_IMAGE);
        }
    }

    // Accesseurs pour la quantité de bois
    public int getStartWood() {
        return startWood;
    }

    public int getWood() {
        return wood;
    }

    public boolean isDispo() {
        return isDispo;
    }

    public void setWood(int wood) {
        this.wood = wood;
        updateImage();
    }

    // Réduit la quantité de bois et gère l'état de l'arbre
    public void reduceWoodAmount() {
        if (!isDispo) return;

        wood -= 5;
        if (wood <= 0) {
            wood = 0;
            scheduleTreeReappearance(); // Planifie la réapparition
        } else if (wood < DEFAULT_WOOD_THRESHOLD) {
            updateImage();
        }
    }

    // Planifie la réapparition de l'arbre après 30 secondes
    private void scheduleTreeReappearance() {
        isDispo = false; // L'arbre devient indisponible
        setImagePath(TREE_TRANSPARENT_IMAGE); // L'arbre disparaît graphiquement

        scheduler.schedule(() -> {
            wood = startWood; // Restaurer la quantité de bois initiale
            isDispo = true; // Rendre l'arbre à nouveau disponible
            updateImage(); // Mettre à jour l'image
        }, RESPAWN_TIME, TimeUnit.SECONDS); // Réapparaît après 30 secondes
    }
}
