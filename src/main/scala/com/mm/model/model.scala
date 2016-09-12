package com.mm.model

import java.util.UUID

case class LoanRequest(amount: Double, durationInDays: Int)

case class Loan(loanId: LoanId, amount: Double, durationInDays: Int)

//object Loan {
//  def apply(loanId: LoanId, loanRequest: LoanRequest): Loan =
//    new Loan(loanId, loanRequest.amount, loanRequest.durationInDays)
//}

case class LoanId(loanId: UUID)

