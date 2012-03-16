/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.ui.widget;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class TiUIScrollView extends TiUIView {

	public static final int TYPE_VERTICAL = 0;
	public static final int TYPE_HORIZONTAL = 1;

	private static final String SHOW_VERTICAL_SCROLL_INDICATOR = "showVerticalScrollIndicator";
	private static final String SHOW_HORIZONTAL_SCROLL_INDICATOR = "showHorizontalScrollIndicator";
	private static final String LCAT = "TiUIScrollView";
	private static final boolean DBG = TiConfig.LOGD;
	private int offsetX = 0, offsetY = 0;
	private boolean setInitialOffset = false;


	private class TiScrollViewLayout extends TiCompositeLayout
	{
		private static final int AUTO = Integer.MAX_VALUE;
		protected int measuredWidth = 0, measuredHeight = 0;
		private int parentWidth = 0, parentHeight = 0;

		public TiScrollViewLayout(Context context, LayoutArrangement arrangement)
		{
			super(context, arrangement, proxy);
		}

		private LayoutParams getParams(View child)
		{
			return (LayoutParams)child.getLayoutParams();
		}
		
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b)
		{
			super.onLayout(changed, l, t, r, b);
			measuredHeight = measuredWidth = 0;
		}

		public void setParentWidth(int width)
		{
			parentWidth = width;
		}

		public void setParentHeight(int height)
		{
			parentHeight = height;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(Math.max(measuredWidth, getMeasuredWidth()), Math.max(measuredHeight, getMeasuredHeight()));
		}

		private int getContentProperty(String property)
		{
			Object value = getProxy().getProperty(property);
			if (value != null) {
				if (value.equals(TiC.SIZE_AUTO)) {
					return AUTO;
				} else if (value instanceof Number) {
					return ((Number) value).intValue();
				} else {
					int type = 0;
					TiDimension dimension;
					if (TiC.PROPERTY_CONTENT_HEIGHT.equals(property)) {
						type = TiDimension.TYPE_HEIGHT;
					} else if (TiC.PROPERTY_CONTENT_WIDTH.equals(property)) {
						type = TiDimension.TYPE_WIDTH;
					}
					dimension = new TiDimension(value.toString(), type);
					return dimension.getUnits() == TiDimension.COMPLEX_UNIT_AUTO ? AUTO : dimension.getIntValue();
				}
			}
			return AUTO;
		}

		private int calculateAbsoluteRight(View child)
		{
			LayoutParams p = getParams(child);
			int contentWidth = getContentProperty(TiC.PROPERTY_CONTENT_WIDTH);
			if (contentWidth == AUTO) {
				int childMeasuredWidth = child.getMeasuredWidth();
				if (!p.sizeOrFillWidthEnabled && p.optionWidth != null) {
					childMeasuredWidth = getDimensionValue(p.optionWidth, parentWidth);
				}
				if (p.optionLeft != null) {
					childMeasuredWidth += getDimensionValue(p.optionLeft, parentWidth);
				}
				if (p.optionRight != null) {
					childMeasuredWidth += getDimensionValue(p.optionRight, parentWidth);
				}

				measuredWidth = Math.max(childMeasuredWidth, measuredWidth);
				// Make parentWidth the minimum value
				measuredWidth = Math.max(parentWidth, measuredWidth);
			} else {
				measuredWidth = contentWidth;
			}

			return measuredWidth;
		}

		private int calculateAbsoluteBottom(View child)
		{
			LayoutParams p = (LayoutParams) child.getLayoutParams();
			int contentHeight = getContentProperty(TiC.PROPERTY_CONTENT_HEIGHT);

			if (contentHeight == AUTO) {
				int childMeasuredHeight = child.getMeasuredHeight();
				if (!p.sizeOrFillHeightEnabled && p.optionHeight != null) {
					childMeasuredHeight = getDimensionValue(p.optionHeight, parentHeight);
				}
				if (p.optionTop != null) {
					childMeasuredHeight += getDimensionValue(p.optionTop, parentHeight);
				}
				if (p.optionBottom != null) {
					childMeasuredHeight += getDimensionValue(p.optionBottom, parentHeight);
				}

				measuredHeight = Math.max(childMeasuredHeight, measuredHeight);
				// Make parentHeight the minimum value
				measuredHeight = Math.max(parentHeight, measuredHeight);
			} else {
				measuredHeight = contentHeight;
			}
			return measuredHeight;
		}

		private int getDimensionValue(TiDimension dimension, int parentValue)
		{
			// getAsPixels doesn't return the correct value for percentages, so we manually calculate the percentage
			// values here
			if (dimension.isUnitPercent()) {
				return (int) ((dimension.getValue() / 100.0) * parentValue);
			}
			return dimension.getAsPixels(this);
		}

		@Override
		protected void constrainChild(View child, int width, int wMode,
				int height, int hMode)
		{
			super.constrainChild(child, width, wMode, height, hMode);
			
			// We need to support an automatically growing contentArea, so this code is
			// updates the measured dimensions as needed. absWidth, absHeight are
			// left in for debugging purposes. ATM
			@SuppressWarnings("unused")
			int absWidth = calculateAbsoluteRight(child);
			@SuppressWarnings("unused")
			int absHeight = calculateAbsoluteBottom(child);
		}

		@Override
		protected int getWidthMeasureSpec(View child)
		{
			int contentWidth = getContentProperty(TiC.PROPERTY_CONTENT_WIDTH);
			if (contentWidth == AUTO) {
				return MeasureSpec.UNSPECIFIED;
			} else return super.getWidthMeasureSpec(child);
		}

		@Override
		protected int getHeightMeasureSpec(View child)
		{
			int contentHeight = getContentProperty(TiC.PROPERTY_CONTENT_HEIGHT);
			if (contentHeight == AUTO) {
				return MeasureSpec.UNSPECIFIED;
			} else return super.getHeightMeasureSpec(child);
		}

		@Override
		protected int getMeasuredWidth(int maxWidth, int widthSpec)
		{
			int contentWidth = getContentProperty(TiC.PROPERTY_CONTENT_WIDTH);
			if (contentWidth == AUTO) {
				return maxWidth; //measuredWidth;
			} else return contentWidth;
		}

		@Override
		protected int getMeasuredHeight(int maxHeight, int heightSpec)
		{
			int contentHeight = getContentProperty(TiC.PROPERTY_CONTENT_HEIGHT);
			if (contentHeight == AUTO) {
				return maxHeight; //measuredHeight;
			}
			else return contentHeight;
		}
	}

	// same code, different super-classes
	private class TiVerticalScrollView extends ScrollView
	{
		private TiScrollViewLayout layout;

		public TiVerticalScrollView(Context context, LayoutArrangement arrangement)
		{
			super(context);
			setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);

			layout = new TiScrollViewLayout(context, arrangement);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
			layout.setLayoutParams(params);
			super.addView(layout, params);
		}

		@Override
		public void addView(View child,
				android.view.ViewGroup.LayoutParams params)
		{
			layout.addView(child, params);
		}
		
		public void onDraw(Canvas canvas) 
		{
			super.onDraw(canvas);
			//setting offset once when this view is visible
			if (!setInitialOffset) {
				scrollTo(offsetX, offsetY);
				setInitialOffset = true;
			}

		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt)
		{
			super.onScrollChanged(l, t, oldl, oldt);

			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_X, l);
			data.put(TiC.EVENT_PROPERTY_Y, t);
			setContentOffset(l, t);
			getProxy().fireEvent(TiC.EVENT_SCROLL, data);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			layout.setParentHeight(MeasureSpec.getSize(heightMeasureSpec));
			layout.setParentWidth(MeasureSpec.getSize(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	private class TiHorizontalScrollView extends HorizontalScrollView
	{
		private TiScrollViewLayout layout;

		public TiHorizontalScrollView(Context context, LayoutArrangement arrangement)
		{
			super(context);
			setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
			setFillViewport(true);
			setScrollContainer(true);

			layout = new TiScrollViewLayout(context, arrangement);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
			layout.setLayoutParams(params);
			super.addView(layout, params);
			
		}

		@Override
		public void addView(View child,
				android.view.ViewGroup.LayoutParams params)
		{
			layout.addView(child, params);
		}
		
		public void onDraw(Canvas canvas) 
		{
			super.onDraw(canvas);
			//setting offset once this view is visible
			if (!setInitialOffset) {
				scrollTo(offsetX, offsetY);
				setInitialOffset = true;
			}

		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt)
		{
			super.onScrollChanged(l, t, oldl, oldt);

			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_X, l);
			data.put(TiC.EVENT_PROPERTY_Y, t);
			setContentOffset(l, t);
			getProxy().fireEvent(TiC.EVENT_SCROLL, data);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			layout.setParentHeight(MeasureSpec.getSize(heightMeasureSpec));
			layout.setParentWidth(MeasureSpec.getSize(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	public TiUIScrollView(TiViewProxy proxy)
	{
		// we create the view after the properties are processed
		super(proxy);
		getLayoutParams().autoFillsHeight = true;
		getLayoutParams().autoFillsWidth = true;
	}

	public void setContentOffset(int x , int y) 
	{
		KrollDict offset = new KrollDict();
		offsetX = x; 
		offsetY = y;
		offset.put(TiC.EVENT_PROPERTY_X, offsetX);
		offset.put(TiC.EVENT_PROPERTY_Y, offsetY);
		getProxy().setProperty(TiC.PROPERTY_CONTENT_OFFSET, offset);
	}
	
	public void setContentOffset(Object hashMap) 
	{
		if (hashMap instanceof HashMap) {
			HashMap contentOffset = (HashMap)hashMap;
			offsetX = TiConvert.toInt(contentOffset, TiC.PROPERTY_X);
			offsetY = TiConvert.toInt(contentOffset, TiC.PROPERTY_Y);
		} else {
			Log.e(LCAT, "contentOffset must be an instance of HashMap");
		}
	}
	
	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue, KrollProxy proxy) 
	{
		if (DBG) {
			Log.d(LCAT, "Property: " + key + " old: " + oldValue + " new: " + newValue);
		}
		if (key.equals(TiC.PROPERTY_CONTENT_OFFSET)) {
			setContentOffset(newValue);
			scrollTo(offsetX, offsetY);
		}
		super.propertyChanged(key, oldValue, newValue, proxy);
	}
	
	@Override
	public void processProperties(KrollDict d)
	{
		boolean showHorizontalScrollBar = false;
		boolean showVerticalScrollBar = false;

		if (d.containsKey(SHOW_HORIZONTAL_SCROLL_INDICATOR)) {
			showHorizontalScrollBar = TiConvert.toBoolean(d, SHOW_HORIZONTAL_SCROLL_INDICATOR);
		}
		if (d.containsKey(SHOW_VERTICAL_SCROLL_INDICATOR)) {
			showVerticalScrollBar = TiConvert.toBoolean(d, SHOW_VERTICAL_SCROLL_INDICATOR);
		}

		if (showHorizontalScrollBar && showVerticalScrollBar) {
			Log.w(LCAT, "Both scroll bars cannot be shown. Defaulting to vertical shown");
			showHorizontalScrollBar = false;
		}
		
		if (d.containsKey(TiC.PROPERTY_CONTENT_OFFSET)) {
			Object offset = d.get(TiC.PROPERTY_CONTENT_OFFSET);
			setContentOffset(offset);
		}

		int type = TYPE_VERTICAL;
		boolean deduced = false;

		if (d.containsKey(TiC.PROPERTY_WIDTH) && d.containsKey(TiC.PROPERTY_CONTENT_WIDTH)) {
			Object width = d.get(TiC.PROPERTY_WIDTH);
			Object contentWidth = d.get(TiC.PROPERTY_CONTENT_WIDTH);
			if (width.equals(contentWidth) || showVerticalScrollBar) {
				type = TYPE_VERTICAL;
				deduced = true;
			}
		}

		if (d.containsKey(TiC.PROPERTY_HEIGHT) && d.containsKey(TiC.PROPERTY_CONTENT_HEIGHT)) {
			Object height = d.get(TiC.PROPERTY_HEIGHT);
			Object contentHeight = d.get(TiC.PROPERTY_CONTENT_HEIGHT);
			if (height.equals(contentHeight) || showHorizontalScrollBar) {
				type = TYPE_HORIZONTAL;
				deduced = true;
			}
		}

		// android only property
		if (d.containsKey(TiC.PROPERTY_SCROLL_TYPE)) {
			Object scrollType = d.get(TiC.PROPERTY_SCROLL_TYPE);
			if (scrollType.equals(TiC.LAYOUT_VERTICAL)) {
				type = TYPE_VERTICAL;
			} else if (scrollType.equals(TiC.LAYOUT_HORIZONTAL)) {
				type = TYPE_HORIZONTAL;
			} else {
				Log.w(LCAT, "scrollType value '" + TiConvert.toString(scrollType) + "' is invalid. Only 'vertical' and 'horizontal' are supported.");
			}
		} else if (!deduced && type == TYPE_VERTICAL) {
			Log.w(LCAT, "Scroll direction could not be determined based on the provided view properties. Default VERTICAL scroll direction being used. Use the 'scrollType' property to explicitly set the scrolling direction.");
		}

		// we create the view here since we now know the potential widget type
		View view = null;
		LayoutArrangement arrangement = LayoutArrangement.DEFAULT;
		if (d.containsKey(TiC.PROPERTY_LAYOUT) && d.getString(TiC.PROPERTY_LAYOUT).equals(TiC.LAYOUT_VERTICAL)) {
			arrangement = LayoutArrangement.VERTICAL;
		} else if (d.containsKey(TiC.PROPERTY_LAYOUT) && d.getString(TiC.PROPERTY_LAYOUT).equals(TiC.LAYOUT_HORIZONTAL)) {
			arrangement = LayoutArrangement.HORIZONTAL;
		}
		switch (type) {
			case TYPE_HORIZONTAL:
				if (DBG) {
					Log.d(LCAT, "creating horizontal scroll view");
				}
				view = new TiHorizontalScrollView(getProxy().getActivity(), arrangement);
				break;
			case TYPE_VERTICAL:
			default:
				if (DBG) {
					Log.d(LCAT, "creating vertical scroll view");
				}
				view = new TiVerticalScrollView(getProxy().getActivity(), arrangement);
		}
		setNativeView(view);
	
		
		nativeView.setHorizontalScrollBarEnabled(showHorizontalScrollBar);
		nativeView.setVerticalScrollBarEnabled(showVerticalScrollBar);
		

		super.processProperties(d);
	}

	public TiScrollViewLayout getLayout()
	{
		View nativeView = getNativeView();
		if (nativeView instanceof TiVerticalScrollView) {
			return ((TiVerticalScrollView)nativeView).layout;
		} else {
			return ((TiHorizontalScrollView)nativeView).layout;
		}
	}

	public void scrollTo(int x, int y)
	{
		getNativeView().scrollTo(x, y);
		getNativeView().computeScroll();
	}

	public void scrollToBottom()
	{
		View view = getNativeView();
		if (view instanceof TiHorizontalScrollView) {
			TiHorizontalScrollView scrollView = (TiHorizontalScrollView)view;
			scrollView.fullScroll(View.FOCUS_RIGHT);
		} else if (view instanceof TiVerticalScrollView) {
			TiVerticalScrollView scrollView = (TiVerticalScrollView)view;
			scrollView.fullScroll(View.FOCUS_DOWN);
		}
	}

	@Override
	public void add(TiUIView child)
	{
		super.add(child);

		if (getNativeView() != null) {
			getLayout().requestLayout();
			if (child.getNativeView() != null) {
				child.getNativeView().requestLayout();
			}
		}
	}
	
	@Override
	public void remove(TiUIView child)
	{
		if (child != null) {
			View cv = child.getNativeView();
			if (cv != null) {
				View nv = getLayout();
				if (nv instanceof ViewGroup) {
					((ViewGroup) nv).removeView(cv);
					children.remove(child);
					child.setParent(null);
				}
			}
		}
	}

}
