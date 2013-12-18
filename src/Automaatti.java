import java.util.Random; // Random-luokka käyttöön.

/*
 * Lausekielinen ohjelmointi, syksy 2013, toinen harjoitustyö.
 *
 * Apuluokka, jonka avulla pelikenttään sijoitetaan madon ruokaa.
 *
 * VAIN KURSSIN VASTUUOPETTAJA SAA MUUTTAA TÄTÄ LUOKKAA.
 *
 * ÄLÄ KOPIOI METODEJA TÄSTÄ LUOKASTA OMAAN OHJELMAASI.
 *
 * Jorma Laurikkala, Informaatiotieteiden yksikkö, Tampereen yliopisto,
 * jorma.laurikkala@uta.fi.
 *
 * Versio 1.0.
 *
 * Viimeksi muutettu 1.12.2013.
 *
 */

public class Automaatti {

   /*__________________________________________________________________________
    *
    * 1. Julkiset luokkavakiot.
    *
    */
   
   // Merkki kentän taustalle.
   public static final char TAUSTA = ' ';
   
   // Ruoan symboli.
   public static final char RUOKA = '+';

   // Kentän rivien vähimmäismäärä.
   public static final int RIVMINLKM = 3;

   // Kentän sarakkeiden vähimmäismäärä.
   public static final int SARMINLKM = 7;

   /*__________________________________________________________________________
    *
    * 2. Kätketyt attribuutit.
    *
    */

   // Maailmalta kätketty pseudosatunnaislukugeneraattori.
   private static Random generaattori;

   // Tosi, jos on kutsuttu kaynnista-metodia.
   private static boolean kaynnissa = false;

   /*__________________________________________________________________________
    *
    * 3. Harjoitustyöohjelmasta kutsuttavat julkiset metodit.
    *
    */

   /* Käynnistetään ruoka-automaatti. Automaatin pseudosatunnaislukugeneraattori
    * alustetaan siemenluvun avulla. Metodia voidaan kutsua vain kerran; uusi kutsu
    * aiheuttaa ajonaikaisen virheen.
    */
   public static final void kaynnista(int siemen) {
      // Ensimmäinen kutsu.
      if (!kaynnissa) {
         // Luodaan pseudosatunnaislukugeneraattori annetulla siemenluvulla.
         // Tietyllä siemenluvulla saadaan tietty sarja pseudosatunnaislukuja.
         generaattori = new Random(siemen);

         // Automaatti on nyt käynnissä.
         kaynnissa = true;
      }

      // Heitetään poikkeus, jos metodia kutsuttiin uudelleen.
      else
         throw new IllegalArgumentException("Automaton already running!");
   }

   /* Sijoittaa ruokamerkin satunnaisesti valittuun paikkaan kentällä.
    * Ruoka sijoitetaan vain tyhjään (taustamerkin sisältävään) sisäpaikkaan.
    * Toisin sanoen ruokamerkki ei voi olla reunalla eikä se voi korvata madon
    * merkkejä. Paluuarvo on true, jos kentällä on tilaa ruoalle. Paluuarvo on
    * false, jos kentälle ei ole varattu muistia tai muistia on varattu liian
    * vähän tai jos kenttä on täynnä. Metodi ei tarkista kentän sisältöä tämän
    * tarkemmin. Metodia voi kutsua vasta, kun automaatti on käynnistetty.
    * Metodin kutsu ennen automaatin käynnistämistä aiheuttaa ajonaikaisen
    * virheen.
    */
   public static final boolean tarjoile(char[][] kentta) {
      // Heitetään poikkeus, jos automaattia ei ole käynnistetty.
      if (!kaynnissa)
         throw new IllegalArgumentException("Automaton not running!");

      // Oletetaan, että matoa ei voida ruokkia.
      boolean ruokaOnKentalla = false;

      // Yritetään ruokkia vain, jos taulukolle on varattu muistia ja muistia
      // on sen verran, että ohjelma ei kaadu taulukkoa käsiteltäessä.
      if (kentta != null && kentta.length >= RIVMINLKM 
      && kentta[0].length >= SARMINLKM) {
         // Päätellään viimeisen sisäpaikan rivi- ja sarakeindeksit.
         int vikaSisarivi = kentta.length - 2;
         int vikaSisasarake = kentta[0].length - 2;

         // Päätellään sisäpaikkojen lukumäärä.
         int sisapaikkoja = (kentta.length - 2) * (kentta[0].length - 2);

         // Taulukko tyhjien sisäpaikkojen rivi- ja sarakeindekseille.
         // Taulukolle varataan varmuuden vuoksi muistia suurimman mahdollisen
         // tilatarpeen mukaan (kaikki sisäpaikat tyhjiä).
         int[][] tyhjatPaikat = new int[sisapaikkoja][2];

         // Laskuri tyhjille sisäpaikoille.
         int tyhjia = 0;

         // Käydään läpi kaikki sisäpaikat ja sijoitetaan tyhjien paikkojen
         // indeksit taulukkoon. Taulukon rivin ensimmäisessä alkiossa on
         // paikan rivi-indeksi ja toisessa alkiossa paikan sarakeindeksi.
         for (int rivi = 1; rivi <= vikaSisarivi; rivi++)
            for (int sarake = 1; sarake <= vikaSisasarake; sarake++)
               // Löydettiin tyhjä paikka.
               if (kentta[rivi][sarake] == TAUSTA) {
                  // Paikan rivi- ja sarakeindeksit paikkataulukon riville.
                  tyhjatPaikat[tyhjia][0] = rivi;
                  tyhjatPaikat[tyhjia][1] = sarake;

                  // Päivitetään laskuria.
                  tyhjia++;
               }

         // Ruoka mahtuu kentälle.
         if (tyhjia > 0) {
            // Valitaan satunnaisesti tyhjä paikka.
            int paikka = generaattori.nextInt(tyhjia);

            // Sijoitetaan valittuun paikkaan ruokaa.
            int ruokaRivi = tyhjatPaikat[paikka][0];
            int ruokaSarake = tyhjatPaikat[paikka][1];
            kentta[ruokaRivi][ruokaSarake] = RUOKA;

            // Käännetään lippu.
            ruokaOnKentalla = true;
         }
      }

      // Palautetaan lippumuuttujan arvo.
      return ruokaOnKentalla;
   }
}