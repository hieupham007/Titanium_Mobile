/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@Kroll.proxy(creatableInModule=MapModule.class, propertyAccessors = {
	TiC.PROPERTY_SUBTITLE,
	TiC.PROPERTY_TITLE,
	TiC.PROPERTY_LATITUDE,
	TiC.PROPERTY_LONGITUDE,
	TiC.PROPERTY_DRAGGABLE
})
public class AnnotationProxy extends KrollProxy
{
	private static final String TAG = "AnnotationProxy";

	private MarkerOptions markerOptions;
	private Marker marker;

	public AnnotationProxy()
	{
		super();
		
		markerOptions = new MarkerOptions();
	}

	public AnnotationProxy(TiContext tiContext)
	{
		this();
	}

	@Override
	protected KrollDict getLangConversionTable() {
		KrollDict table = new KrollDict();
		table.put(TiC.PROPERTY_SUBTITLE, TiC.PROPERTY_SUBTITLEID);
		table.put(TiC.PROPERTY_TITLE, TiC.PROPERTY_TITLEID);
		return table;
	}

	public void processOptions() {
		double longitude = 0;
		double latitude = 0;
		if (hasProperty(TiC.PROPERTY_LONGITUDE)) {
			longitude = TiConvert.toDouble(getProperty(TiC.PROPERTY_LONGITUDE));
		}
		if (hasProperty(TiC.PROPERTY_LATITUDE)) {
			latitude = TiConvert.toDouble(getProperty(TiC.PROPERTY_LATITUDE));
		}

		LatLng position = new LatLng(latitude, longitude);
		markerOptions.position(position);
		
		if (hasProperty(TiC.PROPERTY_TITLE)) {
			markerOptions.title(TiConvert.toString(getProperty(TiC.PROPERTY_TITLE)));
		}
		if (hasProperty(TiC.PROPERTY_SUBTITLE)) {
			markerOptions.snippet(TiConvert.toString(getProperty(TiC.PROPERTY_SUBTITLE)));
		}
		if (hasProperty(TiC.PROPERTY_DRAGGABLE)) {
			markerOptions.draggable(TiConvert.toBoolean(getProperty(TiC.PROPERTY_DRAGGABLE)));
		}
		
	}
	
	public MarkerOptions getMarkerOptions() {
		return markerOptions;
	}
	
	public void setMarker(Marker m) {
		marker = m;
	}
	
	public Marker getMarker() {
		return marker;
	}
	
}