package fr.ign.artiscales.main.map.theseMC.nbHU;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fr.ign.artiscales.main.map.MapRenderer;

public class NbHU extends MapRenderer {
	static String nameMap = "nbHU";
	static String text = "Nombre de logements simulés";

	public NbHU(int imageWidth, int imageHeight, File rootMapstyle, File tomapshp, File outfolder) {
		super(imageWidth, imageHeight, nameMap, text, rootMapstyle, new File(rootMapstyle, "svgModel.svg"), tomapshp, outfolder);
	}

	public static void main(String[] args) throws MalformedURLException, NoSuchAuthorityCodeException, IOException, FactoryException {
		File rootMapStyle = new File("/home/ubuntu/boulot/these/result0308/mapStyle/");
		File outMap = new File("/home/ubuntu/boulot/these/result0308/indic/parcelStat/DDense/variante0/map/");
		outMap.mkdirs();
		MapRenderer mpR = new NbHU(1000, 1000, rootMapStyle,
				new File("/home/ubuntu/boulot/these/result0308/indic/parcelStat/DDense/variante0/commStat.shp"), outMap);
		mpR.renderCityInfo();
		mpR.generateSVG();
	}

}
