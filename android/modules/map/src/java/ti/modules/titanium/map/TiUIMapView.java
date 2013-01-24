package ti.modules.titanium.map;

import java.util.ArrayList;
import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

public class TiUIMapView extends TiUIFragment {

	private GoogleMap map;
	protected boolean animate = false;
	protected boolean preLayout = true;
	protected ArrayList<Marker> markers;

	public static final int MSG_VIEW_CREATED = 600;
	public TiUIMapView(final TiViewProxy proxy, Activity activity) {
		super(proxy, activity);
		markers = new ArrayList<Marker>();
	}

	@Override
	protected Fragment createFragment() {
		return SupportMapFragment.newInstance();
	}

	
	protected void processPreloadAnnotations() {
		ArrayList<AnnotationProxy> annotations = ((ViewProxy)proxy).getPreloadAnnotations();
		for (int i = 0; i < annotations.size(); i++) {
			addAnnotation(annotations.get(i));
		}
	}
	
	protected void onViewCreated() {
		processMapProperties(proxy.getProperties());
		processPreloadAnnotations();
	}

	@Override
	public void processProperties(KrollDict d) {
		super.processProperties(d);

		if (getMap() == null) {
			return;
		}
		processMapProperties(d);
	}
	
	public void processMapProperties(KrollDict d) {

		if (d.containsKey(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocation(d.getBoolean(TiC.PROPERTY_USER_LOCATION));
		}
		if (d.containsKey(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(d.getInt(TiC.PROPERTY_MAP_TYPE));
		}
		if (d.containsKey(TiC.PROPERTY_TRAFFIC)) {
			setTrafficEnabled(d.getBoolean(TiC.PROPERTY_TRAFFIC));
		}
		if (d.containsKey(TiC.PROPERTY_ANIMATE)) {
			animate = d.getBoolean(TiC.PROPERTY_ANIMATE);
		}
		if (d.containsKey(TiC.PROPERTY_REGION)) {
			updateCamera(d.getKrollDict(TiC.PROPERTY_REGION));
		}
		if (d.containsKey(TiC.PROPERTY_ANNOTATIONS)) {
			Object[] annotations = (Object[]) d.get(TiC.PROPERTY_ANNOTATIONS);
			addAnnotations(annotations);
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) {
		
		if (key.equals(TiC.PROPERTY_USER_LOCATION)) {
			setUserLocation(TiConvert.toBoolean(newValue));
		}
		if (key.equals(TiC.PROPERTY_MAP_TYPE)) {
			setMapType(TiConvert.toInt(newValue));
		}
		if (key.equals(TiC.PROPERTY_REGION)) {
			updateCamera((HashMap) newValue);
		}
		if (key.equals(TiC.PROPERTY_TRAFFIC)) {
			setTrafficEnabled(TiConvert.toBoolean(newValue));
		}
		if (key.equals(TiC.PROPERTY_ANIMATE)) {
			animate = TiConvert.toBoolean(newValue);
		}
		if (key.equals(TiC.PROPERTY_ANNOTATIONS)) {
			updateAnnotations((Object[]) newValue);
		}
		super.propertyChanged(key, oldValue, newValue, proxy);

	}

	public GoogleMap getMap() {
		if (map == null) {
			map = ((SupportMapFragment) getFragment()).getMap();
		}

		return map;		
	}

	protected void setUserLocation(boolean enabled) {
		getMap().setMyLocationEnabled(enabled);
	}
	
	protected void setMapType(int type) {
		getMap().setMapType(type);
	}
	
	protected void setTrafficEnabled(boolean enabled) {
		getMap().setTrafficEnabled(enabled);
	}
	
	protected void updateCamera(HashMap<String, Object> dict) {
		double longitude = 0;
		double longitudeDelta = 0;
		double latitude = 0;
		double latitudeDelta = 0;

		if (dict.containsKey(TiC.PROPERTY_LATITUDE)) {
			latitude = TiConvert.toDouble(dict, TiC.PROPERTY_LATITUDE);
		}
		if (dict.containsKey(TiC.PROPERTY_LONGITUDE)) {
			longitude = TiConvert.toDouble(dict, TiC.PROPERTY_LONGITUDE);
		}
		
		CameraPosition.Builder cameraBuilder = new CameraPosition.Builder();
		LatLng location = new LatLng(latitude, longitude);
		cameraBuilder.target(location);
		
		if (dict.containsKey(TiC.PROPERTY_LATITUDE_DELTA)) {
			latitudeDelta = TiConvert.toDouble(dict, TiC.PROPERTY_LATITUDE_DELTA);
		}

		if (dict.containsKey(TiC.PROPERTY_LONGITUDE_DELTA)) {
			longitudeDelta = TiConvert.toDouble(dict, TiC.PROPERTY_LONGITUDE_DELTA);
		}
	 
		if (latitudeDelta != 0 && longitudeDelta != 0) {
			LatLng northeast = new LatLng(latitude + (latitudeDelta / 2.0), longitude + (longitudeDelta / 2.0));
			LatLng southwest = new LatLng(latitude - (latitudeDelta / 2.0), longitude - (longitudeDelta / 2.0));

			final LatLngBounds bounds = new LatLngBounds(southwest, northeast);
			if (preLayout) {
				map.setOnCameraChangeListener(new OnCameraChangeListener() {

					@Override
					public void onCameraChange(CameraPosition arg0) {
						moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
						// Remove listener to prevent position reset on camera move.
						map.setOnCameraChangeListener(null);
						preLayout = false;
					}

				});
				return;
			} else {
				moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
				return;
			}
		}
		
		CameraPosition position = cameraBuilder.build();
		CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
		moveCamera(camUpdate);
	}
	
	protected void moveCamera(CameraUpdate camUpdate)
	{
		if (animate) {
			map.animateCamera(camUpdate);
		} else {
			map.moveCamera(camUpdate);
		}
	}
	
	protected void addAnnotation(AnnotationProxy annotation) {
		annotation.processOptions();
		//add annotation to map view
		Marker m = map.addMarker(annotation.getMarkerOptions());
		annotation.setMarker(m);
		markers.add(m);
	}

	protected void addAnnotations(Object[] annotations) 
	{
		for (int i = 0; i < annotations.length; i++) {
			Object obj = annotations[i];
			if (obj instanceof AnnotationProxy) {
				AnnotationProxy annotation = (AnnotationProxy) obj;
				addAnnotation(annotation);
			}
		}
	}
	
	protected void updateAnnotations(Object[] annotations)
	{
		//First, remove old annotations from map
		removeAllAnnotations();
		//Then we add new annotations to the map
		addAnnotations(annotations);
	}
	
	protected void removeAllAnnotations() {
		for (int i = 0; i < markers.size(); i++) {
			markers.get(i).remove();
			markers.remove(i);
		}
	}
	
	protected void removeAnnotation(Object annotation) {
		if (annotation instanceof AnnotationProxy) {
			Marker removedMarker = ((AnnotationProxy)annotation).getMarker();
			for (int i = 0; i < markers.size(); i++) {
				Marker marker = markers.get(i);
				if (removedMarker.equals(marker)) {
					marker.remove();
					markers.remove(i);
					return;
				}
			}
		}
		if (annotation instanceof String) {
			String title = (String)annotation;
			for (int i = 0; i < markers.size(); i++) {
				Marker marker = markers.get(i);
				if (marker.getTitle().equals(title)) {
					marker.remove();
					markers.remove(i);
					return;
				}
			}
		}
	}

}