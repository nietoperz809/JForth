: countDown
  cr
  begin
    dup . cr
    1-
    dup
    0< if drop true else false then
  until
  cr
;

"test of begin until" . 25 countDown
