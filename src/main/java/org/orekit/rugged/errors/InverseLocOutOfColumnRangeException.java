/* Copyright 2013-2015 CS Systèmes d'Information
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
package org.orekit.rugged.errors;


/** This class is a specialized exception for inverse location errors.
 * @author Luc Maisonobe
 */

public class InverseLocOutOfColumnRangeException extends RuggedException {

    /** Serializable UID. */
    private static final long serialVersionUID = 20150518L;

    /** Simple constructor.
     * @param expectedColumn expected column number for the ground point
     * @param minColumn minimum column number
     * @param maxColumn maximum column number
     */
    public InverseLocOutOfColumnRangeException(final double expectedColumn, final int minColumn, final int maxColumn) {
        super(RuggedMessages.GROUND_POINT_OUT_OF_COLUMN_RANGE, expectedColumn, minColumn, maxColumn);
    }

    /** Get the expected column number for the ground point.
     * @return expected column number for the ground point
     */
    public double getExpectedColumn() {
        return ((Double) getParts()[0]).doubleValue();
    }

}
