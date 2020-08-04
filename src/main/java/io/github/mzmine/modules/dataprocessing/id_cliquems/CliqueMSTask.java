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

package io.github.mzmine.modules.dataprocessing.id_cliquems;


import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.PeakIdentity;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.datamodel.PolarityType;
import io.github.mzmine.datamodel.impl.SimplePeakIdentity;
import io.github.mzmine.modules.dataprocessing.id_camera.CameraSearchTask;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.AnClique;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.ComputeAdduct;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.ComputeCliqueModule;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.ComputeIsotopesModule;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.OutputAn;
import io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation.PeakData;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.AbstractTask;
import io.github.mzmine.taskcontrol.TaskStatus;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang3.mutable.MutableDouble;

public class CliqueMSTask extends AbstractTask {
  // Logger.
  private static final Logger logger = Logger.getLogger(CliqueMSTask.class.getName());

  //progress constants
  //TODO change the constant values
  public final double EIC_PROGRESS = 0.4 ; // EIC calculation takes about 10% time
  public final double MATRIX_PROGRESS = 0.3 ; // Cosine matrix calculation takes about 60% time
  public final double NET_PROGRESS = 0.1 ; // Network calculations takes 10% time
  public final double ISO_PROGRESS = 0.1 ; // Isotope calculation takes 10% time




  // Feature list to process.
  private final PeakList peakList;

  // Task progress
  private final MutableDouble progress = new MutableDouble(0.0);

  // Project
  private final MZmineProject project;

  // Parameters.
  private final ParameterSet parameters;

  public CliqueMSTask(final MZmineProject project, final ParameterSet parameters,
      final PeakList list){
    this.project = project;
    this.parameters = parameters;
    peakList = list;
  }

  @Override
  public String getTaskDescription() {

    return "Identification of pseudo-spectra in " + peakList;
  }

  @Override
  public double getFinishedPercentage() {
    return progress.getValue();
  }

  @Override
  public void cancel(){
    super.cancel();
  }

  @Override
  public void run() {
    setStatus(TaskStatus.PROCESSING);

    this.progress.setValue(0.0);

    try {
      //TODO multiple rawDataFile support
      ComputeCliqueModule cm = new ComputeCliqueModule(peakList,peakList.getRawDataFile(0),progress, this);
      // Check if not canceled
      if (isCanceled())
        return;
      AnClique anClique =  cm.getClique(parameters.getParameter(CliqueMSParameters.FILTER).getValue(),
          parameters.getParameter(CliqueMSParameters.MZ_DIFF).getValue(),
          parameters.getParameter(CliqueMSParameters.RT_DIFF).getValue(),
          parameters.getParameter(CliqueMSParameters.IN_DIFF).getValue(),
          parameters.getParameter(CliqueMSParameters.TOL).getValue());
      // Check if not canceled
      if (isCanceled())
        return;
      ComputeIsotopesModule cim = new ComputeIsotopesModule(anClique,this,progress);
      cim.getIsotopes(parameters.getParameter(CliqueMSParameters.ISOTOPES_MAX_CHARGE).getValue(),
          parameters.getParameter(CliqueMSParameters.ISOTOPES_MAXIMUM_GRADE).getValue(),
          parameters.getParameter(CliqueMSParameters.ISOTOPES_MZ_TOLERANCE).getValue(),
          parameters.getParameter(CliqueMSParameters.ISOTOPE_MASS_DIFF).getValue());
      // Check if not canceled
      if (isCanceled())
        return;
      ComputeAdduct computeAdduct = new ComputeAdduct(anClique,this, PolarityType.POSITIVE);
      Set<OutputAn> outputAnSet = computeAdduct.getAnnotation(
          parameters.getParameter(CliqueMSParameters.ANNOTATE_TOP_MASS).getValue(),
          parameters.getParameter(CliqueMSParameters.ANNOTATE_TOP_MASS_FEATURE).getValue(),
          parameters.getParameter(CliqueMSParameters.SIZE_ANG).getValue(),
           10,
          parameters.getParameter(CliqueMSParameters.ANNOTATE_FILTER).getValue(),
          parameters.getParameter(CliqueMSParameters.ANNOTATE_EMPTY_SCORE).getValue(),
          parameters.getParameter(CliqueMSParameters.ANNOTATE_NORMALIZE).getValue());


      addFeatureIdentity(anClique);
      // Check if not canceled
      if (isCanceled())
        return;


      // Finished.
      this.progress.setValue(1.0);
      setStatus(TaskStatus.FINISHED);
    }
    catch(Exception e){
      setStatus(TaskStatus.ERROR);
      setErrorMessage("Could not calculate cliques for features "+ peakList.getName()+" \n" +
          e.getMessage());
      e.printStackTrace();
    }
  }

  private void addFeatureIdentity(AnClique anClique){
    List<PeakData> pdList =  anClique.getPeakDataList();

    for(PeakData pd : pdList){
      SimplePeakIdentity identity = new SimplePeakIdentity("Node #"+pd.getNodeID());
      identity.setPropertyValue(PeakIdentity.PROPERTY_METHOD,"CliqueMS algorithm");
      identity.setPropertyValue("Clique-ID",String.valueOf(pd.getCliqueID()));
      identity.setPropertyValue("Isotope",pd.getIsotopeAnnotation());
      this.peakList.findRowByID(pd.getPeakListRowID()).addPeakIdentity(identity,true);
    }

  }
}
