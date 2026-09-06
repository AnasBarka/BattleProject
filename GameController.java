import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Contrôleur principal de l'application.
 * Il est responsable de la gestion des éléments du jeu, de l'initialisation, 
 * et de l'interaction entre la vue et les données.
 * 
 * 
 * rsponsable :
 * De la gestion de la logique métier (ex. : initialisation des éléments de jeu,
 * vérification des positions disponibles, génération aléatoire, etc.).
 * 
 * 
 * Note, il aurait été possible d’instancier la vue en dehors du contrôleur (dans Main), cela aurait 
 * permis de décharger le contrôleur des éléments de logique propre à l’UI
 * 
 * fréquent que des éléments propres à la technologie d’affichage (vue) soit présents au sein du
 * contrôleur. Il revient toutefois au développeur de minimiser, autant que possible, la présence
 * de ces éléments afin de permettre, encore une fois, une maintenance plus efficace.
 */
public class GameController {

    // Vue pour afficher les éléments graphiques
    private final GameView view;

    // Liste des éléments de jeu (arbres, villes, etc.)
    //elle etais Final de base
    private List<GameElement> gameElements;
    //cree pour supprimer et ajouter des unite
    private List<Unit> unitElements = new ArrayList<>();

    private City city1;
    private City city2;


    // Paramètres du jeu
    private final int ROWS = 35; // Nombre de lignes de la grille
    private final int COLUMNS = ROWS; // Nombre de colonnes (carré)

    private final Coordinate GRID_XY = new Coordinate(COLUMNS, ROWS); 

    // Boucle principale du jeu
    private Timeline gameLoop;
    private static final double GAME_LOOP_INTERVAL_SECONDS = 1.0; // Intervalle de la boucle (en millisecondes)
    
    private int gameLoopCounter = 0; // Compteur de cycles
    private double elapsedSeconds = 0;


    private double treeRatio = 0.05; // Par défaut, 10 % des cases peuvent avoir un arbre
    private static final int setCurrentHealthWhenPressedKeyCode = 0;
    private static final int howManyStonesInMapInitialize = 2;


    /**
     * Constructeur du contrôleur.
     * @param primaryStage La fenêtre principale de l'application.
     */
    public GameController(Stage primaryStage) {

        GameElement.setController(this);
        // Initialise la vue et les éléments du jeu
        this.view = new GameView(this);

        this.gameElements = initializeGameElements();

        // Injection du contrôleur dans la classe GameElement

        // Configure et lance le jeu
        initialize(primaryStage);
        startGame();
    }

    /**
     * Configure la vue avec les éléments du jeu et initialise l'affichage.
     * @param primaryStage La fenêtre principale.
     */
    public void initialize(Stage primaryStage) {
        // Passe les éléments du jeu à la vue pour qu'elle puisse les afficher
        view.initialize(primaryStage, gameElements, GRID_XY);
    }

    /**
     * Démarre la boucle principale du jeu.
     * Cette boucle met à jour régulièrement l'affichage et la logique du jeu.
     */
    public void startGame() {
        // Configure la boucle pour mettre à jour le jeu toutes les 65 ms
        gameLoop = new Timeline(new KeyFrame(Duration.seconds(GAME_LOOP_INTERVAL_SECONDS), e -> run()));
        gameLoop.setCycleCount(Timeline.INDEFINITE); // La boucle tourne indéfiniment
        gameLoop.play(); // Démarre la boucle
    }

    /**
     * Méthode appelée à chaque cycle de la boucle de jeu.
     * Elle met à jour l'affichage et redessine les éléments du jeu.
     */
    private void run() {
        gameLoopCounter++; // Incrémenter le compteur à chaque cycle
        elapsedSeconds = gameLoopCounter * GAME_LOOP_INTERVAL_SECONDS;

        if (gameElements.isEmpty()) {
            this.gameElements = initializeGameElements();
            this.gameLoopCounter = 0;
            this.elapsedSeconds = 0;
        }

        //La méthode view.drawBackground(ROWS, COLUMNS) demande à la vue de dessiner l'arrière-plan,
        //mais c'est la vue qui gère réellement l'affichage.
        // Dessine l'arrière-plan (par exemple, la grille)
        view.drawBackground(ROWS, COLUMNS);

        /*La méthode view.drawGameElements(gameElements) passe la liste des éléments à afficher à la vue,
        mais le dessin est géré uniquement par la vue. */
        // Dessine tous les éléments du jeu (villes, arbres, etc.)
        view.drawGameElements(gameElements);


        for (GameElement element : gameElements) {
            if (element instanceof City) {
                City city = (City) element;
                view.drawWoodCarried(city.getWoodCollected(), city.getX(), city.getY());
                //System.out.println("city"+city.getWoodCollected());

            }
        }

        //parceque on ne peut pas manipuler la liste toute en iterant
        List<GameElement> toRemove = new ArrayList<>();
        List<GameElement> toAdd = new ArrayList<>();

        boolean anyCavalierFighting = false; // Vérifie si une unité est en combat ou a une cible
        boolean anyCavalierWithTarget = false; // Vérifie si un cavalier a une cible
        int cmptCavalier = 0;
        int totalPiquiers = 0;

        for (GameElement element : gameElements) {

            if (element instanceof City) {
                City city = (City) element;
                // Générez des collecteurs pour cette ville, si nécessaire
                generateUnitAndFlagAtIntervals(city, toAdd, gameElements); // Ajouter les nouveaux collecteurs à la liste 'toAdd'
            }

            if (element instanceof Unit) {
                Unit unit = (Unit) element;
                if (!unit.isAlive()) {
                    toRemove.add(unit); // Marque pour suppression
                }

                if(unit instanceof Cavalier){
                    cmptCavalier++;
                    if (unit.isFighting() || unit.getTarget() != null) {
                        anyCavalierFighting = true; // Un cavalier est en combat ou a une cible
                    }
                }
                if (element instanceof Piquier) {
                    totalPiquiers++;
                }
                unit.move(gameElements);

            }

            if (element instanceof Collectable) {
                Collectable item = (Collectable) element;
        
                // Parcourir toutes les unités pour vérifier la proximité
                for (Unit unit : unitElements) {
                    if (item.isAdjacent(item, unit)) {
                        // Collecter la pierre
                        item.onCollect(unit, unitElements);
                        toRemove.add(item); // Marquer la pierre pour suppression
                        break; // Une fois collectée, ignorer les autres unités
                    }
                }
            }
        }

        // Appliquer les modifications après la boucle
        gameElements.removeAll(toRemove);
        unitElements.removeAll(toRemove);

        gameElements.addAll(toAdd);

        if (cmptCavalier>0) {
            Cavalier.updateSafetyDistance(anyCavalierFighting); // Aucun combat détecté pour les cavaliers
        }

        Piquier.updateCollectiveVisionRange(totalPiquiers);

        //System.out.println(gameElements);
    }

    /**
     * Initialise tous les éléments du jeu (villes et arbres).
     * @return Une liste contenant les éléments du jeu.
     */
    private List<GameElement> initializeGameElements() {
        List<GameElement> elements = new ArrayList<>();
        // Ajoute les villes
        initializeCities(elements);
        // Ajoute les arbres
        initializeTrees(elements);
        //ajouter les pierres philo
        //dans un boucle pour pouvoir utiliser la methode dans les key
        for (int i = 0; i < howManyStonesInMapInitialize; i++) {
            initializePhiloStone(elements);
        }


        System.out.println(elements);


        return elements;
    }


    /**
     * Ajoute les villes à la liste des éléments du jeu.
     * @param elements La liste des éléments du jeu.
     */
    private void initializeCities(List<GameElement> elements) {
        // Ajoute une ville en haut de la grille (joueur 1)
        City city1 = new City(true);
        elements.add(city1);
        this.city1 = city1;

        // Ajoute une ville en bas de la grille (joueur 2)
        City city2 = new City(false);
        elements.add(city2);
        this.city2 = city2;

        
    }

    /**
     * Ajoute des arbres à des positions aléatoires sur la grille.
     * Chaque arbre a une quantité de bois aléatoire.
     * @param elements La liste des éléments du jeu.
     */
    private void initializeTrees(List<GameElement> elements) {
        Random random = new Random();
    
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                // Décide si un arbre doit être placé à cette position
                if (random.nextDouble() < treeRatio && !isPositionOccupied(elements, x, y)) {
                    elements.add(new Tree(x, y));
                }
            }
        }
    }

    private void initializePhiloStone(List<GameElement> elements) {

        Coordinate XYposStone = this.initializePos(COLUMNS - 1, ROWS - 1, elements);
        elements.add(new PhilosopherStone(XYposStone.x, XYposStone.y));
    }
    

    private void generateUnitAndFlagAtIntervals(City city, List<GameElement> toAdd, List<GameElement> elements) {
      
        // on utilise une liste et pas un tableu regarde le rapport pour voir le warnmig si on utiliser un tab et pk
        // Liste des classes d'unités à générer et de leurs coûts en bois(liste plus securiser)
        List<Class<? extends Unit>> unitTypes = List.of(Collector.class, Deserter.class, Cavalier.class, Piquier.class);
        int[] woodCosts = new int[]{Collector.NEEDED_WOOD_FOR_GENERATE_UNIT, Deserter.NEEDED_WOOD_FOR_GENERATE_UNIT, 
                                    Cavalier.NEEDED_WOOD_FOR_GENERATE_UNIT, Piquier.NEEDED_WOOD_FOR_GENERATE_UNIT};
                                    
        int[] generationIntervals = new int[]{Collector.UNIT_GENERATE_INTERVAL, Deserter.UNIT_GENERATE_INTERVAL, 
                                            Cavalier.UNIT_GENERATE_INTERVAL, Piquier.UNIT_GENERATE_INTERVAL};

        // Itérer sur chaque type d'unité
        for (int i = 0; i < unitTypes.size(); i++) {
            // Vérifie si le temps écoulé correspond à un multiple de l'intervalle de génération
            if (elapsedSeconds % generationIntervals[i] == 0 && city.getWoodCollected() >= woodCosts[i]) {
                // Générer l'unité en fonction de la classe
                GameElement unit = city.generateUnit(unitTypes.get(i));
                city.decreaseCityWood(woodCosts[i]);
                // Ajouter l'unité à la liste 'toAdd' pour l'affichage
                toAdd.add(unit);
                this.addUnitElement(unit);
            }
        }
        
        if (elapsedSeconds % Flag.getFlagIntervalSeconds() == 0) {
            Coordinate XYposStone = this.initializePos(COLUMNS - 1, ROWS - 1, elements);
            Flag flag = new Flag(XYposStone.x, XYposStone.y);
            toAdd.add(flag);
        }
    }


    public void handleKeyPress(KeyCode keyCode) {
        GameElement newElement = null; // Référence pour l'unité générée
    
        if (keyCode == KeyCode.A) {
            //newElement = city1.generateCollector();
            newElement = city1.generateUnit(Collector.class); // Nouvelle méthode
            System.out.println("Collector generated in the north city.");

        } else if (keyCode == KeyCode.Z) {
            //newElement = city1.generateDeserter();
            newElement = city1.generateUnit(Deserter.class); // Nouvelle méthode
            System.out.println("Deserter generated in the north city.");

        } else if (keyCode == KeyCode.E) {
            //newElement = city1.generateCavalier();
            newElement = city1.generateUnit(Cavalier.class); // Nouvelle méthode
            System.out.println("Cavalier generated in the north city.");

        }else if (keyCode == KeyCode.R) {
            //newElement = city1.generatePiquier();
            newElement = city1.generateUnit(Piquier.class); // Nouvelle méthode
            System.out.println("Piquiers generated in the north city.");
        }
         
        
        else if (keyCode == KeyCode.W) {
            //newElement = city2.generateCollector();
            newElement = city2.generateUnit(Collector.class); // Nouvelle méthode
            System.out.println("Collector generated in the south city.");
        } else if (keyCode == KeyCode.X) {
            //newElement = city2.generateDeserter();
            newElement = city2.generateUnit(Deserter.class); // Nouvelle méthode
            System.out.println("Deserter generated in the south city.");
        } else if (keyCode == KeyCode.C) {
            //newElement = city2.generateCavalier();
            newElement = city2.generateUnit(Cavalier.class); // Nouvelle méthode
            System.out.println("Cavalier generated in the south city.");
        }else if (keyCode == KeyCode.V) {
            //newElement = city2.generatePiquier();
            newElement = city2.generateUnit(Piquier.class); // Nouvelle méthode
            System.out.println("Piquiers generated in the south city.");
        }

        else if (keyCode == KeyCode.J) {
            toggleMovementForUnitType(Collector.class);// Activer/Désactiver le mouvement des collecteurs
            System.out.println("Collectors' movement toggled.");
        } else if (keyCode == KeyCode.K) {
            toggleMovementForUnitType(Deserter.class); // Activer/Désactiver le mouvement des déserteurs
            System.out.println("Deserters' movement toggled.");
        } else if (keyCode == KeyCode.L) {
            toggleMovementForUnitType(Cavalier.class);// Activer/Désactiver le mouvement des cavaliers
            System.out.println("Cavaliers' movement toggled.");
        }else if (keyCode == KeyCode.M) {
            toggleMovementForUnitType(Piquier.class); // Activer/Désactiver le mouvement des piquiers
            System.out.println("Piquiers' movement toggled.");
        }

        else if (keyCode == KeyCode.P) {
            initializePhiloStone(getGameElements()); // Réinitialiser les PV de toutes les unités
            System.out.println("PhiloStone cree.");//hardcoder changer ca
        }
        else if (keyCode == KeyCode.I) {
            if (!Flag.isFlagActive()) {
                Coordinate XYposStone = this.initializePos(COLUMNS - 1, ROWS - 1, getGameElements());
                Flag flag = new Flag(XYposStone.x, XYposStone.y);
                newElement = flag;
                System.out.println("flag cree.");//hardcoder changer ca
            }
        }

        else if (keyCode == KeyCode.U) {
            resetUnitHealth(); // Réinitialiser les PV de toutes les unités
            System.out.println("All unit healths");//hardcoder changer ca
        }
        else if (keyCode == KeyCode.O) {
            resetSimulation(); // Réinitialiser les PV de toutes les unités
            System.out.println("All SIMULATION reset.");
        }

    
        // Ajouter l'unité aux deux listes si elle est générée
        if (newElement != null) {
            addUnitToLists(newElement);
        }
    }

    //DEPLACEMENT------------------------------------------------------------------------------------------------------------------------------

    
    public void moveToTarget(Coordinate targetCoord, Unit elementToMove){ 
        /*
        Met à jour directement les coordonnées de l'unite vers le meilleur voisin trouvé par getNextCoordinateForTarget.
        */
        Coordinate nextPosition = getNextCoordinateForTarget(elementToMove, elementToMove.getPosition(), targetCoord);
        
        if (!isPositionOccupied(gameElements, nextPosition.x, nextPosition.y)) {
            elementToMove.setX(nextPosition.x);
            elementToMove.setY(nextPosition.y);
        } 
        else {
            System.out.println("Position occupied. Staying in place.");
        }
    }
    
    
    //La logique principale pour déplacer element  se trouve dans :
    //Calcule la meilleure coordonnée voisine pour se rapprocher du fruit.
    public Coordinate getNextCoordinateForTarget(Unit elementToMove, Coordinate currentCoordinate, Coordinate targetCoordinate) {

        double minDistance = ROWS + COLUMNS;
        int minIndex = -1;
    
        // Obtenir la liste des coordonnées adjacentes accessibles.
        ArrayList<Coordinate> accessibleAdjacentCoordinatesList = getAccessibleAdjacentCoordinates(elementToMove, currentCoordinate);

        /*
        1. Liste des voisins accessibles :
            Récupère les voisins valides grâce à getAccessibleAdjacentCoordinates.
        2. Distance minimale :
            Pour chaque voisin, calcule la distance au fruit avec getDistanceWith.
        3. Choix du voisin :
            Retient la coordonnée ayant la distance minimale.
        */
    
        // Vérifier si la liste est vide avant de continuer.
        if (accessibleAdjacentCoordinatesList.isEmpty()) {
            System.out.println("Warning: No accessible adjacent coordinates. Staying in place.");
            return currentCoordinate; // Rester en place si aucun voisin accessible.
        }
    
        // Parcourir la liste pour trouver la coordonnée ayant la distance minimale.
        for (int index = 0; index < accessibleAdjacentCoordinatesList.size(); index++) {
            Coordinate coord = accessibleAdjacentCoordinatesList.get(index);
            double distance = coord.getDistanceWith(targetCoordinate);
    
            // Mettre à jour la distance minimale et l'index.
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = index;
            }
        }
    
        // Vérification finale pour s'assurer qu'une coordonnée valide a été trouvée.
        if (minIndex == -1) {
            System.out.println("Warning: No valid coordinates found. Staying in place.");
            return currentCoordinate; // Rester en place si aucun voisin n'est valide.
        }
    
        // Retourner la coordonnée avec la distance minimale.
        return accessibleAdjacentCoordinatesList.get(minIndex);
        /*
        Supposons que la tête soit à (5, 5) et le fruit à (7, 7). Les voisins accessibles sont :

        [(4, 5), (5, 4), (5, 6), (6, 5)]

        Les distances au fruit (7, 7) sont respectivement :

        [(4, 5) → 3.61, (5, 4) → 3.61, (5, 6) → 2.83, (6, 5) → 2.83]

        La méthode choisit (5, 6) ou (6, 5) comme prochaine position.
        */
    }
    
    /*Retourne toutes les coordonnées adjacentes à une position donnée (y compris hors des limites de la grille).
        Par exemple, pour (x=5, y=5), cela retourne :	
    [(4, 4), (4, 5), (4, 6), (5, 4), (5, 6), (6, 4), (6, 5), (6, 6)]*/
    public ArrayList<Coordinate> getAccessibleAdjacentCoordinates(Unit elementToMove, Coordinate coord){

        ArrayList<Coordinate> resultList = new ArrayList<Coordinate>();
        ArrayList<Coordinate> adjacentCoordinatesList = getAdjacentCoordinates(coord);

        
        for (Coordinate adjacent : adjacentCoordinatesList) {//gameelement??

            if (isCoordinateInBoard(adjacent) 
            && !isPositionOccupied(gameElements, adjacent.x, adjacent.y) ) { // Exclure les dernières positions visitées
                resultList.add(adjacent);
            }
        }
        return resultList;
    }

    public ArrayList<Coordinate> getAdjacentCoordinates(Coordinate coord){

        ArrayList<Coordinate> resultList = new ArrayList<Coordinate>();
        
        for(int i = coord.x-1 ;  i <= coord.x+1 ; i++){
            for(int j = coord.y-1 ;  j <= coord.y+1 ; j++){
                if(!(i == coord.x && j == coord.y) ){
                    resultList.add(new Coordinate(i, j));
                }
            }     
        }
        return resultList;
    }
        
    //Vérifie si une coordonnée est dans les limites de la grille (entre 0 et ROWS-1 pour x et y).
    public boolean isCoordinateInBoard(Coordinate coord){
        return(coord.x >= 0 && coord.y >= 0 && coord.x < COLUMNS && coord.y < ROWS); 
    }

    public boolean isAdjacentToTarget(Coordinate current, Coordinate target) {
        int deltaX = Math.abs(current.x - target.x);
        int deltaY = Math.abs(current.y - target.y);
    
        // Si la différence entre les coordonnées est ≤ 1 pour les deux axes, c'est adjacent
        return (deltaX <= 1 && deltaY <= 1);
    }

    // Méthode utilitaire pour obtenir une position libre
    public Coordinate initializePos(int columns, int rows) {
        int x, y;
        do {
            x = getRandomBetween(0, columns);
            y = getRandomBetween(0, rows);
        } while (isPositionOccupied(gameElements, x, y));
        return new Coordinate(x, y);
    }

    //surcharge de la methode initializePos pour inisialiser les pierre philo sur la map
    public Coordinate initializePos(int columns, int rows, List<GameElement> elements) {
        int x, y;
        do {
            x = getRandomBetween(0, columns);
            y = getRandomBetween(0, rows);
        } while (isPositionOccupied(elements, x, y));
        return new Coordinate(x, y);
    }

    // Méthode statique pour générer un nombre aléatoire dans une plage
    public int getRandomBetween(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    private boolean isPositionOccupied(List<GameElement> elements, int x, int y) {
        for (GameElement element : elements) {
            if (element instanceof City && ((City) element).getCityPoints().contains(new java.awt.Point(x, y))) {
                return true; // La position fait partie d'une ville
            }
            if (element instanceof Tree) {
                Tree tree = (Tree) element;
                if (tree.getX() == x && tree.getY() == y) {
                    return tree.isDispo(); // Retourne vrai si l'arbre est disponible, sinon faux
                }
            }
            if (element.getX() == x && element.getY() == y) {
                return true; // Position occupée par un autre élément
            }
        }
        return false; // Aucun élément n'occupe la position
    }
    
    public List<GameElement> getGameElements(){
        return this.gameElements;
    }

    public void setGameElements(List<GameElement> elements){
        this.gameElements = elements;
    }

    // Méthode pour ajouter un élément
    public void addUnitElement(GameElement element) {
        this.unitElements.add((Unit)element);
        
    }

    private void addUnitToLists(GameElement element) {
        gameElements.add(element); // Ajouter à la liste principale
        if (element instanceof Unit) {
            unitElements.add((Unit) element); // Ajouter à la liste des unités
        }
    }
    //utiliser une seul fois dans la classe unit
    //Rendre unitElements immuable à l'extérieur du contrôleur en retournant une copie de la liste :
    // Getter pour accéder aux unités
    public List<Unit> getUnitInGameElements() {
        return new ArrayList<>(this.unitElements);
    }

    public void resetUnitHealth() {
        // Parcours de tous les éléments du jeu
        for (GameElement element : gameElements) {
            // Vérifier si l'élément est une instance d'unité
            if (element instanceof Unit) {
                Unit unit = (Unit) element;
                // Mettre les PV de l'unité à 10
                unit.setCurrentHealth(setCurrentHealthWhenPressedKeyCode);
                System.out.println("Unit health reset to 10: " + unit);
            }
        }
    }

    public void resetSimulation(){
        this.gameElements.clear();
        this.unitElements.clear();
    }

    // Méthode générique pour activer/désactiver le mouvement des unités
    public void toggleMovementForUnitType(Class<? extends Unit> unitType) {
        for (GameElement element : gameElements) {
            if (unitType.isInstance(element)) {
                Unit unit = (Unit) element;
                unit.setMoveStopped(!unit.isMoveStopped());
            }
        }
    }

    public int getROW(){
        return this.ROWS;
    }

    public int getCOL(){
        return this.COLUMNS;
    }
}
