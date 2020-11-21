package composants;

public enum Ville {

	MADRID (true),
	LISBOA (false),
	CADIZ (false),
	BARCELONA (false),
	PAMPLONA (false),
	MARSEILLE (false),
	BREST (false),
	DIEPPE (false),
	PARIS (true),
	EDINBURGH (false),
	LONDON (true),
	AMSTERDAM (false),
	BRUXELLES (false),
	ESSEN (false),
	ZURICH (false),
	FRANKFURT (false),
	STOCKHOLM (false),
	KOBENHAVN (false),
	BERLIN (true),
	MUNCHEN (false),
	WIEN (true),
	VENEZIA (false),
	ROMA (true),
	PALERMO (false),
	BRINDISI (false),
	SARAJEVO (false),
	ZAGRAB (false),
	DANZIG (false),
	RIGA (false),
	PETROGRAD (false),
	WARSZAWA (false),
	WILNO (false),
	SMOLENSK (false),
	MOSKVA (true),
	BUDAPEST (false),
	KYIV (false),
	KHARKOV (false),
	ROSTOV (false),
	SOCHI (false),
	SEVASTOPOL (false),
	BUCURESTI (false),
	CONSTANTINOPLE (false),
	ERZURUM (false),
	ANGORA (true),
	SMYRNA (false),
	ATHINA (true),
	SOFIA (false);
	
	private boolean bigCity;
	
	private Ville(boolean bigCity) {
		this.bigCity = bigCity;
	}
	
	public boolean getBigCity() {
		return bigCity;
	}
}
