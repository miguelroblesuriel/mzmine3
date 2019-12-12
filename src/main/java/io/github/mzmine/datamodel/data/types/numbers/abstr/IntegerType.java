/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel.data.types.numbers.abstr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import javax.annotation.Nonnull;
import io.github.mzmine.datamodel.data.ModularFeatureListRow;
import io.github.mzmine.datamodel.data.types.modifiers.BindingsFactoryType;
import io.github.mzmine.datamodel.data.types.rowsum.BindingsType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public abstract class IntegerType extends NumberType<IntegerProperty>
    implements BindingsFactoryType {

  protected IntegerType() {
    super(new DecimalFormat("0"));
  }

  @Override
  public NumberFormat getFormatter() {
    return DEFAULT_FORMAT;
  }

  @Override
  @Nonnull
  public String getFormattedString(@Nonnull IntegerProperty value) {
    if (value.getValue() == null)
      return "";
    return getFormatter().format(value.getValue().intValue());
  }

  @Override
  public IntegerProperty createProperty() {
    return new SimpleIntegerProperty();
  }


  @Override
  public NumberBinding createBinding(BindingsType bind, ModularFeatureListRow row) {
    // get all properties of all features
    IntegerProperty[] prop =
        row.streamFeatures().map(f -> f.get(this)).toArray(IntegerProperty[]::new);
    switch (bind) {
      case AVERAGE:
        return Bindings.createIntegerBinding(() -> {
          int sum = 0;
          int n = 0;
          for (IntegerProperty p : prop) {
            if (p.getValue() != null) {
              sum += p.get();
              n++;
            }
          }
          return sum / n;
        }, prop);
      case MIN:
        return Bindings.createIntegerBinding(() -> {
          int min = Integer.MAX_VALUE;
          for (IntegerProperty p : prop)
            if (p.getValue() != null && p.get() < min)
              min = p.get();
          return min;
        }, prop);
      case MAX:
        return Bindings.createIntegerBinding(() -> {
          int max = Integer.MIN_VALUE;
          for (IntegerProperty p : prop)
            if (p.getValue() != null && p.get() > max)
              max = p.get();
          return max;
        }, prop);
      case SUM:
        return Bindings.createIntegerBinding(() -> {
          int sum = 0;
          for (IntegerProperty p : prop)
            if (p.getValue() != null)
              sum += p.get();
          return sum;
        }, prop);
      case COUNT:
        return Bindings.createLongBinding(() -> {
          return Arrays.stream(prop).filter(p -> p.getValue() != null).count();
        }, prop);
      default:
        return null;
    }
  }
}
