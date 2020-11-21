package utile;

import java.util.ArrayList;

public class Chemin {

	private ArrayList<Voisin> voisins;
	private int longueur;
	private int score;
	private int nombreRoutes;
	
	public Chemin(ArrayList<Voisin> voisins) {
		this.voisins = voisins;
		
		calculLongueurEtScore();
		calculNombreRoutes();
	}
	
	public void calculLongueurEtScore() {
		for (Voisin voisin : this.voisins) {
			this.longueur += voisin.getDistance();
			this.score += voisin.getScoreRoute();
		}
	}
	
	public void calculNombreRoutes() {
		this.nombreRoutes = this.voisins.size()-1;
	}

	public ArrayList<Voisin> getVoisins() {
		return voisins;
	}

	public int getLongueur() {
		return longueur;
	}

	public int getScore() {
		return score;
	}
	
	public int getNombreRoutes() {
		return nombreRoutes;
	}
	
	public String toString() {
		String cheminAffiche = "";
		
		for (Voisin voisin : this.voisins) {
			cheminAffiche += voisin.getVille() + " ";
		}
		
		return cheminAffiche;
	}
}
