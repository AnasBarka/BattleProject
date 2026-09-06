import java.util.List;
import java.util.Random;

public class PhilosopherStone extends Collectable {
    private static final String STONE_PATH = "/img/Stone.png";
    private static final int stoneSize = 1; // Taille de l'unité


    public PhilosopherStone(int x, int y) {
        super(x, y, stoneSize, STONE_PATH);
    }
    
    @Override
    public void onCollect(Unit unitWhoCollect, List<Unit> allUnits) {
        Random rand = new Random();
        boolean survives = rand.nextBoolean(); // 50 % de chance de survivre

        //si il survie il vas etre invinvible avec des point de vie illimiter
        if (survives) {
            System.out.println(unitWhoCollect.getClass().getSimpleName() + " became invincible!");
            unitWhoCollect.setInvincible(true); // L'unité devient invincible
        } else {
            //si il survie pas il meurt triste sort
            System.out.println(unitWhoCollect.getClass().getSimpleName() + " perished!");
            unitWhoCollect.die(); // L'unité meurt
        }
    }
    
}
