package ti.modules.titanium.map;

import java.util.HashMap;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.TiConvert;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class Route {
	
	private PolylineOptions options;
	private Polyline route;
	private Object points;
	private float width;
	private int color;
	private String name;
	

	public Route() {
		options = new PolylineOptions();
	}

	
	public void processOptions() {
		processPoints(points);
		options.width(width);
		options.color(color);
		
	}

	public void processPoints(Object points) {
		//multiple points
		if (points instanceof Object[]) {
			Object[] pointsArray = (Object[]) points;
			for (int i = 0; i < pointsArray.length; i++) {
				Object obj = pointsArray[i];
				if (obj instanceof HashMap<?, ?>) {
					HashMap<String, String> point = (HashMap<String, String>) obj;
					LatLng location = new LatLng(TiConvert.toDouble(point.get("latitude")), TiConvert.toDouble(point.get("longitude")));
					options.add(location);
				}
			}
		}
		//single point
		if (points instanceof HashMap) {
			HashMap<String, String> point = (HashMap<String, String>) points;
			LatLng location = new LatLng(TiConvert.toDouble(point.get("latitude")), TiConvert.toDouble(point.get("longitude")));
			options.add(location);
		}
	}
	
	public PolylineOptions getOptions() {
		return options;
	}
	
	public void setRoute(Polyline r) {
		route = r;
	}
	
	public Polyline getRoute() {
		return route;
	}
	
	public void setPoints(Object p) {
		points = p;
	}
	
	public void setColor(int c) {
		color = c;
	}
	
	public void setWidth(float w) {
		width = w;
	}
	
	public void setName(String t) {
		name = t;
	}
	
	public String getName() {
		return name;
	}
	
}
