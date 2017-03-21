variable placeHolder

: factorial
  dup 1 >
  if
    dup 1- placeHolder @ execute
	else
    1
	then
  * 
;

' factorial placeHolder !

"value of 10 factorial: " . 10 factorial . cr
