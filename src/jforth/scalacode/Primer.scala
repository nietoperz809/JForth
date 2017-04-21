package jforth.scalacode

/**
  * Created by Administrator on 4/19/2017.
  */
object Primer extends App
{
  //val primes: Stream[Int] = 2 #:: Stream.from(3, 2).filter(isPrime)

  lazy val primes: Stream[Int] = 2 #:: Stream.from(3).filter(i =>
    primes.takeWhile{j => j * j <= i}.forall{ k => i % k > 0})

  def sublist (a:Int, b:Int): List[Int] =
    {
      primes.toList.drop(a).dropRight(b)
    }

  val l: List[Int] = primes.slice(100000, 100020).toList

//  val x = List.tabulate(5)(n => n * n)
//
//  def fromBinaryList (in:List[Int]): Int =
//  {
//    var sum = 0
//    in foreach (x => {sum <<= 1; sum += (x&1)})
//    sum
//  }
//
//  println (fromBinaryList(List(0,1,1,1,1,1,1,1)))
}
