import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Collector extends Unit {
    private static final String COLLECTOR_CITY1_PATH = "/img/Collector1.png";
    private static final String COLLECTOR_CITY2_PATH = "/img/Collector2.png";
    
    private int woodCarried;  // Quantité de bois que le collecteur porte
    private boolean isCollectingWood; // Indique si le collecteur est en train de collecter du bois
    private static int maxWood = 25;

    private static int maxHealth = 150;  // Points de vie maximum
    private static int attackDamage = 5;  // Dégâts infligés par attaque
    private static final int unitSize = 1;

    private static final Map<Class<? extends Unit>, Double> attackBonuses = new HashMap<>();

    static {
        attackBonuses.put(Collector.class, 1.0); // Bonus contre Deserter
    }


    protected static final int UNIT_GENERATE_INTERVAL = 5;
    protected static final int NEEDED_WOOD_FOR_GENERATE_UNIT = 0;

    private ArrayList<Coordinate> bugPositions = new ArrayList<>();



    // Constructeur
    public Collector(int x, int y, City homeCity) {
        super(x, y, unitSize, homeCity.getIsTeam1() ? COLLECTOR_CITY1_PATH : COLLECTOR_CITY2_PATH, homeCity, maxHealth, attackDamage);
        this.woodCarried = 0;
        this.isCollectingWood = false;
        this.bugPositions = this.listePosBug(bugPositions);
    }
   

    // Accesseurs et mutateurs
    public int getWoodCarried() {
        return woodCarried;
    }

    public void setWoodCarried(int woodCarried) {
        this.woodCarried = woodCarried;
    }

    public ArrayList<Coordinate> getBugPos() {
        return bugPositions;
    }


    public boolean isFull() {
        return woodCarried == maxWood;
    }

    public boolean isCollectingWood() {
        return isCollectingWood;
    }

    public void setIsCollectingWood(boolean isCollectingWood) {
        this.isCollectingWood = isCollectingWood;
    }

    // Collecter du bois à partir de l'arbre
    public void collectWood(Tree tree) {//bizzar condition
        if (tree != null && woodCarried < maxWood && tree.isDispo()) {
            this.setIsCollectingWood(true);
            tree.reduceWoodAmount();
            woodCarried += this.getAttackDamage();
            //System.out.println("bois dans le collector"+this.getWoodCarried());
        } else {
            this.setIsCollectingWood(false);
        }
    }

    // Déposer le bois dans la ville
    public void depositWood() {
        if (isFull()) {
            getHomeCity().depositWood(woodCarried);
            //System.out.println("depose le bois en ville");
            woodCarried = 0;
        }
    }

    // Méthode pour trouver l'arbre le plus proche
    public Tree findClosestTree(List<GameElement> gameElements) {
        Tree closestTree = null;
        double minDistance = Double.MAX_VALUE;

        // Parcourt tous les éléments pour trouver l'arbre le plus proche
        for (GameElement element : gameElements) {
            if (element instanceof Tree) {
                Tree tree = (Tree) element;
                //si elle a de wood et une position adjacent disponible
                if (tree.isDispo()) {
                    // Calculer la distance entre le collecteur et l'arbre
                    double distance = this.getPosition().getDistanceWith(tree.getPosition());

                    // Si l'arbre est plus proche, on le met à jour
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestTree = tree;
                    }
                }
            }
        }
        return closestTree;
    }


    public ArrayList<Coordinate> listePosBug(ArrayList<Coordinate> bugPositions){
        if (this.getHomeCity().getIsTeam1()) {
            for (int i = 0; i < this.getHomeCity().getSize()-1; i++) {
                bugPositions.add(new Coordinate(this.getHomeCity().getX()+this.getHomeCity().getSize(), this.getHomeCity().getY()+i));
            }
        }
        else{
            for (int i = 0; i < this.getHomeCity().getSize()-1; i++) {
                bugPositions.add(new Coordinate(this.getHomeCity().getX()-1, this.getHomeCity().getY()+i));
            }
        }
        return bugPositions;
    }

    public Boolean checkIfPosBug() {
        
        for (Coordinate bugPos : this.getBugPos()) {
            if (bugPos.x == this.getX() && bugPos.y == this.getY()) {
                return true;
            }
        }
        return false;
    }

    // Déplacer le collecteur vers l'arbre
    public void moveToTree(Tree closestTree) {
        Coordinate targetCoord = new Coordinate(closestTree.getX(), closestTree.getY());

        if (controller.isAdjacentToTarget(getPosition(), targetCoord)) {
            setIsCollectingWood(true);  // Commencer à collecter du bois
        } else {
            // essye de mettre setcollectingwood a false
            controller.moveToTarget(targetCoord, this);  // Se déplacer vers l'arbre
        }
    }

    // Retourner à la ville et déposer le bois
    public void returnToCity() {
        //System.out.println("reviens en ville");
        this.setIsCollectingWood(false);
        // Vérifie l'adjacence
        //***********************pourquoi pas appler a la methode adjacent dans le controller?
        boolean isAdjacent = Math.abs(this.getHomeCity().getDepositGatePosition().x - getX()) <= 1 && Math.abs(this.getHomeCity().getDepositGatePosition().y - getY()) <= 1;
        
        while(this.checkIfPosBug()){
            controller.moveToTarget(new Coordinate(controller.getCOL()/2, controller.getROW()/2), this);
        }
        

        if (isAdjacent) {
            // Dépose le bois à la ville
            this.depositWood();
        } else {
            // Se déplacer vers la porte de dépôt
            controller.moveToTarget(getHomeCity().getDepositGatePosition(), this);
        }
    }

    // Si aucun arbre n'est disponible
    public void handleNoTree() {
        if (isFull()) {
            // Le collecteur est plein, il retourne à la ville
            returnToCity();
        } else {
            // Aucun arbre et le collecteur n'est pas plein
            //System.out.println("No trees available, and collector is not full.");
        }
    }

    // Si un arbre est disponible
    public void handleTreeAvailable(Tree closestTree) {
        if (!isFull()) {
            //c'est l'ajout de adjacent qui a regler le proble de collecte des arbres a distance
            if (isCollectingWood() && closestTree.isDispo() && controller.isAdjacentToTarget(this.getPosition(), closestTree.getPosition())) {
                collectWood(closestTree);  // Collecte le bois de l'arbre
            } else {
                // Se déplacer vers l'arbre le plus proche et commencer à collecter
                moveToTree(closestTree);
            }
        } else {
            // Le collecteur est plein, il doit retourner à la ville
            returnToCity();
        }
    }

    @Override
    protected Map<Class<? extends Unit>, Double> getAttackBonuses() {
        return attackBonuses;
    }

    @Override
    protected void moveBehavior(List<GameElement> gameElements){
        //Trouve l'arbre le plus proche
        Tree closestTree = this.findClosestTree(gameElements);
        
        if (closestTree == null) {
            this.handleNoTree();  // Aucune arbre disponible, gérer ce cas
        } 
        else {
            this.handleTreeAvailable(closestTree);  // Un arbre est disponible, gérer ce cas
        } 
    }
}
