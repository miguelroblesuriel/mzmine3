/*
 *  Copyright 2006-2020 The MZmine Development Team
 *
 *  This file is part of MZmine.
 *
 *  MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 *  General Public License as published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MZmine; if not,
 *  write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 *  USA
 */

package io.github.mzmine.util.scans;

import io.github.mzmine.datamodel.DataPoint;

public class IndexedDataPoint implements DataPoint {

  private final double mz;
  private final double intensity;
  private final int index;

  public IndexedDataPoint(double mz, double intensity, int index) {
    this.mz = mz;
    this.intensity = intensity;
    this.index = index;
  }

  @Override
  public double getMZ() {
    return mz;
  }

  @Override
  public double getIntensity() {
    return intensity;
  }

  public int getIndex() {
    return index;
  }
}
