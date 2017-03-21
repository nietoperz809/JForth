: textOut
  "text" openWriter
  swap
  0 do
    dup i swap writeString 
    dup "  Hi Brian" swap writeString
    dup writeEol
  loop
  dup 123.456 swap writeString
  dup writeEol
  closeWriter
;

10 textOut
