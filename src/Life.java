
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
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

public class Life implements ActionListener {

	static final Color[] COLORI = { Color.WHITE, Color.BLACK,
			new Color(180, 170, 200) };
	static final Dimension DIM_CELLA = new Dimension(10, 10); // dim cella
	static final int RIGHE = 50;
	static final int COLONNE = 75;
	private Cellula[][] cellule;
	private Timer timer; // Swing timer's task is performed in the event
							// dispatch thread, diverso da java.util.timer
	private int generazioni = 0;
	private boolean avviato = false;
	private boolean mouseOn = false;
	int nThreads = 1;
	int daR;
	int aR;
	int quotaR;
	private Point[] confinanti = { new Point(-1, -1), new Point(0, -1),
			new Point(1, -1), new Point(-1, 0), new Point(1, 0),
			new Point(-1, 1), new Point(0, 1), new Point(1, 1) };

	private Container pane;
	private JPanel griglia;
	private JTextArea lthreads;
	private JTextField tthreads;
	private JButton bcambia;
	private JButton bReset;
	private JButton bPause;
	private JButton bGo;
	private JButton bKill;
	private JLabel lgenerazioni;
	private JSlider slider;
	private JLabel lspeed;

	private Container widgets;
	private JPanel stillLifes;
	private JPanel oscillators;
	private JPanel spaceships;
	private JTextArea info;
	private JButton block;
	private JButton beehive;
	private JButton loaf;
	private JButton boat;
	private JButton blinker;
	private JButton toad;
	private JButton beacon;
	private JButton pulsar;
	private JButton glider;
	private JButton LWSS;

	public static void main(String[] arg) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Life();
			}
		});
	}

	public Life() {
		creaGUI();
	}

	public void creaGUI() {
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

	public void disponi() {
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
		int i = slider.getValue();
		timer = new Timer((int) (1010 / (i + 1)), this);
		lspeed = new JLabel("1 gen/sec");

		griglia = new JPanel(new GridLayout(RIGHE, COLONNE, 1, 1));
		griglia.setBackground(Color.LIGHT_GRAY);

		cellule = new Cellula[RIGHE][COLONNE];
		for (int r = 0; r < RIGHE; r++) {
			for (int c = 0; c < COLONNE; c++) {
				cellule[r][c] = new Cellula(r, c);
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
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int velocita = slider.getValue();
				timer.setDelay(1010 / (velocita + 1));
				lspeed.setText(Math.max(1,
						(int) (1000 / (1010f / (velocita + 1)))) + " gen/sec");
			}
		});
	}

	public void disponiWidgets() {
		info = new JTextArea(
				"\nNOTA: seleziona un widget e poi clicca sulla cella che dovrà\n"
						+ "corrispondere all'angolo in alto a sinistra della figura.\n");
		info.setBackground(Color.LIGHT_GRAY);

		stillLifes = new JPanel(new GridBagLayout());
		stillLifes.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloStillLifes = new TitledBorder(new LineBorder(
				Color.BLACK, 3), "Still Lifes");
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

		block = new JButton(new ImageIcon("images/1_block.png"));
		block.setPreferredSize(new Dimension(block.getIcon().getIconWidth(),
				block.getIcon().getIconHeight()));
		GridBagConstraints limitib1 = new GridBagConstraints();
		limitib1.gridx = 0;
		limitib1.gridy = 1;
		stillLifes.add(block, limitib1);

		GridBagConstraints limitic1 = new GridBagConstraints();
		limitic1.gridx = 0;
		limitic1.gridy = 2;
		stillLifes.add(new Label("Beehive"), limitic1);

		beehive = new JButton(new ImageIcon("images/1_beehive.png"));
		beehive.setPreferredSize(new Dimension(
				beehive.getIcon().getIconWidth(), beehive.getIcon()
						.getIconHeight()));
		GridBagConstraints limitid1 = new GridBagConstraints();
		limitid1.gridx = 0;
		limitid1.gridy = 3;
		stillLifes.add(beehive, limitid1);

		GridBagConstraints limitie1 = new GridBagConstraints();
		limitie1.gridx = 0;
		limitie1.gridy = 4;
		stillLifes.add(new Label("Loaf"), limitie1);

		loaf = new JButton(new ImageIcon("images/1_loaf.png"));
		loaf.setPreferredSize(new Dimension(loaf.getIcon().getIconWidth(), loaf
				.getIcon().getIconHeight()));
		GridBagConstraints limitif1 = new GridBagConstraints();
		limitif1.gridx = 0;
		limitif1.gridy = 5;
		stillLifes.add(loaf, limitif1);

		GridBagConstraints limitig1 = new GridBagConstraints();
		limitig1.gridx = 0;
		limitig1.gridy = 6;
		stillLifes.add(new Label("Boat"), limitig1);

		boat = new JButton(new ImageIcon("images/1_boat.png"));
		boat.setPreferredSize(new Dimension(boat.getIcon().getIconWidth(), boat
				.getIcon().getIconHeight()));
		GridBagConstraints limitih1 = new GridBagConstraints();
		limitih1.gridx = 0;
		limitih1.gridy = 7;
		stillLifes.add(boat, limitih1);

		oscillators = new JPanel(new GridBagLayout());
		oscillators.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloOscillators = new TitledBorder(new LineBorder(
				Color.BLACK, 3), "Oscillators");
		oscillators.setBorder(titoloOscillators);

		GridBagConstraints limitia = new GridBagConstraints();
		limitia.gridx = 0;
		limitia.gridy = 0;
		oscillators.add(new Label("Blinker"), limitia);

		blinker = new JButton(new ImageIcon("images/2_blinker.gif"));
		blinker.setPreferredSize(new Dimension(
				blinker.getIcon().getIconWidth(), blinker.getIcon()
						.getIconHeight()));
		GridBagConstraints limitib = new GridBagConstraints();
		limitib.gridx = 0;
		limitib.gridy = 1;
		oscillators.add(blinker, limitib);

		GridBagConstraints limitic = new GridBagConstraints();
		limitic.gridx = 0;
		limitic.gridy = 2;
		oscillators.add(new Label("Toad"), limitic);

		toad = new JButton(new ImageIcon("images/2_toad.gif"));
		toad.setPreferredSize(new Dimension(toad.getIcon().getIconWidth(), toad
				.getIcon().getIconHeight()));
		GridBagConstraints limitid = new GridBagConstraints();
		limitid.gridx = 0;
		limitid.gridy = 3;
		oscillators.add(toad, limitid);

		GridBagConstraints limitie = new GridBagConstraints();
		limitie.gridx = 0;
		limitie.gridy = 4;
		oscillators.add(new Label("Beacon"), limitie);

		beacon = new JButton(new ImageIcon("images/2_beacon.gif"));
		beacon.setPreferredSize(new Dimension(beacon.getIcon().getIconWidth(),
				beacon.getIcon().getIconHeight()));
		GridBagConstraints limitif = new GridBagConstraints();
		limitif.gridx = 0;
		limitif.gridy = 5;
		oscillators.add(beacon, limitif);

		GridBagConstraints limitig = new GridBagConstraints();
		limitig.gridx = 0;
		limitig.gridy = 6;
		oscillators.add(new Label("Pulsar"), limitig);

		pulsar = new JButton(new ImageIcon("images/2_pulsar.gif"));
		pulsar.setPreferredSize(new Dimension(pulsar.getIcon().getIconWidth(),
				pulsar.getIcon().getIconHeight()));
		GridBagConstraints limitih = new GridBagConstraints();
		limitih.gridx = 0;
		limitih.gridy = 7;
		oscillators.add(pulsar, limitih);

		spaceships = new JPanel(new GridBagLayout());
		spaceships.setBackground(Color.LIGHT_GRAY);
		TitledBorder titoloSpaceships = new TitledBorder(new LineBorder(
				Color.BLACK, 3), "Spaceships");
		spaceships.setBorder(titoloSpaceships);

		GridBagConstraints limitia3 = new GridBagConstraints();
		limitia3.gridx = 0;
		limitia3.gridy = 0;
		spaceships.add(new Label("Glider"), limitia3);

		glider = new JButton(new ImageIcon("images/3_glider.gif"));
		glider.setPreferredSize(new Dimension(glider.getIcon().getIconWidth(),
				glider.getIcon().getIconHeight()));
		GridBagConstraints limitib3 = new GridBagConstraints();
		limitib3.gridx = 0;
		limitib3.gridy = 1;
		spaceships.add(glider, limitib3);

		GridBagConstraints limitic3 = new GridBagConstraints();
		limitic3.gridx = 0;
		limitic3.gridy = 2;
		spaceships.add(new Label("LW Spaceship"), limitic3);

		LWSS = new JButton(new ImageIcon("images/3_LWSS.gif"));
		LWSS.setPreferredSize(new Dimension(LWSS.getIcon().getIconWidth(), LWSS
				.getIcon().getIconHeight()));
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

	public synchronized void actionPerformed(ActionEvent e) {
		Object evento = e.getSource();

		if (evento == tthreads) {
			tthreads.setEditable(false);
			nThreads = Integer.parseInt(tthreads.getText());
			bcambia.setText("Cambia");
		}

		if (evento == bcambia) {
			if (tthreads.isEditable()) {
				tthreads.setEditable(false);
				nThreads = Integer.parseInt(tthreads.getText());
				bcambia.setText("Cambia");
			} else {
				tthreads.setEditable(true);
				bcambia.setText("Applica");
			}
			return;
		}

		if (evento == bGo) {
			if (!(nThreads >= 1)) {
				JOptionPane
						.showMessageDialog(
								null,
								"Inserisci almeno 1 come numero di "
										+ "thread da usare per il passaggio tra generazioni.");
				tthreads.setBackground(Color.WHITE);
				tthreads.setEditable(true);
				tthreads.setText(null);
				return;
			}
			bGo.setEnabled(false);
			bPause.setEnabled(true);
			avviato = true;
			timer.start();
			return;
		}

		if (evento == bPause) {
			bPause.setEnabled(false);
			bGo.setEnabled(true);
			avviato = false;
			timer.stop();
			return;
		}

		if (evento == bReset) {
			bPause.setEnabled(false);
			bGo.setEnabled(true);
			bKill.setEnabled(true);
			tthreads.setEditable(false);
			tthreads.setText("1");
			nThreads = 1;
			avviato = false;
			resetWidgets();
			timer.stop();
			for (int r = 0; r < RIGHE; r++) {
				for (int c = 0; c < COLONNE; c++) {
					cellule[r][c].clear();
				}
			}
			generazioni = 0;
			lgenerazioni.setText("Generazioni: 0");
			return;
		}

		if (evento == bKill) {
			resetWidgets();
			bKill.setEnabled(false);
			return;
		}

		if (evento == block) {
			resetWidgets();
			block.setEnabled(false);
			return;
		}

		if (evento == beehive) {
			resetWidgets();
			beehive.setEnabled(false);
			return;
		}

		if (evento == loaf) {
			resetWidgets();
			loaf.setEnabled(false);
			return;
		}

		if (evento == boat) {
			resetWidgets();
			boat.setEnabled(false);
			return;
		}

		if (evento == blinker) {
			resetWidgets();
			blinker.setEnabled(false);
			return;
		}

		if (evento == toad) {
			resetWidgets();
			toad.setEnabled(false);
			return;
		}

		if (evento == beacon) {
			resetWidgets();
			beacon.setEnabled(false);
			return;
		}

		if (evento == pulsar) {
			resetWidgets();
			pulsar.setEnabled(false);
			return;
		}

		if (evento == glider) {
			resetWidgets();
			glider.setEnabled(false);
			return;
		}

		if (evento == LWSS) {
			resetWidgets();
			LWSS.setEnabled(false);
			return;
		}

		if (!avviato)
			return;

		++generazioni;
		lgenerazioni.setText("Generazioni: " + generazioni);

		quotaR = (int) Math.ceil(RIGHE / nThreads);

		daR = 0;
		aR = quotaR;
		Slave[] threadsCheck = new Slave[nThreads];
		for (int pos = 0; pos < nThreads && daR < RIGHE; pos++) { // basta
																	// pos<nThreads?
			(threadsCheck[pos] = new Slave(daR, Math.min(aR, RIGHE), true))
					.start();// checkStato
			daR = aR;
			aR += quotaR;
		}
		waitThreads(threadsCheck);

		daR = 0;
		aR = quotaR;
		Slave[] threadsAggiorna = new Slave[nThreads];
		for (int pos = 0; pos < nThreads && daR < RIGHE; pos++) { // basta
																	// pos<nThreads?
			(threadsAggiorna[pos] = new Slave(daR, Math.min(aR, RIGHE), false))
					.start();// aggiornaStato
			daR = aR;
			aR += quotaR;
		}
		waitThreads(threadsAggiorna);

	}

	public void resetWidgets() {
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

	public void waitThreads(Slave[] slaves) {
		for (Slave slave : slaves)
			try {
				slave.join();
			} catch (InterruptedException e) {
			}
	}

	public class Slave extends Thread {

		private final int daR;
		private final int aR;
		private final boolean op; // true check, false update

		private Slave(int daR, int aR, boolean op) {
			this.daR = daR;
			this.aR = aR;
			this.op = op;
		}

		@Override
		public void run() {
			if (op) {
				for (int r = daR; r < aR; r++) {
					for (int c = 0; c < COLONNE; c++) {
						cellule[r][c].checkStato();
					}
				}
			} else {
				for (int r = daR; r < aR; r++) {
					for (int c = 0; c < COLONNE; c++) {
						cellule[r][c].aggiornaStato();
					}
				}
			}
		}
	}

	public class Cellula extends JLabel implements MouseListener {
		int riga;
		int colonna;
		private int statoAttuale; // 0 morta, 1 viva, 2 uccisa
		private int statoFuturo;

		Cellula(int riga, int colonna) {
			this.riga = riga;
			this.colonna = colonna;
			statoAttuale = 0;
			statoFuturo = 0;
			setOpaque(true);
			setBackground(COLORI[0]);
			addMouseListener(this);
			this.setPreferredSize(DIM_CELLA);
		}

		void checkStato() {
			int nViciniVivi = 0;
			for (int i = 0; i < 8; i++) {
				// sommo RIGHE (e COLONNE) per gestire i -1
				int statoVicino = cellule[(riga + confinanti[i].x + RIGHE)
						% RIGHE][(colonna + confinanti[i].y + COLONNE)
						% COLONNE].statoAttuale;
				if (statoVicino == 2)
					continue;
				nViciniVivi += statoVicino;
			}
			if (statoAttuale == 1) {
				if (nViciniVivi < 2)
					statoFuturo = 0;
				if (nViciniVivi > 3)
					statoFuturo = 0;
			} else if (statoAttuale == 0 && nViciniVivi == 3)
				statoFuturo = 1;
		}

		void aggiornaStato() {
			if (statoAttuale != statoFuturo) {
				statoAttuale = statoFuturo;
				setBackground(COLORI[statoAttuale]);
			}
		}

		void clear() {
			if (statoAttuale != 0 || statoFuturo != 0) {
				statoAttuale = 0;
				statoFuturo = 0;
				setBackground(COLORI[statoAttuale]);
			}
		}

		@Override
		public void mouseClicked(MouseEvent arg0) { // premuto e rilasciato
		}

		@Override
		public void mousePressed(MouseEvent arg0) { // premuto
			if (!bKill.isEnabled()) {
				statoAttuale = 2;
				statoFuturo = 2;
				setBackground(COLORI[2]);
				bKill.setEnabled(true);

			} else if (!block.isEnabled()) { 
				bianche(riga, colonna, 4, 4);
				nere(riga, colonna, new int[] { 11, 12, 21, 22 });
				block.setEnabled(true);

			} else if (!beehive.isEnabled()) {
				bianche(riga, colonna, 5, 6);
				nere(riga, colonna, new int[] { 12, 13, 21, 24, 32, 33 });
				beehive.setEnabled(true);

			} else if (!loaf.isEnabled()) {
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[] { 12, 13, 21, 24, 32, 34, 43 });
				loaf.setEnabled(true);

			} else if (!boat.isEnabled()) {
				bianche(riga, colonna, 5, 5);
				nere(riga, colonna, new int[] { 11, 12, 21, 23, 32 });
				boat.setEnabled(true);

			} else if (!blinker.isEnabled()) {
				bianche(riga, colonna, 5, 5);
				nere(riga, colonna, new int[] { 21, 22, 23 });
				blinker.setEnabled(true);

			} else if (!toad.isEnabled()) {
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[] { 22, 23, 24, 31, 32, 33 });
				toad.setEnabled(true);

			} else if (!beacon.isEnabled()) {
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[] { 11, 12, 21, 34, 43, 44 });
				beacon.setEnabled(true);

			} else if (!pulsar.isEnabled()) {
				bianche(riga, colonna, 9, 9);
				bianche(riga, colonna + 9, 9, 8);
				bianche(riga + 9, colonna, 8, 9);
				bianche(riga + 9, colonna + 9, 8, 8);
				nere(riga, colonna, new int[] { 15, 25, 35, 36, 51, 52, 53, 56,
						57, 63, 65, 67, 75, 76 });
				nere(riga, colonna + 9, new int[] { 12, 22, 31, 32, 50, 51, 54,
						55, 56, 60, 62, 64, 71, 72 });
				nere(riga + 9, colonna, new int[] { 5, 6, 13, 15, 17, 21, 22,
						23, 26, 27, 45, 46, 55, 65 });
				nere(riga + 9, colonna + 9, new int[] { 1, 2, 10, 12, 14, 20,
						21, 24, 25, 26, 41, 42, 52, 62 });
				pulsar.setEnabled(true);

			} else if (!glider.isEnabled()) {
				bianche(riga, colonna, 6, 6);
				nere(riga, colonna, new int[] { 12, 23, 31, 32, 33 });
				glider.setEnabled(true);

			} else if (!LWSS.isEnabled()) {
				bianche(riga, colonna, 7, 9);
				nere(riga, colonna, new int[] { 12, 15, 26, 32, 36, 43, 44, 45,
						46 });
				LWSS.setEnabled(true);

			} else {
				mouseOn = true;
				if (statoAttuale != 2) {
					statoAttuale = 1;
					statoFuturo = 1;
					setBackground(COLORI[1]);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			mouseOn = false;
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			setBorder(BorderFactory.createLineBorder(Color.BLUE));
			if (mouseOn) {
				statoAttuale = 1;
				statoFuturo = 1;
				if (statoAttuale != 2)
					setBackground(COLORI[1]);
			}
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			setBorder(BorderFactory.createEmptyBorder());
		}
	}

	public void bianche(int riga, int colonna, int altezza, int larghezza) {
		for (int r = riga; r < riga + altezza; r++)
			for (int c = colonna; c < colonna + larghezza; c++) {
				cellule[r % RIGHE][c % COLONNE].statoAttuale = 0;
				cellule[r % RIGHE][c % COLONNE].statoFuturo = 0;
				cellule[r % RIGHE][c % COLONNE].setBackground(COLORI[0]);
			}
	}

	public void nere(int riga, int colonna, int[] posizioni) {
		int ri;
		int co;
		for (int numero : posizioni) {
			ri = numero / 10;
			co = numero % 10;
			cellule[(riga + ri) % RIGHE][(colonna + co) % COLONNE].statoAttuale = 1;
			cellule[(riga + ri) % RIGHE][(colonna + co) % COLONNE].statoFuturo = 1;
			cellule[(riga + ri) % RIGHE][(colonna + co) % COLONNE]
					.setBackground(COLORI[1]);
		}
	}

	public class Point {
		int x; // riga
		int y; // colonna

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}

