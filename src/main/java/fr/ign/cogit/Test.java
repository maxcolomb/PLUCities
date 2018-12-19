package fr.ign.cogit;

import java.io.File;
import java.util.List;

import org.geotools.referencing.CRS;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.convert.FromGeomToLineString;
import fr.ign.cogit.geoxygene.convert.FromGeomToSurface;
import fr.ign.cogit.geoxygene.sig3d.calculation.parcelDecomposition.FlagParcelDecomposition;
import fr.ign.cogit.geoxygene.sig3d.calculation.parcelDecomposition.OBBBlockDecomposition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.conversion.WktGeOxygene;
import fr.ign.cogit.util.GetFromGeom;

public class Test {

	public static void main(String[] args) throws Exception {

		
		
		DirectPosition.PRECISION = 5;

		String strMuliPol1 = "POLYGON ((928720.68999999994412065 6693812.88999999966472387, 928743.07999999995809048 6693831.30999999959021807, 928744.2099999999627471 6693832.09999999962747097, 928808.76000000000931323 6693891.04999999981373549, 928881.61999999999534339 6693958.33000000007450581, 928945.92000000004190952 6693887.20000000018626451, 928934.19999999995343387 6693876.53000000026077032, 928896.47999999998137355 6693823.58999999985098839, 928857.52000000001862645 6693775.74000000022351742, 928818.25 6693744.78000000026077032, 928789.82999999995809048 6693713.32000000029802322, 928754.28000000002793968 6693764.34999999962747097, 928726.84999999997671694 6693745.57000000029802322, 928722.09999999997671694 6693752.37999999988824129, 928749.56000000005587935 6693771.28000000026077032, 928727.72999999998137355 6693802.74000000022351742, 928720.68999999994412065 6693812.88999999966472387))";
		String strMuliPol2 = "POLYGON ((928554.27000000001862645 6693894.55999999959021807, 928568.97999999998137355 6693909.84999999962747097, 928575.32999999995809048 6693916.59999999962747097, 928576.33999999996740371 6693917.92999999970197678, 928576.76000000000931323 6693919.59999999962747097, 928576.93999999994412065 6693921.04999999981373549, 928576.98999999999068677 6693924.88999999966472387, 928576.85999999998603016 6693927.51999999955296516, 928576.55000000004656613 6693930.15000000037252903, 928575.92000000004190952 6693933.11000000033527613, 928575.17000000004190952 6693935.73000000044703484, 928574.26000000000931323 6693938.30999999959021807, 928572.76000000000931323 6693941.38999999966472387, 928569.41000000003259629 6693946.75, 928570.66000000003259629 6693947.9599999999627471, 928599.2900000000372529 6693975.55999999959021807, 928612.11999999999534339 6693987.7900000000372529, 928624.55000000004656613 6693977.65000000037252903, 928699.59999999997671694 6693914.73000000044703484, 928715.57999999995809048 6693901.03000000026077032, 928727.58999999996740371 6693911.9599999999627471, 928741.43999999994412065 6693924.59999999962747097, 928755.19999999995343387 6693937.30999999959021807, 928808.76000000000931323 6693891.04999999981373549, 928744.2099999999627471 6693832.09999999962747097, 928743.07999999995809048 6693831.30999999959021807, 928720.68999999994412065 6693812.88999999966472387, 928714.39000000001396984 6693807.75999999977648258, 928689.83999999996740371 6693787.91000000014901161, 928679.67000000004190952 6693779.5400000000372529, 928676.01000000000931323 6693782.45000000018626451, 928664.06999999994877726 6693794.80999999959021807, 928661.98999999999068677 6693796.98000000044703484, 928659.19999999995343387 6693799.96999999973922968, 928652.11999999999534339 6693807.41999999992549419, 928644.91000000003259629 6693814.73000000044703484, 928637.9599999999627471 6693822.28000000026077032, 928637.68000000005122274 6693822.0400000000372529, 928636.06000000005587935 6693820.5400000000372529, 928621.44999999995343387 6693838.13999999966472387, 928619.75 6693839.79999999981373549, 928575.34999999997671694 6693877.33999999985098839, 928570.2099999999627471 6693881.5400000000372529, 928565.61999999999534339 6693885.32000000029802322, 928554.27000000001862645 6693894.55999999959021807))";

		String strMuliPol3 = "POLYGON ((928859.60999999998603016 6694119.92999999970197678, 928863.16000000003259629 6694123.21999999973922968, 928879.96999999997206032 6694140.51999999955296516, 928877.15000000002328306 6694146.17999999970197678, 928888.73999999999068677 6694156.58999999985098839, 928890.76000000000931323 6694154.33000000007450581, 928905.02000000001862645 6694166.46999999973922968, 928907.92000000004190952 6694169.00999999977648258, 928915.09999999997671694 6694175.91000000014901161, 928921.78000000002793968 6694183.29999999981373549, 928928.63000000000465661 6694179.59999999962747097, 928946.60999999998603016 6694170.36000000033527613, 928959.68000000005122274 6694146.94000000040978193, 928971.75 6694132.71999999973922968, 928972.84999999997671694 6694115.36000000033527613, 928974.88000000000465661 6694098.91999999992549419, 928977.98999999999068677 6694093.92999999970197678, 928987.80000000004656613 6694080.74000000022351742, 928994.78000000002793968 6694086.44000000040978193, 928990.59999999997671694 6694091.2900000000372529, 928999.23999999999068677 6694098.90000000037252903, 929008.07999999995809048 6694093.83000000007450581, 929011.52000000001862645 6694095.0400000000372529, 929015.38000000000465661 6694104.17999999970197678, 929020.66000000003259629 6694106.94000000040978193, 929030.71999999997206032 6694100.16999999992549419, 929033.32999999995809048 6694109.58000000007450581, 929038.25 6694113.28000000026077032, 929044.07999999995809048 6694108.12999999988824129, 929065.18999999994412065 6694115.51999999955296516, 929085.43999999994412065 6694122.37999999988824129, 929112.5 6694096.54999999981373549, 929132.03000000002793968 6694076.80999999959021807, 929130.32999999995809048 6694061.16999999992549419, 929119.18000000005122274 6694048.04999999981373549, 929107.5 6694035.37000000011175871, 929095.85999999998603016 6694023.63999999966472387, 929073.5 6694005.66000000014901161, 929069.5 6694002.79999999981373549, 929059.67000000004190952 6693998.70000000018626451, 929050.10999999998603016 6693994.00999999977648258, 929040.85999999998603016 6693988.7099999999627471, 929029.15000000002328306 6693981.07000000029802322, 929005.82999999995809048 6693962.16000000014901161, 928985.9599999999627471 6693942.30999999959021807, 928981.90000000002328306 6693937.40000000037252903, 928962.91000000003259629 6693911.24000000022351742, 928956.83999999996740371 6693896.33999999985098839, 928945.92000000004190952 6693887.20000000018626451, 928881.61999999999534339 6693958.33000000007450581, 928808.76000000000931323 6693891.04999999981373549, 928755.19999999995343387 6693937.30999999959021807, 928741.43999999994412065 6693924.59999999962747097, 928727.58999999996740371 6693911.9599999999627471, 928715.57999999995809048 6693901.03000000026077032, 928699.59999999997671694 6693914.73000000044703484, 928624.55000000004656613 6693977.65000000037252903, 928612.11999999999534339 6693987.7900000000372529, 928599.2900000000372529 6693975.55999999959021807, 928570.66000000003259629 6693947.9599999999627471, 928569.41000000003259629 6693946.75, 928567.07999999995809048 6693948.65000000037252903, 928562.71999999997206032 6693952.55999999959021807, 928558.51000000000931323 6693956.17999999970197678, 928584.13000000000465661 6694010.2900000000372529, 928588.78000000002793968 6694020.41000000014901161, 928592.39000000001396984 6694030.0400000000372529, 928595.35999999998603016 6694039.86000000033527613, 928599.44999999995343387 6694058.2099999999627471, 928606.81999999994877726 6694112.32000000029802322, 928610.27000000001862645 6694126.37000000011175871, 928618.09999999997671694 6694152.55999999959021807, 928619.15000000002328306 6694158.9599999999627471, 928622.44999999995343387 6694166, 928700.4599999999627471 6694092.74000000022351742, 928740.63000000000465661 6694128.55999999959021807, 928747.53000000002793968 6694120.36000000033527613, 928754.06000000005587935 6694119.62999999988824129, 928769.09999999997671694 6694098.61000000033527613, 928783.21999999997206032 6694082.40000000037252903, 928786.55000000004656613 6694084.08000000007450581, 928803.32999999995809048 6694089.2900000000372529, 928828.58999999996740371 6694098.62000000011175871, 928835.5 6694104.80999999959021807, 928859.60999999998603016 6694119.92999999970197678))";

		String strMuliPol4 = "POLYGON ((932004.7099999999627471 6691119.69000000040978193, 931990.34999999997671694 6691107.33999999985098839, 931982.22999999998137355 6691100.46999999973922968, 931948.17000000004190952 6691131.19000000040978193, 931905.0400000000372529 6691170.13999999966472387, 931920.14000000001396984 6691190.59999999962747097, 931958.31999999994877726 6691158.53000000026077032, 931973.28000000002793968 6691146.0400000000372529, 931979.52000000001862645 6691140.79999999981373549, 932004.7099999999627471 6691119.69000000040978193))";
		
		
		
		double roadEpsilon = 0;
		double noise = 0;
		double maximalArea = 1000;
		double maximalWidth = 10;

		// Exterior from the UrbanBlock if necessary or null
		IMultiCurve<IOrientableCurve> imC = null;
		// Roads are created for this number of decomposition level
		int decompositionLevelWithRoad = 3;
		// Road width
		double roadWidth = 5.0;
		// Boolean forceRoadaccess
		boolean forceRoadAccess = false;

		IPolygon pol1 = (IPolygon) FromGeomToSurface.convertGeom(WktGeOxygene.makeGeOxygene(strMuliPol1)).get(0);
		IPolygon pol2 = (IPolygon) FromGeomToSurface.convertGeom(WktGeOxygene.makeGeOxygene(strMuliPol2)).get(0);
		IPolygon pol3 = (IPolygon) FromGeomToSurface.convertGeom(WktGeOxygene.makeGeOxygene(strMuliPol3)).get(0);
		IPolygon pol4 = (IPolygon) FromGeomToSurface.convertGeom(WktGeOxygene.makeGeOxygene(strMuliPol4)).get(0);

		File dataGeo = new File("/home/yo/Documents/these/ArtiScales/dataGeo/");
		String inputUrbanBlock = GetFromGeom.getIlots(dataGeo)
				.getAbsolutePath();
		IFeatureCollection<IFeature> featC = ShapefileReader.read(inputUrbanBlock);
		List<IOrientableCurve> lOC = FromGeomToLineString.convert(featC.get(0).getGeom());
		IMultiCurve<IOrientableCurve> iMultiCurve = new GM_MultiCurve<>(lOC);

		System.out.println("pour le polygone 1 : ");
		// 1
		OBBBlockDecomposition decomposition1 = new OBBBlockDecomposition(pol1, maximalArea, maximalWidth, roadEpsilon,
				iMultiCurve, roadWidth, forceRoadAccess);
		IFeatureCollection<IFeature> featColl1 = decomposition1.decompParcel(noise);

		ShapefileWriter.write(featColl1, "/tmp/tmp1.shp", CRS.decode("EPSG:2154"));
		System.out.println();
		// 2
		System.out.println("pour le polygone 2 : ");

		OBBBlockDecomposition decomposition2 = new OBBBlockDecomposition(pol2, maximalArea, maximalWidth, roadEpsilon,
				iMultiCurve, roadWidth, forceRoadAccess);
		IFeatureCollection<IFeature> featColl2 = decomposition2.decompParcel(noise);

		ShapefileWriter.write(featColl2, "/tmp/tmp2.shp", CRS.decode("EPSG:2154"));

		// 3
		System.out.println("pour le polygone 3 : ");

		OBBBlockDecomposition decomposition3 = new OBBBlockDecomposition(pol3, maximalArea, maximalWidth, roadEpsilon,
				iMultiCurve, roadWidth, forceRoadAccess);
		IFeatureCollection<IFeature> featColl3 = decomposition3.decompParcel(noise);

		ShapefileWriter.write(featColl3, "/tmp/tmp3.shp", CRS.decode("EPSG:2154"));
		System.out.println();

		//4
		System.out.println("pour le polygone 4 : ");

		OBBBlockDecomposition decomposition4 = new OBBBlockDecomposition(pol4, maximalArea, maximalWidth, roadEpsilon,
				iMultiCurve, roadWidth, forceRoadAccess);
		IFeatureCollection<IFeature> featColl4 = decomposition4.decompParcel(noise);

		if ( (decomposition4.howManyIt(pol4, noise, forceRoadAccess)-2)<=0) {
			System.out.println("we in "); 
			FlagParcelDecomposition flagDecomp = new FlagParcelDecomposition( pol4, ShapefileReader.read(GetFromGeom.getBuild(dataGeo).getAbsolutePath()), maximalArea, maximalWidth, roadWidth,iMultiCurve);
			 featColl4 = flagDecomp.decompParcel(0);
		}
		ShapefileWriter.write(featColl4, "/tmp/tmp4.shp", CRS.decode("EPSG:2154"));
		System.out.println();
	}

}
