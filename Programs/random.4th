: randomtest
  cr
  25 0
  do
    dup random . cr
  loop
  drop
;

"long random numbers: " . 100 randomtest cr

"double random numbers: " . 100.0 randomtest cr
