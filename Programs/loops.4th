cr

: dl1
  10 0 do
    " " . i .
  loop
;

"do loop with positive increment: " . dl1 cr
 
: dl2
  0 10 do
    " " . i .
    -1
  +loop
;

"do loop with negative increment: " . dl2 cr

: dl3
  0 10 do
    " " . i .
    -2
  +loop
;

"do loop with negative 2 increment: " . dl3 cr

: dl4
  0 10 do
    5 0 do
      "i = " . i .", j = " . j .
      cr
    1 +loop
    cr
  -2
  +loop
;

cr "nested do loops: " . cr dl4

: dl5
  10 0 do
    "i = " . i .
    ", mod = " . i 2 mod .
    cr
  loop
;

"test of mod function: " . cr dl5 cr

: dl6
  10 0 do
    i 2 mod 0=
    if
      "even = " . i .
    else
      "odd = " . i .
    then
    cr
  loop ;

"test of if else then conditionals: " . cr dl6 cr
