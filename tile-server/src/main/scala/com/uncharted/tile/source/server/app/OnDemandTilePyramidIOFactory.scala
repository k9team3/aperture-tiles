/*
 * Copyright (c) 2015 Uncharted Software Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.uncharted.tile.source.server.app



import java.util.{List => JavaList}

import com.oculusinfo.binning.io.{PyramidIOFactory, PyramidIO}
import com.oculusinfo.factory.ConfigurableFactory
import com.oculusinfo.factory.properties.EnumProperty
import com.oculusinfo.tilegen.binning.{LegacyOnDemandBinningPyramidIO, OnDemandBinningPyramidIO, OnDemandAccumulatorPyramidIO}
import grizzled.slf4j.Logging
import org.json.JSONObject

import scala.util.Try


object OnDemandTilePyramidIOFactory {
  val ON_DEMAND_ALGORITHM = new EnumProperty[OnDemandAlgorithm]("algorithm", "Which on-demand algorithm to use",
    classOf[OnDemandAlgorithm],OnDemandAlgorithm.Binning)
}
/**
 * Factory for building an on-demand pyramid io
 */
class OnDemandTilePyramidIOFactory (name: String,
                                    parent: ConfigurableFactory[_],
                                    path: JavaList[String],
                                    contextProvider: SparkContextProvider)
  extends ConfigurableFactory[PyramidIO](name, classOf[PyramidIO], parent, path) with Logging
{
  import OnDemandTilePyramidIOFactory._

  addProperty(ON_DEMAND_ALGORITHM)
  addProperty(PyramidIOFactory.INITIALIZATION_DATA)

  def this (parent: ConfigurableFactory[_], path: JavaList[String], contextProvider: SparkContextProvider) =
    this("on-demand", parent, path, contextProvider)

  protected def create: PyramidIO = {
    Try {
      val config: JSONObject = getPropertyValue(PyramidIOFactory.INITIALIZATION_DATA)
      getPropertyValue(ON_DEMAND_ALGORITHM) match {
        case OnDemandAlgorithm.Accumulator => new OnDemandAccumulatorPyramidIO(contextProvider.getSQLContext(config))
        case OnDemandAlgorithm.Binning => new OnDemandBinningPyramidIO(contextProvider.getSQLContext(config))
        case OnDemandAlgorithm.LegacyBinning => new LegacyOnDemandBinningPyramidIO(contextProvider.getSQLContext(config))
        case _ => null
      }
    }.recover {
      case t: Throwable => {
        error("Error trying to create FileBasedPyramidIO", t)
        null
      }
    }.get
  }
}
