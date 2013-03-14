/**
* @author Giuliana Mazzi
* @version 1.0 del 13 marzo 2013
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Definisce il comportamento di una cellula della griglia del Game of Life.
 * Ogni cella e' identificata da un numero di riga, uno di colonna, dallo stato in cui
 * si trova nella generazione attuale e dalle otto cellule confinanti. C'e' anche uno stato
 * futuro che identifica lo stato in cui si trovera' nella generazione successiva.
 * 
 * La classe implementa la classe astratta MouseListener che serve per cogliere i
 * movimenti e i click del mouse su una particolare cellula. Di conseguenza ne sono sovrascritti
 * i relativi metodi astratti.
 */
@SuppressWarnings("serial")
public class Cellula extends JLabel implements MouseListener{
		private static final Dimension DIM_CELLA = new Dimension(10, 10);
		private static final Color[] COLORI ={Color.WHITE, Color.BLACK,	new Color(180, 170, 200)};
		private Life gioco;
		private int riga;
		private int colonna;
		/**
		 * 0 morta, 1 viva, 2 uccisa
		 */
		private int statoAttuale;
		private int statoFuturo;
		/**
		 * Ogni cellula ha otto cellule confinanti. Qui sono identificate dalle loro
		 * posizioni relative, ovvero dallo scostamento di riga e colonna rispetto
		 * alla cellula presa in esame.
		 */
		private Point[] confinanti = {new Point(-1, -1), new Point(0, -1),
				new Point(1, -1), new Point(-1, 0), new Point(1, 0),
				new Point(-1, 1), new Point(0, 1), new Point(1, 1)};
		
		/**
		 * Costruisce una cellula per la griglia del Game of Life.
		 * @param riga e' la riga in cui si trova la cellula sulla griglia di gioco
		 * @param colonna e' la colonna in cui si trova la cellula sulla griglia di gioco
		 * @param gioco e' l'istanza di Life per la quale si crea la cellula
		 */
		public Cellula(int riga, int colonna, Life gioco){
			this.gioco = gioco;
			this.riga = riga;
			this.colonna = colonna;
			statoAttuale = 0;
			statoFuturo = 0;
			setOpaque(true);
			setBackground(COLORI[0]);
			addMouseListener(this);
			this.setPreferredSize(DIM_CELLA);
		}

		/**
		 * In base allo stato attuale della cellula e dei suoi otto vicini, si definsce lo
		 * stato futuro che prendera' nella generazione successiva. Considerando che la
		 * griglia di gioco e' un toroide, i vicini delle cellule che si trovano sul contorno
		 * sono le cellule che si trovano al capo opposto della griglia. Per implementare
		 * questa caratteristica e' stato usato il modulo e un array con le posizioni relative
		 * dei confinanti (corrette con l'aggiunta del numero di righe (o colonne) per evitare
		 * che ci si potesse riferire alla riga o alla colonna -1).  
		 */
		public void checkStato(){
			int nViciniVivi = 0;
			for(int i = 0; i < 8; i++){
				int statoVicino = gioco.cellule[(riga + confinanti[i].x + Life.RIGHE) % Life.RIGHE]
						[(colonna + confinanti[i].y + Life.COLONNE) % Life.COLONNE].statoAttuale;
				if(statoVicino == 2)
					continue;
				nViciniVivi += statoVicino;
			}
			if(statoAttuale == 1){
				if(nViciniVivi < 2)
					statoFuturo = 0;
				if(nViciniVivi > 3)
					statoFuturo = 0;
			}else if(statoAttuale == 0 && nViciniVivi == 3)
				statoFuturo = 1;
		}

		/**
		 * Si aggiorna lo stato attuale in base alle elaborazioni fatte da checkStato() e
		 * salvate in statoFuturo.
		 */
		public void aggiornaStato(){
			if(statoAttuale != statoFuturo){
				statoAttuale = statoFuturo;
				setBackground(COLORI[statoAttuale]);
			}
		}

		/**
		 * Ripulisce la griglia, riportando la cellula allo stato morto.
		 */
		public void pulisci() {
			if(statoAttuale != 0 || statoFuturo != 0){
				statoAttuale = 0;
				statoFuturo = 0;
				setBackground(COLORI[statoAttuale]);
			}
		}

		/**
		 * Gestisce il caso di pressione e rilascio del tasto del mouse sulla stessa cellula.
		 * Il metodo e' vuoto perche' riconducibile ai metodi mousePressed()+mouseReleased().
		 */
		@Override
		public void mouseClicked(MouseEvent arg0){
		}
		
		/**
		 * Gestisce il caso di pressione del tasto del mouse su una cellula, senza rilasciarlo.
		 * In base ai pulsanti eventualmente attivati nei due frame dell'interfaccia grafica,
		 * si ha un effetto diverso sulla cellula.
		 * 
		 * Per la costruzione sulla griglia delle figure prese dai widget, si invocano i metodi
		 * bianche() e nere(). Il primo rende "morte" tutte le cellule nel rettangolo di
		 * dimensione pari alla figura, considerando la cellula selezionata dall'utente
		 * come angolo in alto a sinistra. Poi, con nere() si rendono "vive" le cellule che
		 * nella figura sono nere. Cio' e' effettuato passando al metodo nere() un array di
		 * numeri minori di 99, corrispondenti alle cellule che dovranno essere nere. In
		 * particolare, in questi numeri la prima cifra indica la riga e la seconda la colonna,
		 * calcolate come riferimenti relativi a partire dalla posizione assoluta della cellula
		 * selezionata dall'utente. Nel caso del pulsar che ha dimensione maggiore di 9, si e'
		 * divisa la figura in quattro quadranti.
		 */
		@Override
		public void mousePressed(MouseEvent arg0){
			if(!gioco.bKill.isEnabled()){
				statoAttuale = 2;
				statoFuturo = 2;
				setBackground(COLORI[2]);
				gioco.bKill.setEnabled(true);

			}else if(!gioco.block.isEnabled()){ 
				bianche(riga, colonna, 4, 4);
				nere(riga, colonna, new int[]{11,12,21,22});
				gioco.block.setEnabled(true);

			}else if(!gioco.beehive.isEnabled()){
				bianche(riga, colonna, 5, 6);
				nere(riga, colonna, new int[]{12,13,21,24,32,33});
				gioco.beehive.setEnabled(true);

			}else if(!gioco.loaf.isEnabled()){
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[]{12,13,21,24,32,34,43});
				gioco.loaf.setEnabled(true);

			}else if(!gioco.boat.isEnabled()){
				bianche(riga, colonna, 5, 5);
				nere(riga, colonna, new int[]{11,12,21,23,32});
				gioco.boat.setEnabled(true);

			}else if(!gioco.blinker.isEnabled()){
				bianche(riga, colonna, 5, 5);
				nere(riga, colonna, new int[]{21,22,23});
				gioco.blinker.setEnabled(true);

			}else if(!gioco.toad.isEnabled()){
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[]{22,23,24,31,32,33});
				gioco.toad.setEnabled(true);

			}else if(!gioco.beacon.isEnabled()){
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[]{11,12,21,34,43,44});
				gioco.beacon.setEnabled(true);

			}else if(!gioco.pulsar.isEnabled()){
				bianche(riga, colonna, 9, 9);
				bianche(riga, colonna + 9, 9, 8);
				bianche(riga + 9, colonna, 8, 9);
				bianche(riga + 9, colonna + 9, 8, 8);
				nere(riga, colonna, new int[]{15,25,35,36,51,52,53,56,57,63,65,67,75,76});
				nere(riga, colonna + 9, new int[]{12,22,31,32,50,51,54,55,56,60,62,64,71,72});
				nere(riga + 9, colonna, new int[]{5,6,13,15,17,21,22,23,26,27,45,46,55,65});
				nere(riga + 9, colonna + 9, new int[]{1,2,10,12,14,20,21,24,25,26,41,42,52,62});
				gioco.pulsar.setEnabled(true);

			}else if(!gioco.glider.isEnabled()){
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[]{12,23,31,32,33});
				gioco.glider.setEnabled(true);

			}else if(!gioco.LWSS.isEnabled()){
				bianche(riga, colonna, 7, 9);
				nere(riga, colonna, new int[]{12,15,26,32,36,43,44,45,46});
				gioco.LWSS.setEnabled(true);

			}else{
				gioco.mouseOn = true;
				if(statoAttuale != 2){
					statoAttuale = 1;
					statoFuturo = 1;
					setBackground(COLORI[1]);
				}
			}
		}
		
		/**
		 * Gestisce il rilascio del tasto del mouse, mettendo a false il booleano mouseOn
		 * dell'istanza del Game of Life in esecuzione.
		 */
		@Override
		public void mouseReleased(MouseEvent arg0){
			gioco.mouseOn = false;
		}

		/**
		 * Gestisce cosa accade quando il mouse entra nello spazio di una cellula. Il contorno
		 * della cellula diventa blu per evidenziare che il focus e' sulla cellula. Inoltre,
		 * se il tasto del mouse e' attivo e la cellula non e' stato uccisa, la cellula e'
		 * resa viva. Serve nella costruzione dello stato iniziale del Game of Life.
		 */
		@Override
		public void mouseEntered(MouseEvent arg0){
			setBorder(BorderFactory.createLineBorder(Color.BLUE));
			if(gioco.mouseOn){
				if(statoAttuale != 2){
					statoAttuale = 1;
					statoFuturo = 1;
					setBackground(COLORI[1]);
				}
			}
		}

		/**
		 * Gestisce cosa accade quando il mouse lascia lo spazio di una cellula. Si toglie il
		 * contorno blu.
		 */
		@Override
		public void mouseExited(MouseEvent arg0){
			setBorder(BorderFactory.createEmptyBorder());
		}
	

	/**
	 * Il metodo bianche() assieme al metodo nere() costruisce sulla griglia il widget
	 * selezionato dall'utente (qui in particolare si costruisce la parte di cellule bianche).
	 *   
	 * @param riga e' la riga della cella selezionata per posizionare il widget
	 * @param colonna e' la colonna della cella selezionata per posizionare il widget
	 * @param altezza e' l'altezza del widget selezionato
	 * @param larghezza e' la larghezza del widget selezionato
	 */
	private void bianche(int riga, int colonna, int altezza, int larghezza){
		for(int r = riga; r < riga + altezza; r++)
			for(int c = colonna; c < colonna + larghezza; c++){
				gioco.cellule[r % Life.RIGHE][c % Life.COLONNE].statoAttuale = 0;
				gioco.cellule[r % Life.RIGHE][c % Life.COLONNE].statoFuturo = 0;
				gioco.cellule[r % Life.RIGHE][c % Life.COLONNE].setBackground(COLORI[0]);
			}
	}

	/**
	 * Il metodo nere() assieme al metodo bianche() costruisce sulla griglia il widget
	 * selezionato dall'utente (qui in particolare si costruisce la parte di cellule nere).
	 *  
	 * @param riga e' la riga della cella selezionata per posizionare il widget
	 * @param colonna e' la colonna della cella selezionata per posizionare il widget
	 * @param posizioni sono le posizioni delle cellule nere (vive) nel widget.
	 * Sono numeri minori di 99 in cui la prima cifra indica la riga e la seconda
	 * indica la colonna della cellula da annerire.
	 */
	private void nere(int riga, int colonna, int[] posizioni){
		int ri;
		int co;
		for(int numero : posizioni){
			ri = numero / 10;
			co = numero % 10;
			gioco.cellule[(riga + ri) % Life.RIGHE][(colonna + co) % Life.COLONNE].statoAttuale = 1;
			gioco.cellule[(riga + ri) % Life.RIGHE][(colonna + co) % Life.COLONNE].statoFuturo = 1;
			gioco.cellule[(riga + ri) % Life.RIGHE][(colonna + co) % Life.COLONNE].setBackground(COLORI[1]);
		}
	}
	
	/**
	 * Questa classe rappresenta un punto, inteso come coordinate di una cellula sulla griglia.
	 * La coordinata x definisce la riga della cellula, mentre la y definisce la colonna.
	 * Piu' precisamente, le coordinate sono usate come riferimenti relativi delle otto cellule
	 * confinanti con una cellula presa in esame.
	 * Per esempio, la cellula confinante di nord ovest ha come valori {-1,-1}, ovvero 
	 * si trova ad una riga ed ad una colonna in meno di quella presa a riferimento.
	 */
	private class Point{
		int x;
		int y;

		Point(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
}
