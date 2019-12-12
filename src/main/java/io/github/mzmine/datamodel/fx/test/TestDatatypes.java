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

package io.github.mzmine.datamodel.fx.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import io.github.mzmine.datamodel.FeatureStatus;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.data.ModularFeature;
import io.github.mzmine.datamodel.data.ModularFeatureList;
import io.github.mzmine.datamodel.data.ModularFeatureListRow;
import io.github.mzmine.datamodel.data.TypeColumnUndefinedException;
import io.github.mzmine.datamodel.data.types.DataType;
import io.github.mzmine.datamodel.data.types.DetectionType;
import io.github.mzmine.datamodel.data.types.RawFileType;
import io.github.mzmine.datamodel.data.types.modifiers.BindingsFactoryType;
import io.github.mzmine.datamodel.data.types.numbers.AreaType;
import io.github.mzmine.datamodel.data.types.numbers.IDType;
import io.github.mzmine.datamodel.data.types.numbers.MZType;
import io.github.mzmine.datamodel.data.types.rowsum.BindingsType;
import io.github.mzmine.project.impl.RawDataFileImpl;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import junit.framework.Assert;

public class TestDatatypes {
  Logger logger = Logger.getLogger(TestDatatypes.class.getName());
  ModularFeatureList flist;
  ModularFeatureListRow data;
  ModularFeatureListRow empty;
  private FeatureStatus detection;
  private ModularFeatureList flistWithRaw;

  // test binding
  private ModularFeatureList flistWithBinding;
  private float area[] = new float[] {100, 200};
  private double mz[] = new double[] {50, 51};
  private RawDataFile[] rawsBinding;


  @Before
  public void setUp() throws Exception {
    // create feature list and add column types
    flist = new ModularFeatureList("flist");
    flist.addRowType(new DetectionType());
    flist.addRowType(new MZType());

    detection = FeatureStatus.ESTIMATED;
    // create row
    data = new ModularFeatureListRow(flist);
    data.set(DetectionType.class, detection);
    data.set(MZType.class, 200d);

    flist.addRow(data);

    // second
    List<RawDataFile> raw = new ArrayList<>();
    raw.add(new RawDataFileImpl("Raw"));
    flistWithRaw = new ModularFeatureList("flist name", raw);

    // with binding
    rawsBinding = new RawDataFile[] {new RawDataFileImpl("raw1"), new RawDataFileImpl("raw2")};
    flistWithBinding = new ModularFeatureList("flist with binding", rawsBinding);
    flistWithBinding.addRowType(new AreaType());
    flistWithBinding.addRowType(new MZType());
    // ftype
    flistWithBinding.addFeatureType(new AreaType());
    flistWithBinding.addFeatureType(new MZType());

    ModularFeatureListRow row = new ModularFeatureListRow(flistWithBinding);
    flistWithBinding.addRow(row);
  }

  @Test
  public void simpleSumBinding() {
    DoubleProperty a = new SimpleDoubleProperty();
    DoubleProperty b = new SimpleDoubleProperty();
    DoubleProperty sum = new SimpleDoubleProperty();

    sum.bind(a.add(b));
    sum.add(b);
    logger.info("Sum=" + sum.get() + "   " + sum.getValue());
    a.set(10);
    logger.info("Sum=" + sum.get() + "   " + sum.getValue());
    b.set(5);
    logger.info("Sum=" + sum.get() + "   " + sum.getValue());
  }

  @Test
  public void simpleAvgBinding() {
    DoubleProperty a = new SimpleDoubleProperty();
    DoubleProperty b = new SimpleDoubleProperty();
    DoubleProperty avg = new SimpleDoubleProperty();
    DoubleBinding avgBinding = Bindings.createDoubleBinding(() -> {
      int i = a.getValue() != null ? 1 : 0;
      i += b.getValue() != null ? 1 : 0;

      return (a.getValue().doubleValue() + b.getValue().doubleValue()) / i;
    }, a, b);
    avg.bind(avgBinding);
    logger.info("avg=" + avg.get() + "   " + avg.getValue());
    a.set(10);
    logger.info("avg=" + avg.get() + "   " + avg.getValue());
    b.set(5);
    logger.info("avg=" + avg.get() + "   " + avg.getValue());
  }

  @Test
  public void testBinding() {
    ModularFeatureListRow row = flistWithBinding.getRow(0);
    // add bindings first and check after changing values
    DoubleProperty mzProperty = row.get(MZType.class);
    FloatProperty areaProperty = row.get(AreaType.class);

    DataType<FloatProperty> type = row.getTypeColumn(AreaType.class);
    if (type instanceof BindingsFactoryType)
      areaProperty.bind(((BindingsFactoryType) type).createBinding(BindingsType.SUM, row));

    DataType<DoubleProperty> typeMZ = row.getTypeColumn(MZType.class);
    if (typeMZ instanceof BindingsFactoryType)
      mzProperty.bind(((BindingsFactoryType) typeMZ).createBinding(BindingsType.AVERAGE, row));

    logger.info("avg mz=" + row.getMZ().getValue());
    logger.info("sum area=" + row.getArea().getValue());

    // add values
    for (int i = 0; i < rawsBinding.length; i++) {
      ModularFeature f = row.getFeature(rawsBinding[i]);
      f.set(AreaType.class, area[i]);
      f.set(MZType.class, mz[i]);
      logger.info("after settings values: avg mz=" + row.getMZ().getValue());
      logger.info("after setting values: sum area=" + row.getArea().getValue());
    }
  }

  @Test(expected = TypeColumnUndefinedException.class)
  public void testMissingTypeColumn() {
    data.set(AreaType.class, 20f);
  }

  @Test(expected = ClassCastException.class)
  public void testInsertWrongTypeByInstance() {
    DataType type = new MZType();
    data.set(type, "String");
  }

  @Test(expected = ClassCastException.class)
  public void testInsertWrongTypeByClass() {
    Class c = MZType.class;
    data.set(c, "String");
  }

  @Test
  public void testDataTypes() {
    // is not defined as column - should be null
    Assert.assertNull(data.get(AreaType.class));

    // detection type is present
    Assert.assertEquals(detection.toString(), data.getDetectionType().toString());
    Assert.assertEquals(detection.toString(), data.getFormattedString(DetectionType.class));
    Entry<DataType<ObjectProperty<FeatureStatus>>, ObjectProperty<FeatureStatus>> entry =
        data.getEntry(DetectionType.class);
    Assert.assertEquals(detection.toString(), entry.getKey().getFormattedString(entry.getValue()));

    // should contain a value
    Assert.assertNotNull(data.get(MZType.class).get());
    Assert.assertNotNull(data.get(new MZType()).get());
  }


  @Test
  public void createMinimalTest() throws IOException {
    // create and add
    flistWithRaw.addRowType(new IDType());
    flistWithRaw.addFeatureType(new DetectionType());

    for (int i = 0; i < 2; i++) {
      RawDataFile raw = flistWithRaw.getRawDataFile(0);
      ModularFeature p = new ModularFeature(flistWithRaw);
      p.set(RawFileType.class, raw);
      p.set(DetectionType.class, FeatureStatus.DETECTED);

      ModularFeatureListRow r = new ModularFeatureListRow(flistWithRaw);
      r.set(IDType.class, (i));
      r.addPeak(raw, p);
      flistWithRaw.addRow(r);
    }
  }

  @Test
  public void testInsertSubType() {
    // adding RawDataFileImpl into a DataType<RawDataFile>
    ModularFeature p = new ModularFeature(flistWithRaw);
    p.set(RawFileType.class, flistWithRaw.getRawDataFile(0));
  }

}
