**JForth**
----------

This is a pretty complete Forth interpreter that supports also loops and various data types.

The original code is from: http://linuxenvy.com/bprentice/JForth/

Sorry, there's no documentation. In order to see how things work, please take a look at the test cases
in TestCases.java

Have fun.

Currently supported data types
------------------------------
`As of JForth, Build: 1689, 01/19/2020 09:05:28 PM`
`Numbers can be typed using the following notations:`

Long
> Ex: 1234  
0xffd2 ( hex input)  
0b1100101 ( binary input)   
01:00:00 ( equals 3600, timer value hh:mm:ss)

Double
> Ex: 123.456 

BigInt
> Ex: 123456789012345678901234567890L

DoubleSequence
> Ex: {1,2,4.456,7}

StringSequence
> Ex: {a,b,hello,666}

PolynomialFunction
> Ex: 2x^3-x^2+7x-9 

Complex
> 12+6i

Fraction
> 3/4


Predefined Words
------------------------------

<pre>
!               -- Store value into variable or array
'               -- Push word from dictionary onto stack
(               -- Begin comment
*               -- Multiply TOS and TOS-1
+               -- Add 2 values on stack
+!              -- Add value to variable
+loop           -- adds value to loop counter i
-               -- Substract values
.               -- Pop TOS and print it
."              -- String output
.s              -- Show whole data stack
.v              -- Show whole variable stack
/               -- Divide TOS-1 by TOS
/mod            -- Dividend and Remainder
0<              -- Gives 1 of TOS smaller than 0
0=              -- Gives 1 if TOS is zero
0>              -- Gives 1 if TOS greater than zero
1+              -- Add 1 to TOS
1-              -- Substract 1 from TOS
2*              -- Multiply TOS by 2
2+              -- Add 2 to TOS
2-              -- Substract 2 from TOS
2/              -- Divide TOS by 2
2dup            -- null
2over           -- null
2rot            -- null
2swap           -- null
:               -- Begin word definition
;               -- End word definition
<               -- null
<.              -- Restore last stack object
<<              -- Rotate left
<>              -- null
=               -- null
>               -- null
>>              -- Rotate right
>r              -- Put TOS to variable stack
?dup            -- Duplicate TOS if not zero
@               -- Put variable value on stack
E               -- Natural logarithm base
PI              -- Circle constant PI
Sf=             -- Antiderive of a polynomial
abs             -- Absolute value
accept          -- Read string from keyboard
acos            -- Inverse cosine
again           -- null
altsum          -- Add all elements together but alternates sign
and             -- Binary and of 2 values
apply           -- Apply polynomial to sequence
array           -- Create array
asin            -- Inverse sine
ask             -- Show yes/no box
asyncmsg        -- Show asynchronous message box
atan            -- Inverse tangent
atan2           -- Second arctan, see: https://de.wikipedia.org/wiki/Arctan2
b64             -- make Base64 from String
begin           -- null
big             -- BigPrint
bin             -- Set number base to 2
break           -- Breaks out of the forth word
bye             -- End the Forth interpreter
clear           -- Clear the stack
clearHist       -- Clear History
clltz           -- Get collatz sequence
closeByteReader -- Close file
closeReader     -- Close file
closeWriter     -- Close file
complex         -- Create a complex from 2 numbers
conj            -- Conjugate of complex or fraction
constant        -- create new Constant
cos             -- Cosine
cosh            -- Cosinus hyperbolicus
cr              -- Emit carriage return
crossP          -- Cross product of 3D vectors
dec             -- Set number base to 10
depth           -- null
detM            -- Determinant of a Matrix
diagM           -- Create diagonal Matrix from List
dir             -- Get directory
do              -- null
dotP            -- Dot product of 3D vectors
drop            -- null
dup             -- null
editor          -- Enter line editor
else            -- null
emit            -- Emit single char to console
execute         -- executes word from stack
exp             -- E^x
f'=             -- Derive a polynomial
fact            -- Factorial
factor          -- Prime factorisation
false           -- Gives 0
fib             -- Fibonacci number
fitPoly         -- Make polynomial sequence of Points
forget          -- Delete word from dictionary
forth           -- execute forth line asynchronously
fraction        -- Create a fraction from 2 Numbers
gMean           -- Geometric mean
gamma           -- Gamma funcction
gaussian        -- Gaussian random number
gcd             -- Greates common divisor
hash            -- generate hash string
hex             -- Set number base to 16
hexStr          -- Make hex string
http            -- run web server
i               -- put loop variable i on stack
idM             -- Create Identity Matrix
if              -- null
intersect       -- Make intersection of 2 sequences
invM            -- Inverse of a Matrix
isPrime         -- Primality test
j               -- put loop variable j on stack
java            -- compile and run java class
js              -- evaluate js expression string
key             -- Get key from keyboard
lagPoly         -- Make lagrange polynomial sequence of Points
lcm             -- Least common multiple
leave           -- null
length          -- Get length of what is on the stack
list            -- Put program in editor on stack
ln              -- Natural logarithm
loadHist        -- Load history
log10           -- Logarithm to base 10
loop            -- repeat loop
lpick           -- Get one Element from sequence
lupM            -- Determinant of a Matrix
max             -- Biggest value
mean            -- Mean value of sequence
min             -- Smallest value
mix             -- Mix two Lists
mod             -- Division remainder
msg             -- Show message box
not             -- Gives 0 if TOS is not 0, otherwise 1
openByteReader  -- Open file for reading
openReader      -- Open file
openWriter      -- Open file for Writing
or              -- Binary or of 2 values
over            -- null
permute         -- Generate permutation
phi             -- Phi of complex number
pick            -- Get value from arbitrary Positon and place it on TOS
ping            -- Check a host
playFile        -- Play Wave audio file
playHist        -- Execute History
playStr         -- Play Wave String
pow             -- Exponentation
prod            -- Product of all values
psp             -- Push space on stack
qMean           -- Quadratic Mean of sequence
r>              -- Move variable to data stack
r@              -- Copy variable to data stack
random          -- Pseudo random number
readByte        -- Read byte from file
readLine        -- Read line from file
recurse         -- Re-run current word
rev             -- Reverse a sequence
roll            -- null
rot             -- null
round           -- Round double value
run             -- Runs program in editor
runFile         -- run program file
sam             -- Make SAM data
saveHist        -- Save history
say             -- speak a string
seq             -- generate sequence
setbase         -- Set a new number base
shuffle         -- Random shuffles a sequence
sin             -- Sine
sinh            -- Sinus hyperbolicus
sleep           -- Sleep some milliseconds
sort            -- Sort a Sequence
sp              -- Emit single space
spaces          -- Emit multiple spaces
split           -- Split object into partitions
sqrt            -- Square root
stdDev          -- Standard Deviation of sequence
subSeq          -- Subsequence of string or list
sum             -- Add all elements together
sumq            -- Make sum of squares
swap            -- null
tan             -- Tangent
tanh            -- Tangent hyperbolicus
then            -- null
tick            -- Get clock value
time            -- Get a time string
toBig           -- Make BigInt values of what is on the stack
toBits          -- Make bit sequence from number
toDList         -- Create List of digits
toDouble        -- Make double value of what is on the stack
toFraction      -- Make fraction from value on the stack
toList          -- Make list of what is on the stack
toLong          -- Make long values of what is on the stack
toM             -- Make Matrix from Sequences
toPoly          -- Make polynomial from doubleSequence
toStr           -- Make string of what is on the stack
transM          -- Transpose a Matrix
true            -- Gives 1
tuck            -- null
type            -- Get type of TOS as string
udpget          -- Receive udp packet
udpput          -- Send udp packet
unb64           -- make String from Base64
unhexStr        -- Make Hexstr to Bytes
unique          -- Only keep unique elements of sequence
unlink          -- Delete file
until           -- null
urlDec          -- Decode URL encoded string
urlEnc          -- URL encode a string
var             -- Variance of sequence
variable        -- Create new variable
what            -- Show description about a word
words           -- Show all words
wordsd          -- Show words and description
writeByte       -- Write byte into file
writeEol        -- Write string end into file
writeString     -- Write string to file
x=              -- Solve a polynomial
xor             -- Xors two values
zeta            -- Riemann Zeta function
</pre>

Line Editor
------------------------------
The integrated line editor is invoked by _'editor'_
It knows these commands:

<pre>
 #l         -- List (with line numbers)
 #t         -- print list as String
 #c         -- clear all
 #h         -- this help text
 #dir       -- List directory
 #x         -- leave line editor
 #r text    -- read file where text is the file name
 #s test    -- save file where text is the file name
 #innn text -- Insert before, where nnn is the line number and text is the content
 #nnn       -- Delete line nnn
 #u         -- undo last list change
 #e         -- execute file in editor
 any other  -- input is appended to the buffer.

</pre>

