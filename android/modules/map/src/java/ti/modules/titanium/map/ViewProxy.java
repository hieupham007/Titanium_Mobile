/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.map;


import java.util.ArrayList;

import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.os.Message;

@Kroll.proxy(creatableInModule = MapModule.class, propertyAccessors = {
	TiC.PROPERTY_USER_LOCATION,
	TiC.PROPERTY_MAP_TYPE,
	TiC.PROPERTY_REGION,
	TiC.PROPERTY_TRAFFIC,
	TiC.PROPERTY_ANNOTATIONS
})
public class ViewProxy extends TiViewProxy 
{
	private static final String TAG = "MapViewProxy";
	
	private static final int MSG_FIRST_ID = TiViewProxy.MSG_LAST_ID + 1;
	
	private static final int MSG_ADD_ANNOTATION = MSG_FIRST_ID + 500;
	private static final int MSG_ADD_ANNOTATIONS = MSG_FIRST_ID + 501;
	
	private static final int MSG_REMOVE_ANNOTATION = MSG_FIRST_ID + 502;
	private static final int MSG_REMOVE_ALL_ANNOTATIONS = MSG_FIRST_ID + 503;


	
	private ArrayList<AnnotationProxy> preloadAnnotations;
	
	public ViewProxy() {
		super();
		preloadAnnotations = new ArrayList<AnnotationProxy>();
	}
	
	public TiUIView createView(Activity activity) {
		return new TiUIMapView(this, activity);
	}

	public boolean handleMessage(Message msg) 
	{
		AsyncResult result = null;
		switch (msg.what) {

		case MSG_ADD_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			handleAddAnnotation((AnnotationProxy)result.getArg());
			result.setResult(null);
			return true;
		}
		
		case MSG_ADD_ANNOTATIONS: {
			result = (AsyncResult) msg.obj;
			handleAddAnnotations((AnnotationProxy[])result.getArg());
			result.setResult(null);
			return true;
		}

		case MSG_REMOVE_ANNOTATION: {
			result = (AsyncResult) msg.obj;
			handleRemoveAnnotation(result.getArg());
			result.setResult(null);
			return true;
		}
		
		case MSG_REMOVE_ALL_ANNOTATIONS: {
			result = (AsyncResult) msg.obj;
			handleRemoveAllAnnotations();
			result.setResult(null);
			return true;
		}

		default : {
			return super.handleMessage(msg);
		}
		}
	}
	

	public ArrayList<AnnotationProxy> getPreloadAnnotations() {
		return preloadAnnotations;
	}

	@Kroll.method
	public void addAnnotation(AnnotationProxy annotation) {
		if (TiApplication.isUIThread()) {
			handleAddAnnotation(annotation);
		} else {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_ADD_ANNOTATION), annotation);
		}
	}
	
	private void handleAddAnnotation(AnnotationProxy annotation) {
		TiUIView view = peekView();
		if (view instanceof TiUIMapView) {
			TiUIMapView mapView = (TiUIMapView) peekView();
			if (mapView.getMap() != null) {
				mapView.addAnnotation(annotation);

			} else {
				preloadAnnotations.add(annotation);
			}
		} else {
			preloadAnnotations.add(annotation);
		}
	}
	
	@Kroll.method
	public void addAnnotations(AnnotationProxy[] annotations) {
		if (TiApplication.isUIThread()) {
			handleAddAnnotations(annotations);
		} else {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_ADD_ANNOTATIONS), annotations);
		}
	}
	
	private void handleAddAnnotations(AnnotationProxy[] annotations) {
		for (int i = 0; i < annotations.length; i++) {
			handleAddAnnotation(annotations[i]);
		}
	}
	
	@Kroll.method
	public void removeAllAnnotations() {
		if (TiApplication.isUIThread()) {
			handleRemoveAllAnnotations();
		} else {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_REMOVE_ALL_ANNOTATIONS));
		}
	}
	
	public void handleRemoveAllAnnotations() {
		TiUIView view = peekView();
		if (view instanceof TiUIMapView) {
			TiUIMapView mapView = (TiUIMapView) peekView();
			mapView.removeAllAnnotations();
		}
	}
	
	@Kroll.method
	public void removeAnnotation(Object annotation) {
		if (!(annotation instanceof AnnotationProxy || annotation instanceof String)) {
			Log.e(TAG, "Unsupported argument type for removeAnnotation");
			return;
		}

		if (TiApplication.isUIThread()) {
			handleRemoveAnnotation(annotation);
		} else {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_REMOVE_ANNOTATION), annotation);
		}
		
	}
	
	public void handleRemoveAnnotation(Object annotation) {
		TiUIView view = peekView();
		if (view instanceof TiUIMapView) {
			TiUIMapView mapView = (TiUIMapView) peekView();
			mapView.removeAnnotation(annotation);
		}
	}
}
