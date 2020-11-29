/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.visualization.spectra.simplespectra;

import io.github.mzmine.datamodel.features.Feature;
import java.util.Collection;
import javax.annotation.Nonnull;

import io.github.mzmine.datamodel.*;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineRunnableModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExitCode;

/**
 * Spectrum visualizer
 */
public class SpectraVisualizerModule implements MZmineRunnableModule {

  private static final String MODULE_NAME = "Spectra visualizer";
  private static final String MODULE_DESCRIPTION = "Spectra visualizer."; // TODO

  @Override
  public @Nonnull String getName() {
    return MODULE_NAME;
  }

  @Override
  public @Nonnull String getDescription() {
    return MODULE_DESCRIPTION;
  }

  @Override
  @Nonnull
  public ExitCode runModule(@Nonnull MZmineProject project, @Nonnull ParameterSet parameters,
      @Nonnull Collection<Task> tasks) {
    RawDataFile dataFiles[] = parameters.getParameter(SpectraVisualizerParameters.dataFiles)
        .getValue().getMatchingRawDataFiles();

    int scanNumber = parameters.getParameter(SpectraVisualizerParameters.scanNumber).getValue();
    String massList = parameters.getParameter(SpectraVisualizerParameters.massList).getValue();

    addNewSpectrumTab(dataFiles[0], scanNumber, massList);

    return ExitCode.OK;
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile,
      int scanNumber) {
    return addNewSpectrumTab(dataFile, scanNumber, null, null, null, null);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile,
      int scanNumber, String massList) {
    return addNewSpectrumTab(dataFile, scanNumber, null, null, null, null, massList);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile, int scanNumber,
      Feature peak) {
    return addNewSpectrumTab(dataFile, scanNumber, peak, null, null, null);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile, int scanNumber,
      IsotopePattern detectedPattern) {
    return addNewSpectrumTab(dataFile, scanNumber, null, detectedPattern, null, null);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile, int scanNumber,
      Feature peak, IsotopePattern detectedPattern, IsotopePattern predictedPattern) {
    return addNewSpectrumTab(dataFile, scanNumber, peak, detectedPattern, predictedPattern,
        null);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile, int scanNumber,
      Feature peak, IsotopePattern detectedPattern, IsotopePattern predictedPattern,
      IsotopePattern spectrum){
    return addNewSpectrumTab(dataFile, scanNumber, peak, detectedPattern, predictedPattern, spectrum, null);
  }

  public static SpectraVisualizerTab addNewSpectrumTab(RawDataFile dataFile, int scanNumber,
      Feature peak, IsotopePattern detectedPattern, IsotopePattern predictedPattern,
      IsotopePattern spectrum, String massList) {

    Scan scan = dataFile.getScan(scanNumber);

    if (scan == null) {
      MZmineCore.getDesktop().displayErrorMessage(
          "Raw data file " + dataFile + " does not contain scan #" + scanNumber);
      return null;
    }

    if (massList != null && massList.trim().length() == 0) {
      massList = null;
    }

    // check if the scan contains the specified mass list
    if (massList != null) {
      MassList massListObject = scan.getMassList(massList);
      if (massListObject == null) {
        MZmineCore.getDesktop().displayErrorMessage(
          "Raw data file " + dataFile + " scan #" + scanNumber + " does not contain mass list " + massList);
        return null;
      }
    }

    SpectraVisualizerTab newTab = new SpectraVisualizerTab(dataFile, scanNumber, massList, true);
    newTab.loadRawData(scan);

    if (peak != null)
      newTab.loadSinglePeak(peak);

    if (detectedPattern != null)
      newTab.loadIsotopes(detectedPattern);

    if (predictedPattern != null)
      newTab.loadIsotopes(predictedPattern);

    if (spectrum != null)
      newTab.loadSpectrum(spectrum);

    //newWindow.show();
    MZmineCore.getDesktop().addTab(newTab);

    return newTab;

  }

  @Override
  public @Nonnull MZmineModuleCategory getModuleCategory() {
    return MZmineModuleCategory.VISUALIZATIONRAWDATA;
  }

  @Override
  public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
    return SpectraVisualizerParameters.class;
  }

}
