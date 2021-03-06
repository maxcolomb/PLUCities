package fr.ign.artiscales.main.indicators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import au.com.bytecode.opencsv.CSVReader;
import fr.ign.artiscales.main.map.MapRenderer;
import fr.ign.artiscales.main.map.theseMC.SurfParcelFailedMap;
import fr.ign.artiscales.main.map.theseMC.SurfParcelSimulatedMap;
import fr.ign.artiscales.main.util.FromGeom;
import fr.ign.artiscales.main.util.SimuTool;
import fr.ign.artiscales.pm.parcelFunction.ParcelGetter;
import fr.ign.artiscales.pm.parcelFunction.ParcelState;
import fr.ign.artiscales.tools.geoToolsFunctions.vectors.Collec;
import fr.ign.cogit.simplu3d.util.SimpluParametersJSON;

public class ParcelStat extends Indicators {

	File parcelOGFile;
	int nbParcelIgnored, nbParcelSimulated, nbParcelSimulFailed, nbParcelSimulatedU, nbParcelSimulFailedU, nbParcelSimulatedAU, nbParcelSimulFailedAU,
			nbParcelSimulatedNC, nbParcelSimulFailedNC, nbParcelSimulatedCentre, nbParcelSimulFailedCentre, nbParcelSimulatedBanlieue,
			nbParcelSimulFailedBanlieue, nbParcelSimulatedPeriUrb, nbParcelSimulFailedPeriUrb, nbParcelSimulatedRural, nbParcelSimulFailedRural,
			simuledFromOriginal, simuledFromDensification, simuledFromTotalRecomp, simuledFromZoneCut, simuledFromPartRecomp, failedFromOriginal,
			failedFromZoneCut, failedFromDensification, failedFromTotalRecomp, failedFromPartRecomp;
	double surfParcelIgnored, surfParcelSimulated, surfParcelSimulFailed, surfParcelSimulatedU, surfParcelSimulFailedU, surfParcelSimulatedAU,
			surfParcelSimulFailedAU, surfParcelSimulatedNC, surfParcelSimulFailedNC, surfParcelSimulatedCentre, surfParcelSimulFailedCentre,
			surfParcelSimulatedBanlieue, surfParcelSimulFailedBanlieue, surfParcelSimulatedPeriUrb, surfParcelSimulFailedPeriUrb,
			surfParcelSimulatedRural, surfParcelSimulFailedRural;
	// surfaceSDPParcelle, surfaceEmpriseParcelle;
	SimpleFeatureCollection preciseParcelCollection;
	String firstLine;
	private static String indicName = "parcelStat";

	public ParcelStat(SimpluParametersJSON p, File rootFile, String scenarName, String variantName) throws Exception {
		super(p, rootFile, scenarName, variantName, getIndicName());
		setBasics();
	}

	public ParcelStat(SimpluParametersJSON p, File rootFile, String scenarName, String variantName, List<String> listCities) throws Exception {
		super(p, rootFile, scenarName, variantName, getIndicName(), listCities);
		setBasics();
	}

	private void setBasics() throws FileNotFoundException {
		parcelOGFile = FromGeom.getParcel(new File(getRootFile(), "dataGeo"));
		firstLine = "INSEE,nb_parcel_simulated,nb_parcel_simu_failed,surf_parcel_ignored,surf_parcel_simulated,surf_parcel_simulFailed,nbParcelSimulatedU,nbParcelSimulFailedU,nbParcelSimulatedAU,nbParcelSimulFailedAU,nbParcelSimulatedNC,nbParcelSimulFailedNC,nbParcelSimulatedCentre,nbParcelSimulFailedCentre,nbParcelSimulatedBanlieue,nbParcelSimulFailedBanlieue,nbParcelSimulatedPeriUrb,nbParcelSimulFailedPeriUrb,nbParcelSimulatedRural,nbParcelSimulFailedRural,surfParcelSimulatedU,surfParcelSimulFailedU,surfParcelSimulatedAU,surfParcelSimulFailedAU,surfParcelSimulatedNC,surfParcelSimulFailedNC,surfParcelSimulatedCentre,surfParcelSimulFailedCentre,surfParcelSimulatedBanlieue,surfParcelSimulFailedBanlieue,surfParcelSimulatedPeriUrb,surfParcelSimulFailedPeriUrb,surfParcelSimulatedRural,surfParcelSimulFailedRural,simuledFromOriginal,simuledFromDensification,simuledFromTotalRecomp,simuledFromPartRecomp,simuledFromZoneCut,failedFromOriginal,failedFromDensification,failedFromTotalRecomp,failedFromPartRecomp,failedFromZoneCut";
	}

	public static void main(String[] args) throws Exception {
		File rootFile = new File("./result2903/");
		File rootParam = new File(rootFile, "paramFolder");
		List<File> lF = new ArrayList<>();
		// String[] scenarios = { "CDense" };
		String[] scenarios = { "CPeuDense", "DDense", "DPeuDense" };

		String variant = "base";
		// indicName = "parcelStat-RNU";

		for (String scenario : scenarios) {
			lF.add(new File(rootParam, "/paramSet/" + scenario + "/parameterTechnic.json"));
			lF.add(new File(rootParam, "/paramSet/" + scenario + "/parameterScenario.json"));
			// for (File f : (new File(rootFile, "SimPLUDepot/" + scenario + "/")).listFiles()) {
			// String variant = f.getName();
			SimpluParametersJSON p = new SimpluParametersJSON(lF);
			run(p, rootFile, scenario, variant);
		}
	}
	// }

	// public static void main(String[] args) throws Exception {
	// File rootFile = new File("./result2903/");
	// File rootParam = new File(rootFile, "paramFolder");
	// List<File> lF = new ArrayList<>();
	// String[] scenarios = { "CDense", "CPeuDense", "DDense", "DPeuDense" };
	// String[] typeDocs = { "RNU", "PLU", "CC" };
	//
	// String variant = "base";
	// for (String typeDoc : typeDocs) {
	// List<String> listDoc = FromGeom.getZipByTypeDoc(new File(rootFile, "dataRegulation"), typeDoc);
	// setIndicName("ParcelStat-" + typeDoc);
	//
	// for (String scenario : scenarios) {
	// System.out.println("run " + scenario + " variant: " + variant + " doc ? " + typeDoc);
	//
	// lF.add(new File(rootParam, "/paramSet/" + scenario + "/parameterTechnic.json"));
	// lF.add(new File(rootParam, "/paramSet/" + scenario + "/parameterScenario.json"));
	// // for (File f : (new File(rootFile, "SimPLUDepot/" + scenario + "/")).listFiles()) {
	// // String variant = f.getName();
	// SimpluParametersJSON p = new SimpluParametersJSON(lF);
	// run(p, rootFile, scenario, variant, listDoc);
	// }
	// }
	// }

	public static void run(SimpluParametersJSON p, File rootFile, String scenario, String variant) throws Exception {
		run(p, rootFile, scenario, variant, null);
	}

	public static void run(SimpluParametersJSON p, File rootFile, String scenario, String variant, List<String> listCities) throws Exception {

		// ParcelStat parc = new ParcelStat(p, rootFile, scenario, f.getName());
		ParcelStat parc;

		if (listCities != null && !listCities.isEmpty()) {
			parc = new ParcelStat(p, rootFile, scenario, variant);
		} else {
			parc = new ParcelStat(p, rootFile, scenario, variant, listCities);
		}
		SimpleFeatureCollection parcelStatSHP = parc.markSimuledParcels();
		parc.caclulateStatParcel();
		parc.writeLine("AllZone", "genStat");
		parc.setCountToZero();
		List<String> listInsee = FromGeom.getInsee(new File(parc.getRootFile(), "/dataGeo/old/communities.shp"), "DEPCOM");

		for (String city : listInsee) {
			SimpleFeatureCollection commParcel = ParcelGetter.getParcelByCommunityCode(parcelStatSHP, city);
			System.out.println("city " + city);
			parc.calculateStatParcel(commParcel);
			parc.writeLine(city, "genStat");
			parc.toString();
			parc.setCountToZero();
		}
		parc.setCommStatFile(parc.joinStatParcelToCommunities());
		parc.createMap(parc);
		parc.createGraph(new File(parc.getIndicFolder(), "genStat.csv"));
		// }
	}

	public void createMap(ParcelStat parc) throws IOException, NoSuchAuthorityCodeException, FactoryException {
		List<MapRenderer> allOfTheMaps = new ArrayList<MapRenderer>();
		MapRenderer surfParcelSimulatedMap = new SurfParcelSimulatedMap(1000, 1000, new File(parc.getRootFile(), "mapStyle"), getCommStatFile(),
				parc.getMapDepotFolder());
		allOfTheMaps.add(surfParcelSimulatedMap);
		MapRenderer surfParcelFailedMap = new SurfParcelFailedMap(1000, 1000, parc.getMapStyle(), getCommStatFile(), parc.getMapDepotFolder());
		allOfTheMaps.add(surfParcelFailedMap);

		for (MapRenderer map : allOfTheMaps) {
			map.renderCityInfo();
			map.generateSVG();
		}
	}

	public void createGraph(File distrib) throws IOException {
		// Number
		String[] xTypeSimulated = { "nbParcelSimulatedCentre", "nbParcelSimulatedBanlieue", "nbParcelSimulatedPeriUrb", "nbParcelSimulatedRural" };
		String[] xTypeSimulFailed = { "nbParcelSimulFailedCentre", "nbParcelSimulFailedBanlieue", "nbParcelSimulFailedPeriUrb",
				"nbParcelSimulFailedRural" };
		String[][] xType = { xTypeSimulated, xTypeSimulFailed };
		makeGraphDouble(distrib, getGraphDepotFolder(), SimuTool.makeWordPHDable(scenarName) + " - Variante : " + variantName, xType, "typologie",
				"Nombre de parcelles");

		String[] xZoneSimulated = { "nbParcelSimulatedU", "nbParcelSimulatedAU", "nbParcelSimulatedNC" };
		String[] xZoneSimulFailed = { "nbParcelSimulFailedU", "nbParcelSimulFailedAU", "nbParcelSimulFailedNC" };
		String[][] xZone = { xZoneSimulated, xZoneSimulFailed };
		makeGraphDouble(distrib, getGraphDepotFolder(), SimuTool.makeWordPHDable(scenarName) + " - Variante : " + variantName, xZone, "type de zone",
				"Nombre de parcelles");

		String[] xRecompSimulated = { "simuledFromOriginal", "simuledFromDensification", "simuledFromTotalRecomp", "simuledFromZoneCut" };
		String[] xRecompSimulFailed = { "failedFromOriginal", "failedFromDensification", "failedFromTotalRecomp", "failedFromZoneCut" };
		String[][] xRecomp = { xRecompSimulated, xRecompSimulFailed };
		makeGraphDouble(distrib, getGraphDepotFolder(), SimuTool.makeWordPHDable(scenarName) + " - Variante : " + variantName, xRecomp,
				"Processus de recomposition des parcelles", "Nombre de parcelles");

		// Surface
		String[] xTypeSimulatedSurf = { "surfParcelSimulatedCentre", "surfParcelSimulatedBanlieue", "surfParcelSimulatedPeriUrb",
				"surfParcelSimulatedRural" };
		String[] xTypeSimulFailedSurf = { "surfParcelSimulFailedCentre", "surfParcelSimulFailedBanlieue", "surfParcelSimulFailedPeriUrb",
				"surfParcelSimulatedRural" };
		String[][] xTypeSurf = { xTypeSimulatedSurf, xTypeSimulFailedSurf };
		makeGraphDouble(distrib, getGraphDepotFolder(), SimuTool.makeWordPHDable(scenarName) + " - Variante : " + variantName, xTypeSurf, "typologie",
				"Surface de parcelles (km²)");

		String[] xZoneSimulatedSurf = { "surfParcelSimulatedU", "surfParcelSimulatedAU", "surfParcelSimulatedNC" };
		String[] xZoneSimulFailedSurf = { "surfParcelSimulFailedU", "surfParcelSimulFailedAU", "surfParcelSimulFailedNC" };
		String[][] xZoneSurf = { xZoneSimulatedSurf, xZoneSimulFailedSurf };
		makeGraphDouble(distrib, getGraphDepotFolder(), SimuTool.makeWordPHDable(scenarName) + " - Variante : " + variantName, xZoneSurf,
				"type de zone", "Surface de parcelles (km²)");
	}

	public static void makeGraphDouble(File csv, File graphDepotFile, String title, String[][] xes, String xTitle, String yTitle) throws IOException {
		// Create Chart
		CategoryChart chart = new CategoryChartBuilder().width(600).height(600).title(title).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
		int count = 0;
		for (String[] x : xes) {
			List<String> label = new ArrayList<String>();
			List<Double> yS = new ArrayList<Double>();
			for (String s : x) {
				label.add(makeLabelPHDable(s));
				// SeriesData csvData= CSVImporter.getSeriesDataFromCSVFile(csv, DataOrientation.Columns, s, y);
				CSVReader csvR = new CSVReader(new FileReader(csv));
				int iX = 0;
				int iCode = 0;
				String[] fLine = csvR.readNext();
				// get them first line
				for (int i = 0; i < fLine.length; i++) {
					if (fLine[i].equals(s))
						iX = i;
					if (fLine[i].equals("INSEE"))
						iCode = i;
				}

				for (String[] lines : csvR.readAll()) {
					if (lines[iCode].equals("AllZone")) {
						yS.add(Double.valueOf(lines[iX]));
						break;
					}
				}
				csvR.close();
			}
			String simulOrNot = "Simulée";
			if (count == 1) {
				simulOrNot = "Simulation échouée";
			}
			chart.addSeries(simulOrNot, label, yS);
			count++;
		}

		// chart.addSeries(yTitle, label, yS);
		// Histogram histogram1 ;
		// Histogram histogram2 ;
		// chart.addSeries("histogram 1", histogram1.getxAxisData(), histogram1.getyAxisData());
		//
		// chart.addSeries("histogram 2", histogram2.getxAxisData(), histogram2.getyAxisData());
		// Customize Chart
		// chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
		chart.getStyler().setLegendVisible(true);
		chart.getStyler().setHasAnnotations(true);
		chart.getStyler().setXAxisLabelRotation(45);
		BitmapEncoder.saveBitmap(chart, graphDepotFile + "/" + SimuTool.makeCamelWordOutOfPhrases(xTitle + yTitle), BitmapFormat.PNG);
		chart.getStyler().setLegendVisible(false);
		BitmapEncoder.saveBitmap(chart, graphDepotFile + "/" + SimuTool.makeCamelWordOutOfPhrases(xTitle + yTitle) + "-headless", BitmapFormat.PNG);

		// new SwingWrapper(chart).displayChart();
	}

	public static void makeGraph(File csv, File graphDepotFile, String title, String[] x, String xTitle, String yTitle) throws IOException {
		// Create Chart
		CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(title).xAxisTitle(xTitle).yAxisTitle(yTitle).build();
		List<String> label = new ArrayList<String>();
		List<Double> yS = new ArrayList<Double>();
		for (String s : x) {
			label.add(makeLabelPHDable(s));
			// SeriesData csvData= CSVImporter.getSeriesDataFromCSVFile(csv, DataOrientation.Columns, s, y);
			CSVReader csvR = new CSVReader(new FileReader(csv));
			int iX = 0;
			int iCode = 0;
			String[] fLine = csvR.readNext();
			// get them first line
			for (int i = 0; i < fLine.length; i++) {
				if (fLine[i].equals(s))
					iX = i;
				if (fLine[i].equals("code"))
					iCode = i;
			}
			for (String[] lines : csvR.readAll()) {
				if (lines[iCode].equals("ALLLL")) {
					yS.add(Double.valueOf(lines[iX]));
					break;
				}
			}
			csvR.close();
		}

		chart.addSeries(yTitle, label, yS);

		// Customize Chart
		// chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setHasAnnotations(true);
		chart.getStyler().setXAxisLabelRotation(45);
		BitmapEncoder.saveBitmap(chart, graphDepotFile + "/" + x[0], BitmapFormat.PNG);
		// new SwingWrapper(chart).displayChart();
	}

	public String writeLine(String geoEntity, String nameFile) throws IOException {
		String result = geoEntity + "," + nbParcelSimulated + "," + nbParcelSimulFailed + "," + round(surfParcelIgnored / 1000000, 3) + ","
				+ round(surfParcelSimulated / 1000000, 3) + "," + round(surfParcelSimulFailed / 1000000, 3) + "," + nbParcelSimulatedU + ","
				+ nbParcelSimulFailedU + "," + nbParcelSimulatedAU + "," + nbParcelSimulFailedAU + "," + nbParcelSimulatedNC + ","
				+ nbParcelSimulFailedNC + "," + nbParcelSimulatedCentre + "," + nbParcelSimulFailedCentre + "," + nbParcelSimulatedBanlieue + ","
				+ nbParcelSimulFailedBanlieue + "," + nbParcelSimulatedPeriUrb + "," + nbParcelSimulFailedPeriUrb + "," + nbParcelSimulatedRural + ","
				+ nbParcelSimulFailedRural + "," + round(surfParcelSimulatedU / 1000000, 3) + "," + round(surfParcelSimulFailedU / 1000000, 3) + ","
				+ round(surfParcelSimulatedAU / 1000000, 3) + "," + round(surfParcelSimulFailedAU / 1000000, 3) + ","
				+ round(surfParcelSimulatedNC / 1000000, 3) + "," + round(surfParcelSimulFailedNC / 1000000, 3) + ","
				+ round(surfParcelSimulatedCentre / 1000000, 3) + "," + round(surfParcelSimulFailedCentre / 1000000, 3) + ","
				+ round(surfParcelSimulatedBanlieue / 1000000, 3) + "," + round(surfParcelSimulFailedBanlieue / 1000000, 3) + ","
				+ round(surfParcelSimulatedPeriUrb / 1000000, 3) + "," + round(surfParcelSimulFailedPeriUrb / 1000000, 3) + ","
				+ round(surfParcelSimulatedRural / 1000000, 3) + "," + round(surfParcelSimulFailedRural / 1000000, 3) + "," + simuledFromOriginal
				+ "," + simuledFromDensification + "," + simuledFromTotalRecomp + "," + simuledFromPartRecomp + "," + simuledFromZoneCut + ","
				+ failedFromOriginal + "," + failedFromDensification + "," + failedFromTotalRecomp + "," + failedFromPartRecomp + ","
				+ failedFromZoneCut;

		// + "," + surfaceSDPParcelle + "," + surfaceEmpriseParcelle;
		toGenCSV(nameFile, firstLine, result);
		return result;
	}

	public void caclulateStatParcel() throws IOException {
		File parcelStatShapeFile = new File(getIndicFolder(), "parcelStatted.shp");
		if (!parcelStatShapeFile.exists()) {
			markSimuledParcels();
		}
		ShapefileDataStore parcelSimuledSDS = new ShapefileDataStore(parcelStatShapeFile.toURI().toURL());
		SimpleFeatureCollection parcelSimuled = parcelSimuledSDS.getFeatureSource().getFeatures();
		calculateStatParcel(parcelSimuled);
		parcelSimuledSDS.dispose();
	}

	public void calculateStatParcel(SimpleFeatureCollection parcelSimuled) throws IOException {

		SimpleFeatureIterator itParcel = parcelSimuled.features();
		// nbParcelSimulatedCentre, nbParcelSimulFailedCentre, nbParcelSimulatedPeriUrb,
		// nbParcelSimulFailedPeriUrb, nbParcelSimulatedRural, nbParcelSimulFailedRural
		try {
			while (itParcel.hasNext()) {
				SimpleFeature ft = itParcel.next();
				double area = ((Geometry) ft.getDefaultGeometry()).getArea();
				String code = (String) ft.getAttribute("CODE");
				switch ((String) ft.getAttribute("DoWeSimul")) {
				case "noSelection":
					surfParcelIgnored = surfParcelIgnored + area;
					nbParcelIgnored++;
					break;
				case "simulated":
					surfParcelSimulated = surfParcelSimulated + area;
					nbParcelSimulated++;
					if (ft.getAttribute("U").equals("T") || ft.getAttribute("U").equals(true)) {
						surfParcelSimulatedU = surfParcelSimulatedU + area;
						nbParcelSimulatedU++;
					} else if (ft.getAttribute("AU").equals("T") || ft.getAttribute("AU").equals(true)) {
						surfParcelSimulatedAU = surfParcelSimulatedAU + area;
						nbParcelSimulatedAU++;
					} else if (ft.getAttribute("NC").equals("T") || ft.getAttribute("NC").equals(true)) {
						surfParcelSimulatedNC = surfParcelSimulatedNC + area;
						nbParcelSimulatedNC++;
					}
					// System.out.println(FromGeom.getTypo(FromGeom.getCommunitiesIris(new File(rootFile, "dataGeo")), (Geometry) ft.getDefaultGeometry()));
					switch (ParcelState.parcelInTypo(FromGeom.getCommunitiesIris(new File(getRootFile(), "dataGeo")), ft)) {
					case "rural":
						nbParcelSimulatedRural++;
						surfParcelSimulatedRural = surfParcelSimulatedRural + area;

						break;
					case "periUrbain":
						nbParcelSimulatedPeriUrb++;
						surfParcelSimulatedPeriUrb = surfParcelSimulatedPeriUrb + area;

						break;
					case "centre":
						nbParcelSimulatedCentre++;
						surfParcelSimulatedCentre = surfParcelSimulatedCentre + area;

						break;
					case "banlieue":
						nbParcelSimulatedBanlieue++;
						surfParcelSimulatedBanlieue = surfParcelSimulatedBanlieue + area;

						break;
					}

					// determine the production source of the parcel
					if (code.contains("div")) {
						simuledFromDensification++;
					} else if (code.contains("New") && code.contains("Section")) {
						simuledFromTotalRecomp++;
					} else if (code.contains("newSection") && code.contains("Natural")) {
						simuledFromPartRecomp++;
					} else if (code.contains("bis")) {
						simuledFromZoneCut++;
					} else {
						simuledFromOriginal++;
					}

					break;
				case "simuFailed":
					surfParcelSimulFailed = surfParcelSimulFailed + area;
					nbParcelSimulFailed++;
					if (ft.getAttribute("U").equals("T") || ft.getAttribute("U").equals(true)) {
						nbParcelSimulFailedU++;
						surfParcelSimulFailedU = surfParcelSimulFailedU + area;
					} else if (ft.getAttribute("AU").equals("T") || ft.getAttribute("AU").equals(true)) {
						nbParcelSimulFailedAU++;
						surfParcelSimulFailedAU = surfParcelSimulFailedAU + area;
					} else if (ft.getAttribute("NC").equals("T") || ft.getAttribute("NC").equals(true)) {
						nbParcelSimulFailedNC++;
						surfParcelSimulFailedNC = surfParcelSimulFailedNC + area;
					}
					switch (ParcelState.parcelInTypo(FromGeom.getCommunitiesIris(new File(getRootFile(), "dataGeo")), ft)) {
					case "rural":
						nbParcelSimulFailedRural++;
						surfParcelSimulFailedRural = surfParcelSimulFailedRural + area;
						break;
					case "periUrbain":
						nbParcelSimulFailedPeriUrb++;
						surfParcelSimulFailedPeriUrb = surfParcelSimulFailedPeriUrb + area;
						break;
					case "centre":
						nbParcelSimulFailedCentre++;
						surfParcelSimulFailedCentre = surfParcelSimulFailedCentre + area;
						break;
					case "banlieue":
						nbParcelSimulFailedBanlieue++;
						surfParcelSimulFailedBanlieue = surfParcelSimulFailedBanlieue + area;
						break;
					}
					// determine the production source of the parcel
					if (code.contains("div")) {
						failedFromDensification++;
					} else if (code.contains("New") && code.contains("Section")) {
						failedFromTotalRecomp++;
					} else if (code.contains("newSection") && code.contains("Natural")) {
						failedFromPartRecomp++;
					} else if (code.contains("bis")) {
						failedFromZoneCut++;
					} else {
						failedFromOriginal++;
					}
					break;
				}

			}
		} catch (Exception problem) {
			problem.printStackTrace();
		} finally {
			itParcel.close();
		}
	}

	/**
	 * mark the parcels that has been selected or not (noSelection) and where a building has been simulated (simulated) or not (simuFailed)
	 * 
	 * @return the newly marked parcel shapeFile
	 * @throws IOException
	 */
	public SimpleFeatureCollection markSimuledParcels() throws IOException {

		ShapefileDataStore parcelSimuledSDS = new ShapefileDataStore(getParcelDepotGenFile().toURI().toURL());
		SimpleFeatureCollection parcelSimuled = parcelSimuledSDS.getFeatureSource().getFeatures();
		SimpleFeatureIterator itParcel = parcelSimuled.features();

		DefaultFeatureCollection result = new DefaultFeatureCollection();

		try {
			while (itParcel.hasNext()) {
				SimpleFeature ft = itParcel.next();
				String field = "noSelection";
				if (isParcelReallySimulated(ft)) {
					field = "simulated";
				} else if (ft.getAttribute("DoWeSimul").equals("true")) {
					field = "simuFailed";
				}
				ft.setAttribute("DoWeSimul", field);
				result.add(ft);
			}
		} catch (Exception problem) {
			problem.printStackTrace();
		} finally {
			itParcel.close();
		}
		Collec.exportSFC(result, new File(getIndicFolder(), "parcelStatted.shp"));

		parcelSimuledSDS.dispose();

		return DataUtilities.collection(result.collection());
	}

	/**
	 * for each parcel, set the already existing field "IsBuild" if a new building has been simulated on this parcel
	 * 
	 * @throws Exception
	 */
	public boolean isParcelReallySimulated(SimpleFeature parcel) throws Exception {
		File simuBuildFiles = new File(super.getRootFile() + "/SimPLUDepot" + "/" + scenarName + "/" + variantName + "/TotBatSimuFill.shp");
		ShapefileDataStore batiSDS = new ShapefileDataStore(simuBuildFiles.toURI().toURL());
		SimpleFeatureCollection batiColl = batiSDS.getFeatureSource().getFeatures();

		Geometry parcelGeometry = (Geometry) parcel.getDefaultGeometry();

		SimpleFeatureCollection snapBatiCollec = Collec.snapDatas(batiColl, parcelGeometry);
		SimpleFeatureIterator batiFeaturesIt = snapBatiCollec.features();
		try {
			while (batiFeaturesIt.hasNext()) {
				SimpleFeature bati = batiFeaturesIt.next();
				if (((Geometry) bati.getDefaultGeometry()).buffer(-0.5).intersects(parcelGeometry)) {
					return true;
				}
			}
		} catch (Exception problem) {
			problem.printStackTrace();
		} finally {
			batiFeaturesIt.close();
		}
		batiSDS.dispose();
		return false;
	}

	public void setCountToZero() {
		simuledFromZoneCut = failedFromZoneCut = failedFromOriginal = failedFromDensification = failedFromTotalRecomp = failedFromPartRecomp = simuledFromOriginal = simuledFromDensification = simuledFromTotalRecomp = simuledFromPartRecomp = nbParcelIgnored = nbParcelSimulated = nbParcelSimulFailed = nbParcelSimulatedU = nbParcelSimulFailedU = nbParcelSimulatedAU = nbParcelSimulFailedAU = nbParcelSimulatedNC = nbParcelSimulFailedNC = nbParcelSimulatedCentre = nbParcelSimulFailedCentre = nbParcelSimulatedBanlieue = nbParcelSimulFailedBanlieue = nbParcelSimulatedPeriUrb = nbParcelSimulFailedPeriUrb = nbParcelSimulatedRural = nbParcelSimulFailedRural = 0;
		surfParcelIgnored = surfParcelSimulated = surfParcelSimulFailed = surfParcelSimulatedU = surfParcelSimulFailedU = surfParcelSimulatedAU = surfParcelSimulFailedAU = surfParcelSimulatedNC = surfParcelSimulFailedNC = surfParcelSimulatedCentre = surfParcelSimulFailedCentre = surfParcelSimulatedBanlieue = surfParcelSimulFailedBanlieue = surfParcelSimulatedPeriUrb = surfParcelSimulFailedPeriUrb = surfParcelSimulatedRural = surfParcelSimulFailedRural = 0;
		// surfaceSDPParcelle = surfaceEmpriseParcelle =
	}
	public static String getIndicName() {
		return indicName;
	}

	public static void setIndicName(String indicName) {
		ParcelStat.indicName = indicName;
	}
}
