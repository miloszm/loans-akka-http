package com.mm.services

import java.util.UUID

trait IdGenerator {
  def generateId(): UUID
}

class RandomIdGenerator extends IdGenerator {
  override def generateId(): UUID = UUID.fromString("6b3ee5d0-cf76-418d-960c-03de73a9086b")  //randomUUID()
}
