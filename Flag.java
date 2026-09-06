import java.util.List;

public class Flag extends Collectable {

    private static final String FLAG_PATH = "/img/Flag.png";
    private static final int flagSize = 1; // Taille de l'unité

    private static boolean isActive = false; // Indique si un drapeau est présent sur la carte
    private static double bonusToHealt = 0.5;

    private static final int FLAG_INTERVAL_SECONDS = 120; // Temps pour générer un autre type d'unité


    public Flag(int x, int y) {
        super(x, y, flagSize, FLAG_PATH);
        activateFlag();
    }

    public static boolean isFlagActive() {
        return isActive;
    }

    public static void activateFlag() {
        isActive = true;
    }
    
    public static void deactivateFlag() {
        isActive = false;
    }

    public static int getFlagIntervalSeconds() {
        return FLAG_INTERVAL_SECONDS;
    }

    @Override
    public void onCollect(Unit unitWhoCollect, List<Unit> allUnits) {
        System.out.println(unitWhoCollect.getClass().getSimpleName() + " collected the Flag!");

        for (Unit unit : allUnits) {
            if (unit.getHomeCity() == unitWhoCollect.getHomeCity()) {
                unit.bonusHealt((int) (unit.getMaxHealth() * bonusToHealt)); // +50% des points de vie
            }
        }

        // Désactiver le drapeau après la collecte
        deactivateFlag();
    }
}
