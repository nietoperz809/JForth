5 constant size

size array strings

"ab" 0 strings !
"cd" 1 strings !
"ef" 2 strings !
"gh" 3 strings !
"ij" 4 strings !

: showStrings
  size 0
  do
    i strings @ .
    cr
  loop
;

cr "content of string array:" . cr showStrings cr

variable s
"Hi Brian" s !

variable l
s @ length l !

l @ array a

: letters
  l @ 0 do
    s @ i dup 1+ subString i a ! 
  loop
  l @ 0 do
    i a @ . cr
  loop
;

: reverse
  "" swap dup
  0 swap length do
    dup i 1- i subString rot swap + swap
    -1
  +loop
  drop
;

"split and reverse \"" . s @ . "\"" . cr
letters
s @ reverse . cr cr
