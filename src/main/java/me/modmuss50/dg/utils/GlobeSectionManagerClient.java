package me.modmuss50.dg.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class GlobeSectionManagerClient {

	private static Int2ObjectMap<GlobeSection> selectionMap = new Int2ObjectArrayMap<>();

	private static final GlobeSection EMPTY = new GlobeSection();

	public static GlobeSection getGlobeSection(int globeID) {
		if (!selectionMap.containsKey(globeID)) {
			return EMPTY;
		}
		return selectionMap.get(globeID);
	}

	public static void provideGlobeSectionUpdate(int globeID, GlobeSection globeSection) {
		selectionMap.put(globeID, globeSection);
	}


}
