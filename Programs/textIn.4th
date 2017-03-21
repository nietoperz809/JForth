: textIn
  "text" openReader
  begin
    dup
    readLine
    "EOF" =
    if
      true
    else
      . cr false
    then
  until
  closeReader
;

textIn
