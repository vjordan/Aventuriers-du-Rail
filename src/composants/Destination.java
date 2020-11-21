package composants;

public class Destination {
	
	private Ville A;
	private Ville B;
	private int points;
	private boolean principale;
	private boolean extension;

	public Destination(Ville A, Ville B, int points, boolean principale, boolean extension) {
		this.A = A;
		this.B = B;
		this.points	= points;
		this.principale = principale;
		this.extension = extension;
	}

	public Ville getA() {
		return A;
	}

	public Ville getB() {
		return B;
	}

	public int getPoints() {
		return points;
	}
	
	public boolean isPrincipale() {
		return principale;
	}
	
	public boolean isExtension() {
		return extension;
	}
	
	public String toString() {
		return this.A + "-" + this.B;
	}
}
