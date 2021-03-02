25 constant size
variable range

size array values

: fill size 0 do range @ gaussian i values ! loop ;

: show
  size 0
  do
    i values @ . cr
  loop ;

variable excflag
variable index

: bubbleSort
  begin
    true excflag !
    0 index !
    begin
      index @ values @
      index @ 1+ values @
      >
      if
        index @ dup values @
        index @ 1+ dup values @
        rot rot values !
        swap values !
        false excflag !
      then
      1 index +!
      index @ size 1- < not
    until
    excflag @ true =
  until
;

"filling array with 25 random long values" . 1000 range ! fill cr
"content of array before sort: " . cr show cr
bubbleSort
"content of array after sort: " . cr show cr

"filling array with 25 random double values" . 1000.0 range ! fill cr
"content of array before sort: " . cr show cr
bubbleSort
"content of array after sort: " . cr show cr

variable characters "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" characters !

: randomString
  ""
  15 random 0 do
    characters @ dup length random dup 1+ subString +
  loop
;

: stringFill
  size 0
  do
    randomString i values !
  loop
;


"filling array with 25 random strings" . stringFill cr
"content of array before sort: " . cr show cr
bubbleSort
"content of array after sort: " . cr show cr

