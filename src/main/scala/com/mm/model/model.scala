package com.mm.model

import java.util.UUID

case class LoanRequest(amount: Double, durationInDays: Int)

case class Loan(loanId: LoanId, amount: Double, durationInDays: Int)

//object Loan {
//  def apply(loanId: LoanId, loanRequest: LoanRequest): Loan =
//    new Loan(loanId, loanRequest.amount, loanRequest.durationInDays)
//}

case class LoanId(loanId: UUID)

case class OfferRequest(amount: Double, interestRate: Double)
case class OfferId(offerId: UUID)
case class Offer(offerId: OfferId, loanId: LoanId, amount: Double, interestRate: Double)
case class PartialOffer(amount: Double, interestRate: Double)
case class AccumulatedOffer(amount: Double, interestRate: Double)

//object Offer {
//  def apply(offerId: OfferId, loanId: LoanId, offerRequest: OfferRequest): Offer =
//    new Offer(offerId, loanId, offerRequest.amount, offerRequest.interestRate)
//}

case class CurrentOffer(amount: Double, combinedInterest: Double)

class LoanNotFoundException(message: String) extends Exception(message)
