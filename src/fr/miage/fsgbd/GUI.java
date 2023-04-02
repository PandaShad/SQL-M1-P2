package fr.miage.fsgbd;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 */
public class GUI extends JFrame implements ActionListener {
    TestInteger testInt = new TestInteger();
    BTreePlus<Integer> bInt;
    private JButton buttonClean, buttonRemove, buttonLoad, buttonSave, buttonAddMany, buttonAddItem, buttonRefresh, buttonLoadFile, buttonTest, buttonNextNode;
    private JTextField txtNbreItem, txtNbreSpecificItem, txtU, txtFile, removeSpecific, txtNextNode, txtMinMap, txtMaxMap, txtAvgMap, txtMinCSV, txtMaxCSV, txtAvgCSV;
    private final JTree tree = new JTree();

    public GUI() {
        super();
        build();
    }

    private void loadFile() throws IOException {
        int rowNumber = 0;
        BufferedReader br = new BufferedReader(new FileReader("./src/fr/miage/fsgbd/test.csv"));
        // to consume first line of csv with headers
        String headerLine = br.readLine();
        try {
            String line;
            while((line = br.readLine()) != null) {
                rowNumber ++;
                String[] value = line.split(",");
                bInt.addValueFromFile(rowNumber, (Integer.parseInt(value[0])));
            }
            // display the map
            bInt.showMap();
        } catch(IOException e) {
            e.printStackTrace();
        }
        bInt.initNextValues();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonLoad || e.getSource() == buttonClean || e.getSource() == buttonSave || e.getSource() == buttonRefresh) {
            if (e.getSource() == buttonLoad) {
                BDeserializer<Integer> load = new BDeserializer<Integer>();
                bInt = load.getArbre(txtFile.getText());
                if (bInt == null)
                    System.out.println("Echec du chargement.");

            } else if (e.getSource() == buttonClean) {
                if (Integer.parseInt(txtU.getText()) < 2)
                    System.out.println("Impossible de créer un arbre dont le nombre de clés est inférieur ? 2.");
                else
                    bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);
            } else if (e.getSource() == buttonSave) {
                BSerializer<Integer> save = new BSerializer<Integer>(bInt, txtFile.getText());
            }else if (e.getSource() == buttonRefresh) {
                tree.updateUI();
            }
        } else {
            if (bInt == null)
                bInt = new BTreePlus<Integer>(Integer.parseInt(txtU.getText()), testInt);

            if (e.getSource() == buttonAddMany) {
                for (int i = 0; i < Integer.parseInt(txtNbreItem.getText()); i++) {
                    int valeur = (int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
                    boolean done = bInt.addValeur(valeur);
                    bInt.initNextValues();

					/*
					  On pourrait forcer l'ajout mais on risque alors de tomber dans une boucle infinie sans "règle" faisant sens pour en sortir

					while (!done)
					{
						valeur =(int) (Math.random() * 10 * Integer.parseInt(txtNbreItem.getText()));
						done = bInt.addValeur(valeur);
					}
					 */
                }

            } else if (e.getSource() == buttonLoadFile) {
                try {
                    this.loadFile();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getSource() == buttonAddItem) {
                if (!bInt.addValeur(Integer.parseInt(txtNbreSpecificItem.getText())))
                    System.out.println("Tentative d'ajout d'une valeur existante : " + txtNbreSpecificItem.getText());
                txtNbreSpecificItem.setText(
                        String.valueOf(
                                Integer.parseInt(txtNbreSpecificItem.getText()) + 2
                        )
                );
                bInt.resetAllNextValues();

            } else if (e.getSource() == buttonRemove) {
                bInt.removeValeur(Integer.parseInt(removeSpecific.getText()));
                bInt.resetAllNextValues();
            } else if (e.getSource() == buttonTest) {
                Map<String,Double> timeResults = new HashMap<String,Double>();
                timeResults = bInt.tests();
                txtMinMap.setText(timeResults.get("minMap").toString() +" ms");
                txtMaxMap.setText(timeResults.get("maxMap").toString() +" ms");
                txtAvgMap.setText(timeResults.get("avgMap").toString() +" ms");
                txtMinCSV.setText(timeResults.get("minCSV").toString() +" ms");
                txtMaxCSV.setText(timeResults.get("maxCSV").toString() +" ms");
                txtAvgCSV.setText(timeResults.get("avgCSV").toString() +" ms");
            } else if (e.getSource() == buttonNextNode) {
                this.txtNextNode.setText(bInt.getNextNodeKeys());
            }
        }

        tree.setModel(new DefaultTreeModel(bInt.bArbreToJTree()));
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        tree.updateUI();
    }

    private void build() {
        setTitle("Indexation - B Arbre");
        setSize(760, 760);
        setLocationRelativeTo(this);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildContentPane());
    }

    private JPanel buildContentPane() {
        GridBagLayout gLayGlob = new GridBagLayout();

        JPanel pane1 = new JPanel();
        pane1.setLayout(gLayGlob);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 2, 0);

        JLabel labelU = new JLabel("Nombre max de clés par noeud (2m): ");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        pane1.add(labelU, c);

        txtU = new JTextField("4", 7);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 2;
        pane1.add(txtU, c);

        JLabel labelBetween = new JLabel("Nombre de clefs à ajouter:");
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(labelBetween, c);

        txtNbreItem = new JTextField("10000", 7);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        pane1.add(txtNbreItem, c);


        buttonAddMany = new JButton("Ajouter n éléments aléatoires à l'arbre");
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddMany, c);

        buttonNextNode = new JButton("Next node");
        c.gridx = 0;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonNextNode);

        JLabel labelNextSheet = new JLabel("Valeur de la feuille actuelle : ");
        c.gridx = 0;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(labelNextSheet, c);

        txtNextNode = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 10;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(txtNextNode, c);

        JLabel labelSpecific = new JLabel("Ajouter une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelSpecific, c);

        txtNbreSpecificItem = new JTextField("50", 7);
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtNbreSpecificItem, c);

        buttonAddItem = new JButton("Ajouter l'élément");
        c.gridx = 2;
        c.gridy = 3;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonAddItem, c);

        JLabel labelRemoveSpecific = new JLabel("Retirer une valeur spécifique:");
        c.gridx = 0;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelRemoveSpecific, c);

        removeSpecific = new JTextField("54", 7);
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(removeSpecific, c);

        buttonRemove = new JButton("Supprimer l'élément n de l'arbre");
        c.gridx = 2;
        c.gridy = 4;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRemove, c);

        JLabel labelFilename = new JLabel("Nom de fichier : ");
        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelFilename, c);

        txtFile = new JTextField("arbre.abr", 7);
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtFile, c);

        buttonSave = new JButton("Sauver l'arbre");
        c.gridx = 2;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonSave, c);

        buttonLoad = new JButton("Charger l'arbre");
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.5;
        c.gridwidth = 1;
        pane1.add(buttonLoad, c);

        buttonClean = new JButton("Reset");
        c.gridx = 2;
        c.gridy = 6;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonClean, c);

        buttonRefresh = new JButton("Refresh");
        c.gridx = 2;
        c.gridy = 7;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(buttonRefresh, c);

        buttonLoadFile = new JButton("Load CSV file");
        c.gridx = 2;
        c.gridy = 8;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(buttonLoadFile);

        buttonTest = new JButton("Tests de vitesse de recherche via Map ou via recherche dans CSV");
        c.gridx = 1;
        c.gridy = 11;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(buttonTest, c);

        JLabel labelMinMap = new JLabel("Valeur min Map : ");
        c.gridx = 0;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(labelMinMap, c);

        txtMinMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMinMap, c);

        JLabel labelMinCSV = new JLabel("Valeur min CSV : ");
        c.gridx = 2;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 2;
        pane1.add(labelMinCSV, c);

        txtMinCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 12;
        c.weightx = 1;
        c.gridwidth = 3;
        pane1.add(txtMinCSV, c);

        JLabel labelMaxMap = new JLabel("Valeur max Map : ");
        c.gridx = 0;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelMaxMap, c);

        txtMaxMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMaxMap, c);

        JLabel labelMaxCSV = new JLabel("Valeur max CSV : ");
        c.gridx = 2;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelMaxCSV, c);

        txtMaxCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 13;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtMaxCSV, c);

        JLabel labelAvgMap = new JLabel("Valeur moyenne Map : ");
        c.gridx = 0;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelAvgMap, c);

        txtAvgMap = new JTextField("", 7);
        c.gridx = 1;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtAvgMap, c);

        JLabel labelAvgCSV = new JLabel("Valeur moyenne CSV : ");
        c.gridx = 2;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(labelAvgCSV, c);

        txtAvgCSV = new JTextField("", 7);
        c.gridx = 3;
        c.gridy = 14;
        c.weightx = 1;
        c.gridwidth = 1;
        pane1.add(txtAvgCSV, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 400;       //reset to default
        c.weighty = 1.0;   //request any extra vertical space
        c.gridwidth = 4;   //2 columns wide
        c.gridx = 0;
        c.gridy = 8;

        JScrollPane scrollPane = new JScrollPane(tree);
        pane1.add(scrollPane, c);

        tree.setModel(new DefaultTreeModel(null));
        tree.updateUI();

        txtNbreItem.addActionListener(this);
        buttonAddItem.addActionListener(this);
        buttonAddMany.addActionListener(this);
        buttonLoad.addActionListener(this);
        buttonSave.addActionListener(this);
        buttonRemove.addActionListener(this);
        buttonClean.addActionListener(this);
        buttonRefresh.addActionListener(this);
        buttonLoadFile.addActionListener(this);
        buttonNextNode.addActionListener(this);
        buttonTest.addActionListener(this);

        return pane1;
    }
}

