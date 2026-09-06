import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cavalier extends Unit {
    private static final String CAVALIER_CITY1_PATH = "/img/Cavalier.png";
    private static final String CAVALIER_CITY2_PATH = "/img/Cavalier2.png";

    // Attributs constants pour les cavaliers
    private static int maxHealth = 150; // Points de vie maximum
    private static int attackDamage = 15; // Dégâts infligés par attaque
    private static final int unitSize = 1; // Taille de l'unité

    // Map spécifique à Deserter
    private static final Map<Class<? extends Unit>, Double> attackBonuses = new HashMap<>();

    static {
        attackBonuses.put(Deserter.class, 2.0); // Bonus contre Piquier
    }

    protected static final int UNIT_GENERATE_INTERVAL = 15;
    protected static final int NEEDED_WOOD_FOR_GENERATE_UNIT = 100;

    private static int safetyDistanceFixe = 1; // Distance de sécurité par défaut
    private static int safetyDistanceIncrement = safetyDistanceFixe; // Distance de sécurité par défaut

    // Constructeur
    public Cavalier(int x, int y, City homeCity) {
        super(x, y, unitSize, homeCity.getIsTeam1() ? CAVALIER_CITY1_PATH : CAVALIER_CITY2_PATH, homeCity, maxHealth, attackDamage);
    }


    // Méthode pour trouver le déserteur le plus proche
    public Unit findClosestDeserter(List<GameElement> gameElements) {
        Unit closestDeserter = null;
        double minDistance = Double.MAX_VALUE;

        for (GameElement element : gameElements) {
            if (element instanceof Deserter && ((Unit) element).getHomeCity() != this.getHomeCity()) {
                Unit deserter = (Unit) element;
                double distance = this.getPosition().getDistanceWith(deserter.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestDeserter = deserter;
                }
            }
        }
        return closestDeserter;
    }

    // Méthode pour trouver le cavalier allié le plus proche
    public Unit findClosestAllyCavalier(List<GameElement> gameElements) {
        Unit closestAllyCavalier = null;
        double minDistance = Double.MAX_VALUE;

        for (GameElement element : gameElements) {
            if (element instanceof Cavalier && ((Unit) element).getHomeCity() == this.getHomeCity() && element != this) {
                Unit allyCavalier = (Unit) element;
                double distance = this.getPosition().getDistanceWith(allyCavalier.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestAllyCavalier = allyCavalier;
                }
            }
        }
        return closestAllyCavalier;
    }

    // Méthode pour ajuster la distance avec un allié cavalier
    private void adjustDistanceWithAlly(Unit allyCavalier) {
        if (allyCavalier == null) return;

        double distance = this.getPosition().getDistanceWith(allyCavalier.getPosition());
        //System.out.println("Distance whit ally: "+distance+" and seftyD: "+safetyDistanceIncrement);
        if (distance < safetyDistanceIncrement) {
            // Fuit dans la direction opposée
            Coordinate allyPos = allyCavalier.getPosition();
            int dx = this.getX() - allyPos.x;
            int dy = this.getY() - allyPos.y;
            Coordinate newTargetPos = new Coordinate(this.getX() + Integer.signum(dx), this.getY() + Integer.signum(dy));
            controller.moveToTarget(newTargetPos, this);
        }
        else if (distance > safetyDistanceIncrement) {
            // Se déplace vers l'allié
            controller.moveToTarget(allyCavalier.getPosition(), this);
        }
        //si elle est egale == on fais rien
    }

    // Méthode pour chasser un déserteur
    private void moveToDeserter(Unit deserter) {
        if (deserter == null) return;

        //System.out.println("mouve to deserteur");
        Coordinate targetCoord = deserter.getPosition();

        /*if (controller.isAdjacentToTarget(this.getPosition(), targetCoord)) {
            attack(deserter);
        } else {*/

        controller.moveToTarget(targetCoord, this);
        
    }

    // Méthode statique pour ajuster la distance de sécurité pour tout les cavalier mode garde
    public static void updateSafetyDistance(boolean combatOccurred) {
        //System.out.println("Avant safetyDistance: "+safetyDistanceIncrement);
        //System.out.println("combatOccured: "+combatOccurred);

        if (combatOccurred) {
            if (safetyDistanceIncrement > safetyDistanceFixe+1) {
                safetyDistanceIncrement = safetyDistanceFixe+1;
            }
        }
        else{
            if (safetyDistanceIncrement < Unit.getDetectionRangeUnit()-1) {
                safetyDistanceIncrement++;
            }
        }
       
        //System.out.println("Apres safetyDistance: "+safetyDistanceIncrement);

    }


    @Override
    protected Map<Class<? extends Unit>, Double> getAttackBonuses() {
        return attackBonuses;
    }

    @Override
    protected void moveBehavior(List<GameElement> gameElements){
        Unit closestDeserter = findClosestDeserter(gameElements);
        Unit closestAllyCavalier = findClosestAllyCavalier(gameElements);

        if (closestDeserter != null) {
            double deserterDistance = this.getPosition().getDistanceWith(closestDeserter.getPosition());
            double allyDistance = closestAllyCavalier != null ? this.getPosition().getDistanceWith(closestAllyCavalier.getPosition()) : Double.MAX_VALUE;

            if (deserterDistance < allyDistance) {
                moveToDeserter(closestDeserter);
                this.setTarget(closestDeserter);
            } else {
                adjustDistanceWithAlly(closestAllyCavalier);
                this.setTarget(null);

            }
        } 
        else if (closestAllyCavalier != null) {
            adjustDistanceWithAlly(closestAllyCavalier);
            this.setTarget(null);

        } 
        else {
            //System.out.println("No targets available. Cavalier stays idle.");
        }
    }
    
}
