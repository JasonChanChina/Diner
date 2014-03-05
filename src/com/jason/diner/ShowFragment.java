package com.jason.diner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.Interface.UIInterface;
import com.jason.Task.ImageLoadTask;
import com.jason.Task.MyAsyncTask;

public class ShowFragment extends Fragment implements UIInterface {

	private ListView showList;
	private View rootView;
	private ArrayList<Integer> indexList;

	@Override
	public void updateData(String json) {
		// TODO Auto-generated method stub
		if (!Helper.json2Order(json, Document.MainDoc().order)) {
			Toast.makeText(Document.MainDoc().mainActivity.activity,
					"请求数据异常，请重试！", Toast.LENGTH_SHORT).show();
		} else {

			Iterator iter = Document.MainDoc().order.dishes.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Map.Entry<String, ArrayList<ArrayList<DishInfo>>> entry = (Map.Entry<String, ArrayList<ArrayList<DishInfo>>>) iter
						.next();

				String key = entry.getKey();
				ArrayList<ArrayList<DishInfo>> value = entry.getValue();
				Document.MainDoc().order.categorySize.put(key, value.size());

				// tag
				ArrayList<DishInfo> dishTags = new ArrayList<DishInfo>();
				DishInfo dishTag = new DishInfo();
				dishTag.dishId = "" + -1;
				dishTag.dishCategory = key;
				dishTags.add(dishTag);
				Document.MainDoc().order.dishesBlinding.add(dishTags);

				// context
				for (int i = 0; i < value.size(); i++) {
					Document.MainDoc().order.dishesBlinding.add(value.get(i));
				}

			}

		}

	}

	@Override
	public void updateUI() {
		// TODO Auto-generated method stub
		MyShowAdapter adapter = new MyShowAdapter(
				Document.MainDoc().mainActivity.activity,
				Document.MainDoc().order);
		showList.setAdapter(adapter);
		Document.MainDoc().mainActivity.mDrawerList.setItemChecked(1, true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.show_fragment, container, false);
		showList = (ListView) rootView.findViewById(R.id.showList);

		MyAsyncTask mTask = new MyAsyncTask(this);
		String param = "rule=" + Helper.rule2Json(Document.MainDoc().rule);
		mTask.execute(Document.MainDoc().server.getOrderUrl(null));

		return rootView;
	}

}

class MyShowAdapter extends BaseAdapter {

	// 要使用到的数据源
	private ArrayList<ArrayList<DishInfo>> data;
	private LayoutInflater inflater;
	private Context context;

	public MyShowAdapter(Context context, OrderInfo order) {

		this.context = context;
		this.data = order.dishesBlinding;
		inflater = LayoutInflater.from(context);

	}

	// item的总行数
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data == null ? 0 : data.size();
	}

	// item对象
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);

	}

	// item的id
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 绘制每一个item
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ArrayList<DishInfo> item = (ArrayList<DishInfo>) getItem(position);

		boolean isTag = false;
		if (item.get(0).dishId.trim().equals("-1")) {
			isTag = true;
		}

		TextView dishTag;
		ViewPager dishPager;
		if (isTag) {
			convertView = inflater.inflate(R.layout.show_fragment_item_tag,
					null);
			dishTag = (TextView) convertView.findViewById(R.id.dishTag);
			dishTag.setText(item.get(0).dishCategory);
		} else {
			convertView = inflater.inflate(R.layout.show_fragment_item, null);
			dishPager = (ViewPager) convertView.findViewById(R.id.dishPager);

			ArrayList<View> viewList = new ArrayList<View>();
			for (int i = 0; i < item.size(); i++) {
				View viewItem = inflater.inflate(R.layout.show_viewpager_item,
						null);

				if (i % 2 == 0) {
					viewItem.setBackgroundResource(R.color.background_color_light);
				} else {
					viewItem.setBackgroundResource(R.color.background_gray_normal);
				}

				TextView dishName = (TextView) viewItem
						.findViewById(R.id.dishName);
				TextView dishTaste = (TextView) viewItem
						.findViewById(R.id.dishTaste);
				TextView dishFood = (TextView) viewItem
						.findViewById(R.id.dishFood);
				TextView dishCooking = (TextView) viewItem
						.findViewById(R.id.dishCooking);
				TextView dishCategory = (TextView) viewItem
						.findViewById(R.id.dishCategory);
				TextView dishMark = (TextView) viewItem
						.findViewById(R.id.dishMark);

				dishName.setText(item.get(i).dishName);
				dishTaste.setText(item.get(i).dishTaste);
				dishFood.setText(item.get(i).dishFood);
				dishCooking.setText(item.get(i).dishCooking);
				dishCategory.setText(item.get(i).dishCategory);
				if (i > 0) {
					dishMark.setText("备选");
				}

				try {
					String address = item.get(i).dishImage;
					Bitmap bitmap = Document.MainDoc().imageCache
							.getImage(address);// 从缓存中取图片

					ImageView dishImage = (ImageView) viewItem
							.findViewById(R.id.dishImage);
					if (bitmap != null) {
						dishImage.setImageBitmap(Helper.toRoundCorner(bitmap));
					} else {
						dishImage.setImageBitmap(Helper.toRoundCorner(Helper
								.Drawable2Bitmap(R.drawable.ic_launcher)));
						ImageLoadTask imageLoadTask = new ImageLoadTask();
						String url = Document.MainDoc().server.url;
						imageLoadTask.execute(url, address, this);// 执行异步任务
					}
				} catch (Exception e) {
					Test.error("ShopFragment.MyShopAdapter.getView()",
							e.toString());
				}

				viewList.add(viewItem);

			}
			dishPager.setAdapter(new MyItemPagerAdapter(viewList));
			dishPager.setCurrentItem(0);
		}
		return convertView;
	}

}

class MyItemPagerAdapter extends PagerAdapter {
	private ArrayList<View> viewList;

	public MyItemPagerAdapter(ArrayList<View> viewList) {
		this.viewList = viewList;
	}

	@Override
	public int getCount() {
		return viewList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView(viewList.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		container.addView(viewList.get(position));
		return viewList.get(position);

	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

}
