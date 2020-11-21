package utile;

import composants.Ville;

public class Voisin {

	private Ville ville;
	private int distance;
	private int scoreRoute;
	
	public Voisin(Ville ville, int distance, int scoreRoute) {
		this.ville = ville;
		this.distance = distance;
		this.scoreRoute = scoreRoute;
	}

	public Ville getVille() {
		return ville;
	}

	public int getDistance() {
		return distance;
	}

	public int getScoreRoute() {
		return scoreRoute;
	}
}
