/**
* @author Giuliana Mazzi
* @version 1.0 del 17 marzo 2013
*/

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**La classe Life definisce una versione del Game of Life di John Conway.
 * Si tratta di un gioco il cui esito dipende solo dalla configurazione iniziale
 * di una griglia di cellule, il cui stato puo' essere morto (bianco) o vivo (nero).
 * Per ogni cellula il passaggio da uno stato a quello successivo dipende dallo
 * stato proprio e degli otto vicini sulla base di tre regole:
 * - una cellula morta con 3 vicini vivi, resuscita
 * - una cellula viva con 2 o 3 vicini vivi, sopravvive
 * - una cellula viva con meno di 2 o piu' di 3 vicini vivi, muore
 * Inoltre, in questa versione, una cellula puo' essere anche nello stato "uccisa"
 * ovvero morta definitivamente.
 * 
 * La griglia e' implementata come un toroide, quindi, i vicini delle cellule che si trovano
 * sul contorno sono le cellule che si trovano al capo opposto della griglia.
 * 
 * Col passare delle generazioni tendono ad emergere delle figure standard.
 * Quelle piu' comuni sono rappresentate nella finestra dei widgets e possono
 * essere posizionate sulla griglia per definire lo stato iniziale del gioco.
*/
public class Life implements ActionListener{
	
	public static final int RIGHE = 50;
	public static final int COLONNE = 75;
	public Cellula[][] cellule;			//griglia di cellule
	public boolean mouseOn = false;		//identifica la pressione del mouse
	private Timer timer; 				// uso il timer Swing (event dispatch thread)
	private int generazioni = 0;		
	private boolean avviato = false;	//dice se il gioco e' in corso 
	private int nThreads = 1;			//di default si usa un thread per il calcolo delle generazioni 
	
	Container pane;
	JPanel griglia;
	JTextArea lthreads;
	JTextField tthreads;
	JButton bcambia;
	JButton bReset;
	JButton bPause;
	JButton bGo;
	JButton bKill;
	JLabel lgenerazioni;
	JSlider slider;
	JLabel lspeed;

	Container widgets;
	JPanel stillLifes;
	JPanel oscillators;
	JPanel spaceships;
	JTextArea info;
	JButton block;
	JButton beehive;
	JButton loaf;
	JButton boat;
	JButton blinker;
	JButton toad;
	JButton beacon;
	JButton pulsar;
	JButton glider;
	JButton LWSS;

	/**
	 * Avvia il Game of Life usando invokeLater. Questo e' necessario perche' Swing non e'
	 * thread-safe (ovvero c'e' rischio di race condition tra i thread che modificano
	 * componenti Swing) e quindi ogni modifica su un componente Swing va fatta solo con
	 * l'Event dispatcher Thread.
	 * Con InvokeLater l'esecuzione del gioco viene accodata alle richieste pendenti di EDT.
	 */
	public static void main(String[] arg){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Life();
			}
		});
	}

	/**
	 * Costruisce un Game of Life
	 */
	public Life(){
		creaGUI();
	}

	/**
	 * Costruisce l'interfaccia grafica del Game of Life, creando due finestre: una
	 * per la griglia del gioco e una per i widget di selezione. In entrambi i casi
	 * si usa GridBagLayout come Layout manager in modo da avere la massima liberta'
	 * nell'organizzazione degli elementi nel frame. 
	 */
	public void creaGUI(){
		JFrame windowForGame = new JFrame("Game Of Life - Giuliana Mazzi");
		pane = windowForGame.getContentPane();
		pane.setLayout(new GridBagLayout());
		pane.setBackground(Color.LIGHT_GRAY);
		disponi();
		windowForGame.setLocation(370, 10);
		windowForGame.setResizable(false);
		windowForGame.setVisible(true);
		windowForGame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowForGame.pack();

		JFrame windowForWidgets = new JFrame("Widgets - Giuliana Mazzi");
		widgets = windowForWidgets.getContentPane();
		widgets.setLayout(new GridBagLayout());
		widgets.setBackground(Color.LIGHT_GRAY);
		disponiWidgets();
		windowForWidgets.setLocation(10, 10);
		windowForWidgets.setResizable(false);
		windowForWidgets.setVisible(true);
		windowForWidgets.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowForWidgets.pack();
	}

	/**
	 * Organizza gli elementi nel frame relativo alla griglia di gioco.
	 */
	public void disponi(){
		lthreads = new JTextArea("Threads in uso:");
		lthreads.setBackground(Color.LIGHT_GRAY);
		tthreads = new JTextField(3);
		tthreads.setText("1");
		tthreads.setHorizontalAlignment(JTextField.CENTER);
		tthreads.setEditable(false);
		bcambia = new JButton("Cambia");
		bcambia.setPreferredSize(new Dimension(70, 22));
		bcambia.setMargin(new Insets(0, 0, 0, 0));
		bcambia.setToolTipText("cambia il numero di thread da usare per il passaggio delle generazioni");
		bGo = new JButton("AVVIO");
		bGo.setForeground(Color.RED);
		bGo.setToolTipText("avvia il game of life");
		bPause = new JButton("Pausa");
		bPause.setToolTipText("metti in pausa il game of life");
		bPause.setEnabled(false);
		bReset = new JButton("Reset");
		bReset.setToolTipText("pulisci la griglia e resetta il numero di thread");
		bKill = new JButton("Uccidi");
		bKill.setToolTipText("premi Uccidi e poi seleziona la cellula da far morire definitivamente");
		lgenerazioni = new JLabel("Generazioni: 0");
		slider = new JSlider(0, 100, 1);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(2);
		slider.setPaintTicks(true);
		slider.setBackground(Color.LIGHT_GRAY);
		slider.setForeground(Color.BLACK);
		slider.setLabelTable(slider.createStandardLabels(10));
		slider.setPaintLabels(true);
		timer = new Timer((int) (1010 / (slider.getValue() + 1)), this);
		lspeed = new JLabel("1 gen/sec");

		griglia = new JPanel(new GridLayout(RIGHE, COLONNE, 1, 1));
		griglia.setBackground(Color.LIGHT_GRAY);

		cellule = new Cellula[RIGHE][COLONNE];
		for(int r = 0; r < RIGHE; r++){
			for(int c = 0; c < COLONNE; c++){
				cellule[r][c] = new Cellula(r, c, this);
				griglia.add(cellule[r][c]);
			}
		}

		GridBagConstraints limiti1 = new GridBagConstraints();
		limiti1.gridx = 0;
		limiti1.gridy = 0;
		limiti1.insets = new Insets(5, 5, 5, 5);
		limiti1.gridwidth = 8;
		limiti1.fill = GridBagConstraints.VERTICAL;
		pane.add(griglia, limiti1);

		GridBagConstraints limiti2 = new GridBagConstraints();
		limiti2.gridx = 0;
		limiti2.gridy = 1;
		limiti2.insets = new Insets(5, 10, 5, 5);
		limiti2.gridwidth = 1;
		pane.add(lthreads, limiti2);

		GridBagConstraints limiti3 = new GridBagConstraints();
		limiti3.gridx = 1;
		limiti3.gridy = 1;
		limiti3.insets = new Insets(5, 0, 5, 5);
		limiti3.anchor = GridBagConstraints.WEST;
		limiti3.gridwidth = 1;
		limiti3.weightx = 0.01;
		pane.add(tthreads, limiti3);

		GridBagConstraints limiti4 = new GridBagConstraints();
		limiti4.gridx = 2;
		limiti4.gridy = 1;
		limiti4.insets = new Insets(0, 0, 0, 80);
		limiti4.anchor = GridBagConstraints.WEST;
		limiti4.gridwidth = 1;
		limiti4.weightx = 0.01;
		pane.add(bcambia, limiti4);

		GridBagConstraints limiti5 = new GridBagConstraints();
		limiti5.gridx = 3;
		limiti5.gridy = 1;
		limiti5.insets = new Insets(5, 5, 5, 5);
		limiti5.gridwidth = 1;
		limiti5.weightx = 0.03;
		limiti5.fill = GridBagConstraints.HORIZONTAL;
		pane.add(bGo, limiti5);

		GridBagConstraints limiti6 = new GridBagConstraints();
		limiti6.gridx = 4;
		limiti6.gridy = 1;
		limiti6.insets = new Insets(5, 5, 5, 5);
		limiti6.gridwidth = 1;
		limiti6.weightx = 0.03;
		limiti6.fill = GridBagConstraints.HORIZONTAL;
		pane.add(bPause, limiti6);

		GridBagConstraints limiti7 = new GridBagConstraints();
		limiti7.gridx = 5;
		limiti7.gridy = 1;
		limiti7.insets = new Insets(5, 5, 5, 5);
		limiti7.gridwidth = 1;
		limiti7.weightx = 0.03;
		limiti7.fill = GridBagConstraints.HORIZONTAL;
		pane.add(bReset, limiti7);

		GridBagConstraints limiti8 = new GridBagConstraints();
		limiti8.gridx = 6;
		limiti8.gridy = 1;
		limiti8.insets = new Insets(5, 5, 5, 5);
		limiti8.gridwidth = 1;
		limiti8.weightx = 0.03;
		limiti8.fill = GridBagConstraints.HORIZONTAL;
		pane.add(bKill, limiti8);

		GridBagConstraints limiti9 = new GridBagConstraints();
		limiti9.gridx = 7;
		limiti9.gridy = 1;
		limiti9.gridwidth = 1;
		limiti9.weightx = 0.3;
		pane.add(lgenerazioni, limiti9);

		GridBagConstraints limiti10 = new GridBagConstraints();
		limiti10.gridx = 0;
		limiti10.gridy = 2;
		limiti10.gridwidth = 7;
		limiti10.fill = GridBagConstraints.HORIZONTAL;
		pane.add(slider, limiti10);

		GridBagConstraints limiti11 = new GridBagConstraints();
		limiti11.gridx = 7;
		limiti11.gridy = 2;
		limiti11.gridwidth = 1;
		pane.add(lspeed, limiti11);

		tthreads.addActionListener(this);
		bcambia.addActionListener(this);
		bGo.addActionListener(this);
		bPause.addActionListener(this);
		bReset.addActionListener(this);
		bKill.addActionListener(this);
		slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				int velocita = slider.getValue();
				timer.setDelay(1010 / (velocita + 1));
				lspeed.setText(Math.max(1,(int)(1000 / (1010f / (velocita + 1)))) + " gen/sec");
			}
		});
	}

	/**
	 * Organizza gli elementi nel frame relativo ai widget selezionabili.
	 */
	public void disponiWidgets(){
		info = new JTextArea("\nNOTA: seleziona un widget e poi clicca sulla cella che deve\n"
						+ "corrispondere all'angolo in alto a sinistra della figura.\n");
		info.setBackground(Color.LIGHT_GRAY);

		stillLifes = new JPanel(new GridBagLayout());
		stillLifes.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloStillLifes = new TitledBorder(new LineBorder(Color.BLACK, 3), "Still Lifes");
		stillLifes.setBorder(titoloStillLifes);

		GridBagConstraints limitiz = new GridBagConstraints();
		limitiz.gridx = 0;
		limitiz.gridy = 0;
		limitiz.gridwidth = 3;
		widgets.add(info, limitiz);

		GridBagConstraints limitia1 = new GridBagConstraints();
		limitia1.gridx = 0;
		limitia1.gridy = 0;
		stillLifes.add(new Label("Block"), limitia1);

		block = new JButton(new ImageIcon(getClass().getResource("images/1_block.png")));
		block.setPreferredSize(new Dimension(block.getIcon().getIconWidth(), block.getIcon().getIconHeight()));
		GridBagConstraints limitib1 = new GridBagConstraints();
		limitib1.gridx = 0;
		limitib1.gridy = 1;
		stillLifes.add(block, limitib1);

		GridBagConstraints limitic1 = new GridBagConstraints();
		limitic1.gridx = 0;
		limitic1.gridy = 2;
		stillLifes.add(new Label("Beehive"), limitic1);

		beehive = new JButton(new ImageIcon(getClass().getResource("images/1_beehive.png")));
		beehive.setPreferredSize(new Dimension(beehive.getIcon().getIconWidth(), beehive.getIcon().getIconHeight()));
		GridBagConstraints limitid1 = new GridBagConstraints();
		limitid1.gridx = 0;
		limitid1.gridy = 3;
		stillLifes.add(beehive, limitid1);

		GridBagConstraints limitie1 = new GridBagConstraints();
		limitie1.gridx = 0;
		limitie1.gridy = 4;
		stillLifes.add(new Label("Loaf"), limitie1);

		loaf = new JButton(new ImageIcon(getClass().getResource("images/1_loaf.png")));
		loaf.setPreferredSize(new Dimension(loaf.getIcon().getIconWidth(), loaf.getIcon().getIconHeight()));
		GridBagConstraints limitif1 = new GridBagConstraints();
		limitif1.gridx = 0;
		limitif1.gridy = 5;
		stillLifes.add(loaf, limitif1);

		GridBagConstraints limitig1 = new GridBagConstraints();
		limitig1.gridx = 0;
		limitig1.gridy = 6;
		stillLifes.add(new Label("Boat"), limitig1);

		boat = new JButton(new ImageIcon(getClass().getResource("images/1_boat.png")));
		boat.setPreferredSize(new Dimension(boat.getIcon().getIconWidth(), boat.getIcon().getIconHeight()));
		GridBagConstraints limitih1 = new GridBagConstraints();
		limitih1.gridx = 0;
		limitih1.gridy = 7;
		stillLifes.add(boat, limitih1);

		oscillators = new JPanel(new GridBagLayout());
		oscillators.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloOscillators = new TitledBorder(new LineBorder(Color.BLACK, 3), "Oscillators");
		oscillators.setBorder(titoloOscillators);

		GridBagConstraints limitia = new GridBagConstraints();
		limitia.gridx = 0;
		limitia.gridy = 0;
		oscillators.add(new Label("Blinker"), limitia);

		blinker = new JButton(new ImageIcon(getClass().getResource("images/2_blinker.gif")));
		blinker.setPreferredSize(new Dimension(blinker.getIcon().getIconWidth(), blinker.getIcon().getIconHeight()));
		GridBagConstraints limitib = new GridBagConstraints();
		limitib.gridx = 0;
		limitib.gridy = 1;
		oscillators.add(blinker, limitib);

		GridBagConstraints limitic = new GridBagConstraints();
		limitic.gridx = 0;
		limitic.gridy = 2;
		oscillators.add(new Label("Toad"), limitic);

		toad = new JButton(new ImageIcon(getClass().getResource("images/2_toad.gif")));
		toad.setPreferredSize(new Dimension(toad.getIcon().getIconWidth(), toad.getIcon().getIconHeight()));
		GridBagConstraints limitid = new GridBagConstraints();
		limitid.gridx = 0;
		limitid.gridy = 3;
		oscillators.add(toad, limitid);

		GridBagConstraints limitie = new GridBagConstraints();
		limitie.gridx = 0;
		limitie.gridy = 4;
		oscillators.add(new Label("Beacon"), limitie);

		beacon = new JButton(new ImageIcon(getClass().getResource("images/2_beacon.gif")));
		beacon.setPreferredSize(new Dimension(beacon.getIcon().getIconWidth(), beacon.getIcon().getIconHeight()));
		GridBagConstraints limitif = new GridBagConstraints();
		limitif.gridx = 0;
		limitif.gridy = 5;
		oscillators.add(beacon, limitif);

		GridBagConstraints limitig = new GridBagConstraints();
		limitig.gridx = 0;
		limitig.gridy = 6;
		oscillators.add(new Label("Pulsar"), limitig);

		pulsar = new JButton(new ImageIcon(getClass().getResource("images/2_pulsar.gif")));
		pulsar.setPreferredSize(new Dimension(pulsar.getIcon().getIconWidth(), pulsar.getIcon().getIconHeight()));
		GridBagConstraints limitih = new GridBagConstraints();
		limitih.gridx = 0;
		limitih.gridy = 7;
		oscillators.add(pulsar, limitih);

		spaceships = new JPanel(new GridBagLayout());
		spaceships.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloSpaceships = new TitledBorder(new LineBorder(Color.BLACK, 3), "Spaceships");
		spaceships.setBorder(titoloSpaceships);

		GridBagConstraints limitia3 = new GridBagConstraints();
		limitia3.gridx = 0;
		limitia3.gridy = 0;
		spaceships.add(new Label("Glider"), limitia3);

		glider = new JButton(new ImageIcon(getClass().getResource("images/3_glider.gif")));
		glider.setPreferredSize(new Dimension(glider.getIcon().getIconWidth(), glider.getIcon().getIconHeight()));
		GridBagConstraints limitib3 = new GridBagConstraints();
		limitib3.gridx = 0;
		limitib3.gridy = 1;
		spaceships.add(glider, limitib3);

		GridBagConstraints limitic3 = new GridBagConstraints();
		limitic3.gridx = 0;
		limitic3.gridy = 2;
		spaceships.add(new Label("LW Spaceship"), limitic3);

		LWSS = new JButton(new ImageIcon(getClass().getResource("images/3_LWSS.gif")));
		LWSS.setPreferredSize(new Dimension(LWSS.getIcon().getIconWidth(), LWSS.getIcon().getIconHeight()));
		GridBagConstraints limitid3 = new GridBagConstraints();
		limitid3.gridx = 0;
		limitid3.gridy = 3;
		spaceships.add(LWSS, limitid3);

		GridBagConstraints limiti0 = new GridBagConstraints();
		limiti0.gridx = 0;
		limiti0.gridy = 1;
		limiti0.anchor = GridBagConstraints.NORTH;
		limiti0.gridwidth = 1;
		widgets.add(stillLifes, limiti0);

		GridBagConstraints limiti1 = new GridBagConstraints();
		limiti1.gridx = 1;
		limiti1.gridy = 1;
		limiti1.anchor = GridBagConstraints.NORTH;
		limiti1.gridwidth = 1;
		widgets.add(oscillators, limiti1);

		GridBagConstraints limiti2 = new GridBagConstraints();
		limiti2.gridx = 2;
		limiti2.gridy = 1;
		limiti2.anchor = GridBagConstraints.NORTH;
		limiti2.gridwidth = 1;
		widgets.add(spaceships, limiti2);

		block.addActionListener(this);
		beehive.addActionListener(this);
		loaf.addActionListener(this);
		boat.addActionListener(this);
		blinker.addActionListener(this);
		toad.addActionListener(this);
		beacon.addActionListener(this);
		pulsar.addActionListener(this);
		glider.addActionListener(this);
		LWSS.addActionListener(this);
	}

	/**
	 * Definisce il comportamento del programma a seconda dell'interazione con l'utente.
	 */
	public synchronized void actionPerformed(ActionEvent e){
		int daR;
		int aR;
		int quotaR;
		
		Object evento = e.getSource();

		if(evento == tthreads){
			tthreads.setEditable(false);
			try{
				nThreads = Integer.parseInt(tthreads.getText());
				if(!(nThreads >= 1)){
					noThread();
					return;
				}
			}catch(NumberFormatException g){
				noThread();
				return;
			}
			bcambia.setText("Cambia");
			return;
		}

		if(evento == bcambia){
			if(tthreads.isEditable()){
				tthreads.setEditable(false);
				try{
					nThreads = Integer.parseInt(tthreads.getText());
					if(!(nThreads >= 1)){
						noThread();
						return;
					}
				}catch(NumberFormatException g){
					noThread();
					return;
				}
				bcambia.setText("Cambia");
			}else{
				tthreads.setEditable(true);
				bcambia.setText("Applica");
			}
			return;
		}

		if(evento == bGo){
			bGo.setEnabled(false);
			bPause.setEnabled(true);
			avviato = true;
			timer.start();
			return;
		}

		if(evento == bPause){
			bPause.setEnabled(false);
			bGo.setEnabled(true);
			avviato = false;
			timer.stop();
			return;
		}

		if(evento == bReset){
			bPause.setEnabled(false);
			bGo.setEnabled(true);
			bKill.setEnabled(true);
			tthreads.setEditable(false);
			tthreads.setText("1");
			nThreads = 1;
			avviato = false;
			resetWidgets();
			slider.setValue(1);
			timer.stop();
			for(int r = 0; r < RIGHE; r++){
				for(int c = 0; c < COLONNE; c++){
					cellule[r][c].pulisci();
				}
			}
			generazioni = 0;
			lgenerazioni.setText("Generazioni: 0");
			return;
		}

		if(evento == bKill){
			resetWidgets();
			bKill.setEnabled(false);
			return;
		}

		if(evento == block){
			resetWidgets();
			block.setEnabled(false);
			return;
		}

		if(evento == beehive){
			resetWidgets();
			beehive.setEnabled(false);
			return;
		}

		if(evento == loaf){
			resetWidgets();
			loaf.setEnabled(false);
			return;
		}

		if(evento == boat){
			resetWidgets();
			boat.setEnabled(false);
			return;
		}

		if(evento == blinker){
			resetWidgets();
			blinker.setEnabled(false);
			return;
		}

		if(evento == toad){
			resetWidgets();
			toad.setEnabled(false);
			return;
		}

		if(evento == beacon){
			resetWidgets();
			beacon.setEnabled(false);
			return;
		}

		if(evento == pulsar){
			resetWidgets();
			pulsar.setEnabled(false);
			return;
		}

		if(evento == glider){
			resetWidgets();
			glider.setEnabled(false);
			return;
		}

		if(evento == LWSS){
			resetWidgets();
			LWSS.setEnabled(false);
			return;
		}

		if(!avviato)
			return;

		++generazioni;
		lgenerazioni.setText("Generazioni: " + generazioni);

		quotaR = (int)Math.floor((double)RIGHE / nThreads);

		daR = 0;
		aR = quotaR;
		if(nThreads<=0)
			nThreads=1;
		Slave[] threadsCheck = new Slave[nThreads];
		for(int pos = 0; pos < nThreads; pos++){
			if (pos == nThreads-1)
				aR = RIGHE;
			(threadsCheck[pos] = new Slave(daR, aR, true)).start();
			daR = aR;
			aR += quotaR;
		}
		waitThreads(threadsCheck);

		daR = 0;
		aR = quotaR;
		Slave[] threadsAggiorna = new Slave[nThreads];
		for(int pos = 0; pos < nThreads; pos++){
			if (pos == nThreads-1)
				aR = RIGHE;
			(threadsAggiorna[pos] = new Slave(daR, aR, false)).start();
			daR = aR;
			aR += quotaR;
		}
		waitThreads(threadsAggiorna);

	}

	/**
	 * Riporta tutti i widget di selezione allo stato di "non selezionato".
	 */
	private void resetWidgets(){
		block.setEnabled(true);
		beehive.setEnabled(true);
		loaf.setEnabled(true);
		boat.setEnabled(true);
		blinker.setEnabled(true);
		toad.setEnabled(true);
		beacon.setEnabled(true);
		pulsar.setEnabled(true);
		glider.setEnabled(true);
		LWSS.setEnabled(true);
	}
	
	/**
	 * Informa di input invalido nel campo relativo al numero di thread da usare.
	 */
	private void noThread(){
		JOptionPane.showMessageDialog(null,"Inserisci almeno 1 come numero di "
				+ "thread da usare per il passaggio tra generazioni.");
		tthreads.setEditable(true);
		bcambia.setText("Applica");
	}

	/**
	 * Aspetta che tutti i thread creati abbiano terminato l'esecuzione.
	 * @param slaves sono i thread che sono stati creati per il passaggio di una generazione
	 */
	private void waitThreads(Slave[] slaves){
		for(Slave slave : slaves)
			try{
				slave.join();
			}catch (InterruptedException e){
			}
	}

	/**
	 * Definisce il comportamento dei thread creati per effettuare le elaborazioni richieste
	 * dal passaggio di una generazione. Il lavoro e' diviso in base alle righe: ad ogni thread
	 * e' affidata l'elaborazione della nuova generazione per un certo gruppo di righe. 
	 */
	private class Slave extends Thread{

		private final int daR;
		private final int aR;
		private final boolean op;

		/**
		 * 
		 * @param daR e' la riga dalla quale il thread e' responsabile
		 * @param aR e' la riga dalla quale il thread non e' piu' responsabile
		 * @param op e' l'operazione da svolgere: true corrisponde a checkStato(),
		 * false a aggiornaStato()
		 */
		private Slave(int daR, int aR, boolean op){
			this.daR = daR;
			this.aR = aR;
			this.op = op;
		}

		/**
		 * Definisce nel dettaglio cosa deve fare il thread.
		 */
		@Override
		public void run(){
			if(op){
				for(int r = daR; r < aR; r++){
					for(int c = 0; c < COLONNE; c++){
						cellule[r][c].checkStato();
					}
				}
			}else{
				for(int r = daR; r < aR; r++){
					for(int c = 0; c < COLONNE; c++){
						cellule[r][c].aggiornaStato();
					}
				}
			}
		}
	}


}

