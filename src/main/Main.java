package main;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import composants.Couleur;
import composants.Destination;
import composants.Route;
import composants.Ville;
import utile.Chemin;
import utile.Voisin;

public class Main {

	public static void main(String[] args) {
		
		ArrayList<Route> routes = ajoutRoutes();
		ArrayList<Destination> destinations = ajoutDestinations();
		HashMap<Ville, ArrayList<Voisin>> voisins = ajoutVoisins(routes);
		
		calculRendementCouleurs(routes);
		calculRendementDestinations(destinations, voisins);
		calculFrequenceVilles(destinations, voisins);
	}
	
	public static HashMap<Ville, ArrayList<Voisin>> ajoutVoisins(ArrayList<Route> routes) {
		
		HashMap<Ville, ArrayList<Voisin>> voisins = new HashMap<Ville, ArrayList<Voisin>>();
		
		for (Ville ville : Ville.values()) {
			voisins.put(ville, new ArrayList<Voisin>());
		}
		
		for (Route route : routes) {
			Ville A = route.getA();
			Ville B = route.getB();
			int longueur = route.getLongueur();
			int score = route.getScore();
			ArrayList<Voisin> voisinsA = voisins.get(A);
			ArrayList<Voisin> voisinsB = voisins.get(B);
			
			boolean voisinDejaRepere = false;
			for (Voisin voisin : voisinsA) {
				if (voisin.getVille() == B) {
					voisinDejaRepere = true;
					break;
				}
			}
			
			if (!voisinDejaRepere) {
				voisinsA.add(new Voisin(B, longueur, score));
				voisinsB.add(new Voisin(A, longueur, score));
				voisins.replace(A, voisinsA);
				voisins.replace(B, voisinsB);
			}
		}
		
		return voisins;
	}
	
	public static void calculRendementCouleurs(ArrayList<Route> routes) {
		
		HashMap<Couleur, Integer> scoreRoutes = new HashMap<>();
		HashMap<Couleur, Integer> longueurRoutes = new HashMap<>();		
		for (Couleur couleur : Couleur.values()) {
			scoreRoutes.put(couleur, 0);
			longueurRoutes.put(couleur, 0);
		}
		
		for (Route route : routes) {
			Couleur couleurRoute = route.getCouleur();
			int scoreRoute = scoreRoutes.get(couleurRoute);
			scoreRoute += route.getScore();
			scoreRoutes.replace(couleurRoute, scoreRoute);
			int longueurRoute = longueurRoutes.get(couleurRoute);
			longueurRoute += route.getLongueur();
			longueurRoutes.replace(couleurRoute, longueurRoute);
		}
		
		HashMap<Couleur, Double> rendementCouleurs = new HashMap<Couleur, Double>();
		for (Couleur couleur : Couleur.values()) {
			rendementCouleurs.put(couleur, Double.parseDouble(new DecimalFormat(".##").format(Double.valueOf(scoreRoutes.get(couleur))/Double.valueOf(longueurRoutes.get(couleur))).replace(",", ".")));
		}
		
		rendementCouleurs = rendementCouleurs.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Couleur couleur : rendementCouleurs.keySet()) {
			System.out.println(couleur + " : " + rendementCouleurs.get(couleur));
		}
		System.out.println();
	}
	
	public static void calculRendementDestinations(ArrayList<Destination> destinations, HashMap<Ville, ArrayList<Voisin>> voisins) {
		
		int nombreRoutesLimite = 1;
		int longueurLimite = 3;
		
		HashMap<Destination, Double> rendementDestinations = new HashMap<Destination, Double>();
		HashMap<Destination, Integer> nombreCheminsParDestination = new HashMap<Destination, Integer>();
		HashMap<Destination, ArrayList<Chemin>> meilleursCheminsPlusCourtsParDestination = new HashMap<Destination, ArrayList<Chemin>>();
		HashMap<Destination, ArrayList<Chemin>> tousCheminsParDestination = new HashMap<Destination, ArrayList<Chemin>>();
		for (Destination destination : destinations) {
			ArrayList<ArrayList<Chemin>> calculChemins = calculChemins(destination, voisins);
			ArrayList<Chemin> tousChemins = calculChemins.get(0);
			ArrayList<Chemin> cheminsPlusCourts = calculChemins.get(1);
			rendementDestinations.put(destination, Double.parseDouble(new DecimalFormat(".##").format(Double.valueOf(destination.getPoints()+cheminsPlusCourts.get(0).getScore())/Double.valueOf(cheminsPlusCourts.get(0).getLongueur())).replace(",", ".")));
			nombreCheminsParDestination.put(destination, tousChemins.size());
			meilleursCheminsPlusCourtsParDestination.put(destination, cheminsPlusCourts);
			tousCheminsParDestination.put(destination, tousChemins);
		}
		
		rendementDestinations = rendementDestinations.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Destination destination : rendementDestinations.keySet()) {
			System.out.println((destination.isExtension() ? "(1912) " : "") + (destination.isPrincipale() ? "(P) " : "") + destination.toString() + "(" + destination.getPoints() + ") : " + rendementDestinations.get(destination) + " (" + nombreCheminsParDestination.get(destination) + " chemins)");
			for (Chemin chemin : meilleursCheminsPlusCourtsParDestination.get(destination)) {
				System.out.println("\t" + chemin.toString() + "(" + chemin.getNombreRoutes() + "/" + chemin.getLongueur() + "/" + chemin.getScore() + ")");
			}
			double meilleurRendement = rendementDestinations.get(destination);
			ArrayList<Chemin> meilleursCheminsLucratifsParDestination = new ArrayList<Chemin>();
			for (Chemin chemin : tousCheminsParDestination.get(destination)) {
				if (chemin.getNombreRoutes() <= meilleursCheminsPlusCourtsParDestination.get(destination).get(0).getNombreRoutes()+nombreRoutesLimite) {
					if (chemin.getLongueur() <= meilleursCheminsPlusCourtsParDestination.get(destination).get(0).getLongueur()+longueurLimite) {
						double rendementChemin = Double.parseDouble(new DecimalFormat(".##").format(Double.valueOf(destination.getPoints()+chemin.getScore())/Double.valueOf(chemin.getLongueur())).replace(",", "."));
						if (rendementChemin > meilleurRendement) {
							meilleursCheminsLucratifsParDestination.clear();
							meilleursCheminsLucratifsParDestination.add(chemin);
							meilleurRendement = rendementChemin;
						} else if (rendementChemin == meilleurRendement && rendementChemin != rendementDestinations.get(destination)) {
							meilleursCheminsLucratifsParDestination.add(chemin);
						}
					}
				} else {
					break;
				}
			}
			for (Chemin chemin : meilleursCheminsLucratifsParDestination) {
				System.out.println("\t(" + meilleurRendement + ") " + chemin.toString() + "(" + chemin.getNombreRoutes() + "/" + chemin.getLongueur() + "/" + chemin.getScore() + ")");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void calculFrequenceVilles(ArrayList<Destination> destinations, HashMap<Ville, ArrayList<Voisin>> voisins) {
		
		HashMap<Ville, Integer> frequenceVilles = new HashMap<Ville, Integer>();
		for (Ville ville : Ville.values()) {
			frequenceVilles.put(ville, 0);
		}
		
		for (Destination destination : destinations) {
			ArrayList<Chemin> cheminsPlusCourts = calculChemins(destination, voisins).get(1);
			ArrayList<Ville> villesParcourues = new ArrayList<Ville>();
			for (Voisin voisin : cheminsPlusCourts.get(0).getVoisins()) {
				villesParcourues.add(voisin.getVille());
			}
			if (cheminsPlusCourts.size() > 1) {
				for (int i = 1; i < cheminsPlusCourts.size(); i++) {
					for (Voisin voisin : cheminsPlusCourts.get(i).getVoisins()) {
						if (!villesParcourues.contains(voisin.getVille())) {
							villesParcourues.add(voisin.getVille());
						}
					}
				}
			}
			
			for (Ville villeParcourue : villesParcourues) {
				int frequenceVille = frequenceVilles.get(villeParcourue);
				frequenceVille++;
				frequenceVilles.replace(villeParcourue, frequenceVille);
			}
		}
		
		frequenceVilles = frequenceVilles.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Ville ville : frequenceVilles.keySet()) {
			System.out.println(ville + " : " + frequenceVilles.get(ville));
		}
		System.out.println();
	}
	
	public static ArrayList<ArrayList<Chemin>> calculChemins(Destination destination, HashMap<Ville, ArrayList<Voisin>> voisins) {
		
		ArrayList<Chemin> tousChemins = new ArrayList<Chemin>();
		ArrayList<Chemin> meilleursCheminsPlusCourts = new ArrayList<Chemin>();
		
		ArrayList<Voisin> listeVilleA = new ArrayList<Voisin>();
		listeVilleA.add(new Voisin(destination.getA(), 0, 0));
		ArrayList<Chemin> cheminsPossiblesActuels = new ArrayList<Chemin>();
		cheminsPossiblesActuels.add(new Chemin(listeVilleA));
		int nombreRoutesPlusFaible = 0;
		int longueurPlusFaible = 0;
		int scoreLongueurPlusFaible = 0;
		
		while (!cheminsPossiblesActuels.isEmpty()) {
			ArrayList<Chemin> cheminsPossiblesNouveaux = new ArrayList<Chemin>();
			for (Chemin chemin : cheminsPossiblesActuels) {
				Ville derniereVilleChemin = chemin.getVoisins().get(chemin.getVoisins().size()-1).getVille();
				for (Voisin voisin : voisins.get(derniereVilleChemin)) {
					if (ajouterChemin(chemin, voisin.getVille(), voisins)) {
						ArrayList<Voisin> cheminVoisins = new ArrayList<Voisin>(chemin.getVoisins());
						cheminVoisins.add(voisin);
						Chemin cheminPossibleNouveau = new Chemin(cheminVoisins);
						if (voisin.getVille() == destination.getB()) {
							tousChemins.add(cheminPossibleNouveau);
							if (nombreRoutesPlusFaible == 0) {
								meilleursCheminsPlusCourts.add(cheminPossibleNouveau);
								nombreRoutesPlusFaible = cheminPossibleNouveau.getNombreRoutes();
								longueurPlusFaible = cheminPossibleNouveau.getLongueur();
								scoreLongueurPlusFaible = cheminPossibleNouveau.getScore();
							} else if (cheminPossibleNouveau.getNombreRoutes() == nombreRoutesPlusFaible) {
								if (cheminPossibleNouveau.getLongueur() < longueurPlusFaible) {
									meilleursCheminsPlusCourts.clear();
									meilleursCheminsPlusCourts.add(cheminPossibleNouveau);
									longueurPlusFaible = cheminPossibleNouveau.getLongueur();
									scoreLongueurPlusFaible = cheminPossibleNouveau.getScore();
								} else if (cheminPossibleNouveau.getLongueur() == longueurPlusFaible) {
									if (cheminPossibleNouveau.getScore() > scoreLongueurPlusFaible) {
										meilleursCheminsPlusCourts.clear();
										meilleursCheminsPlusCourts.add(cheminPossibleNouveau);
										scoreLongueurPlusFaible = cheminPossibleNouveau.getScore();
									} else if (cheminPossibleNouveau.getScore() == scoreLongueurPlusFaible) {
										meilleursCheminsPlusCourts.add(cheminPossibleNouveau);
									}
								}
							}
						} else {
							cheminsPossiblesNouveaux.add(cheminPossibleNouveau);
						}
					}
				}
			}
			cheminsPossiblesActuels = cheminsPossiblesNouveaux;
		}
		
		ArrayList<ArrayList<Chemin>> resultat = new ArrayList<ArrayList<Chemin>>();
		resultat.add(tousChemins);
		resultat.add(meilleursCheminsPlusCourts);
		
		return resultat;
	}
	
	public static boolean ajouterChemin(Chemin chemin, Ville ville, HashMap<Ville, ArrayList<Voisin>> voisins) {
		
		if (chemin.getVoisins().size() == 1) {
			return true;
		}
		
		Ville avantDerniereVilleChemin = chemin.getVoisins().get(chemin.getVoisins().size()-2).getVille();
		if (ville == avantDerniereVilleChemin) {
			return false;
		}
		
		for (int i = 0; i <= chemin.getVoisins().size()-2; i++) {
			for (Voisin villeVoisins : voisins.get(chemin.getVoisins().get(i).getVille())) {
				if (villeVoisins.getVille() == ville) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static ArrayList<Route> ajoutRoutes() {
		
		ArrayList<Route> routes = new ArrayList<Route>();
		
		routes.add(new Route(Ville.EDINBURGH, Ville.LONDON, Couleur.NOIR, 4, 0, false));
		routes.add(new Route(Ville.EDINBURGH, Ville.LONDON, Couleur.ORANGE, 4, 0, false));
		routes.add(new Route(Ville.LONDON, Ville.AMSTERDAM, Couleur.GRIS, 2, 2, false));
		routes.add(new Route(Ville.LONDON, Ville.DIEPPE, Couleur.GRIS, 2, 1, false));
		routes.add(new Route(Ville.LONDON, Ville.DIEPPE, Couleur.GRIS, 2, 1, false));
		routes.add(new Route(Ville.DIEPPE, Ville.BREST, Couleur.ORANGE, 2, 0, false));
		routes.add(new Route(Ville.BREST, Ville.PARIS, Couleur.NOIR, 3, 0, false));
		routes.add(new Route(Ville.BREST, Ville.PAMPLONA, Couleur.ROSE, 4, 0, false));
		routes.add(new Route(Ville.DIEPPE, Ville.PARIS, Couleur.ROSE, 1, 0, false));
		routes.add(new Route(Ville.DIEPPE, Ville.BRUXELLES, Couleur.VERT, 2, 0, false));
		routes.add(new Route(Ville.BRUXELLES, Ville.AMSTERDAM, Couleur.NOIR, 1, 0, false));
		routes.add(new Route(Ville.AMSTERDAM, Ville.ESSEN, Couleur.JAUNE, 3, 0, false));
		routes.add(new Route(Ville.AMSTERDAM, Ville.FRANKFURT, Couleur.BLANC, 2, 0, false));
		routes.add(new Route(Ville.BRUXELLES, Ville.FRANKFURT, Couleur.BLEU, 2, 0, false));
		routes.add(new Route(Ville.BRUXELLES, Ville.PARIS, Couleur.JAUNE, 2, 0, false));
		routes.add(new Route(Ville.BRUXELLES, Ville.PARIS, Couleur.ROUGE, 2, 0, false));
		routes.add(new Route(Ville.PARIS, Ville.FRANKFURT, Couleur.BLANC, 3, 0, false));
		routes.add(new Route(Ville.PARIS, Ville.FRANKFURT, Couleur.ORANGE, 3, 0, false));
		routes.add(new Route(Ville.PARIS, Ville.ZURICH, Couleur.GRIS, 3, 0, true));
		routes.add(new Route(Ville.PARIS, Ville.MARSEILLE, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.PARIS, Ville.PAMPLONA, Couleur.BLEU, 4, 0, false));
		routes.add(new Route(Ville.PARIS, Ville.PAMPLONA, Couleur.VERT, 4, 0, false));
		routes.add(new Route(Ville.ZURICH, Ville.MARSEILLE, Couleur.ROSE, 2, 0, true));
		routes.add(new Route(Ville.PAMPLONA, Ville.MARSEILLE, Couleur.ROUGE, 4, 0, false));
		routes.add(new Route(Ville.BARCELONA, Ville.MARSEILLE, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.PAMPLONA, Ville.MADRID, Couleur.NOIR, 3, 0, true));
		routes.add(new Route(Ville.PAMPLONA, Ville.MADRID, Couleur.BLANC, 3, 0, true));
		routes.add(new Route(Ville.PAMPLONA, Ville.BARCELONA, Couleur.GRIS, 2, 0, true));
		routes.add(new Route(Ville.MADRID, Ville.BARCELONA, Couleur.JAUNE, 2, 0, false));
		routes.add(new Route(Ville.MADRID, Ville.LISBOA, Couleur.ROSE, 3, 0, false));
		routes.add(new Route(Ville.LISBOA, Ville.CADIZ, Couleur.BLEU, 2, 0, false));
		routes.add(new Route(Ville.CADIZ, Ville.MADRID, Couleur.ORANGE, 3, 0, false));
		routes.add(new Route(Ville.ESSEN, Ville.KOBENHAVN, Couleur.GRIS, 3, 1, false));
		routes.add(new Route(Ville.ESSEN, Ville.KOBENHAVN, Couleur.GRIS, 3, 1, false));
		routes.add(new Route(Ville.KOBENHAVN, Ville.STOCKHOLM, Couleur.JAUNE, 3, 0, false));
		routes.add(new Route(Ville.KOBENHAVN, Ville.STOCKHOLM, Couleur.BLANC, 3, 0, false));
		routes.add(new Route(Ville.ESSEN, Ville.BERLIN, Couleur.BLEU, 2, 0, false));
		routes.add(new Route(Ville.BERLIN, Ville.DANZIG, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.DANZIG, Ville.RIGA, Couleur.NOIR, 3, 0, false));
		routes.add(new Route(Ville.DANZIG, Ville.WARSZAWA, Couleur.GRIS, 2, 0, false));
		routes.add(new Route(Ville.FRANKFURT, Ville.ESSEN, Couleur.VERT, 2, 0, false));
		routes.add(new Route(Ville.FRANKFURT, Ville.BERLIN, Couleur.NOIR, 3, 0, false));
		routes.add(new Route(Ville.FRANKFURT, Ville.BERLIN, Couleur.ROUGE, 3, 0, false));
		routes.add(new Route(Ville.BERLIN, Ville.WARSZAWA, Couleur.ROSE, 4, 0, false));
		routes.add(new Route(Ville.BERLIN, Ville.WARSZAWA, Couleur.JAUNE, 4, 0, false));
		routes.add(new Route(Ville.BERLIN, Ville.WIEN, Couleur.VERT, 3, 0, false));
		routes.add(new Route(Ville.WIEN, Ville.WARSZAWA, Couleur.BLEU, 4, 0, false));
		routes.add(new Route(Ville.FRANKFURT, Ville.MUNCHEN, Couleur.ROSE, 2, 0, false));
		routes.add(new Route(Ville.MUNCHEN, Ville.WIEN, Couleur.ORANGE, 3, 0, false));
		routes.add(new Route(Ville.MUNCHEN, Ville.ZURICH, Couleur.JAUNE, 2, 0, true));
		routes.add(new Route(Ville.ZURICH, Ville.VENEZIA, Couleur.VERT, 2, 0, true));
		routes.add(new Route(Ville.VENEZIA, Ville.MUNCHEN, Couleur.BLEU, 2, 0, true));
		routes.add(new Route(Ville.MARSEILLE, Ville.ROMA, Couleur.GRIS, 4, 0, true));
		routes.add(new Route(Ville.VENEZIA, Ville.ROMA, Couleur.NOIR, 2, 0, false));
		routes.add(new Route(Ville.ROMA, Ville.BRINDISI, Couleur.BLANC, 2, 0, false));
		routes.add(new Route(Ville.ROMA, Ville.PALERMO, Couleur.GRIS, 4, 1, false));
		routes.add(new Route(Ville.PALERMO, Ville.BRINDISI, Couleur.GRIS, 3, 1, false));
		routes.add(new Route(Ville.BRINDISI, Ville.ATHINA, Couleur.GRIS, 4, 1, false));
		routes.add(new Route(Ville.ATHINA, Ville.SARAJEVO, Couleur.VERT, 4, 0, false));
		routes.add(new Route(Ville.ATHINA, Ville.SOFIA, Couleur.ROSE, 3, 0, false));
		routes.add(new Route(Ville.SOFIA, Ville.SARAJEVO, Couleur.GRIS, 2, 0, true));
		routes.add(new Route(Ville.VENEZIA, Ville.ZAGRAB, Couleur.GRIS, 2, 0, false));
		routes.add(new Route(Ville.ZAGRAB, Ville.WIEN, Couleur.GRIS, 2, 0, false));
		routes.add(new Route(Ville.BUDAPEST, Ville.WIEN, Couleur.ROUGE, 1, 0, false));
		routes.add(new Route(Ville.BUDAPEST, Ville.WIEN, Couleur.BLANC, 1, 0, false));
		routes.add(new Route(Ville.BUDAPEST, Ville.ZAGRAB, Couleur.ORANGE, 2, 0, false));
		routes.add(new Route(Ville.ZAGRAB, Ville.SARAJEVO, Couleur.ROUGE, 3, 0, false));
		routes.add(new Route(Ville.SARAJEVO, Ville.BUDAPEST, Couleur.ROSE, 3, 0, false));
		routes.add(new Route(Ville.STOCKHOLM, Ville.PETROGRAD, Couleur.GRIS, 8, 0, true));
		routes.add(new Route(Ville.PALERMO, Ville.SMYRNA, Couleur.GRIS, 6, 2, false));
		routes.add(new Route(Ville.RIGA, Ville.WILNO, Couleur.VERT, 4, 0, false));
		routes.add(new Route(Ville.RIGA, Ville.PETROGRAD, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.PETROGRAD, Ville.WILNO, Couleur.BLEU, 4, 0, false));
		routes.add(new Route(Ville.PETROGRAD, Ville.MOSKVA, Couleur.BLANC, 4, 0, false));
		routes.add(new Route(Ville.WARSZAWA, Ville.WILNO, Couleur.ROUGE, 3, 0, false));
		routes.add(new Route(Ville.WILNO, Ville.SMOLENSK, Couleur.JAUNE, 3, 0, false));
		routes.add(new Route(Ville.WILNO, Ville.KYIV, Couleur.GRIS, 2, 0, false));
		routes.add(new Route(Ville.KYIV, Ville.SMOLENSK, Couleur.ROUGE, 3, 0, false));
		routes.add(new Route(Ville.SMOLENSK, Ville.MOSKVA, Couleur.ORANGE, 2, 0, false));
		routes.add(new Route(Ville.WARSZAWA, Ville.KYIV, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.BUDAPEST, Ville.KYIV, Couleur.GRIS, 6, 0, true));
		routes.add(new Route(Ville.BUDAPEST, Ville.BUCURESTI, Couleur.GRIS, 4, 0, true));
		routes.add(new Route(Ville.BUCURESTI, Ville.KYIV, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.KYIV, Ville.KHARKOV, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.KHARKOV, Ville.MOSKVA, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.BUCURESTI, Ville.SEVASTOPOL, Couleur.BLANC, 4, 0, false));
		routes.add(new Route(Ville.KHARKOV, Ville.ROSTOV, Couleur.VERT, 2, 0, false));
		routes.add(new Route(Ville.ROSTOV, Ville.SEVASTOPOL, Couleur.GRIS, 4, 0, false));
		routes.add(new Route(Ville.SEVASTOPOL, Ville.SOCHI, Couleur.GRIS, 2, 1, false));
		routes.add(new Route(Ville.SOCHI, Ville.ROSTOV, Couleur.GRIS, 2, 0, false));
		routes.add(new Route(Ville.SOFIA, Ville.BUCURESTI, Couleur.GRIS, 2, 0, true));
		routes.add(new Route(Ville.SOFIA, Ville.CONSTANTINOPLE, Couleur.BLEU, 3, 0, false));
		routes.add(new Route(Ville.BUCURESTI, Ville.CONSTANTINOPLE, Couleur.JAUNE, 3, 0, false));
		routes.add(new Route(Ville.ATHINA, Ville.SMYRNA, Couleur.GRIS, 2, 1, false));
		routes.add(new Route(Ville.SMYRNA, Ville.CONSTANTINOPLE, Couleur.GRIS, 2, 0, true));
		routes.add(new Route(Ville.CONSTANTINOPLE, Ville.ANGORA, Couleur.GRIS, 2, 0, true));
		routes.add(new Route(Ville.SMYRNA, Ville.ANGORA, Couleur.ORANGE, 3, 0, true));
		routes.add(new Route(Ville.CONSTANTINOPLE, Ville.SEVASTOPOL, Couleur.GRIS, 4, 2, false));
		routes.add(new Route(Ville.SEVASTOPOL, Ville.ERZURUM, Couleur.GRIS, 4, 2, false));
		routes.add(new Route(Ville.SOCHI, Ville.ERZURUM, Couleur.ROUGE, 3, 0, true));
		routes.add(new Route(Ville.ERZURUM, Ville.ANGORA, Couleur.NOIR, 3, 0, false));
		
		return routes;
	}
	
	public static ArrayList<Destination> ajoutDestinations() {
		
		ArrayList<Destination> destinations = new ArrayList<Destination>();
		
		destinations.add(new Destination(Ville.EDINBURGH, Ville.ATHINA, 21, true, false));
		destinations.add(new Destination(Ville.CADIZ, Ville.STOCKHOLM, 21, true, false));
		destinations.add(new Destination(Ville.KOBENHAVN, Ville.ERZURUM, 21, true, false));
		destinations.add(new Destination(Ville.BREST, Ville.PETROGRAD, 20, true, false));
		destinations.add(new Destination(Ville.LISBOA, Ville.DANZIG, 20, true, false));
		destinations.add(new Destination(Ville.PALERMO, Ville.MOSKVA, 20, true, false));
		destinations.add(new Destination(Ville.FRANKFURT, Ville.SMOLENSK, 13, false, false));
		destinations.add(new Destination(Ville.AMSTERDAM, Ville.WILNO, 12, false, false));
		destinations.add(new Destination(Ville.BERLIN, Ville.MOSKVA, 12, false, false));
		destinations.add(new Destination(Ville.WIEN, Ville.STOCKHOLM, 11, false, false));
		destinations.add(new Destination(Ville.ATHINA, Ville.WILNO, 11, false, false));
		destinations.add(new Destination(Ville.VENEZIA, Ville.CONSTANTINOPLE, 10, false, false));
		destinations.add(new Destination(Ville.ANGORA, Ville.KHARKOV, 10, false, false));
		destinations.add(new Destination(Ville.LONDON, Ville.WIEN, 10, false, false));
		destinations.add(new Destination(Ville.RIGA, Ville.BUCURESTI, 10, false, false));
		destinations.add(new Destination(Ville.ESSEN, Ville.KYIV, 10, false, false));
		destinations.add(new Destination(Ville.BRUXELLES, Ville.DANZIG, 9, false, false));
		destinations.add(new Destination(Ville.BERLIN, Ville.ROMA, 9, false, false));
		destinations.add(new Destination(Ville.BERLIN, Ville.BUCURESTI, 8, false, false));
		destinations.add(new Destination(Ville.PARIS, Ville.WIEN, 8, false, false));
		destinations.add(new Destination(Ville.SMOLENSK, Ville.ROSTOV, 8, false, false));
		destinations.add(new Destination(Ville.BARCELONA, Ville.MUNCHEN, 8, false, false));
		destinations.add(new Destination(Ville.MADRID, Ville.ZURICH, 8, false, false));
		destinations.add(new Destination(Ville.ROMA, Ville.SMYRNA, 8, false, false));
		destinations.add(new Destination(Ville.MARSEILLE, Ville.ESSEN, 8, false, false));
		destinations.add(new Destination(Ville.BREST, Ville.VENEZIA, 8, false, false));
		destinations.add(new Destination(Ville.KYIV, Ville.SOCHI, 8, false, false));
		destinations.add(new Destination(Ville.SARAJEVO, Ville.SEVASTOPOL, 8, false, false));
		destinations.add(new Destination(Ville.MADRID, Ville.DIEPPE, 8, false, false));
		destinations.add(new Destination(Ville.BARCELONA, Ville.BRUXELLES, 8, false, false));
		destinations.add(new Destination(Ville.PALERMO, Ville.CONSTANTINOPLE, 8, false, false));
		destinations.add(new Destination(Ville.AMSTERDAM, Ville.PAMPLONA, 7, false, false));
		destinations.add(new Destination(Ville.PARIS, Ville.EDINBURGH, 7, false, false));
		destinations.add(new Destination(Ville.PARIS, Ville.ZAGRAB, 7, false, false));
		destinations.add(new Destination(Ville.LONDON, Ville.BERLIN, 7, false, false));
		destinations.add(new Destination(Ville.BREST, Ville.MARSEILLE, 7, false, false));
		destinations.add(new Destination(Ville.ZURICH, Ville.BRINDISI, 6, false, false));
		destinations.add(new Destination(Ville.KYIV, Ville.PETROGRAD, 6, false, false));
		destinations.add(new Destination(Ville.WARSZAWA, Ville.SMOLENSK, 6, false, false));
		destinations.add(new Destination(Ville.ZAGRAB, Ville.BRINDISI, 6, false, false));
		destinations.add(new Destination(Ville.ZURICH, Ville.BUDAPEST, 6, false, false));
		destinations.add(new Destination(Ville.SOFIA, Ville.SMYRNA, 5, false, false));
		destinations.add(new Destination(Ville.ATHINA, Ville.ANGORA, 5, false, false));
		destinations.add(new Destination(Ville.ROSTOV, Ville.ERZURUM, 5, false, false));
		destinations.add(new Destination(Ville.BUDAPEST, Ville.SOFIA, 5, false, false));
		destinations.add(new Destination(Ville.FRANKFURT, Ville.KOBENHAVN, 5, false, false));
		
		destinations.add(new Destination(Ville.LONDON, Ville.SOCHI, 20, true, true));
		destinations.add(new Destination(Ville.AMSTERDAM, Ville.ROSTOV, 19, true, true));
		destinations.add(new Destination(Ville.PAMPLONA, Ville.KYIV, 18, true, true));
		destinations.add(new Destination(Ville.PARIS, Ville.SEVASTOPOL, 17, true, true));
		destinations.add(new Destination(Ville.RIGA, Ville.BRINDISI, 17, true, true));
		destinations.add(new Destination(Ville.ESSEN, Ville.ANGORA, 16, true, true));
		destinations.add(new Destination(Ville.MADRID, Ville.MOSKVA, 25, false, true));
		destinations.add(new Destination(Ville.MADRID, Ville.ANGORA, 21, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.ANGORA, 20, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.MOSKVA, 19, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.MOSKVA, 18, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.ANGORA, 17, false, true));
		destinations.add(new Destination(Ville.ROMA, Ville.MOSKVA, 17, false, true));
		destinations.add(new Destination(Ville.MADRID, Ville.ATHINA, 16, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.ATHINA, 16, false, true));
		destinations.add(new Destination(Ville.MUNCHEN, Ville.PETROGRAD, 14, false, true));
		destinations.add(new Destination(Ville.MOSKVA, Ville.ATHINA, 14, false, true));
		destinations.add(new Destination(Ville.MOSKVA, Ville.ANGORA, 14, false, true));
		destinations.add(new Destination(Ville.CADIZ, Ville.FRANKFURT, 13, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.ATHINA, 13, false, true));
		destinations.add(new Destination(Ville.MADRID, Ville.WIEN, 13, false, true));
		destinations.add(new Destination(Ville.MADRID, Ville.BERLIN, 13, false, true));
		destinations.add(new Destination(Ville.BERLIN, Ville.ANGORA, 13, false, true));
		destinations.add(new Destination(Ville.PAMPLONA, Ville.PALERMO, 12, false, true));
		destinations.add(new Destination(Ville.WARSZAWA, Ville.SEVASTOPOL, 12, false, true));
		destinations.add(new Destination(Ville.STOCKHOLM, Ville.WILNO, 12, false, true));
		destinations.add(new Destination(Ville.WIEN, Ville.MOSKVA, 12, false, true));
		destinations.add(new Destination(Ville.BERLIN, Ville.ATHINA, 11, false, true));
		destinations.add(new Destination(Ville.ROMA, Ville.ANGORA, 11, false, true));
		destinations.add(new Destination(Ville.RIGA, Ville.KHARKOV, 10, false, true));
		destinations.add(new Destination(Ville.BRUXELLES, Ville.STOCKHOLM, 10, false, true));
		destinations.add(new Destination(Ville.MADRID, Ville.ROMA, 10, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.ROMA, 10, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.MADRID, 10, false, true));
		destinations.add(new Destination(Ville.WIEN, Ville.ANGORA, 10, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.ROMA, 10, false, true));
		destinations.add(new Destination(Ville.SOCHI, Ville.SMYRNA, 9, false, true));
		destinations.add(new Destination(Ville.EDINBURGH, Ville.ESSEN, 9, false, true));
		destinations.add(new Destination(Ville.DIEPPE, Ville.KOBENHAVN, 9, false, true));
		destinations.add(new Destination(Ville.VENEZIA, Ville.WARSZAWA, 8, false, true));
		destinations.add(new Destination(Ville.WIEN, Ville.ATHINA, 8, false, true));
		destinations.add(new Destination(Ville.MUNCHEN, Ville.SARAJEVO, 7, false, true));
		destinations.add(new Destination(Ville.DANZIG, Ville.BUDAPEST, 7, false, true));
		destinations.add(new Destination(Ville.BUCURESTI, Ville.ERZURUM, 7, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.MADRID, 7, false, true));
		destinations.add(new Destination(Ville.PARIS, Ville.BERLIN, 7, false, true));
		destinations.add(new Destination(Ville.AMSTERDAM, Ville.VENEZIA, 6, false, true));
		destinations.add(new Destination(Ville.SOFIA, Ville.KYIV, 6, false, true));
		destinations.add(new Destination(Ville.WIEN, Ville.ROMA, 6, false, true));
		destinations.add(new Destination(Ville.ROMA, Ville.ATHINA, 6, false, true));
		destinations.add(new Destination(Ville.DIEPPE, Ville.MARSEILLE, 5, false, true));
		destinations.add(new Destination(Ville.WARSZAWA, Ville.BUDAPEST, 5, false, true));
		destinations.add(new Destination(Ville.LONDON, Ville.PARIS, 3, false, true));
		destinations.add(new Destination(Ville.BERLIN, Ville.WIEN, 3, false, true));
		destinations.add(new Destination(Ville.LISBOA, Ville.CADIZ, 2, false, true));
		
		return destinations;
	}
}
