package org.cerion.stocklist.overlays

import org.cerion.stocklist.PriceList
import org.cerion.stocklist.arrays.FloatArray
import org.cerion.stocklist.functions.types.PriceOverlay

class ParabolicSAR() : PriceOverlayBase(PriceOverlay.PSAR, 0.02, 0.2) {

    constructor(vararg params: Number) : this() {
        setParams(*params)
    }

    override fun eval(list: PriceList): FloatArray {
        return parabolicSAR(list, getFloat(0), getFloat(1))
    }

    override fun getName(): String = "Parabolic SAR"

    private fun parabolicSAR(list: PriceList, step: Float, max: Float): FloatArray {
        val result = FloatArray(list.size)
        val close = list.mClose
        var start = 1

        while (close.get(start - 1) == close.get(start))
            start++

        when {
            close.get(start - 1) > close.get(start) -> sarFalling(list, result, start, list.high(start - 1), step, max)
            close.get(start - 1) < close.get(start) -> sarRising(list, result, start, list.low(start - 1), step, max)
            else -> println("error")
        } //above should fix this

        return result
    }

    private fun sarRising(list: PriceList, result: FloatArray, start: Int, sar_start: Float, step: Float, max: Float) {
        result.mVal[start] = sar_start

        var alpha = step
        var sar = sar_start
        var ep = list.high(start)

        for (i in start + 1 until list.size) {
            ep = Math.max(ep, list.high(i))
            if (ep == list.high(i) && alpha + step <= max)
                alpha += step

            if (ep - sar < 0)
                println("sarRising() error")

            sar += alpha * (ep - sar)

            if (sar > list.low(i)) {
                sarFalling(list, result, i, ep, step, max)
                return
            }

            result.mVal[i] = sar
        }

    }

    private fun sarFalling(list: PriceList, result: FloatArray, start: Int, sar_start: Float, step: Float, max: Float) {
        //System.out.println(p.date + "\t" + sar_start + "\tFalling");
        result.mVal[start] = sar_start

        var alpha = step
        var sar = sar_start
        var ep = list.low(start)

        for (i in start + 1 until list.size) {
            ep = Math.min(ep, list.low(i))
            if (ep == list.low(i) && alpha + step <= max)
                alpha += step

            if (sar - ep < 0)
                println("sarFalling error")

            sar -= alpha * (sar - ep)
            if (sar < list.high(i)) {
                sarRising(list, result, i, ep, step, max)
                return
            }

            result.mVal[i] = sar
        }
    }
}
