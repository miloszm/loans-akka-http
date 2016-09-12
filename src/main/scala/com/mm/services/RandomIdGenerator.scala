package com.mm.services

import java.util.UUID
import java.util.UUID._

trait IdGenerator {
  def generateId(): UUID
}

class RandomIdGenerator extends IdGenerator {
  override def generateId(): UUID = randomUUID()
}
