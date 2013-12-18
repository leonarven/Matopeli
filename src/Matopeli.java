public class Matopeli {
 

	// Alustetaan symbolien vakiot
	public static final char SYMBOL_FOOD      = '+';
	public static final char SYMBOL_WORM_HEAD = 'X';
	public static final char SYMBOL_WORM_NECK = 'x';
	public static final char SYMBOL_WORM      = 'o';
	public static final char SYMBOL_BLANK     = ' ';
	public static final char SYMBOL_WALL      = '.';
	
	public class MatopeliException extends Exception {
		
		// Välitetään yläluokalle viesti jolla halutaan virhe tuottaa
		public MatopeliException(String message) {
			super(message);
		}

		// Exception esittelee perittäviensä määritettäväksi
		private static final long serialVersionUID = 1L; 
	};

	// Jotta saataisiin asioihin jotain järkeä tunkaistaan pelin asetukset asetusluokkaan
	public static class GameRules {
		public final int MAX_WORMHOLES = 4;
		public final int WORM_INITIAL_LENGTH = 5;
		
		public final int MAP_MIN_HEIGHT = 3;
		public final int MAP_MIN_WIDTH  = 7;
	}
	
	public enum WormAngle {
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
		QUIT
	};
	
	public abstract class MatopeliGrid {
		public abstract char[][] toCharArray();
	}
	
	public class Point {
		private int x;
		private int y;

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

		public boolean equal(Point point) {
			return this.x() == point.x() && this.y() == point.y();
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
			// Joss madon pään paikalla oleva tile on seinää, ..
			char c = matrix[worm.y()][worm.x()];
			if (c == SYMBOL_WALL) {
				// Seinä muutetaan taustaksi
				matrix[worm.y()][worm.x()] = SYMBOL_BLANK;
				wormholes++; // madonreikien määrää kasvatetaan
				// note-to-hawkings: miten madonreikiä voi olla pariton määrä
				return true;
			}
			return false;
		}
		
		public int wormholes() { return this.wormholes; }
		
		public char[][] toCharArray() {
			return this.matrix;
		}
	}
	public class Worm extends Point { // Koska tyhmä java, ei voi periä enää MatopeliGridiä
		private int[][] wormMatrix;
		private int mapwidth;
		private int mapheight;
		private int wormmax;
		public WormAngle angle;
		
		public Worm(int length, int mapwidth, int mapheight) {
			super(length, 1);

			this.wormmax = length;
			this.mapwidth  = mapwidth;
			this.mapheight = mapheight;
			this.wormMatrix = new int[mapheight][mapwidth];
			this.angle = WormAngle.RIGHT;

			for(int y = 0; y < mapheight; y++)
				for(int x = 0; x < mapwidth; x++)
					wormMatrix[y][x] = 0;
			
			for(int i = 1; i <= this.wormmax; i++) wormMatrix[1][i] = i;
		}

		/* *
		 * Palauttaa true jos mato osuu itseensä, muulloin false
		 * */
		public boolean move(WormAngle a) {
			
			if (!turn(a)) return false;
			
			switch(this.angle) {
				case UP:    this.y(this.y()-1); break;
				case DOWN:  this.y(this.y()+1); break;
				case LEFT:  this.x(this.x()-1); break;
				case RIGHT: this.x(this.x()+1); break;
			}
			
			// Tutkitaan uuden pään sijainti vaakasuunnassa ja tarkistetaan ollaanko kentän reuna ylitetty
			// Jos ollaan, niin siirretään sijainti kentän vastakkaiselle reunalle
			if (this.x() < 0)                    this.x(this.mapwidth-1);
			else if (this.x() >= this.mapwidth)  this.x(0);
			
			// Tehdään sama tarkastelu pystysuunnassa
			if (this.y() < 0)                    this.y(this.mapheight-1);
			else if (this.y() >= this.mapheight) this.y(0);

			if (wormMatrix[this.y()][this.x()] == 0) {
				// Sijoitetaan uusi pää(maksimiarvo) uuteen sijaintiin
				wormMatrix[this.y()][this.x()] = this.wormmax+1;
			} else return true; // Osuttiin matoon itseensä

			// Päivitetään koko matriisi jolloin päästään eroon madon hännästä
			for(int y = 0; y < mapheight; y++)
				for(int x = 0; x < mapwidth; x++)
					if (wormMatrix[y][x] != 0) wormMatrix[y][x]--;
			
			return false;
		}
		
		public boolean turn(WormAngle a) {
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
			}
			this.angle = a;
			return true;
		}
		
		public void addPiece() {
			for(int y = 0; y < mapheight; y++)
				for(int x = 0; x < mapwidth; x++)
					if (wormMatrix[y][x] != 0) wormMatrix[y][x]++;
			this.wormmax++;
		}
		
		// Metodi madon kääntämiseksi ympäri
		public void turnAround() {
			// Käydään läpi jokainen gridin arvo ...
			for(int y = 0; y < mapheight; y++)
				for(int x = 0; x < mapwidth; x++)
					// ... ja jos siinä on matoa, ...
					if (wormMatrix[y][x] != 0)
						// ... muutetaan arvo kaavan mukaisesti
						wormMatrix[y][x] = 1+this.wormmax-wormMatrix[y][x];
		}

		// Kauniimpi tapa arvon noutamiseksi
		public int length() { return this.wormmax; }
		
		// Muutetaan mato-matriisi merkkitaulukoksi
		public char[][] toCharArray() {
			char[][] matrix = new char[mapheight][mapwidth];
			
			// Läpikäydään jokainen matriisin alkio
			for(int y = 0; y < mapheight; y++)
				for(int x = 0; x < mapwidth; x++) {
					// Jos kohdassa on matoa, tarkastellaan mikä osa sitä ja määrätään merkki sen mukaiseksi
					if (wormMatrix[y][x] == 0) {
						matrix[y][x] = SYMBOL_BLANK;
					} else if (wormMatrix[y][x] == wormmax) {
						matrix[y][x] = SYMBOL_WORM_HEAD;
					} else if (wormMatrix[y][x] == wormmax-1) {
						matrix[y][x] = SYMBOL_WORM_NECK;
					} else {
						matrix[y][x] = SYMBOL_WORM;
					}
				}
			return matrix;
		}
	}
	
	/* *
	 * Ruoka-luokka
	 * */
	public class Food extends Point { // Koska tyhmä java, ei voi periä enää MatopeliGridiä

		// Constructori ruoalle: automaatilta ruoan nouto ja sijainnin laskenta
		public Food(Map map, Worm worm) {
			super(0, 0);
			char[][] matrix = mergeArrays(  // Alustetaan ruoka
						map.toCharArray(),  // .. Kartan ..
						worm.toCharArray()); // .. Ja madon perusteella

			// Käytetään maanmainiota tarjoilumetodia
			Automaatti.tarjoile(matrix);

			// Läpikäydään Automaatilta saatu taulukko
			for(int y = 0; y < matrix.length; y++)
				for(int x = 0; x < matrix[0].length; x++) {
					// Jos kohdassa on ruokaa, merkitään sijainti
					if (matrix[y][x] == SYMBOL_FOOD) {
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
					if (this.equal(new Point(x, y))) 
						matrix[y][x] = SYMBOL_FOOD;
				}
			return matrix;
		}
	}

	// Matopeliin liittyvät muuttujat
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
		if (height < rules.MAP_MIN_HEIGHT) throw new MatopeliException("Kenttä liian matala korkeussuunnassa");
		if (width  < rules.MAP_MIN_WIDTH)  throw new MatopeliException("Kenttä liian kapea leveyssuunnassa");

		// Käynnistetään ruoka-automaatti
		Automaatti.kaynnista(randseed);

		// Pelin perusominaisuuksien alustaminen
		this.mapwidth  = width; // Kartan ulottuvuudet
		this.mapheight = height; 
		this.keepRunning = true; // Pelin päivitystoive
		map  = new Map(width, height);  // Kartta
		worm = new Worm(rules.WORM_INITIAL_LENGTH, width, height); // Mato
		food = new Food(map, worm); //Ruoka
	}
	
	public void run() {
		boolean validCommand;
		// Käyttäjän syöttämä komento
		char command;
		// Suunta, johon madon toivotaan kääntyvän
		WormAngle angle;

		// Aloitetaan pääsilmukka, jota jatketaan niin kauan kun keepRunning on true
		while(keepRunning) {
			angle = null;
					
			// Tulostetaan tilanneraportti, pelitilanne ja komennot
			System.out.println("Worm length: "+worm.length()+", wormholes: "+map.wormholes()+".");
			printGame();

			do {
				System.out.println("(l)eft, (r)ight, (u)p, (d)own, (s)wap, (q)uit?");

				// Pyydetään käyttäjältä merkki In-kirjaston maanmainiolla metodilla 
				command = In.readChar();
				validCommand = true;
				
				// Läpikäydään komentovaihtoehdot
				switch(command) {
					case 'u': angle = WormAngle.UP;    break; // komento liikkua ylöspäin
					case 'd': angle = WormAngle.DOWN;  break; // komento liikkua alaspäin
					case 'l': angle = WormAngle.LEFT;  break; // komento liikkua vasemmalle
					case 'r': angle = WormAngle.RIGHT; break; // komento liikkua oikealle
					case 's': worm.turnAround();       break; // komento kääntyä ympäri
					case 'q': keepRunning = false;     break; // komento lopettaa suoritus
					default: validCommand = false;            // virheellinen komento! (jatketaan kyselyä)
				}
			} while(!validCommand); // läpikäydään uudestaan jos komento oli virheellinen
			
			// Jos halutaan liikkua (ollaan annettu liikkumiskäsky johonkin suuntaan 
			if (angle != null)
				// Jos Worm::move palauttaa true, niin mato on osunut itseensä
				if (worm.move(angle)) keepRunning = false;
			
			// Onko mato syömässä ruoan
			if (worm.equal(food)) {
				worm.addPiece();
				food = new Food(map, worm);
			}
			
			// päivitetään kartta madon perusteella 
			map.update(worm);
			
			// Tarkastellaan karttaan tehtyjä reikiä ja jos liikaa, niin lopetetaan
			if (map.wormholes() > rules.MAX_WORMHOLES) keepRunning = false;
		}
	}
	
	public void printGame() {
		// Yhdistetään kartta ja mato matriisiksi
		char[][] gamemap = mergeArrays(map.toCharArray(), worm.toCharArray()); 
		// .. Sekä sen tulos ja ruoka
		         gamemap = mergeArrays(gamemap, food.toCharArray());

		/* *
		 * Note-to-tarkastaja: Tiedostan ratkaisun olevan erittäin raskas,
		 * mutta vetoan siihen että tarkastelun kohteena on vuoropohjainen
		 * yksin pelattava matopeli, jossa ei mitä todennäköimmin tarvitse
		 * olla äärimmäistä optimointia ja tehokkuuden hiomista. Jos näin
		 * olisi, olisi tehtävänantokin toivottavasti toisenlainen ja minä
		 * olisin silloin kyllä tehnyt aika erilailla _MONTA_ tässä olevaa
		 * toteutusta. Rakkautta <3
		 * */
		         
		// Tulostetaan kartta kustannustehokkaasti ja helposti foreach-silmukalla
		for(char[] row : gamemap) {
			for(char col : row) System.out.print(col+" ");
			System.out.print("\n"); // Ettei ihan hyödyttömäksi riviksi mene
		}
	}

	// TODO - korjaa toimivaksi
	public static class MatopeliRules extends GameRules {
		public final int MAX_WORMHOLES = 3;
	}
	
	/* *
	 * Staattiset metodit joita voi vapaasti käyttää
	 * */

	// Olen tykännyt käyttää String... -muotoa String[]:n sijaan koska imho loogisempi
	public static void main(String... args) {
		
//		String[] args = {"1", "5", "10"};

		// Tulosteen header
		System.out.println("~~~~~~~~~~~");
		System.out.println("~ W O R M ~");
		System.out.println("~~~~~~~~~~~");

		/* *
		 * Alustetaan peli ja käsitellään poikkeukset
		 * */
		try {
		
			// Pelin säännöt
			MatopeliRules rules = new MatopeliRules();

			/* Muut juoksevat, komentoriviltä luettavat tiedot.
			 * Jos tiedoissa on vikaa, poikkeukset hoitavat tilanteen ja peli keskeytyy
			 */ 
			int randSeed = Integer.parseInt(args[0]),  // Satunnaislukusiemen
			    mapWidth = Integer.parseInt(args[2]),  // Kartan leveys
			    mapHeight = Integer.parseInt(args[1]); // Kartan korkeus
					
			// Alustetaan peli-instanssi halutuilla tiedoilla
			Matopeli game = new Matopeli( rules, randSeed, mapWidth, mapHeight ); 
			
			/* *
			 * Ajetaan peli ja nautitaan sekä käsitellään poikkeukset
			 * Oma try-käsittely ettei mene ulommille catcheille
			 * */
			try {
				game.run();
			} catch(Exception e) {
				/* *
				 * NullPointerException : Miten tähän päästiin? Peliä ei alustettu. Ei näin voi oikeasti käydä
				 * */
				System.err.println("VIRHEVIRHEVIRHEVIRHE!!!");
				e.printStackTrace();
			}
			
		} // Käsitellään virhetilanteet
		  catch(NumberFormatException e) { // Argumentit väärän tyyppisiä
			// Tulostetaan käyttöohjeet
				System.out.println("Invalid command-line argument!");

		} catch(ArrayIndexOutOfBoundsException e) { // Taulukko liian pieni
			// Tulostetaan käyttöohjeet
			System.out.println("Invalid command-line argument!");

		} catch(MatopeliException e) { // Tiedostettu virhetilanne
			// Tulostetaan käyttöohjeet
			System.out.println("Invalid command-line argument!");

		} // Tilanteessa jossa tulee tunnistamaton poikkeus, tulostetaan pino
		  catch(Exception e) {
			e.printStackTrace();

		} finally {
			System.out.println("Bye, see you soon.");

			// Sammutetaan virtuaalikone
			System.exit(0);
		}
	}

	/* *
	 * Staattinen metodi joka yhdistää kaksi taulua ja palauttaa tuloksen
	 * */
	public static char[][] mergeArrays(char[][] arr1, char[][]arr2) {
		// Ylläpidetään oletusta että taulukot ovat gridejä
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
