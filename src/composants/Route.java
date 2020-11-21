package composants;

public class Route {
	
	private Ville A;
	private Ville B;
	private Couleur couleur;
	private int longueur;
	private int locomotives;
	private boolean tunnel;
	private int score;

	public Route(Ville A, Ville B, Couleur couleur, int longueur, int locomotives, boolean tunnel) {
		this.A = A;
		this.B = B;
		this.couleur = couleur;
		this.longueur = longueur;
		this.locomotives = locomotives;
		this.tunnel = tunnel;
		
		calculScore();
	}
	
	public void calculScore() {
		
		switch (this.longueur) {
		case 1 :
			this.score = 1;
			break;
		case 2 :
			this.score = 2;
			break;
		case 3 :
			this.score = 4;
			break;
		case 4 :
			this.score = 7;
			break;
		case 6 :
			this.score = 15;
			break;
		case 8 :
			this.score = 21;
			break;
		}
	}

	public Ville getA() {
		return A;
	}

	public Ville getB() {
		return B;
	}

	public Couleur getCouleur() {
		return couleur;
	}

	public int getLongueur() {
		return longueur;
	}

	public int getLocomotives() {
		return locomotives;
	}

	public boolean isTunnel() {
		return tunnel;
	}
	
	public int getScore() {
		return score;
	}
}
