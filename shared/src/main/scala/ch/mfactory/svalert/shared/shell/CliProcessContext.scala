package ch.mfactory.svalert.shared.shell

import fs2.Stream

trait CliProcessContext[F[_]] {

  def output: Stream[F, Byte]
  def error: Stream[F, Byte]
  def exitValue: F[Int]

}

object CliProcessContext {
  def apply[F[_]](_output: Stream[F, Byte], _error: Stream[F, Byte], _exitValue: F[Int]): CliProcessContext[F] =
    new CliProcessContext[F] {
      val output: Stream[F, Byte] = _output
      val error: Stream[F, Byte] = _error
      val exitValue: F[Int] = _exitValue
    }
}
