import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Piquier extends Unit {
    private static final String PIQUIER_CITY1_PATH = "/img/Piquier.png";
    private static final String PIQUIER_CITY2_PATH = "/img/Piquier2.png";

    // Attributs spécifiques aux piquiers
    private static final int maxHealth = 200; // Points de vie maximum
    private static final int attackDamage = 10; // Dégâts infligés par attaque
    private static final int unitSize = 1; // Taille de l'unité


    private static final Map<Class<? extends Unit>, Double> attackBonuses = new HashMap<>();

    static {
        attackBonuses.put(Cavalier.class, 3.0); // Bonus contre Deserter
    }


    protected static final int UNIT_GENERATE_INTERVAL = 5;
    protected static final int NEEDED_WOOD_FOR_GENERATE_UNIT = 75;

    private Coordinate assignedPosition; // Position assignée aléatoire
    private static int collectiveVisionRange = 0; // Champ de vision collectif partagé par tous les piquiers

    // Cible commune pour tous les piquiers de la même équipe
    private static Unit sharedTargetEnemy = null;
    

    // Constructeur
    public Piquier(int x, int y, City homeCity) {
        super(x, y, unitSize, homeCity.getIsTeam1() ? PIQUIER_CITY1_PATH : PIQUIER_CITY2_PATH, homeCity, maxHealth, attackDamage);
        this.assignedPosition = generateRandomPosition(); // Génère une position aléatoire à la création
    }


    // Générer une position aléatoire sur la carte
    private Coordinate generateRandomPosition() {
        Coordinate XY = controller.initializePos(controller.getCOL() - 1, controller.getROW() - 1);
        return XY;
    }

    // Méthode pour déterminer le champ de vision collectif
    public static void updateCollectiveVisionRange(int totalPiquiers) {
        collectiveVisionRange = totalPiquiers; // Égal au nombre total de piquiers
    }

    // Met à jour la cible commune s'il n'y en a pas encore
    private void updateSharedTarget(Unit enemy) {
        if (sharedTargetEnemy == null || !sharedTargetEnemy.isAlive()) {
            if (enemy.getHomeCity() != this.getHomeCity()) {
                sharedTargetEnemy = enemy;
            }
        }
    }

    // Méthode pour repérer un ennemi dans le champ de vision
    private Unit findEnemyInVisionRange(List<GameElement> gameElements) {
        for (GameElement element : gameElements) {
            if (element instanceof Unit && ((Unit) element).getHomeCity() != this.getHomeCity()) {
                Unit enemy = (Unit) element;
                double distance = this.getPosition().getDistanceWith(enemy.getPosition());
                if (distance < collectiveVisionRange) {
                    return enemy; // Retourne le premier ennemi détecté
                }
            }
        }
        return null; // Aucun ennemi repéré
    }

    // Méthode pour se déplacer vers un ennemi
    private void moveToEnemy(Unit enemy) {
        if (enemy == null) return;

        //System.out.println("Piquier moving towards enemy.");
        Coordinate targetCoord = enemy.getPosition();
        /*if (controller.isAdjacentToTarget(this.getPosition(), targetCoord)) {
            attack(enemy);
        } else {*/
        controller.moveToTarget(targetCoord, this);
        
    }

    // Méthode pour retourner à la position assignée
    private void moveToAssignedPosition() {
        if (this.getPosition().equals(assignedPosition)) {
            //System.out.println("Piquier already at assigned position.");
            return;
        }
        else if(controller.isAdjacentToTarget(this.getPosition(), assignedPosition)){
            //System.out.println("Piquier already at adjacent assigned position.");
            return;
        }

        //System.out.println("Piquier moving towards assigned position.");
        controller.moveToTarget(assignedPosition, this);
    }

    @Override
    protected Map<Class<? extends Unit>, Double> getAttackBonuses() {
        return attackBonuses;
    }

    @Override
    protected void moveBehavior(List<GameElement> gameElements) {
        // Vérifie d'abord si un ennemi est déjà partagé comme cible
        if (sharedTargetEnemy != null && sharedTargetEnemy.isAlive()) {
            moveToEnemy(sharedTargetEnemy); // Se déplace vers l'ennemi partagé
        } else {
            // Sinon, cherche un nouvel ennemi dans le champ de vision
            Unit enemyInRange = findEnemyInVisionRange(gameElements);
            if (enemyInRange != null) {
                updateSharedTarget(enemyInRange); // Met à jour la cible partagée
                moveToEnemy(enemyInRange); // Se déplace vers le nouvel ennemi
            } else {
                moveToAssignedPosition(); // Sinon, retourne à la position assignée
            }
        }
    }

    // Réinitialise la cible commune lorsque nécessaire
    public static void resetSharedTarget() {
        sharedTargetEnemy = null;
    }
}
