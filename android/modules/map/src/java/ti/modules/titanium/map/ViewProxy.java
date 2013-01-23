/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.map;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;

@Kroll.proxy(creatableInModule = MapModule.class, propertyAccessors = {
	TiC.PROPERTY_USER_LOCATION,
	TiC.PROPERTY_MAP_TYPE,
	TiC.PROPERTY_REGION,
	TiC.PROPERTY_TRAFFIC
})
public class ViewProxy extends TiViewProxy 
{
	public TiUIView createView(Activity activity) {
		return new TiUIMapView(this, activity);
	}


	
}
