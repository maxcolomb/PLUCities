package fr.ign.cogit;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;

import fr.ign.cogit.GTFunctions.Vectors;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.indicators.BuildingToHousehold;
import fr.ign.cogit.outputs.XmlGen;
import fr.ign.cogit.rules.io.PrescriptionPreparator;
import fr.ign.cogit.rules.io.ZoneRulesAssociation;
import fr.ign.cogit.rules.predicate.CommonPredicateArtiScales;
import fr.ign.cogit.rules.predicate.MultiplePredicateArtiScales;
import fr.ign.cogit.rules.predicate.PredicateArtiScales;
import fr.ign.cogit.rules.regulation.ArtiScalesRegulation;
import fr.ign.cogit.simplu3d.io.feature.AttribNames;
import fr.ign.cogit.simplu3d.io.nonStructDatabase.shp.LoaderSHP;
import fr.ign.cogit.simplu3d.model.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.model.Environnement;
import fr.ign.cogit.simplu3d.model.Prescription;
import fr.ign.cogit.simplu3d.model.SubParcel;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.loader.LoaderCuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.optimizer.cuboid.OptimisedBuildingsCuboidFinalDirectRejection;
import fr.ign.cogit.simplu3d.util.SDPCalc;
import fr.ign.cogit.util.GetFromGeom;
import fr.ign.cogit.util.SimpluParametersXML;
import fr.ign.cogit.util.VectorFct;
import fr.ign.mpp.configuration.BirthDeathModification;
import fr.ign.mpp.configuration.GraphConfiguration;
import fr.ign.mpp.configuration.GraphVertex;
import fr.ign.parameters.Parameters;

public class SimPLUSimulator {

	// zipCode of the considered Parcels
	String zipCode;
	// parcels containing all of theem
	File parcelsFile;

	// one single parcel to study
	SimpleFeature singleFeat;
	boolean isSingleFeat = false;

	boolean rNU = false;

	File rootFile;

	// Parameters from technical parameters and scenario parameters files
	Parameters p;
	List<File> lF;

	// Building file
	File buildFile;
	// Road file
	File roadFile;

	File codeFile;
	// Predicate File (a csv with a key to make a join with zoning file between id
	// and libelle)
	File predicateFile;
	// PLU Zoning file
	File zoningFile;

	// xml maker objects
	XmlGen resultXml;
	XmlGen logXml;

	File simuFile;
	int compteurOutput = 0;

	// IFeatureCollection<IFeature> iFeatGenC = new FT_FeatureCollection<>();

	// Prescription files
	File filePrescPonct;
	File filePrescLin;
	File filePrescSurf;

	private static List<String> ID_PARCELLE_TO_SIMULATE = new ArrayList<>();

	public static boolean USE_DIFFERENT_REGULATION_FOR_ONE_PARCEL = false;

	public static void main(String[] args) throws Exception {

		List<File> lF = new ArrayList<>();
		// Line to change to select the right scenario

		String rootParam = SimPLUSimulator.class.getClassLoader().getResource("paramSet/scenar0MKDom/").getPath();

		System.out.println(rootParam);

		lF.add(new File(rootParam + "parametreTechnique.xml"));
		lF.add(new File(rootParam + "parametreScenario.xml"));

		Parameters p = Parameters.unmarshall(lF);

		System.out.println(p.getString("nom"));
		// Rappel de la construction du code :

		// 1/ Basically the parcels are filtered on the code with the following
		// attributes
		// codeDep + codeCom + comAbs + section + numero

		// 2/ Alternatively we can decided to active an attribute (Here id)
		AttribNames.setATT_CODE_PARC("Id");

		// ID_PARCELLE_TO_SIMULATE.add("25495000AE0102"); //Test for a simulation
		// with 1 regulation

		USE_DIFFERENT_REGULATION_FOR_ONE_PARCEL = true;
		ID_PARCELLE_TO_SIMULATE.add("25495000AK0005"); // Test for a simulation with
														// 3 regulations on 3 sub
														// parcels

		// RootFolder
		File rootFolder = new File(p.getString("rootFile"));
		// Selected parcels shapefile
		File selectedParcels = new File(p.getString("selectedParcelFile"));
		// GeographicData folder
		File geoFile = new File(p.getString("geoFile"));
		// PLU Folder
		File pluFile = new File(p.getString("pluFile"));

		// writed stuff
		XmlGen resultxml = new XmlGen(rootFolder, "mainSimPLUSIMresult");
		XmlGen logxml = new XmlGen(rootFolder, "mainSimPLUSIMlog");
		;
		SimPLUSimulator simplu = new SimPLUSimulator(rootFolder, geoFile, pluFile, selectedParcels,
				p.getString("listZipCode"), p, lF, resultxml, logxml);

		simplu.run();
		// SimPLUSimulator.fillSelectedParcels(new File(rootFolder), geoFile,
		// pluFile, selectedParcels, 50, "25495", p);

	}

	/**
	 * Constructor to make a new object to run SimPLU3D simulations.
	 * 
	 * @param rootfile        : main folder of an artiscale simulation
	 * @param geoFile         : folder for geographic data
	 * @param pluFile         : folder for PLU data
	 * @param selectedParcels : Folder containing the selection of parcels
	 * @param feat            : single parcel to simulate
	 * @param zipcode         : zipcode of the city that is simulated
	 * @param pa              : parameters file
	 * @param lF              : list of the initials parameters
	 * @param resultxml       : xml maker object to store results
	 * @param logxml          : xml maker object to store temporary stuffs
	 * @throws Exception
	 */
	public SimPLUSimulator(File rootfile, File geoFile, File pluFile, File selectedParcels, String zipcode,
			Parameters pa, List<File> lF, XmlGen resultxml, XmlGen logxml) throws Exception {

		// some static parameters needed
		this.p = pa;
		this.lF = lF;
		this.rootFile = rootfile;
		this.zipCode = zipcode;
		// Si les communes sont au RNU :
		rNU = GetFromGeom.isRNU(p, zipCode);
		if (rNU) {
			zoningFile = GetFromGeom.getPAUzone(pluFile, new File(p.getString("geoFile")), new File(rootfile, "tmp"),
					zipCode);
		} else {
			zoningFile = GetFromGeom.getZoning(pluFile, zipCode);
		}

		this.parcelsFile = selectedParcels;
		this.predicateFile = new File(p.getString("pluPredicate"));
		this.simuFile = new File(parcelsFile.getParentFile(), "simu");
		simuFile.mkdir();

		resultXml = resultxml;
		logXml = logxml;

		// snap datas for lighter geographic files (do not do if it already
		// exists)
		if (!(new File(simuFile.getParentFile(), "/snap/route.shp")).exists()) {
			System.out.println("in snapDatas" + GetFromGeom.getBati(new File(rootfile, "donneeGeographiques")));
			File snapFile = new File(simuFile.getParentFile(), "/snap/");
			snapFile.mkdir();

			buildFile = Vectors.snapDatas(GetFromGeom.getBati(new File(rootfile, "donneeGeographiques")), zoningFile,
					new File(simuFile.getParentFile(), "/snap/batiment.shp"));
			roadFile = Vectors.snapDatas(GetFromGeom.getRoute(new File(rootfile, "donneeGeographiques")), zoningFile,
					new File(simuFile.getParentFile(), "/snap/route.shp"));
			filePrescPonct = Vectors.snapDatas(new File(pluFile, "prescPonctRegroupe.shp"), zoningFile,
					new File(simuFile.getParentFile(), "/snap/prescPonctRegroupe.shp"));
			filePrescLin = Vectors.snapDatas(new File(pluFile, "prescLinRegroupe.shp"), zoningFile,
					new File(simuFile.getParentFile(), "/snap/prescLinRegroupe.shp"));
			filePrescSurf = Vectors.snapDatas(new File(pluFile, "prescSurfRegroupe.shp"), zoningFile,
					new File(simuFile.getParentFile(), "/snap/prescSurfRegroupe.shp"));

		} else {
			buildFile = new File(simuFile.getParentFile(), "/snap/batiment.shp");
			roadFile = new File(simuFile.getParentFile(), "/snap/route.shp");
			filePrescPonct = new File(simuFile.getParentFile(), "/snap/prescPonctRegroupe.shp");
			filePrescLin = new File(simuFile.getParentFile(), "/snap/prescLinRegroupe.shp");
			filePrescSurf = new File(simuFile.getParentFile(), "/snap/prescSurfRegroupe.shp");
		}
		codeFile = new File(pluFile, "/codes/DOC_URBA.shp");
	}

	/**
	 * Constructor to make a new object to run SimPLU3D simulations. Concerns a
	 * single parcel (mainly for the filling method)
	 * 
	 * @param rootfile        : main folder of an artiscale simulation
	 * @param geoFile         : folder for geographic data
	 * @param pluFile         : folder for PLU data
	 * @param selectedParcels : Folder containing the selection of parcels
	 * @param feat            : single parcel to simulate
	 * @param zipcode         : zipcode of the city that is simulated
	 * @param pa              : parameters file
	 * @throws Exception
	 */

	public SimPLUSimulator(File rootfile, File geoFile, File pluFile, File selectedParcels, SimpleFeature feat,
			String zipcode, Parameters pa, List<File> lF, XmlGen resultXml, XmlGen logXml) throws Exception {
		this(rootfile, geoFile, pluFile, selectedParcels, zipcode, pa, lF, resultXml, logXml);
		singleFeat = feat;
		isSingleFeat = true;

	}

	/*
	 * public static List<File> run(File rootFile, File geoFile, File pluFile, File
	 * parcelfiles, String zipcode, Parameters p) throws Exception { SimPLUSimulator
	 * SPLUS = new SimPLUSimulator(rootFile, geoFile, pluFile, parcelfiles, null,
	 * zipcode, p); return SPLUS.run(); }
	 */

	/**
	 * Run a SimPLU simulation on a single parcel
	 * 
	 * @param f main folder
	 * @param p
	 * @return
	 * @throws Exception
	 */
	public File runOneSim(int numParcel) throws Exception {
		// The file that store the results
		File featFile = new File(simuFile, "tmp.shp");
		DefaultFeatureCollection tmp = new DefaultFeatureCollection();
		tmp.add(singleFeat);
		Vectors.exportSFC(tmp.collection(), featFile);

		// SimPLU3D-rules geographic loader
		Environnement env = LoaderSHP.load(simuFile, codeFile, zoningFile, featFile, roadFile, buildFile,
				filePrescPonct, filePrescLin, filePrescSurf, null);

		// Prescription setting
		IFeatureCollection<Prescription> prescriptions = env.getPrescriptions();
		IFeatureCollection<Prescription> prescriptionUse = PrescriptionPreparator.preparePrescription(prescriptions, p);

		// Simulatino on one parcel
		File out = runSimulation(env, numParcel, p, prescriptionUse);
		featFile.delete();
		return out;
	}

	/**
	 * Run a SimPLU3D simulation on all the parcel stored in the parcelFile's
	 * SimpleFeatureCollection
	 * 
	 * @return a list of shapefile containing the simulated buildings
	 * @throws Exception
	 */
	public List<File> run() throws Exception {

		// Loading of configuration file that contains sampling space
		// information and simulated annealing configuration

		// System.out.println(filePrescLin);
		Environnement env = LoaderSHP.load(simuFile, codeFile, zoningFile, parcelsFile, roadFile, buildFile,
				filePrescPonct, filePrescLin, filePrescSurf, null);

		// Prescription setting
		IFeatureCollection<Prescription> prescriptions = env.getPrescriptions();
		IFeatureCollection<Prescription> prescriptionUse = PrescriptionPreparator.preparePrescription(prescriptions, p);

		boolean association = ZoneRulesAssociation.associate(env, predicateFile);

		if (!association) {
			System.out.println("Association between rules and UrbanZone failed");
			return null;
		}

		List<File> listBatiSimu = new ArrayList<File>();

		// We run a simulation for each bPU with a different file for each bPU
		// @TODO : on garde 1 fichier par bPU ? Tu t'en sors après ou c'est pas trop
		// dur
		// ?
		int nbBPU = env.getBpU().size();
		for (int i = 0; i < nbBPU; i++) {
			p = Parameters.unmarshall(lF);
			File file = runSimulation(env, i, p, prescriptionUse);
			if (file != null) {
				listBatiSimu.add(file);
			}

		}
		// Not results
		if (listBatiSimu.isEmpty()) {
			System.out.println(
					"&&&&&&&&&&&&&& Aucun bâtiment n'a été simulé pour la commune " + zipCode + " &&&&&&&&&&&&&&");
			System.exit(1);
		}

		return listBatiSimu;
	}

	/**
	 * Simulation for the ie bPU
	 * 
	 * @param env
	 * @param i
	 * @param p
	 * @param prescriptionUse the prescriptions in Use prepared with
	 *                        PrescriptionPreparator
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "deprecation", "deprecation" })
	public File runSimulation(Environnement env, int i, Parameters p, IFeatureCollection<Prescription> prescriptionUse)
			throws Exception {

		BasicPropertyUnit bPU = env.getBpU().get(i);

		// List ID Parcelle to Simulate is not empty
		if (!ID_PARCELLE_TO_SIMULATE.isEmpty()) {
			// We check if the code is in the list
			if (!ID_PARCELLE_TO_SIMULATE.contains(bPU.getCadastralParcels().get(0).getCode())) {
				return null;
			}
		}

		System.out.println("Parcelle code : " + bPU.getCadastralParcels().get(0).getCode());
		resultXml.beginBalise("parcel-" + bPU.getCadastralParcels().get(0).getCode());
		logXml.beginBalise("parcel-" + bPU.getCadastralParcels().get(0).getCode());

		// si on lance une simulation avec une seule parcelle décrite dans
		// l'environnement, son numéro sera 0 mais le numéro de la parcelle sera
		// conservé
		//
		// @TODO : c'est pas un peu bizarre ça ? Il suffit pas juste de passer 0
		// comme
		// paramètres pour i ?
		// Je pense qu'il faudrait virer le isSingleFeat
		if (isSingleFeat) {
			bPU = env.getBpU().get(0);
		}

		// Instantiation of the sampler
		OptimisedBuildingsCuboidFinalDirectRejection oCB = new OptimisedBuildingsCuboidFinalDirectRejection();

		CommonPredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> pred = null;

		// According to the case, different prediactes may be used
		// Do we consider 1 regualtion by parcel or one by subParcel ?
		if (!USE_DIFFERENT_REGULATION_FOR_ONE_PARCEL || bPU.getCadastralParcels().get(0).getSubParcels().size() < 2) {
			// In this mod there is only one regulation for the entire BPU
			pred = preparePredicateOneRegulation(bPU, p, prescriptionUse, env);

		} else {
			pred = preparePredicateOneRegulationBySubParcel(bPU, p, prescriptionUse, env);
		}

		if (!(pred.isCanBeSimulated())) {
			System.out.println("Parcel is overlapped by graphical prescriptions");
			return null;
		}
		logXml.addLine("usedPredicate", pred.toString());
		// We compute the parcel area
		Double areaParcels = bPU.getCadastralParcels().stream().mapToDouble(x -> x.getArea()).sum();

		// Run of the optimisation on a parcel with the predicate
		GraphConfiguration<Cuboid> cc = oCB.process(bPU, new SimpluParametersXML(p), env, 1, pred);

		IFeatureCollection<IFeature> iFeat3D = new FT_FeatureCollection<>();
		for (GraphVertex<Cuboid> v : cc.getGraph().vertexSet()) {
			IFeature feat = new DefaultFeature(v.getValue().generated3DGeom());
			iFeat3D.add(feat);
		}

		List<Cuboid> cubes = LoaderCuboid.loadFromCollection(iFeat3D);
		SDPCalc surfGen = new SDPCalc();
		double formTot = surfGen.process(cubes);

		// Witting the output
		IFeatureCollection<IFeature> iFeatC = new FT_FeatureCollection<>();

		// For all generated boxes
		for (GraphVertex<Cuboid> v : cc.getGraph().vertexSet()) {

			// Output feature with generated geometry
			// IFeature feat = new
			// DefaultFeature(v.getValue().generated3DGeom());

			IFeature feat = new DefaultFeature(v.getValue().getFootprint());

			// We write some attributes

			AttributeManager.addAttribute(feat, "Longueur", Math.max(v.getValue().length, v.getValue().width),
					"Double");
			AttributeManager.addAttribute(feat, "Largeur", Math.min(v.getValue().length, v.getValue().width), "Double");
			AttributeManager.addAttribute(feat, "Hauteur", v.getValue().height, "Double");
			AttributeManager.addAttribute(feat, "Rotation", v.getValue().orientation, "Double");
			AttributeManager.addAttribute(feat, "SurfaceBox", feat.getGeom().area(), "Double");
			AttributeManager.addAttribute(feat, "SDPShon", formTot * 0.8, "Double");
			AttributeManager.addAttribute(feat, "areaParcel", areaParcels, "Double");
			AttributeManager.addAttribute(feat, "num", i, "Integer");
			iFeatC.add(feat);
		}

		// TODO Prendre la shon (calcul dans
		// simplu3d.experiments.openmole.diversity ? non, c'est la shob et pas
		// la shon !! je suis ingénieur en génie civil que diable. Je ne peux
		// pas me permettre de ne pas prendre en compte un des seuls trucs que
		// je peux sortir de mes quatre ans d'étude pour cette these..!)

		// méthode de calcul d'air simpliste

		File output = new File(simuFile, "out-parcelle_" + i + ".shp");
		logXml.addLine("OutputBuildingPath", output.getCanonicalPath());
		// while (output.exists()) {
		// output = new File(simuFile, "out-parcelle_" + compteurOutput +
		// ".shp");
		// compteurOutput = compteurOutput + 1;
		// }
		output.getParentFile().mkdirs();
		// TODO merge of the iFeatC objects

		ShapefileWriter.write(iFeatC, output.toString(), CRS.decode("EPSG:2154"));

		if (!output.exists()) {
			output = null;
		}
		resultXml.endBalise("parcel-" + bPU.getCadastralParcels().get(0).getCode());
		logXml.endBalise("parcel-" + bPU.getCadastralParcels().get(0).getCode());
		return output;
	}

	private CommonPredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> preparePredicateOneRegulationBySubParcel(
			BasicPropertyUnit bPU, Parameters p2, IFeatureCollection<Prescription> prescriptionUse, Environnement env)
			throws Exception {
		// Instantiation of the rule checker
		// @TODO : ou est-ce qu'on paramétrise le aligne ?
		MultiplePredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> pred = new MultiplePredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>>(
				bPU, true, p2, prescriptionUse, env);

		return pred;
	}

	private static PredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> preparePredicateOneRegulation(
			BasicPropertyUnit bPU, Parameters p, IFeatureCollection<Prescription> prescriptionUse, Environnement env)
			throws Exception {
		List<SubParcel> sP = bPU.getCadastralParcels().get(0).getSubParcels();
		// We sort the subparcel to get the biffests
		sP.sort(new Comparator<SubParcel>() {
			@Override
			public int compare(SubParcel o1, SubParcel o2) {
				return Double.compare(o1.getArea(), o2.getArea());
			}
		});
		SubParcel sPBiggest = sP.get(sP.size() - 1);
		System.out.println("Regulation code : " + sPBiggest.getUrbaZone().getLibelle());

		ArtiScalesRegulation regle = (ArtiScalesRegulation) sPBiggest.getUrbaZone().getZoneRegulation();

		// Instantiation of the rule checker
		// TODO : ou est-ce qu'on paramétrise le aligne ?
		PredicateArtiScales<Cuboid, GraphConfiguration<Cuboid>, BirthDeathModification<Cuboid>> pred = new PredicateArtiScales<>(
				bPU, true, regle, p, prescriptionUse, env);

		return pred;

	}

	/**
	 * Class used to fill a parcel file containing multiple parcels with buildings
	 * simulated with SimPLU
	 * 
	 * @param rootFile            : main file of the ArtiScales's simulation
	 * @param geoFile             : file containing geographical informations
	 * @param pluFile             : file containnin
	 * @param selectedParcels
	 * @param missingHousingUnits
	 * @param zipcode
	 * @param p
	 * @return
	 * @throws Exception
	 */
	protected static int fillSelectedParcels(File rootFile, File geoFile, File pluFile, File selectedParcels,
			int missingHousingUnits, String zipcode, Parameters p, List<File> lF, XmlGen resultxml, XmlGen logxml)
			throws Exception {
		// Itérateurs sur les parcelles où l'on peut construire
		ShapefileDataStore parcelDS = new ShapefileDataStore(selectedParcels.toURI().toURL());
		SimpleFeatureIterator iterator = parcelDS.getFeatureSource().getFeatures().features();

		try {
			// Tant qu'il y a besoin de logements et qu'il y a des parcelles
			// disponibles
			while (missingHousingUnits > 0 && iterator.hasNext()) {
				SimpleFeature sinlgeParcel = iterator.next();
				// On créer un nouveau simulateur
				SimPLUSimulator simPLUsimu = new SimPLUSimulator(rootFile, geoFile, pluFile, selectedParcels,
						sinlgeParcel, zipcode, p, lF, resultxml, logxml);
				// On lance la simulation
				File batiSimulatedFile = simPLUsimu.runOneSim((int) sinlgeParcel.getAttribute("num"));

				// On met à jour le compteur du nombre de logements avec
				// l'indicateur
				// buildingToHousehold

				BuildingToHousehold bTH = new BuildingToHousehold(batiSimulatedFile, p);
				System.out.println("--- missingHousingUnits  " + missingHousingUnits);

				missingHousingUnits = missingHousingUnits - bTH.run();
				if (!iterator.hasNext()) {
					System.out.println(" STILL MISSING : " + missingHousingUnits + " HOUSING UNITS");
				}
			}
		} finally {
			iterator.close();
		}
		// On fusionne les sorties des simulations (c'est plus pratique)
		VectorFct.mergeBatis(new File(selectedParcels.getParentFile(), "simu0"));

		parcelDS.dispose();
		return missingHousingUnits;
	}

}
