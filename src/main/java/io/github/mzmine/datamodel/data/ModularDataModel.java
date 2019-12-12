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

package io.github.mzmine.datamodel.data;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Stream;
import io.github.mzmine.datamodel.data.types.DataType;
import javafx.beans.property.Property;
import javafx.collections.ObservableMap;

public interface ModularDataModel {

  /**
   * All types (columns) of this DataModel
   * 
   * @return
   */
  public ObservableMap<Class<? extends DataType>, DataType> getTypes();

  /**
   * The map containing all mappings to the types defined in getTypes
   * 
   * @param
   * @return
   */
  public ObservableMap<DataType, Property<?>> getMap();

  /**
   * Get DataType column of this DataModel
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default <T extends Property<?>> DataType<T> getTypeColumn(Class<? extends DataType<T>> tclass) {
    DataType<T> type = getTypes().get(tclass);
    return type;
  }

  /**
   * has DataType column of this DataModel
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default <T extends Property<?>> boolean hasTypeColumn(Class<? extends DataType<T>> tclass) {
    DataType<T> type = getTypes().get(tclass);
    return type != null;
  }

  /**
   * Optional.ofNullable(value)
   * 
   * @param <T>
   * @param type
   * @return
   */
  default <T extends Property<?>> Entry<DataType<T>, T> getEntry(DataType<T> type) {
    return new SimpleEntry<>(type, get(type));
  }

  /**
   * Optional.ofNullable(value)
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default <T extends Property<?>> Entry<DataType<T>, T> getEntry(
      Class<? extends DataType<T>> tclass) {
    DataType<T> type = getTypeColumn(tclass);
    return getEntry(type);
  }

  /**
   * Value for this datatype
   * 
   * @param <T>
   * @param type
   * @return
   */
  default Object getValue(DataType type) {
    return getMap().get(type).getValue();
  }

  /**
   * Value for this datatype
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default Object getValue(Class tclass) {
    DataType type = getTypeColumn(tclass);
    return get(type).getValue();
  }

  /**
   * Property for this datatype
   * 
   * @param <T>
   * @param type
   * @return
   */
  default <T extends Property<?>> T get(DataType<T> type) {
    return (T) getMap().get(type);
  }

  /**
   * Property for this datatype
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default <T extends Property<?>> T get(Class<? extends DataType<T>> tclass) {
    DataType<T> type = getTypeColumn(tclass);
    return get(type);
  }

  /**
   * type.getFormattedString(value)
   * 
   * @param <T>
   * @param type
   * @return
   */
  default <T extends Property<?>> String getFormattedString(DataType<T> type) {
    return type.getFormattedString(get(type));
  }

  /**
   * type.getFormattedString(value)
   * 
   * @param <T>
   * @param tclass
   * @return
   */
  default <T extends Property<?>> String getFormattedString(Class<? extends DataType<T>> tclass) {
    DataType<T> type = getTypeColumn(tclass);
    return getFormattedString(type);
  }

  /**
   * Feature list adds the properties
   * 
   * @param <T>
   * @param type
   * @param value
   */
  default void setProperty(DataType<?> type, Property<?> value) {
    // type in defined columns?
    if (!getTypes().containsKey(type.getClass()))
      throw new TypeColumnUndefinedException(this, type.getClass());

    DataType realType = getTypes().get(type.getClass());
    // only set datatype -> property value once
    if (getMap().get(realType) == null)
      getMap().put(realType, value);
  }

  /**
   * Set the value that is wrapped inside a property
   * 
   * @param <T>
   * @param type
   * @param value
   */
  default <T extends Property<?>> void set(DataType<T> type, Object value) {
    // type in defined columns?
    if (!getTypes().containsKey(type.getClass()))
      throw new TypeColumnUndefinedException(this, type.getClass());

    DataType realType = getTypes().get(type.getClass());
    get(realType).setValue(value);
  }

  /**
   * Set the value that is wrapped inside a property
   * 
   * @param <T>
   * @param tclass
   * @param value
   */
  default <T extends Property<?>> void set(Class<? extends DataType<T>> tclass, Object value) {
    // type in defined columns?
    if (!getTypes().containsKey(tclass))
      throw new TypeColumnUndefinedException(this, tclass);

    DataType realType = getTypeColumn(tclass);
    get(realType).setValue(value);
  }

  default void remove(Class<? extends DataType<?>> tclass) {
    DataType type = getTypeColumn(tclass);
    if (type != null)
      getMap().remove(type);
  }

  default Stream<Entry<DataType, Property<?>>> stream() {
    return getMap().entrySet().stream();
  }
}
