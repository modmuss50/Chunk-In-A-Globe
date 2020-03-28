package me.modmuss50.dg.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class GlobeSectionManagerClient {

	private static Int2ObjectMap<GlobeSection> selectionMap = new Int2ObjectArrayMap<>();
	private static Int2ObjectMap<GlobeSection> innerSelectionMap = new Int2ObjectArrayMap<>();

	public static GlobeSection getGlobeSection(int globeID, boolean inner) {
		if (inner) {
			if (!innerSelectionMap.containsKey(globeID)) {
				return new GlobeSection();
			}
			return innerSelectionMap.get(globeID);
		} else {
			if (!selectionMap.containsKey(globeID)) {
				return new GlobeSection();
			}
			return selectionMap.get(globeID);
		}
	}

	public static void provideGlobeSectionUpdate(boolean inner, int globeID, GlobeSection globeSection) {
		if (inner) {
			innerSelectionMap.put(globeID, globeSection);
		} else {
			selectionMap.put(globeID, globeSection);
		}
	}


}
