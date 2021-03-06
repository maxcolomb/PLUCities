package fr.ign.artiscales.main.map.theseMC.compVariant;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fr.ign.artiscales.main.map.MapRenderer;

public class CVNbBat extends MapRenderer {
	static String nameMap = "CVNbBat";

	public CVNbBat(int imageWidth, int imageHeight, File rootMapstyle, File tomapshp, File outfolder, String solo) {
		super(imageWidth, imageHeight, nameMap,
				"Coefficient de variation de la distribution du nombre de bâtiments simulées pour les variantes " + solo + " d'un scénario",
				rootMapstyle, new File(rootMapstyle, "svgModelHUObj.svg"), tomapshp, outfolder);
	}

	public static void main(String[] args) throws MalformedURLException, NoSuchAuthorityCodeException, IOException, FactoryException {
		File rootMapStyle = new File("/home/ubuntu/boulot/these/result2903/mapStyle/");
		File outMap = new File("/home/ubuntu/boulot/these/result2903/indic/compVariant/CDense/mapDepot");
		outMap.mkdirs();
		MapRenderer mpR = new CVNbBat(1000, 1000, rootMapStyle,
				new File("/home/ubuntu/boulot/these/result2903/indic/compVariant/CDense/commStatBTH.shp"), outMap, "");
		mpR.renderCityInfo();
		mpR.generateSVG();
	}

}
