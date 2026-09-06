import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deserter extends Unit {
    private static final String DESERTER_CITY1_PATH = "/img/Deserter1.png";
    private static final String DESERTER_CITY2_PATH = "/img/Deserter2.png";

    // Attributs constants pour les déserteurs
    private static int maxHealth = 125;  // Points de vie maximum
    private static int attackDamage = 10;  // Dégâts infligés par attaque
    private static final int unitSize = 1;  // Taille de l'unité

    
    // Map spécifique
    private static final Map<Class<? extends Unit>, Double> attackBonuses = new HashMap<>();

    static {
        attackBonuses.put(Piquier.class, 1.5); // Bonus contre Deserter
        attackBonuses.put(Deserter.class, 1.25); // Bonus contre Deserter

    }

    protected static final int UNIT_GENERATE_INTERVAL = 10;
    protected static final int NEEDED_WOOD_FOR_GENERATE_UNIT = 50;
    

    // Constructeur
    public Deserter(int x, int y, City homeCity) {
        super(x, y, unitSize, homeCity.getIsTeam1() ? DESERTER_CITY1_PATH : DESERTER_CITY2_PATH, homeCity, maxHealth, attackDamage);
    }

    
    // Méthode pour trouver l'unité ennemie la plus proche
    public Unit findClosestEnemyUnit(List<GameElement> gameElements) {
        Unit closestUnit = null;  // L'unité ennemie la plus proche
        double minDistance = Double.MAX_VALUE;  // Distance minimale trouvée

        // Parcourt tous les éléments du jeu
        for (GameElement element : gameElements) {
            if (element instanceof Unit && ((Unit) element).getHomeCity() != this.getHomeCity()) {
                // Vérifie si l'élément est une unité ennemie
                Unit unit = (Unit) element;
                //verifie si elle a une position adjacent

                double distance = this.getPosition().getDistanceWith(unit.getPosition());  // Calcule la distance
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestUnit = unit;  // Met à jour l'unité la plus proche
                }
            }
        }
        return closestUnit;
    }

    // Méthode pour fuir dans la direction opposée à une unité
    // Méthode pour fuir dans la direction opposée à une unité
    private void moveAwayFrom(Unit target) {
        if (target == null) return;  // Si aucune cible, ne fait rien

        // Calcule la distance entre this et target
        double distance = this.getPosition().getDistanceWith(target.getPosition());


        // Vérifie si la distance est plus petit que la moitié de la grille
        // Math.min(halfRows, halfCols) retourne la plus petite des deux valeurs : la moitié du nombre de lignes ou de colonnes
        //acceder a get statiqument
        if (distance < Unit.getDetectionRangeUnit()) {
            // Calcule le vecteur opposé à la position de la cible
            Coordinate targetPos = target.getPosition();
            int dx = this.getX() - targetPos.x; // Différence en x entre this et la cible
            int dy = this.getY() - targetPos.y; // Différence en y entre this et la cible

            // Integer.signum retourne : 
            // - 1 si le nombre est positif
            // - -1 si le nombre est négatif
            // - 0 si le nombre est nul
            // Cela permet de déterminer la direction (vers le haut, bas, gauche ou droite) pour s'éloigner de la cible.
            Coordinate newTargetPos = new Coordinate(
                this.getX() + Integer.signum(dx), 
                this.getY() + Integer.signum(dy)
            );

            // Déplace l'unité vers la nouvelle position calculée
            controller.moveToTarget(newTargetPos, this);
        } else {
            // Si la distance est inférieure ou égale à la moitié de la plus petite dimension de la grille
            System.out.println("Distance is smaller than piece of the grid size. No movement away.");
        }
    }


    // Méthode pour se déplacer vers un collecteur
    private void moveToCollector(Unit collector) {
        if (collector == null) return;  // Si aucun collecteur, ne fait rien
        controller.moveToTarget(collector.getPosition(), this);  // Se déplace vers le collecteur
    }


    @Override
    protected Map<Class<? extends Unit>, Double> getAttackBonuses() {
        return attackBonuses;
    }

    @Override
    protected void moveBehavior(List<GameElement> gameElements){
        Unit closestEnemyUnit = findClosestEnemyUnit(gameElements);  // Trouve l'unité ennemie la plus proche
        
        if (closestEnemyUnit != null) {
            if (closestEnemyUnit instanceof Collector) {
                moveToCollector(closestEnemyUnit);  // Se déplace vers le collecteur
                
            }
            else{
                moveAwayFrom(closestEnemyUnit);  // Fuit dans la direction opposée
            }
        }
        else {
            // Aucun ennemi détecté, reste immobile
            //System.out.println("No enemy units detected. Deserter stays idle.");
        }
    }
}
