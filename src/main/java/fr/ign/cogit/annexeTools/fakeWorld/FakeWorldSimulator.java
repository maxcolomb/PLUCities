package fr.ign.cogit.annexeTools.fakeWorld;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.ign.cogit.SimPLUSimulator;
import fr.ign.cogit.simplu3d.io.feature.AttribNames;
import fr.ign.parameters.Parameters;

public class FakeWorldSimulator {

	public static void main(String[] args) throws Exception {

		//TODO try before push
		// Parent folder with all subfolder
		String absoluteRootFolder = "/tmp/tmp/";

		File rootFolderFile = new File(absoluteRootFolder);

		for (String pathSubFolder : rootFolderFile.list()) {
			
			 if(! pathSubFolder.contains("art71")) { continue; }
			 

			List<File> lF = new ArrayList<>();
			// Line to change to select the right scenario

			String rootParam = SimPLUSimulator.class.getClassLoader().getResource("paramSet/scenarFakeWorldMax/")
					.getPath();

			lF.add(new File(rootParam + "parametreTechnique.xml"));
			lF.add(new File(rootParam + "parametreScenario.xml"));

			Parameters p = Parameters.unmarshall(lF);

			// Rappel de la construction du code :

			// 1/ Basically the parcels are filtered on the code with the
			// following
			// attributes
			// codeDep + codeCom + comAbs + section + numero

			// 2/ Alternatively we can decided to active an attribute (Here id)
			AttribNames.setATT_CODE_PARC("CODE");

			String currentFolder = absoluteRootFolder + pathSubFolder + "/";

			System.out.println(currentFolder);

			p.set("rootFile", currentFolder);
			p.set("selectedParcelFile", currentFolder + "parcelle.shp");
			p.set("geoFile", currentFolder);
			p.set("pluFile", currentFolder);
			p.set("pluPredicate", currentFolder + "predicate.csv");

			String simulOut = currentFolder + "/out/";
			(new File(simulOut)).mkdirs();
			p.set("simu", simulOut);

			// RootFolder
			File rootFolder = new File(p.getString("rootFile"));
			// Selected parcels shapefile
			SimPLUSimulator simplu = new SimPLUSimulator(rootFolder, rootFolder, p);

			simplu.run();
		}

	}

}
