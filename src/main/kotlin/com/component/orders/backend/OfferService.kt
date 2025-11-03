package com.component.orders.backend

import com.component.orders.models.SIMPLE_DATE_FORMAT_PATTERN
import com.component.orders.models.Offer
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

@Service
class OfferService {

    private val offers = listOf(
        Offer(
            offerCode = "OFF50",
            validUntil = formattedFutureDate(3)
        ),
        Offer(
            offerCode = "SUMMER21",
            validUntil = formattedFutureDate(6)
        ),
        Offer(
            offerCode = "NEWYEAR25",
            validUntil = formattedFutureDate(9)
        )
    )

    fun findOfferForDate(date: Date): List<Offer> {
        return offers.filter { it.validUntil >= date }
    }

    private fun formattedFutureDate(monthsFromNow: Long): Date = SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN).parse(
        LocalDate.now().plusMonths(monthsFromNow).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    )
}
