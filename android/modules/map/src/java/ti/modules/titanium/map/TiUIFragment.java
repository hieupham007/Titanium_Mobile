package ti.modules.titanium.map;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class TiUIFragment extends TiUIView implements Handler.Callback {
	private static int viewId = 1000;

	private Fragment fragment;
	
	private Handler handler;

	public TiUIFragment(TiViewProxy proxy, Activity activity) {
		super(proxy);

		TiCompositeLayout container = new TiCompositeLayout(activity, proxy);
		container.setId(viewId++);
		setNativeView(container);

		FragmentManager manager = ((FragmentActivity) activity).getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		fragment = createFragment();
		transaction.add(container.getId(), fragment);
		transaction.commit();
		
		//initialize handler
		handler = new Handler(TiMessenger.getMainMessenger().getLooper(), this);
		//send a msg to skip a cycle to make sure the map view is created and initialized
		handler.obtainMessage().sendToTarget();

	}

	public Fragment getFragment() {
		return fragment;
	}

	@Override
	public boolean handleMessage(Message msg) {
		//we know here that the map view is available, so we process properties
		onViewCreated();
		return true;
	}
	
	protected void onViewCreated() {
		//override in TiUIMapView to handle initialization
	}

	protected abstract Fragment createFragment();
}