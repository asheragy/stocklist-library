package org.cerion.stocks.core.repository

import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.platform.KMPDate
import org.cerion.stocks.core.platform.KMPTimeStamp
import org.cerion.stocks.core.web.FetchInterval
import org.cerion.stocks.core.web.PriceHistoryDataSource
import java.util.*

interface PriceHistoryDates {
    val dailyStartDate: KMPDate?
    val weeklyStartDate: KMPDate?
    val monthlyStartDate: KMPDate?
    val quarterStartDate: KMPDate?
}

class DefaultPriceHistoryDates : PriceHistoryDates {
    override val dailyStartDate = getYearsBack(5)
    override val weeklyStartDate = getYearsBack(10)
    override val monthlyStartDate = getYearsBack(20)
    override val quarterStartDate: KMPDate? = null

    companion object {
        fun getYearsBack(years: Int): KMPDate {
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -years)
            return KMPDate(cal.time)
        }
    }
}

class CachedPriceListRepository(private val repo: IPriceListRepository, private val api: PriceHistoryDataSource, private val dates: PriceHistoryDates = DefaultPriceHistoryDates()) {

    fun get(symbol: String, interval: Interval): PriceList {
        val fetchInterval = when(interval) {
            Interval.DAILY -> FetchInterval.DAILY
            Interval.WEEKLY -> FetchInterval.WEEKLY
            else -> FetchInterval.MONTHLY
        }

        val cachedResult = repo.get(symbol, fetchInterval)
        var update = false
        val retrieveFrom: Date? = null

        if (cachedResult == null) {
            update = true
        }
        else if(cachedResult.lastUpdated != null) {
            val now = Date()
            var diff = now.time - cachedResult.lastUpdated!!.time
            diff /= (1000 * 60 * 60).toLong()
            val hours = diff
            val days = diff / 24

            println(symbol + " " + fetchInterval.name + " last updated " + cachedResult.lastUpdated + " (" + days + " days ago)")

            // TODO, smarter updates based on last price obtained and weekends
            if (fetchInterval === FetchInterval.DAILY && hours >= 12)
                update = true
            else if (fetchInterval === FetchInterval.WEEKLY && days > 3)
                update = true
            else if (fetchInterval === FetchInterval.MONTHLY && days > 7)
                update = true

            // Incremental update, not sure if all this is necessary but start a few data points earlier to be safe
            // TODO this may be working but do full update and re-verify this later
            /*
            if (update) {
                val cal = Calendar.getInstance()
                cal.time = result.last.date

                when (interval) {
                    Interval.DAILY -> cal.add(Calendar.DAY_OF_MONTH, -1)
                    Interval.WEEKLY -> cal.add(Calendar.DAY_OF_MONTH, -7)
                    Interval.MONTHLY -> cal.add(Calendar.DAY_OF_MONTH, -31)
                    Interval.QUARTERLY,
                    Interval.YEARLY -> throw Exception("Only daily/weekly/monthly allowed")
                }

                retrieveFrom = cal.time
            }
             */
        }

        if (retrieveFrom != null) {
            throw NotImplementedError("add incremental updating")
            //updatePricesIncremental(symbol, interval, start, retrieveFrom)
        }

        val result = if (update)
            updatePrices(symbol, interval, fetchInterval)
        else
            cachedResult!!

        if (interval == Interval.MONTHLY && dates.monthlyStartDate != null)
            return result.truncate(dates.monthlyStartDate!!)
        if (interval == Interval.QUARTERLY)
            return result.toQuarterly()
        if (interval == Interval.YEARLY)
            return result.toYearly()

        return result
    }

    private fun updatePrices(symbol: String, interval: Interval, fetchInterval: FetchInterval): PriceList {
        var list: PriceList? = null
        try {
            //val cal = Calendar.getInstance()
            //cal.set(1990, Calendar.JANUARY, 1)
            val kmpStartDate = when (interval) {
                Interval.DAILY -> dates.dailyStartDate
                Interval.WEEKLY -> dates.weeklyStartDate
                else -> dates.quarterStartDate // Always retrieve quarterly date then truncate month if needed
            }

            val startDate = kmpStartDate?.jvmDate

            val prices = api.getPrices(symbol, fetchInterval, startDate)
            list = PriceList(symbol, prices)
            list.lastUpdated = KMPTimeStamp()
        } catch (e: Exception) {
            // nothing
        }

        if (list != null && list.size > 0) {
            repo.add(list)
            println("Updated prices for $symbol")
        } else {
            throw Exception("Failed to get updated prices for $symbol")
        }

        return list
    }
}