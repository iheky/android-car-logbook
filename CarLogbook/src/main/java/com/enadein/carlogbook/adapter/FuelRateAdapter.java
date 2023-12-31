/*
    CarLogbook.
    Copyright (C) 2014  Eugene Nadein

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.enadein.carlogbook.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enadein.carlogbook.R;
import com.enadein.carlogbook.bean.FuelRateBean;


public class FuelRateAdapter extends CursorAdapter {
	public FuelRateAdapter(Context context, Cursor c) {
		super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		FuelRateHolder holder = new FuelRateHolder();
		LayoutInflater inflater = LayoutInflater.from(context);
		View listItem = inflater.inflate(R.layout.report_item_simple, null);

		holder.nameView = (TextView) listItem.findViewById(R.id.name);
		holder.valueView = (TextView) listItem.findViewById(R.id.value);
		listItem.setTag(holder);

		return listItem;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		FuelRateBean bean = new FuelRateBean();
		bean.populate(cursor);

		FuelRateHolder holder = (FuelRateHolder) view.getTag();
		holder.nameView.setText(bean.getStationId() + "");
		holder.valueView.setText(bean.getMinRate() +"/"+bean.getRate()+"/" + bean.getMaxRate());
	}

	public static class FuelRateHolder {
		public int id;
		public TextView nameView;
		public TextView valueView;
	}
}
