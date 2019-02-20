/*
 * ParallelPropagation.java - Multiple object propagation functions.
 * Copyright (C) 2019 Shiva Iyer <shiva.iyer AT utexas DOT edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.astria;

import java.util.ArrayList;
import java.util.List;
import org.astria.Settings;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.forces.ForceModel;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.PropagatorsParallelizer;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.MultiSatStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateTimeComponents;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.Constants;

public class ParallelPropagation
{
    protected MultiSatStepHandler stepHandler;

    public ParallelPropagation(MultiSatStepHandler hnd)
    {
	stepHandler = hnd;
    }

    public void propagate(String[] cfgjson, String propStart, String propEnd) throws Exception
    {
	List<Propagator> props = new ArrayList<Propagator>(cfgjson.length);
	for (int i = 0; i < cfgjson.length; i++)
	{
	    props.add(buildPropagator(Settings.loadJSON(cfgjson[i])));
	}

	PropagatorsParallelizer plel = new PropagatorsParallelizer(props, stepHandler);
	plel.propagate(new AbsoluteDate(DateTimeComponents.parseDateTime(propStart), DataManager.utcscale),
		       new AbsoluteDate(DateTimeComponents.parseDateTime(propEnd), DataManager.utcscale));
    }

    protected NumericalPropagator buildPropagator(Settings obj)
    {
	double[] Xi = obj.Propagation.InitialState;
	AbsoluteDate tm = new AbsoluteDate(DateTimeComponents.parseDateTime(obj.Propagation.Start),
					   DataManager.utcscale);

	NumericalPropagator prop = new NumericalPropagator(new DormandPrince853Integrator(
		obj.Integration.MinTimeStep, obj.Integration.MaxTimeStep,
		obj.Integration.AbsTolerance, obj.Integration.RelTolerance));
	for (ForceModel fm : obj.forces)
	    prop.addForceModel(fm);

	prop.setInitialState(new SpacecraftState(new CartesianOrbit(new PVCoordinates(new Vector3D(Xi[0], Xi[1], Xi[2]),
										      new Vector3D(Xi[3], Xi[4], Xi[5])),
								    DataManager.eme2000, tm, Constants.EGM96_EARTH_MU),
						 obj.SpaceObject.Mass));

	return(prop);
    }
}