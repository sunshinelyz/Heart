/*
 *  Copyright (C) 2012 Daryl Daly
 *
 *  This file is part of Heart Observe
 *
 *  Heart Observe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Heart Observe is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package ca.ddaly.android.heart;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.model.TimeSeries;


public class TimePressureGraph {

  private static final String TAG = "TimePressureGraph";
  private static TimePressureGraph singleton = null;
  private Context ctxt = null;

  synchronized static TimePressureGraph getInstance(Context ctxt) {
    if (singleton == null) {
      singleton=new TimePressureGraph(ctxt.getApplicationContext());
    }
    return(singleton);
  }

  public TimePressureGraph(Context ctxt) {
    this.ctxt=ctxt;
  }

  /**
   * prepares an intent for starting the graph activity
   *
   * @return: the intent or null if nothing to display
   */
  public Intent getIntent() {
    XYMultipleSeriesDataset dataset = getDataset();

    if (dataset == null) {
      return null;
    }

    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    renderer.setAxisTitleTextSize(16);
    renderer.setChartTitleTextSize(20);
    renderer.setLabelsTextSize(15);
    renderer.setLegendTextSize(15);
    renderer.setPointSize(5f);
    renderer.setMargins(new int[] {20,50,15,20});

    XYSeriesRenderer seriesOne = new XYSeriesRenderer();
    seriesOne.setColor(Color.MAGENTA);
    seriesOne.setPointStyle(PointStyle.CIRCLE);
    renderer.addSeriesRenderer(seriesOne);

    XYSeriesRenderer seriesTwo = new XYSeriesRenderer();
    seriesTwo.setColor(Color.GREEN);
    seriesTwo.setPointStyle(PointStyle.TRIANGLE);
    renderer.addSeriesRenderer(seriesTwo);

    renderer.setChartTitle(ctxt.getString(R.string.graph_title));
    renderer.setXTitle("");
    renderer.setYTitle("mmHg");

    renderer.setShowGridX(true);
    renderer.setYLabelsAlign(Paint.Align.RIGHT);
    renderer.setYAxisMax(getYDataMax(dataset) + 10);
    renderer.setYAxisMin(getYDataMin(dataset) - 10);

    return ChartFactory.getTimeChartIntent(ctxt, getDataset(), renderer,
                                           "MM/dd/yyyy",
					   ctxt.getString(R.string.app_name));
  }

  /**
   * determines maximum Y value of the series in the given dataset
   *
   */
  private double getYDataMax(XYMultipleSeriesDataset dataset) {
    double max = 0;

    for (XYSeries series: dataset.getSeries()) {
      double series_max = series.getMaxY();
      if (series_max > max) {
        max = series_max;
      }
    }
    return max;
  }

   /**
   * determines minimum Y value of the series in the given dataset
   *
   */
  private double getYDataMin(XYMultipleSeriesDataset dataset) {
    double min = 999;

    for (XYSeries series: dataset.getSeries()) {
      double series_min = series.getMinY();
      if (series_min < min) {
        min = series_min;
      }
    }
    return min;
  }

  /**
   * loads a dataset from the database
   *
   * @return: dataset if records found otherwise null
   */
  private XYMultipleSeriesDataset getDataset() {
    Calendar cal = Calendar.getInstance();

    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    TimeSeries systolic = new TimeSeries(ctxt.getString(R.string.systolic));
    TimeSeries diastolic = new TimeSeries(ctxt.getString(R.string.diastolic));

    // TODO -- should be off of the UI thread
    Cursor result = DatabaseHelper.getInstance(ctxt).getReadableDatabase().rawQuery(
                    "Select "
		  + "       date, "
		  + "       systolic, "
		  + "       diastolic "
		  + "From heart "
		  + "Order by date",
		  null);

    if ( result.getCount() > 1 ) {    // using 1 here because one record doesn't graph very well
      while (result.moveToNext()) {
	Long date = result.getLong(0);
	Integer systolic_val = result.getInt(1);
	Integer diastolic_val = result.getInt(2);

	systolic.add(date,systolic_val);
	diastolic.add(date,diastolic_val);
      }


      dataset.addSeries(systolic);
      dataset.addSeries(diastolic);
    } else {
      dataset = null;
    }

    result.close();

    return dataset;
  }
}
