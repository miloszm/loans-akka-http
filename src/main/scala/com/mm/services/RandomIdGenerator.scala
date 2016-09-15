package com.mm.services

import java.util.UUID

trait IdGenerator {
  def generateId(): UUID
}

class RandomIdGenerator extends IdGenerator {
  override def generateId(): UUID = UUID.randomUUID()
}
