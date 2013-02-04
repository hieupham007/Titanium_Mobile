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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

@Kroll.module
public class MapModule extends KrollModule
{
	
	@Kroll.constant public static final int NORMAL_TYPE = GoogleMap.MAP_TYPE_NORMAL;
	@Kroll.constant public static final int TERRAIN_TYPE = GoogleMap.MAP_TYPE_TERRAIN;
	@Kroll.constant public static final int SATELLITE_TYPE = GoogleMap.MAP_TYPE_SATELLITE;
	
	 public static final String PROPERTY_DRAGGABLE = "draggable";
	 public static final String PROPERTY_POINTS = "points";
	 public static final String PROPERTY_TRAFFIC = "traffic";
	 public static final String PROPERTY_MAP = "map";

	
	@Kroll.constant public static final float ANNOTATION_AZURE = BitmapDescriptorFactory.HUE_AZURE;
	@Kroll.constant public static final float ANNOTATION_BLUE = BitmapDescriptorFactory.HUE_BLUE;
	@Kroll.constant public static final float ANNOTATION_CYAN = BitmapDescriptorFactory.HUE_CYAN;
	@Kroll.constant public static final float ANNOTATION_GREEN = BitmapDescriptorFactory.HUE_GREEN;
	@Kroll.constant public static final float ANNOTATION_MAGENTA = BitmapDescriptorFactory.HUE_MAGENTA;
	@Kroll.constant public static final float ANNOTATION_ORANGE = BitmapDescriptorFactory.HUE_ORANGE;
	@Kroll.constant public static final float ANNOTATION_RED = BitmapDescriptorFactory.HUE_RED;
	@Kroll.constant public static final float ANNOTATION_ROSE = BitmapDescriptorFactory.HUE_ROSE;
	@Kroll.constant public static final float ANNOTATION_VIOLET = BitmapDescriptorFactory.HUE_VIOLET;
	@Kroll.constant public static final float ANNOTATION_YELLOW = BitmapDescriptorFactory.HUE_YELLOW;








	public MapModule()
	{
		super();
	}

	public MapModule(TiContext tiContext)
	{
		this();
	}
}
