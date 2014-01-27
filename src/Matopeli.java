/**
 * 
 * @Author: Miro 'leonarven' Nieminen
 * @see {@link http://www.uta.fi/sis/tie/laki/opetus/harjoitustyot/harjoitustyo2.html}
 * @category Lausekielinen Ohjelmointi
 * 
 * 
 * 
 * 
 * 
 */

public class Matopeli {

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_FAILURE = 1;

    // Alustetaan symbolien vakiot
    public static final char SYMBOL_FOOD      = '+';
    public static final char SYMBOL_WORM_HEAD = 'X';
    public static final char SYMBOL_WORM_NECK = 'x';
    public static final char SYMBOL_WORM      = 'o';
    public static final char SYMBOL_BLANK     = ' ';
    public static final char SYMBOL_WALL      = '.';
    
    // Debug-muuttuja testauksen tulosteiden saamiseksi (poista lopullisesta)
    public static final boolean DEBUG = false;
    
    public static final void DEBUG(String msg, Exception e) {
        if (!DEBUG) return;
        
        System.err.println("DEBUG: "+msg);
        if (e != null) e.printStackTrace(System.err);
    }
    public static final void DEBUG(String msg) {
        DEBUG(msg, null);
    }
    
    public static class MatopeliException extends Exception {
        
        // V‰litet‰‰n yl‰luokalle viesti jolla halutaan virhe tuottaa
        public MatopeliException(String message) {
            super(message);
        }

        // Exception esittelee peritt‰viens‰ m‰‰ritett‰v‰ksi
        private static final long serialVersionUID = 1L; 
    };

    public static class CommandlineArgsException extends Exception {
        
        // V‰litet‰‰n yl‰luokalle viesti jolla halutaan virhe tuottaa
        public CommandlineArgsException(String message) {
            super(message);
        }

        // Exception esittelee peritt‰viens‰ m‰‰ritett‰v‰ksi
        private static final long serialVersionUID = 1L; 
    };
    // Jotta saataisiin asioihin jotain j‰rke‰ tunkaistaan pelin asetukset asetusluokkaan
    public static class GameRules {
        public int MAX_WORMHOLES = 4;
        public int WORM_INITIAL_LENGTH = 5;
        
        public int MAP_MIN_HEIGHT = 3;
        public int MAP_MIN_WIDTH  = 7;
    }
    
    public enum WormAngle {
        UNDEFINED('x'),
        UP   ('u'),
        DOWN ('d'),
        LEFT ('l'),
        RIGHT('r');
        
        private char command;
        private WormAngle(char command) {
            this.command = command;
        }
        public String toString() { return command+""; }
    };

    public enum GameCommand {
        MOVE,
        SWAP,
        DEBUG,
        QUIT
    };
    
    public abstract class MatopeliGrid {
        public abstract char[][] toCharArray();
    }
    
    public class Point {
        private int x = 0;
        private int y = 0;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        // Point::getters
        public int x() { return this.x; }
        public int y() { return this.y; }

        // Point::setters
        protected void x(int x) { this.x = x; }
        protected void y(int y) { this.y = y; }

        public boolean equal( int x, int y ) {
            return this.x() == x && this.y() == y;
        }

        public boolean equal( Point point ) {
            return equal(point.x(), point.y());
        }

        public void setPoint( int newX, int newY ) {
            this.x(newX);
            this.y(newY);
        }
        
        // Debug - metodi
        public String toString() {
            return "("+this.x+","+this.y+")";
        }
    }

    public class Map extends MatopeliGrid {
        private char[][] matrix;
        private int wormholes;
        
        public Map(int width, int height) {
            matrix = new char[height][width];
            wormholes = 0;
            
            for( int y = 0; y < height; y++ )
                for( int x = 0; x < width; x++ ) {
                    if ( x == 0 || y == 0 || x == width-1 || y == height-1 ) 
                         matrix[y][x] = SYMBOL_WALL;
                    else matrix[y][x] = SYMBOL_BLANK;
                }
        }
        
        public boolean update(Worm worm) {
            // Joss madon p‰‰n paikalla oleva tile on sein‰‰, ..
            char c = matrix[worm.y()][worm.x()];
            if (c == SYMBOL_WALL) {
                // Sein‰ muutetaan taustaksi
                matrix[worm.y()][worm.x()] = SYMBOL_BLANK;
                wormholes++; // madonreikien m‰‰r‰‰ kasvatetaan
                // note-to-hawkings: miten madonreiki‰ voi olla pariton m‰‰r‰
                return true;
            }
            return false;
        }
        
        public int wormholes() { return this.wormholes; }
        
        public char[][] toCharArray() {
            return this.matrix;
        }
    }
    public class Worm extends Point { // Koska tyhm‰ java, ei voi peri‰ en‰‰ MatopeliGridi‰
        private int[][] wormMatrix;
        private int mapwidth;
        private int mapheight;
        private int wormmax;
        private WormAngle angle;
        
        public Worm(int length, int mapwidth, int mapheight) {
            super(length, 1);

            this.wormmax = length;
            this.mapwidth  = mapwidth;
            this.mapheight = mapheight;
            this.wormMatrix = new int[mapheight][mapwidth];
            this.angle = WormAngle.RIGHT;

            for(int y = 0; y < mapheight; y++)
                for(int x = 0; x < mapwidth; x++)
                    wormMatrix[y][x] = -1;
            
            // Pelin s‰‰ntˆihin perustuva asetelma
            for(int i = 1; i <= this.wormmax; i++) wormMatrix[1][i] = i;
        }

        /* *
         * Palauttaa true jos mato osuu itseens‰, muulloin false
         * */
        public boolean move(WormAngle a) throws MatopeliException {
            
            if (!turn(a)) {
                DEBUG("Cannot turn to "+a+" (old angle "+this.angle()+")"); // FIXME: DEBUG
                return false;
            }
            
            switch(this.angle) {
                case UP:    this.y(this.y()-1); break;
                case DOWN:  this.y(this.y()+1); break;
                case LEFT:  this.x(this.x()-1); break;
                case RIGHT: this.x(this.x()+1); break;
                default:
            }
            
            // Tutkitaan uuden p‰‰n sijainti vaakasuunnassa ja tarkistetaan ollaanko kent‰n reuna ylitetty
            // Jos ollaan, niin siirret‰‰n sijainti kent‰n vastakkaiselle reunalle
            if (this.x() < 0)                    this.x(this.mapwidth-1);
            else if (this.x() >= this.mapwidth)  this.x(0);
            
            // Tehd‰‰n sama tarkastelu pystysuunnassa
            if (this.y() < 0)                    this.y(this.mapheight-1);
            else if (this.y() >= this.mapheight) this.y(0);

            // P‰ivitet‰‰n koko matriisi jolloin p‰‰st‰‰n eroon madon h‰nn‰st‰
            for(int y = 0; y < mapheight; y++)
                for(int x = 0; x < mapwidth; x++)
                    if (wormMatrix[y][x] >= 0) wormMatrix[y][x]--;

            if (wormMatrix[this.y()][this.x()] <= 0) {
                // Sijoitetaan uusi p‰‰(maksimiarvo) uuteen sijaintiin
                wormMatrix[this.y()][this.x()] = this.wormmax;
            } else {
                DEBUG("wormMatrix[this.y()][this.x()] > 0"); // FIXME: DEBUG
                return true; // Osuttiin matoon itseens‰
            }

            return false;
        }
        
        public boolean turn(WormAngle a) throws MatopeliException {
            switch(a) {
                case UP:
                    if (this.angle == WormAngle.DOWN)  return false;
                    break;
                case DOWN: 
                    if (this.angle == WormAngle.UP)    return false;
                    break;
                case LEFT:
                    if (this.angle == WormAngle.RIGHT) return false;
                    break;
                case RIGHT:
                    if (this.angle == WormAngle.LEFT)  return false;
                    break;
                case UNDEFINED:
                    throw new MatopeliException("Tried to turn to angle UNKNOWN");
            }
            this.angle = a;
            return true;
        }
        
        public void addPiece() {
            for(int y = 0; y < mapheight; y++)
                for(int x = 0; x < mapwidth; x++)
                    if (wormMatrix[y][x] >= 0) wormMatrix[y][x]++;
            this.wormmax++;
        }

        // Metodi madon k‰‰nt‰miseksi ymp‰ri
        public void turnAround() {
            // K‰yd‰‰n l‰pi jokainen gridin arvo ...
            for(int py = 0; py < mapheight; py++) {
                for(int px = 0; px < mapwidth; px++) {
                    // ... ja jos siin‰ on matoa, ...
                    if (wormMatrix[py][px] > 0) 
                        // ... muutetaan arvo kaavan mukaisesti
                        wormMatrix[py][px] = 1+this.wormmax-wormMatrix[py][px];
                    if (wormMatrix[py][px] == this.wormmax) this.setPoint(px,  py);
                }
            }
            // K‰‰nnet‰‰n madon oikea suunta
            this.angle = detectAngle();
            DEBUG("Worm::turnAround: New angle "+this.angle);
            
//            this.angle = WormAngle.UNDEFINED;
        }

        // Kauniimpi tapa arvon noutamiseksi
        public int length() { return this.wormmax; }

        public int mapX(int x) {
            int w = wormMatrix[0].length;
            
            if (x < 0) x = x + w;
            else if (x >= w) x = x - w - 1;

            return x;
        }

        public int mapY(int y) {
            int h = wormMatrix.length;
                
            if (y < 0) y = y + h;
            else if (y >= h) y = y - h - 1;

            return y;
        }

        public WormAngle detectAngle() {
            int x = this.x(),
                y = this.y();
            
            if      (wormMatrix[y][mapX(x-1)] == this.wormmax-1) return WormAngle.RIGHT;
            else if (wormMatrix[y][mapX(x+1)] == this.wormmax-1) return WormAngle.LEFT;
            else if (wormMatrix[mapY(y-1)][x] == this.wormmax-1) return WormAngle.DOWN;
            else if (wormMatrix[mapY(y+1)][x] == this.wormmax-1) return WormAngle.UP;

            return WormAngle.UNDEFINED;
        }
        
        // Muutetaan mato-matriisi merkkitaulukoksi
        public char[][] toCharArray() {
            char[][] matrix = new char[mapheight][mapwidth];
            char[] debug_charArray = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'k', 'l', 'm', 'n', 'o', 'p', 'q'};
            
            // L‰pik‰yd‰‰n jokainen matriisin alkio
            for(int y = 0; y < mapheight; y++)
                for(int x = 0; x < mapwidth; x++) {
                    // Jos kohdassa on matoa, tarkastellaan mik‰ osa sit‰ ja m‰‰r‰t‰‰n merkki sen mukaiseksi
                    if (wormMatrix[y][x] <= 0) {
                        matrix[y][x] = SYMBOL_BLANK;
                    } else if (wormMatrix[y][x] == wormmax) {
                        matrix[y][x] = SYMBOL_WORM_HEAD;
                    } else if (wormMatrix[y][x] == wormmax-1) {
                        matrix[y][x] = SYMBOL_WORM_NECK;
                    } else {
                        matrix[y][x] = SYMBOL_WORM;
                    }
                    
                    if (Matopeli.DEBUG) {
                        if (wormMatrix[y][x] <= 0) 
                             matrix[y][x] = SYMBOL_BLANK;
                        else matrix[y][x] = debug_charArray[this.wormmax - wormMatrix[y][x]];
                    } 
                }
            return matrix;
        }
        
        public WormAngle angle() {
            return this.angle;
        }
    }
    /* *
     * Ruoka-luokka
     * */
    public class Food extends Point { // Koska tyhm‰ java, ei voi peri‰ en‰‰ MatopeliGridi‰

        // Constructori ruoalle: automaatilta ruoan nouto ja sijainnin laskenta
        public Food(char[][] hitmatrix) {
            super(-1, -1);

            // K‰ytet‰‰n maanmainiota tarjoilumetodia
            Automaatti.tarjoile(hitmatrix);

            // L‰pik‰yd‰‰n Automaatilta saatu taulukko
            for(int y = 0; y < hitmatrix.length; y++)
                for(int x = 0; x < hitmatrix[0].length; x++) {
                    // Jos kohdassa on ruokaa, merkit‰‰n sijainti
                    if (hitmatrix[y][x] == SYMBOL_FOOD) {
                        this.x(x);
                        this.y(y);
                    }
                }
        }

        // Generoidaan merkkitaulukko ruoan sijainnista.
        public char[][] toCharArray() {
            char[][] matrix = new char[mapheight][mapwidth];

            for(int y = 0; y < mapheight; y++)
                for(int x = 0; x < mapwidth; x++) {
                    matrix[y][x] = SYMBOL_BLANK;
                    if (this.equal(x, y)) 
                        matrix[y][x] = SYMBOL_FOOD;
                }
            return matrix;
        }
    }

    // Matopeliin liittyv‰t muuttujat
    private int     mapwidth;
    private int     mapheight;
    private boolean keepRunning;
    private Worm    worm;
    private Map     map;
    private Food    food;
    private GameRules rules;

    public Matopeli(GameRules rules, int randseed, int width, int height) throws MatopeliException {
        this.rules = rules;

        // joss kartan ulottuvuudet ovat virheelliset
        if (height < rules.MAP_MIN_HEIGHT) throw new MatopeliException("Kentt‰ liian matala korkeussuunnassa");
        if (width  < rules.MAP_MIN_WIDTH)  throw new MatopeliException("Kentt‰ liian kapea leveyssuunnassa");

        // K‰ynnistet‰‰n ruoka-automaatti
        Automaatti.kaynnista(randseed);

        // Pelin perusominaisuuksien alustaminen
        this.mapwidth  = width; // Kartan ulottuvuudet
        this.mapheight = height; 
        this.keepRunning = true; // Pelin p‰ivitystoive
        map  = new Map(width, height);  // Kartta
        worm = new Worm(rules.WORM_INITIAL_LENGTH, width, height); // Mato
        food = new Food(mergeArrays(map.toCharArray(), worm.toCharArray())); //Ruoka
    }
    
    public void run() throws MatopeliException {
        boolean validCommand;
        // K‰ytt‰j‰n syˆtt‰m‰ komento
        char command;
        // Suunta, johon madon toivotaan k‰‰ntyv‰n
        WormAngle angle;

        // Aloitetaan p‰‰silmukka, jota jatketaan niin kauan kun keepRunning on true
        while(keepRunning) {
            angle = null;
                    
            // Tulostetaan tilanneraportti, pelitilanne ja komennot
            System.out.println("Worm length: "+worm.length()+", wormholes: "+map.wormholes()+".");
            printGame();

            do {
                System.out.println("(l)eft, (r)ight, (u)p, (d)own, (s)wap or (q)uit?");

                // Pyydet‰‰n k‰ytt‰j‰lt‰ merkki In-kirjaston maanmainiolla metodilla 
                command = In.readChar();
                validCommand = true;
                
                // L‰pik‰yd‰‰n komentovaihtoehdot
                switch(command) {
                    case 'u': angle = WormAngle.UP;    break; // komento liikkua ylˆsp‰in
                    case 'd': angle = WormAngle.DOWN;  break; // komento liikkua alasp‰in
                    case 'l': angle = WormAngle.LEFT;  break; // komento liikkua vasemmalle
                    case 'r': angle = WormAngle.RIGHT; break; // komento liikkua oikealle
                    case 's': worm.turnAround();       break; // komento k‰‰nty‰ ymp‰ri
                    case 'q': keepRunning = false;     break; // komento lopettaa suoritus
                    default: validCommand = false;            // virheellinen komento! (jatketaan kysely‰)
                }
            } while(!validCommand); // l‰pik‰yd‰‰n uudestaan jos komento oli virheellinen

            // Jos halutaan liikkua (ollaan annettu liikkumisk‰sky johonkin suuntaan 
            if (angle != null)
                // Jos Worm::move palauttaa true, niin mato on osunut itseens‰
                if (worm.move(angle)) {
                    keepRunning = false;
                    DEBUG("DEBUG: worm.move("+angle+") == true"); // FIXME: DEBUG
                }

            // Onko mato syˆm‰ss‰ ruoan
            if (worm.equal(food)) {
                worm.addPiece();
                food = new Food(mergeArrays(worm.toCharArray(), map.toCharArray()));
            }

            // p‰ivitet‰‰n kartta madon perusteella 
            map.update(worm);
            
            // Tarkastellaan karttaan tehtyj‰ reiki‰ ja jos liikaa, niin lopetetaan
            if (map.wormholes() >= rules.MAX_WORMHOLES) {
                keepRunning = false;
                DEBUG("map.wormholes() >= rules.MAX_WORMHOLES"); // FIXME: DEBUG
            }
        }
    }
    
    public void printGame() {
        // Yhdistet‰‰n kartta ja mato matriisiksi
        char[][] gamemap = mergeArrays(map.toCharArray(), food.toCharArray()); 
        // .. Sek‰ sen tulos ja ruoka
                 gamemap = mergeArrays(gamemap, worm.toCharArray());

        /* *
         * Note-to-tarkastaja: Tiedostan ratkaisun olevan eritt‰in raskas,
         * mutta vetoan siihen ett‰ tarkastelun kohteena on vuoropohjainen
         * yksin pelattava matopeli, jossa ei mit‰ todenn‰kˆimmin tarvitse
         * olla ‰‰rimm‰ist‰ optimointia ja tehokkuuden hiomista. Jos n‰in
         * olisi, olisi teht‰v‰nantokin toivottavasti toisenlainen ja min‰
         * olisin silloin kyll‰ tehnyt aika erilailla _MONTA_ t‰ss‰ olevaa
         * toteutusta. Rakkautta <3
         * */
                 
        // Tulostetaan kartta kustannustehokkaasti ja helposti foreach-silmukalla
        for(char[] row : gamemap) {
            for(char col : row) System.out.print(col);
            System.out.print("\n"); // Ettei ihan hyˆdyttˆm‰ksi riviksi mene
        }
    }

    public static class MatopeliRules extends GameRules {
        public int MAX_WORMHOLES = 4;
    }
    
    /* *
     * Staattiset metodit joita voi vapaasti k‰ytt‰‰
     * */

    // Olen tyk‰nnyt k‰ytt‰‰ String... -muotoa String[]:n sijaan koska imho loogisempi
    public static void main(String... args) {

        // Tulosteen header
        System.out.println("~~~~~~~~~~~");
        System.out.println("~ W O R M ~");
        System.out.println("~~~~~~~~~~~");

        /* *
         * Alustetaan peli ja k‰sitell‰‰n poikkeukset
         * */
        try {

            // Pelin s‰‰nnˆt
            MatopeliRules rules = new MatopeliRules();

            /* Muut juoksevat, komentorivilt‰ luettavat tiedot.
             * Jos tiedoissa on vikaa, poikkeukset hoitavat tilanteen ja peli keskeytyy
             */ 
            
            if (args.length != 3) throw new CommandlineArgsException("Invalid count of command-line parameters");

            int randSeed, mapWidth, mapHeight;

            try {
                randSeed  = Integer.parseInt(args[0]);  // Satunnaislukusiemen
                mapWidth  = Integer.parseInt(args[2]);  // Kartan leveys
                mapHeight = Integer.parseInt(args[1]); // Kartan korkeus
            } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
                throw new Matopeli.CommandlineArgsException(e.getMessage());
            }
                    
            // Alustetaan peli-instanssi halutuilla tiedoilla
            Matopeli game = new Matopeli( rules, randSeed, mapWidth, mapHeight ); 
            
            /* *
             * Ajetaan peli ja nautitaan sek‰ k‰sitell‰‰n poikkeukset
             * Oma try-k‰sittely ettei mene ulommille catcheille
             * */
            try {
                game.run();

                DEBUG("game.run() stopped"); // FIXME: DEBUG
            } catch(NullPointerException e) {
                /* *
                 * NullPointerException : Miten t‰h‰n p‰‰stiin? Peli‰ ei alustettu. Ei n‰in voi oikeasti k‰yd‰.
                 * Ei ole hyv‰ t‰m‰. Tulostetaan informatiivinen virheviesti
                 * */
                System.err.println("Fatal exception(NullPointerException) while game(Matopeli).run()");
                throw e;
            } catch(Exception e) {
                /* *
                 * MatopeliException : On tehty jotain laitonta ja kovasti tuhmaa
                 * */
                throw e;
            }
            
        } // K‰sitell‰‰n virhetilanteet
          catch(CommandlineArgsException e) {
            // Tulostetaan k‰yttˆohjeet
            System.out.println("Invalid command-line argument!");

            // Sammutetaan virtuaalikone
///            System.exit(EXIT_FAILURE);
        } // Tilanteessa jossa tulee tunnistamaton poikkeus, tulostetaan pino
          catch(Exception e) {
            e.printStackTrace();
            // Sammutetaan virtuaalikone
//            System.exit(EXIT_FAILURE);
        }

        System.out.println("Bye, see you soon.");
        // Sammutetaan virtuaalikone
        System.exit(EXIT_SUCCESS);
    }

    /* *
     * Staattinen metodi joka yhdist‰‰ kaksi taulua ja palauttaa tuloksen
     * */
    public static char[][] mergeArrays(char[][] arr1, char[][]arr2) {
        // Yll‰pidet‰‰n oletusta ett‰ taulukot ovat gridej‰
        // a.k.a jokainen rivi on saman mittainen
        if (arr1.length != arr2.length || arr1[0].length != arr2[0].length) return null;

        char[][] t = new char[arr1.length][arr1[0].length];
        
        for(int y = 0; y < arr1.length; y++)
            for(int x = 0; x < arr1[0].length; x++) {
                // Asetetaan toisena asetettu taulukko etusijalle
                if (arr2[y][x] == ' ')
                     t[y][x] = arr1[y][x];
                else t[y][x] = arr2[y][x];
            }

        return t;
    }
    /* *
     * Metodi joka yhdist‰‰ kolme taulua k‰ytt‰en kahden taulun yhdist‰mist‰
     * */
    public static char[][] mergeArrays(char[][] arr1, char[][]arr2, char[][]arr3) {
        char[][] arr12 = mergeArrays(arr1, arr2);
        return mergeArrays(arr12, arr3);
    }

    public static void var_dump(char[][] taulu) {
        if (taulu == null) return;
        for(int y = 0; y < taulu.length; y++) {
            for(int x = 0; x < taulu[0].length; x++) System.out.print(" "+taulu[y][x]);
            System.out.println("");
        }
    }
    public static void var_dump(int[][] taulu) {
        if (taulu == null) return;
        for(int y = 0; y < taulu.length; y++) {
            for(int x = 0; x < taulu[0].length; x++) System.out.print(" "+taulu[y][x]);
            System.out.println("");
        }
    }
}
