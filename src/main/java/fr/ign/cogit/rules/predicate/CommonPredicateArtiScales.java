package fr.ign.cogit.rules.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.conversion.AdapterFactory;
import fr.ign.cogit.rules.regulation.Alignements;
import fr.ign.cogit.rules.regulation.ArtiScalesRegulation;
import fr.ign.cogit.simplu3d.analysis.ForbiddenZoneGenerator;
import fr.ign.cogit.simplu3d.model.AbstractBuilding;
import fr.ign.cogit.simplu3d.model.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.model.CadastralParcel;
import fr.ign.cogit.simplu3d.model.Environnement;
import fr.ign.cogit.simplu3d.model.ParcelBoundary;
import fr.ign.cogit.simplu3d.model.ParcelBoundarySide;
import fr.ign.cogit.simplu3d.model.ParcelBoundaryType;
import fr.ign.cogit.simplu3d.model.Prescription;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.AbstractSimpleBuilding;
import fr.ign.cogit.simplu3d.util.CuboidGroupCreation;
import fr.ign.mpp.configuration.AbstractBirthDeathModification;
import fr.ign.mpp.configuration.AbstractGraphConfiguration;
import fr.ign.parameters.Parameters;
import fr.ign.rjmcmc.configuration.ConfigurationModificationPredicate;

/**
 * This abstract class contains generics methods to have only one implementation of regulation if we considere a parcel with one regulation or several subparcels in a parcel with
 * their own regulations
 *
 * @param <O>
 * @param <C>
 * @param <M>
 */
public abstract class CommonPredicateArtiScales<O extends AbstractSimpleBuilding, C extends AbstractGraphConfiguration<O, C, M>, M extends AbstractBirthDeathModification<O, C, M>>
		implements ConfigurationModificationPredicate<C, M> {

	// environnement
	protected Environnement env;
	// Indicate if we can simulate on a parcel
	protected boolean canBeSimulated = true;
	// Indicate if alignement option is activated
	protected boolean align = false;
	// Maxmimal number of cuboids
	protected int nbCuboid = 0;
	// Technical and scenario parameters
	protected Parameters p;
	// Selected prescriptinons
	protected IFeatureCollection<Prescription> prescriptions;
	// Is intersection between cuboids allowed ?
	protected boolean intersection = false;

	// The geometry factory for jts operations
	protected GeometryFactory gf = new GeometryFactory();

	// The property unit on which we make the simulation and its JTS geometry
	protected BasicPropertyUnit currentBPU;
	protected Geometry bPUGeom = null;

	// Cached geometries according to the different limits categories
	protected Geometry jtsCurveLimiteFondParcel = null;
	protected Geometry jtsCurveLimiteFrontParcel = null;
	protected Geometry jtsCurveLimiteLatParcel = null;

	protected Geometry jtsCurveLimiteLatParcelRight = null;
	protected Geometry jtsCurveLimiteLatParcelLeft = null;

	// A geometry that store a forbidden zone (where buildings cannot be built)
	// According to the selected prescriptions
	protected Geometry forbiddenZone = null;

	// Bâtiments dans les parcelles de l'autre côté de la route
	protected Geometry jtsCurveOppositeLimit = null;

	// hauteur des batiments environnants
	Double heighSurroundingBuildings = null;

	public static double distanceHeightBuildings = 50;

	/**
	 * The default constructor with a considered BasicPropertyUnit, technical and scenario parameters and a set of selected prescriptions
	 * 
	 * @param currentBPU
	 * @param align
	 * @param pA
	 * @param presc
	 * @throws Exceptiondistance
	 */
	protected CommonPredicateArtiScales(BasicPropertyUnit currentBPU, boolean align, Parameters pA, IFeatureCollection<Prescription> presc, Environnement env) throws Exception {

		// Set the different initial values
		this.env = env;
		this.p = pA;

		intersection = p.getBoolean("intersection");
		this.prescriptions = presc;
		this.align = align;
		this.nbCuboid = p.getInteger("nbCuboid");
		this.currentBPU = currentBPU;
		// This prepare the geoemtries
		this.prepareCachedGeometries(currentBPU, env);

	}

	/**
	 * Ce constructeur initialise les géométries curveLimiteFondParcel, curveLimiteFrontParcel & curveLimiteLatParcel car elles seront utilisées pour exprimer certaines contraintes
	 * 
	 * @param bPU
	 * @throws Exception
	 */
	protected void prepareCachedGeometries(BasicPropertyUnit bPU, Environnement env) throws Exception {

		// Pour simplifier la vérification, on extrait les différentes bordures de
		// parcelles
		IMultiCurve<IOrientableCurve> curveLimiteFondParcel = new GM_MultiCurve<>();
		IMultiCurve<IOrientableCurve> curveLimiteFrontParcel = new GM_MultiCurve<>();
		IMultiCurve<IOrientableCurve> curveLimiteLatParcel = new GM_MultiCurve<>();
		IMultiCurve<IOrientableCurve> curveLimiteLatRightParcel = new GM_MultiCurve<>();
		IMultiCurve<IOrientableCurve> curveLimiteLatLeftParcel = new GM_MultiCurve<>();
		IMultiCurve<IOrientableCurve> curveOppositeLimit = new GM_MultiCurve<>();

		// On parcourt les parcelles du BasicPropertyUnit (un propriétaire peut
		// avoir plusieurs parcelles)
		for (CadastralParcel cP : bPU.getCadastralParcels()) {

			// On parcourt les limites séparatives
			for (ParcelBoundary sCB : cP.getBoundaries()) {

				// En fonction du type on ajoute à telle ou telle géométrie
				IGeometry geom = sCB.getGeom();

				if (geom == null || geom.isEmpty() || geom.length() < 0.01) {
					continue;
				}
				// Fond de parcel
				if (sCB.getType() == ParcelBoundaryType.BOT) {

					if (geom instanceof IOrientableCurve) {
						curveLimiteFondParcel.add((IOrientableCurve) geom);
					} else {
						System.out.println("Classe SamplePredicate : quelque chose n'est pas un ICurve : " + geom.getClass());
					}
				}
				// Limite latérale
				if (sCB.getType() == ParcelBoundaryType.LAT) {

					if (geom instanceof IOrientableCurve) {
						curveLimiteLatParcel.add((IOrientableCurve) geom);
						if (sCB.getSide() == ParcelBoundarySide.LEFT) {
							curveLimiteLatLeftParcel.add((IOrientableCurve) geom);
						} else if (sCB.getSide() == ParcelBoundarySide.RIGHT) {
							curveLimiteLatRightParcel.add((IOrientableCurve) geom);
						}
					} else {
						System.out.println("Classe SamplePredicate : quelque chose n'est pas un ICurve : " + geom.getClass());
					}
				}
				// Limite front
				if (sCB.getType() == ParcelBoundaryType.ROAD) {

					if (geom instanceof IOrientableCurve) {
						curveLimiteFrontParcel.add((IOrientableCurve) geom);
					} else {
						System.out.println("Classe SamplePredicate : quelque chose n'est pas un ICurve : " + geom.getClass());
					}
				}
			}
		}

		// Limit Opposite
		for (ParcelBoundary salut : bPU.getCadastralParcels().get(0).getBoundariesByType(ParcelBoundaryType.ROAD)) {
			// System.out.println("Number of buildings in env : " + bPU.getBuildings().size());

			if (salut.getOppositeBoundary() != null) {
				IGeometry geom = salut.getOppositeBoundary().getGeom();
				if (geom instanceof IOrientableCurve) {
					curveOppositeLimit.add((IOrientableCurve) geom);
				} else {
					System.out.println("Classe SamplePredicate : quelque chose n'est pas un ICurve : " + geom.getClass());
				}
			}

		}

		// height of the surrounding buildings

		// System.out.println(bPU.getGeom().buffer(distanceHeightBuildings));
		Collection<AbstractBuilding> buildingsHeightCol = env.getBuildings().select(bPU.getGeom().buffer(distanceHeightBuildings));
		// System.out.println("Neighbour buildings :" + buildingsHeightCol.size());
		if (!buildingsHeightCol.isEmpty()) {
			heighSurroundingBuildings = buildingsHeightCol.stream().mapToDouble(x -> x.height(1, 1)).sum() / buildingsHeightCol.size();
		}

		if (!curveOppositeLimit.isEmpty()) {
			this.jtsCurveOppositeLimit = AdapterFactory.toGeometry(gf, curveOppositeLimit);
		}

		if (!curveLimiteFondParcel.isEmpty()) {
			this.jtsCurveLimiteFondParcel = AdapterFactory.toGeometry(gf, curveLimiteFondParcel);
		}

		if (!curveLimiteFrontParcel.isEmpty()) {
			this.jtsCurveLimiteFrontParcel = AdapterFactory.toGeometry(gf, curveLimiteFrontParcel);
		}

		if (!curveLimiteLatParcel.isEmpty()) {
			this.jtsCurveLimiteLatParcel = AdapterFactory.toGeometry(gf, curveLimiteLatParcel);
		}

		if (!curveLimiteLatLeftParcel.isEmpty()) {
			this.jtsCurveLimiteLatParcelLeft = AdapterFactory.toGeometry(gf, curveLimiteLatLeftParcel);
		}

		if (!curveLimiteLatRightParcel.isEmpty()) {
			this.jtsCurveLimiteLatParcelRight = AdapterFactory.toGeometry(gf, curveLimiteLatRightParcel);
		}

		this.bPUGeom = AdapterFactory.toGeometry(gf, bPU.getGeom());

		// Prepare the forbidden geometry from prescription
		ForbiddenZoneGenerator fZG = new ForbiddenZoneGenerator();
		IGeometry geomForbidden = fZG.generateUnionGeometry(prescriptions, currentBPU);

		if (geomForbidden != null && !geomForbidden.isEmpty()) {
			this.forbiddenZone = AdapterFactory.toGeometry(gf, geomForbidden);
		}

		// If the forbidden geometry overlaps the parcel we cannot proceed to the
		// simulation
		if (geomForbidden != null && geomForbidden.contains(currentBPU.getGeom())) {
			canBeSimulated = false;
		}

	}

	/**
	 * This method is executed every time the simulator suggest a new proposition C => current configuration composed of cuboids M => modification the simulator tries to applied
	 * 
	 * Normally there is a maximum of 1 birth and/or 1 death
	 */
	@Override
	public boolean check(C c, M m) {

		// NewCuboids
		List<O> lONewCuboids = m.getBirth();

		// No new cuboids, we do not have to checked
		// Warning only works with certain rules
		if (lONewCuboids.isEmpty()) {
			return true;
		}

		// All current cuboids
		List<O> lAllCuboids = listAllCuboids(c, m);

		// The operators to check the rules
		CommonRulesOperator<O> cRO = new CommonRulesOperator<O>();

		/////////// General constraints that is always checked

		// We check if the number of cuboids does not exceed the max
		// In documentation : Rule-form-001
		if (!cRO.checkNumberOfBuildings(lAllCuboids, nbCuboid)) {
			return false;
		}

		// Checking only for new cuboids
		// In documentation : Rule-form-002
		for (O cuboid : lONewCuboids) {
			// Does the cuboid lays inside the basic property unit
			// In documentation : Rule-form-002
			if (!cRO.checkIfContainsGeometry(cuboid, bPUGeom)) {
				return false;
			}

			// Checking prescriptions alignement
			// Rule-sup-001
			// @TODO : TO IMPLEMENT
			/*
			 * if (!cRO.checkAlignementPrescription(cuboid, prescriptions, align, jtsCurveLimiteFrontParcel)) { return false; }
			 */

			// We check if the cuboids intersects the forbiddent zone
			// ATTENTION : On renvoie faux lorsque l'intersection a lieu
			// Rule-sup-002
			if (cRO.checkIfIntersectsGeometry(cuboid, this.forbiddenZone)) {
				return false;
			}
		}

		///////// Regulation that depends from the SubParcel regulation

		for (O cuboid : lONewCuboids) {

			// Determine the relevant regulations to apply :
			List<ArtiScalesRegulation> lRegles = this.getRegulationToApply(cuboid);

			for (ArtiScalesRegulation regle : lRegles) {

				// is there space enough to put art5 elements in argument?
				if (regle.getArt_5().startsWith("_")) {
					if (!cRO.checkEltFits(lAllCuboids, currentBPU, Double.valueOf(regle.getArt_5().replace("_", "")))) {
						return false;
					}
				}

				// Distance to the front of the parcel
				// Rule-art-0072 && art-0071
				switch (this.getSide()) {
				case UNKNOWN:
					if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteLatParcel, regle.getArt_72())) {
						return false;
					}
					break;
				case LEFT:
					// Building is stuck to the left so the art72 is only applied with the right part of the parcel
					if (regle.getArt_71() == 2) {
						if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteLatParcelRight, regle.getArt_72())) {
							return false;
						}
					}

					break;
				case RIGHT:
					// Building is stuck to the right so the art72 is only applied with the left part of the parcel
					if (regle.getArt_71() == 2) {
						if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteLatParcelLeft, regle.getArt_72())) {
							return false;
						}
					}
					break;
				default:
					System.out.println("Other cas for parcel SIDE ?? " + this.getSide());
					break;
				}

				// Distance to the bottom of the parcel
				// Rule-art-0073
				if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteFondParcel, regle.getArt_73())) {
					return false;
				}

				// Rule art-0074
				// We check for bottom limits
				if (!cRO.checkProspectArt7(cuboid, jtsCurveLimiteFondParcel, regle.getArt_74())) {
					return false;
				}

				// We check for lateral limit
				if (!cRO.checkProspectArt7(cuboid, jtsCurveLimiteLatParcel, regle.getArt_74())) {
					return false;
				}

				//////// Distance to the front of the parcel
				// multiple cases of Art_6 rules
				// Rule-art-006
				String art_6 = regle.getArt_6_defaut();
				int typeArt_6 = regle.getArt_6_type();

				// case where there's only one condition
				// if (typeArt_6.equals("99")) {

				// temporary case (not everything is calculated)
				if (typeArt_6 == 99 || typeArt_6 == 30 || typeArt_6 == 20 || typeArt_6 == 1) {
					// case there's a min and a max
					if (art_6.contains("-")) {
						double min = Double.valueOf(art_6.split("-")[0]);
						double max = Double.valueOf(art_6.split("-")[1]);
						if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteFrontParcel, min) || !cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteFrontParcel, max, false)) {
							return false;
						}
					} else {
						switch (art_6 + "") {
						case "44":
							if (!cRO.checkProspectRNU(cuboid, jtsCurveOppositeLimit)) {
								return false;
							}
							break;
						case "99":
							break;
						default:
							if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteFrontParcel, Double.valueOf(art_6))) {
								return false;
							}
							break;
						}
					}
				}
				// case buildings must be either aligned or having a recoil
				else if (typeArt_6 == 10) {
					if (!cRO.checkDistanceToGeometry(cuboid, jtsCurveLimiteFrontParcel, Double.valueOf(regle.getArt_6_optionel()))
							|| !cRO.checkAlignement(cuboid, jtsCurveLimiteFrontParcel)) {
						return false;
					}
					// } else if (typeArt_6.equals("30")) {
					// //
					// //if (TODO tester l'alignement avec les autres facades){
					// //}
					// //else {
					// //si pas d'enchainement de facade, regarde art_6 : si 0, regarde art_6typeD pour voir si possibilité de retrait
					// //}
					// }
					// else if (typeArt_6.equals("1")) {
					// //
					// //if (TODO tester l'alignement avec les autres facades){
					// //}
					// //else {
					// //si pas d'enchainement de facade, regarde art_6 : si 0, regarde art_6typeD pour voir si possibilité de retrait
					// //}
				} else {
					System.out.println("Art-6 optionel: cas non traité");
				}

				// art_6_opt
				// distance en fonction d'une route
				/*
				 * if (regle.getArt_6_opt() == "1") { double dist = Double.valueOf(regle.getArt_6_optD().split("-")[0]); String nRoute = regle.getArt_6_optD().split("-")[1]; //
				 * TODO dist en fonction d'une route (peut on choper son attribut?
				 * 
				 * }
				 */

				// We check the constrain distance according to existing buildings
				// art_8 (distance aux bâtiments existants)
				if (!cRO.checkDistanceBetweenCuboidandBuildings(cuboid, this.currentBPU, regle.getArt_8())) {
					return false;
				}

			}

			/////////// Groups or whole configuration constraints

			// Getting the maxCES according to the implementation
			// art_9 art_13
			double maxCES = this.getMaxCES();
			// Checking the builtRatio
			if (maxCES != 99) {
				if (!cRO.checkBuiltRatio(lAllCuboids, currentBPU, maxCES)) {
					return false;
				}
			}

			String art12 = this.getArt12Value();
			// art_12
			if (art12 != "99") {
				if (!cRO.checkParking(lAllCuboids, currentBPU, art12, p)) {
					return false;
				}
			}

			// Width and distance between buildings constraints

			CuboidGroupCreation<O> groupCreator = new CuboidGroupCreation<O>();

			List<List<O>> groupList = groupCreator.createGroup(lAllCuboids, 0.1);
			if (intersection) {
				// art_8 et //art_form_4
				// If intersection is allowed, we check the width of the building
				if (!cRO.checkBuildingWidth(groupList, 7.5, determineDoubleDistanceForGroup(groupList)))
					return false;

			} else {
				// art_8
				List<Double> distances = determineDoubleDistanceForList(lAllCuboids);
				if (!cRO.checkDistanceInterCuboids(lAllCuboids, distances)) {
					return false;
				}

			}

		}

		return true;
	}

	/**
	 * Determnig fo all groups the distance to the other cuboids
	 * 
	 * @param lCuboids
	 * @return
	 */
	private List<Double> determineDoubleDistanceForGroup(List<List<O>> lCuboids) {

		List<Double> distances = new ArrayList<>();
		// For each groups
		for (List<O> cuboidGroup : lCuboids) {
			List<Double> distMin = determineDoubleDistanceForList(cuboidGroup);
			// We add it to the list

			double distMinTemp = Collections.min(distMin);
			distances.add(distMinTemp);

		}

		return distances;
	}

	/**
	 * List all the current cuboids of the configuration
	 * 
	 * @param c
	 * @param m
	 * @return
	 */
	protected List<O> listAllCuboids(C c, M m) {
		// On fait la liste de tous les objets après modification
		List<O> lCuboid = new ArrayList<>();

		// On ajoute tous les nouveaux objets
		lCuboid.addAll(m.getBirth());

		// On récupère la boîte (si elle existe) que l'on supprime lors de la
		// modification
		O cuboidDead = null;

		if (!m.getDeath().isEmpty()) {
			cuboidDead = m.getDeath().get(0);
		}

		// On parcourt les objets existants moins celui qu'on supprime
		Iterator<O> iTBat = c.iterator();
		while (iTBat.hasNext()) {

			O cuboidTemp = iTBat.next();

			// Si c'est une boîte qui est amenée à disparaître après
			// modification,
			// elle n'entre pas en jeu dans les vérifications
			if (cuboidTemp == cuboidDead) {
				continue;
			}

			lCuboid.add(cuboidTemp);

		}

		return lCuboid;
	}

	@Override
	public String toString() {
		// TODO ajouter toutes les règles qui s'appliquent (mais ça devrait être inscrit à chaque fois qu'une règle s'applique ?!
		return "CadastralParcelsNb:" + currentBPU.getCadastralParcels().size() + "CadastralParcelsAre" + currentBPU.getCadastralParcels().toString();
	}

	private Alignements alignements = null;

	public Alignements getAlignements() {

		if (alignements == null) {
			return new Alignements(this.getAllRegulation(), this.currentBPU, this.env);
		}
		return alignements;
	}

	protected abstract List<ArtiScalesRegulation> getAllRegulation();

	// Determine for a cuboid the list of constraints that has to be ckecked
	protected abstract List<ArtiScalesRegulation> getRegulationToApply(O cuboid);

	// Determine the maximal CES
	protected abstract double getMaxCES();

	// Determine the min and max constraint
	protected abstract double getMaxHeight();

	protected abstract double getMinHeight();

	// Determine art12 most constraintful value
	protected abstract String getArt12Value();

	// Determine the recoils applied to a list of cuboids
	protected abstract List<Double> determineDoubleDistanceForList(List<O> lCuboids);

	public boolean isCanBeSimulated() {
		return canBeSimulated;
	}

	// Designate if the buildings are stuck to a side
	// ALLOWED VALUE ("LEFT", "RIGHT", "NONE");
	private ParcelBoundarySide side = ParcelBoundarySide.UNKNOWN;

	public ParcelBoundarySide getSide() {
		return side;
	}

	public void setSide(ParcelBoundarySide side) {
		this.side = side;
	}

}
