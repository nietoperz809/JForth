
object ScalaMath
{
  def factorial(n: Long): BigInt =
  {
    def fac(n: BigInt, f: BigInt): BigInt =
      if (n == 0) f else fac(n-1, n*f)
    fac(n, 1)
  }
}
