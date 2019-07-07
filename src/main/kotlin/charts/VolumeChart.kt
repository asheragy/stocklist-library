package org.cerion.stocklist.charts

import org.cerion.stocklist.PriceList
import org.cerion.stocklist.arrays.FloatArray
import org.cerion.stocklist.functions.ISimpleOverlay
import java.util.*

class VolumeChart : StockChart() {
    var logScale = false

    override fun getDataSets(priceList: PriceList): List<IDataSet> {
        val result = ArrayList<IDataSet>()
        val volume = if(logScale) priceList.toLogScale().volume else priceList.volume

        val data = DataSet(volume, "Volume", colorBlack())
        data.lineType = LineType.BAR
        result.addAll(Arrays.asList(data))

        val sets = getOverlayDataSets(volume)
        result.addAll(sets)
        return result
    }

    private fun getOverlayDataSets(volume: FloatArray): List<IDataSet> {
        resetNextColor()
        val result = ArrayList<IDataSet>()

        for (overlay in mOverlays) {
            val ol = overlay as ISimpleOverlay

            val arr = ol.eval(volume)
            result.addAll(getDefaultOverlayDataSets(arr, overlay))
        }

        return result

    }

}
