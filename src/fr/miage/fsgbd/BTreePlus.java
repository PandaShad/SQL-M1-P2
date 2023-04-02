package fr.miage.fsgbd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;

    // Map contenant l'index et la valeur de la ligne associée
    private Map<Integer, Integer> mapData = new HashMap<Integer, Integer>();

    private Noeud<Type> currentNode;

    public ArrayList<Noeud<Type>> nodeList = new ArrayList<Noeud<Type>>();

    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    public void initNextValues() {
        Noeud<Type> node = getFirstLeaf(racine);
        this.currentNode = node;
        nodeList.add(node);
        while (node != null) {
            Noeud<Type> next = checkNext(node);
            if (next != null && next.fils.isEmpty()) {
                nodeList.get(nodeList.size()-1).setNextNode(next);
                nodeList.add(next);
            }
            node = next;
        }
    }

    public Noeud<Type> checkNext(Noeud<Type> n) {
        while (n != null && n.getNoeudSuivant() == null) {
            n = n.getParent();
        }
        return n != null ? getFirstLeaf(n.getNoeudSuivant()) : null;
    }

    
    //Fonction permettant d'aller récupérer la 1er feuille de l'arbre
    public Noeud<Type> getFirstLeaf(Noeud<Type> n) {
        while (!n.fils.isEmpty()) {
            n = n.fils.get(0);
        }
        return n;
    }

    public void resetAllNextValues(){
        nodeList.clear();
        initNextValues();
    }

    public String getNextNodeKeys() {
        String keys = "";
        int length = currentNode.keys.size();
        for(int i = 0; i<length; i++) {
            keys += currentNode.keys.get(i)+ " ";
        }
        this.currentNode = currentNode.getNextNode();
        return keys;
    }

    /**
     * Méthode récursive permettant de récupérer tous les noeuds
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode bArbreToJTree() {
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder txt = new StringBuilder();
        for (Type key : root.keys)
            txt.append(key.toString()).append(" ");

        DefaultMutableTreeNode racine2 = new DefaultMutableTreeNode(txt.toString(), true);
        for (Noeud<Type> fil : root.fils)
            racine2.add(bArbreToJTree(fil));

        return racine2;
    }


    public boolean addValeur(Type valeur) {
        System.out.println("Ajout de la valeur : " + valeur.toString());
        if (racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public void removeValeur(Type valeur) {
        System.out.println("Retrait de la valeur : " + valeur.toString());
        if (racine.contient(valeur) != null) {
            Noeud<Type> newRacine = racine.removeValeur(valeur, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }

    public boolean addValueFromFile(int rowNumber, Type valeur) {
        System.out.println(String.format("Ajout de la valeur : %s", valeur.toString()));
        System.out.println(String.format("Avec pour pointeur la ligne : %d", rowNumber));
        // beurk
        mapData.put(Integer.parseInt(valeur.toString()), rowNumber);
        if(racine.contient(valeur) == null) {
            Noeud<Type> newRacine = racine.addValeur(valeur);
            if(racine != newRacine){
                racine = newRacine;
            }
            return true;
        }
        return false;
    }

    public void showMap() {
        Set<Entry<Integer, Integer>> entries = mapData.entrySet();
        for(Entry<Integer, Integer> entry : entries) {
            System.out.println("Clef : " + entry.getKey() + " - Valeur : " + entry.getValue());
        }
    }

    public double searchInFile(Integer wantedValue, String csvPath) {
        long startTime = System.nanoTime();
        String row;
        int numRow = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvPath));
            String headerLine = br.readLine();
            while ((row = br.readLine()) != null) {
                numRow++;
                String[] data = row.split(",");
                if (wantedValue == Integer.parseInt(data[0])) {
                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime);
                    System.out.println("searchInFile found : " + numRow);
                    System.out.println("searchInFile duration : " + duration);
                    br.close();
                    return duration;
                }
            }
            br.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            return 0;
        }
        return 0;
    }

    public double searchInMap(Integer wantedValue) {
        long startTime = System.nanoTime();
        Integer foundValue = mapData.get(wantedValue);
        try (Stream<String> lines = Files.lines(Path.of("./src/fr/miage/fsgbd/test.csv"))) {
            lines.skip(foundValue-1).findFirst().get();
        }
        catch(IOException e1) {
            e1.printStackTrace();
            return 0;
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("searchInMap found : " + foundValue);
        System.out.println("searchInMap duration : " + duration);
        return duration;
    }

    public Map<String, Double> tests() {
        ArrayList<Integer> listeRecherches = mockup();
        Map<String, Double> timeResults = new HashMap<>();
    
        double totalFile = 0;
        double totalMap = 0;
        double maxCSvValueViaFile = Double.NEGATIVE_INFINITY;
        double maxCSvValueViaMap = Double.NEGATIVE_INFINITY;
        double minCSvValueViaFile = Double.POSITIVE_INFINITY;
        double minCSvValueViaMap = Double.POSITIVE_INFINITY;
    
        for (int i = 0; i < listeRecherches.size(); i++) {
            double tempDurationFile = searchInFile(listeRecherches.get(i), "./src/fr/miage/fsgbd/test.csv");
            double tempDurationMap = searchInMap(listeRecherches.get(i));
    
            maxCSvValueViaFile = Math.max(maxCSvValueViaFile, tempDurationFile);
            maxCSvValueViaMap = Math.max(maxCSvValueViaMap, tempDurationMap);
            minCSvValueViaFile = Math.min(minCSvValueViaFile, tempDurationFile);
            minCSvValueViaMap = Math.min(minCSvValueViaMap, tempDurationMap);
    
            totalMap += tempDurationMap;
            totalFile += tempDurationFile;
        }
    
        double moyenneCsvValueViaFile = totalFile / listeRecherches.size();
        double moyenneCsvValueViaMap = totalMap / listeRecherches.size();
    
        timeResults.put("minMap", minCSvValueViaMap);
        timeResults.put("maxMap", maxCSvValueViaMap);
        timeResults.put("avgMap", moyenneCsvValueViaMap);
        timeResults.put("minCSV", minCSvValueViaFile);
        timeResults.put("maxCSV", maxCSvValueViaFile);
        timeResults.put("avgCSV", moyenneCsvValueViaFile);
    
        return timeResults;
    }

    public ArrayList<Integer> mockup() {
        ArrayList<Integer> res = new ArrayList<>();
        Random rand = new Random();
        ArrayList<Integer> list = new ArrayList<>(mapData.keySet()); 
        for (int i = 0; i < 100; i++) {
            res.add(list.get(rand.nextInt(list.size())));
        }
        return res;
    }
}
