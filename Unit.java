import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Unit extends GameElement {

    private City homeCity; // Ville d'origine

    // Attributs spécifiques à une unité
    private int currentHealth; // Points de vie actuels
    private int maxHealth; // Points de vie maximum
    private int attackDamage; // Dégâts d'attaque
    private Unit target;

    private static double detectionRangeUnit; // Plage de détection de l'unité
    private static final int DIVISION_GRID_PIECE = 1; // Taille de la grille (permet de définir la distance de détection)

    private boolean isMoveStopped = false; // Par défaut, le mouvement est activé
    private boolean isInvincible = false;

    //Paramètres partagés pour toutes les unités enfant
    protected static int UNIT_GENERATE_INTERVAL = 5;  // Intervalle par défaut pour la génération des unités
    protected static int NEEDED_WOOD_FOR_GENERATE_UNIT = 50; // Coût en bois pour générer l'unité

    // Map par défaut, redéfinie dans chaque classe fille
    protected abstract Map<Class<? extends Unit>, Double> getAttackBonuses();

    // Constructeur de base pour une unité
    public Unit(int x, int y, int size, String imagePath, City homeCity, int maxHealth, int attackDamage) {
        super(x, y, size, imagePath);
        this.homeCity = homeCity;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth; // Initialisation des PV au maximum
        this.attackDamage = attackDamage;
        if (detectionRangeUnit == 0) {
            this.detectionRangeUnit =  Math.min(controller.getROW() / DIVISION_GRID_PIECE, controller.getCOL() / DIVISION_GRID_PIECE);
        }
    }

    // Accesseur pour la ville d'origine
    public City getHomeCity() {
        return homeCity;
    }

    // Getters et setters pour les attributs de l'unité
    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.min(currentHealth, maxHealth); // Assure que la santé ne dépasse pas le max
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    public Unit getTarget() {
        if (target != null) 
            return target;

        return null;  
    }

    public void setTarget(Unit target) {
        this.target = target;
    }

    // Méthodes pour obtenir les paramètres par défaut (ou surchargés) pour chaque unité
    public int getUnitGenerateInterval() {
        return UNIT_GENERATE_INTERVAL;
    }

    public int getNeededWoodForGenerateUnit() {
        return NEEDED_WOOD_FOR_GENERATE_UNIT;
    }

    //pas de setter pour detection range
    // Getter pour la plage de détection
    public static double getDetectionRangeUnit() {
    return detectionRangeUnit;
    }

    // Getter pour isMoveStopped
    public boolean isMoveStopped() {
        return this.isMoveStopped;
    }

    // Setter pour isMoveStopped
    public void setMoveStopped(boolean moveStopped) {
        this.isMoveStopped = moveStopped;
    }

    // Réduire les points de vie lors d'une attaque
   // Réduire les points de vie lors d'une attaque
   public void takeDamage(int damage, Unit attacker) {
    int effectiveDamage = damage;

    // Parcourt la map des bonus d'attaque de l'attaquant
    for (Map.Entry<Class<? extends Unit>, Double> entry : attacker.getAttackBonuses().entrySet()) {
        Class<? extends Unit> targetClass = entry.getKey();
        double bonusMultiplier = entry.getValue();

        // Vérifie si la classe actuelle correspond à une cible dans la map
        if (targetClass.isAssignableFrom(this.getClass())) {
            effectiveDamage *= bonusMultiplier; // Applique le bonus au damage
            break; // On peut s'arrêter après avoir trouvé un match
        }
    }

    if (!isInvincible) {
        this.currentHealth = Math.max(0, this.currentHealth - effectiveDamage); // Empêche les PV négatifs
    }

    System.out.println(attacker.getClass().getSimpleName() + " inflige " + effectiveDamage + 
                    " dégâts à " + this.getClass().getSimpleName() + 
                    " (PV restants : " + this.currentHealth + ")");
}



    // Vérifie si l'unité est encore en vie
    public boolean isAlive() {
        //System.out.println("il m'as tuer target");
        return this.currentHealth > 0;
    }

    // Vérifie si l'unité est encore en vie
    // Vérifie si l'unité est encore en vie
    public boolean isFighting() {
        //System.out.println("que la bagarre debute");
        List<Unit> units = controller.getUnitInGameElements();
        if (units == null || units.isEmpty()) {
            System.out.println("Unit Elements is empty. Size: " + units.size());
            return false; // Aucun combat possible si aucune unité n'est présente
        }

        for (Unit unit : units) {
            // Vérifie si l'unité est ennemie et vivante
            if (unit.getHomeCity() != this.getHomeCity() && unit.isAlive()) {
                // Vérifie si l'unité ennemie est adjacente
                if (Math.abs(this.getX() - unit.getX()) <= 1 && Math.abs(this.getY() - unit.getY()) <= 1) {
                    //System.out.println("Combat contre une unité ennemie détectée");
                    this.setTarget(unit); // Définit l'ennemi comme cible
                    return true; // Une unité ennemie adjacente existe
                }
            }
        }
        return false; // Aucune unité ennemie adjacente trouvée
    }

    // Méthode pour soigner l'unité
    public void bonusHealt(int healthPoints) {
        this.currentHealth = Math.min(this.currentHealth + healthPoints, maxHealth);
        System.out.println(this.getClass().getSimpleName() + " healed to " + this.currentHealth + " health.");
    }

    // Méthode pour rendre l'unité invincible
    public void setInvincible(boolean isInvincible) {
        this.isInvincible = isInvincible;
    }

    // Méthode pour tuer l'unité
    public void die() {
        this.currentHealth = 0;
        System.out.println(this.getClass().getSimpleName() + " has died.");
    }

    public void move(List<GameElement> gameElements) {
        // Vérifie si l'unité est en combat
        if (this.isFighting()) {
            this.getTarget().takeDamage(attackDamage , this);  // Inflige des dégâts à la cible
            return;  // Priorité au combat
        }

        // Si le drapeau est actif, l'unité se déplace vers le drapeau
        if (Flag.isFlagActive()) {
            // Cherche le drapeau dans la liste des éléments de jeu
            for (GameElement element : gameElements) {
                if (element instanceof Flag) {
                    Flag flag = (Flag) element;
                    // Déplace l'unité vers le drapeau
                    controller.moveToTarget(flag.getPosition(), this); 
                    break; // Une fois le drapeau trouvé, on arrête la recherche
                }
            }
            return;
        }

        // Si l'unité peut bouger (pas d'arrêt de mouvement), appliquer la logique spécifique à chaque type d'unité
        if (!this.isMoveStopped()) {
            moveBehavior(gameElements);  // Appel à la méthode qui sera redéfinie dans les sous-classes
        }
    }

    // Méthode abstraite pour la logique de déplacement spécifique à chaque type d'unité
    protected abstract void moveBehavior(List<GameElement> gameElements);
}
