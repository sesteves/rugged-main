/* Copyright 2013-2016 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.rugged.atmosphericrefraction;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.FastMath;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.rugged.errors.RuggedException;
import org.orekit.rugged.raster.Tile;
import org.orekit.rugged.utils.ExtendedEllipsoid;
import org.orekit.rugged.utils.NormalizedGeodeticPoint;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import java.util.*;

/**
 * Multi layer model for atmospheric refraction.
 * @author Sergio Esteves
 */
public class MultiLayerModel implements AtmosphericRefraction {

    // maps altitude (lower bound) to refraction index
    private static final Map<Double, Double> meanAtmosphericRefractions;

    private static final Map<Double, ExtendedEllipsoid> atmosphericEllipsoids;

    static {
        Map<Double, Double> refractions = new TreeMap(Collections.reverseOrder());
        refractions.put(-1000.00000000000, 1.00030600000);
        refractions.put(0.00000000000, 1.00027800000);
        refractions.put(1000.00000000000, 1.00025200000);
        refractions.put(3000.00000000000, 1.00020600000);
        refractions.put(5000.00000000000, 1.00016700000);
        refractions.put(7000.00000000000, 1.00013400000);
        refractions.put(9000.00000000000, 1.00010600000);
        refractions.put(11000.00000000000, 1.00008300000);
        refractions.put(14000.00000000000, 1.00005200000);
        refractions.put(18000.00000000000, 1.00002800000);
        refractions.put(23000.00000000000, 1.00001200000);
        refractions.put(30000.00000000000, 1.00000400000);
        refractions.put(40000.00000000000, 1.00000100000);
        refractions.put(50000.00000000000, 1.00000000000);
        refractions.put(100000.00000000000, 1.00000000000);
        meanAtmosphericRefractions = Collections.unmodifiableMap(refractions);

        Map<Double, ExtendedEllipsoid> ellipsoids = new HashMap<Double, ExtendedEllipsoid>();
        try {
            for (Double altitude : refractions.keySet()) {
                OneAxisEllipsoid ellipsoid = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS + altitude,
                        Constants.WGS84_EARTH_FLATTENING, FramesFactory.getITRF(IERSConventions.IERS_2010, true));
                ellipsoid = new ExtendedEllipsoid(ellipsoid.getEquatorialRadius(), ellipsoid.getFlattening(),
                        ellipsoid.getBodyFrame());
                ellipsoids.put(altitude, (ExtendedEllipsoid) ellipsoid);
            }
        } catch (OrekitException e) {
            e.printStackTrace();
        }
        atmosphericEllipsoids = Collections.unmodifiableMap(ellipsoids);
    }

    @Override
    public NormalizedGeodeticPoint getPointOnGround(Vector3D initialPos, Vector3D initialLos, Vector3D initialZenith,
                                                double altitude, Tile tile) {
        NormalizedGeodeticPoint newGeodeticPoint = null;
        try {

            Vector3D pos = initialPos;
            Vector3D los = initialLos;
            Vector3D zenith = initialZenith;
            double theta1 = Vector3D.angle(los, zenith), theta2;
            double previousRefractionIndex = -1;
            NormalizedGeodeticPoint gp = null;
            for (Map.Entry<Double, Double> entry : meanAtmosphericRefractions.entrySet()) {
                if (pos.getZ() < entry.getKey()) {
                    continue;
                }

                if (previousRefractionIndex > 0) {
                    theta2 = FastMath.asin(previousRefractionIndex * FastMath.sin(theta1) / entry.getValue());

                    // get current los
                    double a = FastMath.sqrt((1 - FastMath.pow(FastMath.cos(theta2), 2)) /
                                             (1 - FastMath.pow(FastMath.cos(theta1), 2)));
                    double b = a * FastMath.cos(theta1) - FastMath.cos(theta2);
                    los = new Vector3D(a, los, b, zenith);

                    theta1 = theta2;
                }

                // get intersection point
                ExtendedEllipsoid ellipsoid = atmosphericEllipsoids.get(entry.getKey());
                gp = ellipsoid.pointOnGround(pos, los, 0.0);
                pos = ellipsoid.transform(gp);
                zenith = gp.getZenith();

                if (altitude > entry.getKey()) {
                    break;
                }
                previousRefractionIndex = entry.getValue();
            }

            newGeodeticPoint = tile.cellIntersection(gp, los, 0, 0);
        } catch (RuggedException e) {
            e.printStackTrace();
        }

        return newGeodeticPoint;
    }
}