/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.map;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;

import com.google.android.gms.maps.GoogleMap;

@Kroll.module
public class MapModule extends KrollModule
{

	@Kroll.constant public static final int ANNOTATION_RED = 1;
	@Kroll.constant public static final int ANNOTATION_GREEN = 2;
	@Kroll.constant public static final int ANNOTATION_PURPLE = 3;
	
	@Kroll.constant public static final int NORMAL_TYPE = GoogleMap.MAP_TYPE_NORMAL;
	@Kroll.constant public static final int TERRAIN_TYPE = GoogleMap.MAP_TYPE_TERRAIN;
	@Kroll.constant public static final int SATELLITE_TYPE = GoogleMap.MAP_TYPE_SATELLITE;







	public MapModule()
	{
		super();
	}

	public MapModule(TiContext tiContext)
	{
		this();
	}
}
