package fr.ign.artiscales.main.rules.regulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.simplu3d.model.AbstractBuilding;
import fr.ign.cogit.simplu3d.model.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.model.Building;
import fr.ign.cogit.simplu3d.model.CadastralParcel;
import fr.ign.cogit.simplu3d.model.Environnement;
import fr.ign.cogit.simplu3d.model.ParcelBoundary;
import fr.ign.cogit.simplu3d.model.ParcelBoundarySide;
import fr.ign.cogit.simplu3d.model.ParcelBoundaryType;

public class Alignements {

	public boolean hasAlignement = false;

	private BasicPropertyUnit currentBPU;

	private Environnement env;

	private AlignementType type;

	public enum AlignementType {
		ART7112(0), ART713(1), ART6(2), NONE(99);

		private int value;

		private AlignementType(int type) {
			value = type;
		}

		public int getValueType() {
			return value;
		}

	}

	public Alignements(List<ArtiScalesRegulation> allRegulation, BasicPropertyUnit currentBPU, Environnement env) {
		// Currently we only take the first
		ArtiScalesRegulation regulation = allRegulation.get(0);
		this.currentBPU = currentBPU;
		this.env = env;

		if (regulation.getArt_71() == 1 || regulation.getArt_71() == 2) {
			hasAlignement = true;
			this.type = AlignementType.ART7112;

		}

		if (regulation.getArt_71() == 3) {
			hasAlignement = true;
			this.type = AlignementType.ART713;
		}

		if (regulation.getArt_6_defaut().equals("0") || regulation.getArt_6_type() == 10) {
			hasAlignement = true;
			this.type = AlignementType.ART6;
		}
	}

	public IGeometry[] getRightSide() {
		return getSide(ParcelBoundarySide.RIGHT);
	}

	public IGeometry[] getLeftSide() {
		return getSide(ParcelBoundarySide.LEFT);
	}

	public AlignementType getType() {
		return type;
	}

	public IGeometry[] getSideWithBuilding() {

		List<IGeometry> lGeom = new ArrayList<>();

		// For each parcel
		for (CadastralParcel cO : currentBPU.getCadastralParcels()) {
			// For each boundary
			boucleboundary: for (ParcelBoundary boundary : cO.getBoundariesByType(ParcelBoundaryType.LAT)) {

				// We check if there is some buildings near to the limit
				Collection<AbstractBuilding> buildingsSel = env.getBuildings().select(boundary.getGeom().buffer(0.1));

				// We have some buildings do they belong to the current CadastralParcel
				for (AbstractBuilding currentBuilding : buildingsSel) {
					if (currentBuilding instanceof Building) {
						Building build = (Building) currentBuilding;
						// No !!! we add the geometry and go to the next parcel boundary
						if (!build.getbPU().equals(currentBPU)) {
							lGeom.add(boundary.getGeom());
							continue boucleboundary;
						}
					} else {
						System.out.println("Alignements : Unrecognized building class : " + currentBuilding.getClass());
					}
				}
			}
		}

		IGeometry[] geometryArray = new IGeometry[lGeom.size()];
		geometryArray = lGeom.toArray(geometryArray);

		return geometryArray;
	}

	public IGeometry[] getRoadGeom() {

		List<IGeometry> lGeom = new ArrayList<>();

		// For each parcel
		for (CadastralParcel cO : currentBPU.getCadastralParcels()) {
			// For each boundary
			for (ParcelBoundary boundary : cO.getBoundariesByType(ParcelBoundaryType.ROAD)) {

				// Road r = (Road) boundary.getFeatAdj();
				// filtre r.getName()
				lGeom.add(boundary.getGeom());

			}

		}

		IGeometry[] geometryArray = new IGeometry[lGeom.size()];
		geometryArray = lGeom.toArray(geometryArray);

		return geometryArray;

	}

	private IGeometry[] getSide(ParcelBoundarySide side) {

		List<IGeometry> lGeom = new ArrayList<>();

		for (CadastralParcel cO : currentBPU.getCadastralParcels()) {

			List<ParcelBoundary> boundaries = cO.getBoundariesByType(ParcelBoundaryType.LAT);
			for (ParcelBoundary b : boundaries) {
				if (b.getSide().equals(side)) {
					lGeom.add(b.getGeom());
				}
			}
		}

		IGeometry[] geometryArray = new IGeometry[lGeom.size()];
		geometryArray = lGeom.toArray(geometryArray);

		return geometryArray;
	}

	public boolean getHasAlignement() {
		// TODO Auto-generated method stub
		return hasAlignement;
	}

}
