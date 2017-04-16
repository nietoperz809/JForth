package jforth.Scala


import collection.mutable
import annotation.tailrec
import math.{sqrt, round}

/**
  * Iterator over prime numbers that uses the sieve of Eratosthenes
  *
  * This is an implementation of the sieve of Eratosthenes as described in "The Genuine Sieve of Eratosthenes" by
  * Melissa E. O'Neill.
  *
  * Each time a prime p is discovered an iterator over its multiples starting at p*p is added to a priority queue
  * indexed by its next multiple. If p+1 does not appear at the head of the queue, it is another prime. Otherwise, it
  * is a composite, in which case all the corresponding iterators are advanced.
  */
class SieveOfEratosthenes extends BufferedIterator[Int] {

  /**
    * Iterator over the multiples of a prime number, starting at the square of that number
    * @param prime the base prime
    */
  class CompositeIterator(prime: BigInt) extends BufferedIterator[BigInt] {
    var composite: BigInt = prime * prime

    def hasNext = true

    def head: BigInt = composite

    def next(): BigInt = {
      val n = composite
      composite += prime
      n
    }

    override def toString(): String = prime + ":" + (0 to 2).map(composite + _ * prime).mkString(",") + "..."
  }

  /**
    * Iterators with the smallest next elements come first in the priority queue.
    */
  object CompositeIteratorOrdering extends Ordering[CompositeIterator] {
    def compare(a: CompositeIterator, b: CompositeIterator): Int = b.head.compare(a.head)
  }

  /**
    * Iterators over the multiples of all the primes that have been discovered so far.
    */
  private val composites = new mutable.PriorityQueue[CompositeIterator]()(CompositeIteratorOrdering)

  private var n: Int = 2
  markPrime(2)

  def hasNext = true

  def next(): Int = {
    val prime = n
    n += 1
    while (isComposite(n)) {
      markComposite(n)
      n += 1
    }
    markPrime(n)
    prime
  }

  def head: Int = n

  override def toString(): String = composites.toList.sortWith(_.head < _.head).mkString(", ")

  /**
    * Is the next number composite?
    *
    * The next composite appears at the head of the composites iterator queue.
    *
    * @param n next number
    * @return true if n is composite
    */
  private def isComposite(n: Int) = composites.head.head == n

  /**
    * Mark the next number as a prime
    *
    * @param prime number to mark as prime
    */
  private def markPrime(prime: Int) {
    composites.enqueue(new CompositeIterator(prime))
  }

  /**
    * Mark the next number as a composite
    *
    * Dequeue all the prime multiple iterators that have the specified component as their next element. Advance these
    * iterators to their next multiples and then add then back into the queue.
    *
    * @param composite number to mark as as composite
    */
  private def markComposite(composite: Int) {
    var multipleIterators: List[CompositeIterator] = Nil
    while (composites.head.head == composite) {
      multipleIterators = composites.dequeue() +: multipleIterators
    }
    multipleIterators.foreach(_.next())
    composites ++= multipleIterators
  }
}

object SieveOfEratosthenes {
  def apply() = new SieveOfEratosthenes

  /**
    * Prime factorization of a number
    *
    * @param n number to factorize
    * @return prime factors of n
    */
  def factors(n: BigInt): List[BigInt] = {
    @tailrec
    def factorsRecurse(fs: List[BigInt], n: BigInt): List[BigInt] = {
      val primes = SieveOfEratosthenes()
      primes takeWhile (_ <= round(sqrt(n.toDouble))) find (n % _ == 0) match {
        case Some(factor) => factorsRecurse(factor :: fs, n / factor)
        case None => n :: fs
      }
    }
    factorsRecurse(Nil, n).reverse
  }

  import scala.collection.JavaConverters.seqAsJavaList
  def toJList (l: List[BigInt]): java.util.List[BigInt] = seqAsJavaList(l)
}