package com.mm.services

import com.mm.model._

object LoanService {

  def getCurrentOffer(loanId: LoanId)(implicit loanRepository:LoanRepository): Option[CurrentOffer] = {
    val loan = loanRepository.getLoan(loanId).getOrElse(throw new LoanNotFoundException(s"Loan ${loanId.loanId} not found"))

    loanRepository.getOffers(loanId) match {
      case Nil => None
      case offers => getPartialOffer(offers, loan.amount) match {
        case AccumulatedOffer(0, _) => None
        case AccumulatedOffer(total, interestRateSum) => Some(CurrentOffer(total, interestRateSum / total))
      }
    }
  }

  private def getPartialOffer(offers: List[Offer], loanAmount: Double): AccumulatedOffer = {
    def getPartialOffers(sortedOffers: List[Offer], loanAmount: Double, totalOffer: Double, acc:List[PartialOffer]): List[PartialOffer] = {
      sortedOffers match {
        case Nil => acc
        case h::t if totalOffer < loanAmount =>
          val takeFromOffer = Math.min(h.amount, loanAmount - totalOffer)
          getPartialOffers(t, loanAmount, totalOffer + takeFromOffer, PartialOffer(takeFromOffer, h.interestRate)::acc)
        case _ => acc
      }
    }

    val sortedOffers = offers.sortBy(_.interestRate)
    val partialOffers = getPartialOffers(sortedOffers, loanAmount, 0, List.empty[PartialOffer])
    val (interestRateSum, total) = partialOffers.foldLeft(0.0, 0.0) { (acc, p) =>
      (acc._1 + p.amount * p.interestRate, acc._2 + p.amount)
    }
    AccumulatedOffer(total, interestRateSum)
  }


}
