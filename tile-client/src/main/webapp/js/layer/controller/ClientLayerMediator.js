/*
 * Copyright (c) 2014 Oculus Info Inc.
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

/* JSLint global declarations: these objects don't need to be declared. */
/*global define, console, $, aperture*/

/**
 * Populates the LayerState model based on the contents of a the layer, and makes the appropriate
 * modifications to it as the LayerState model changes.
 */
define(function (require) {
    "use strict";



    var Class = require('../../class'),
        ClientLayerState = require('../model/ClientLayerState'),
        ClientLayerMediator;



    ClientLayerMediator = Class.extend({
        ClassName: "ClientLayerMediator",

        init: function() {
            this.layerStateMap = {};
            this.layerInfos = {};
        },

        registerLayers: function( layers ) {

            var that = this,
                i;

            function register( layer ) {

                var layerState;

                // Create a layer state object for the base map.
                layerState = new ClientLayerState( layer.id );
                layerState.setName( layer.getLayerSpec().name || layer.id );
                layerState.setEnabled( true );
                layerState.setOpacity( 1.0 );
                layerState.setZIndex( 0 );

                // Register a callback to handle layer state change events.
                layerState.addListener( function( fieldName ) {
                    if (fieldName === "opacity") {
                        layer.setOpacity( layerState.getOpacity() );
                    } else if (fieldName === "enabled") {
                        layer.setVisibility( layerState.isEnabled() );
                    } else if (fieldName === "tileView") {
                        layer.setTileView( layerState.getTileFocus(), layerState.getTileViewIndex( layerState.getTileFocus() ) );
                    }
                });

                // Add the layer to the layer statemap.
                that.layerStateMap[layerState.getId()] = layerState;
            }

            // ensure it is an array
            layers = ( $.isArray(layers) ) ? layers : [layers];

            for (i=0; i<layers.length; i++) {
                register( layers[i] );
            }
        },

        getLayerStateMap: function() {
            return this.layerStateMap;
        }

    });
    return ClientLayerMediator;
});
