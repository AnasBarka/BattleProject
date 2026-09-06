import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class City extends GameElement {
    private static final String CITY_TOP_IMAGE = "/img/city1.png";
    private static final String CITY_BOTTOM_IMAGE = "/img/city2.png";

    private Set<Point> cityPoints; // Ensemble des points occupés par la ville
    private int unitGateX, unitGateY; // Coordonnées de la porte des unités
    private int depositGateX, depositGateY; // Coordonnées de la porte de dépôt

    private Boolean team1;
    private int woodCollected = 0;


    private static final int citySize = 5;



    // Constructeur
    public City(boolean isTeam1) {
        // Appel du constructeur secondaire avec les coordonnées calculées
        super(getStartX(isTeam1), getStartY(isTeam1), citySize, isTeam1 ? CITY_TOP_IMAGE : CITY_BOTTOM_IMAGE);
        this.cityPoints = new HashSet<>();
        this.team1 = isTeam1;
        initializeCityPoints(citySize);
        initializeGates(getStartX(isTeam1), getStartY(isTeam1), citySize, isTeam1);
    }

    //methode STATIC peuent etre appler meme sans instance de city
    //elle ne peut que acceder au variable et methode static de la classe
    // Méthodes pour calculer les coordonnées de départ
    private static int getStartX(boolean isTeam1) {
        return (controller.getCOL() - citySize) / 2;
    }

    private static int getStartY(boolean isTeam1) {
        return isTeam1 ? 0 : controller.getROW() - citySize;
    }


    // Initialise les points de la ville en fonction de sa taille
    private void initializeCityPoints(int size) {
        if (size <= 0) throw new IllegalArgumentException("La taille doit être positive.");
        for (int i = getX(); i < getX() + size; i++) {
            for (int j = getY(); j < getY() + size; j++) {
                cityPoints.add(new Point(i, j));
            }
        }
    }


    // Initialise les positions des portes en fonction de la ville
    private void initializeGates(int startX, int startY, int size, boolean isTeam1) {
        int middleRow = startX + (size / 2); // Milieu de la largeur de la ville
        int middleCol = startY + size / 2; // Milieu de la hauteur de la ville
        
        //System.out.println("startx:"+ startX);
        //System.out.println("startY:"+ startY);

        unitGateX = middleRow; 
        unitGateY = isTeam1? startY + size -1 : startY;
       
        depositGateX = isTeam1? startX : startX + size -1;
        depositGateY = middleCol;        
    }

    //meme si on rajoute une unit on dois pas venir faire des modification dans la classe city
    public Unit generateUnit(Class<? extends Unit> unitType) {
        try {
            // Utilise la réflexion pour récupérer un constructeur spécifique de la classe passée en paramètre (unitType).
            // Le constructeur doit prendre trois arguments : deux entiers (x, y) pour les coordonnées
            // et une instance de la classe City.
            return unitType.getDeclaredConstructor(int.class, int.class, City.class)
                        .newInstance(
                            this.getUnitGatePosition().x, // La coordonnée x de la porte de la ville
                            this.getUnitGatePosition().y, // La coordonnée y de la porte de la ville
                            this                           // Référence à cette instance de City
                        );
        } catch (Exception e) {
            // Si une erreur survient (par exemple, constructeur introuvable, ou mauvais paramètres),
            // on lève une RuntimeException avec un message clair, tout en conservant la trace de l'erreur d'origine.
            throw new RuntimeException("Erreur lors de la génération de l'unité : " + unitType.getName(), e);
        }
    }
    
    
    // Getters pour les points et les portes
    public Set<Point> getCityPoints() {
        return cityPoints;
    }

    public Boolean getIsTeam1() {
        return team1;
    }

    public Coordinate getUnitGatePosition() {
        return new Coordinate(unitGateX, unitGateY);
    }

    public Coordinate getDepositGatePosition() {
        return new Coordinate(depositGateX, depositGateY);
    }

    public void depositWood(int woodCarried) {
        woodCollected +=woodCarried;
    }

    public int getWoodCollected() {
        return woodCollected;
    }

    public void decreaseCityWood(int decreasedWood){
        woodCollected -= decreasedWood;
    }
}