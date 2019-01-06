package fr.ign.cogit.rules.regulation.buildingType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.util.GetFromGeom;
import fr.ign.cogit.util.VectorFct;
import fr.ign.parameters.Parameters;

public class MultipleRepartitionBuildingType extends RepartitionBuildingType {
	HashMap<String, List<String>> parcelsInZone;

	public MultipleRepartitionBuildingType(Parameters p, File parcelFile) throws NoSuchElementException, Exception {
		super(p, parcelFile);
		p = getRepartition(p, parcelles.get(0));

		parcelsInZone = new HashMap<String, List<String>>();
		for (IFeature parcelle : parcelles) {

			String bigZone = GetFromGeom.affectToZoneAndTypo(p, parcelle, true);

			if (parcelsInZone.containsKey(bigZone)) {
				List<String> tmpList = parcelsInZone.get(bigZone);
				tmpList.add((String) parcelle.getAttribute("CODE"));
				parcelsInZone.put(bigZone, tmpList);
			} else {
				List<String> tmpList = new ArrayList<String>();
				tmpList.add((String) parcelle.getAttribute("CODE"));
				parcelsInZone.put(bigZone, tmpList);
			}
		}

	}

	public BuildingType rangeInterest(double eval, String codeParcel, Parameters p)
			throws NoSuchElementException, Exception {

		List<String> parcelsWanted = new ArrayList<String>();

		for (List<String> parcels : parcelsInZone.values()) {
			if (parcels.contains(codeParcel)) {
				parcelsWanted = parcels;
			}
		}

		IFeatureCollection<IFeature> parcelRepart = VectorFct.getParcelByCode(parcelles, parcelsWanted);

		System.out.println("parcelRepart size : " + parcelRepart.size());
		makeRepart(p, parcelRepart);

		return rangeInterest(eval);
	}

}