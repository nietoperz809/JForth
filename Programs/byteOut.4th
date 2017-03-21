: byteOut
  "bytes" openWriter
  256 0 do
    dup i swap writeByte 
  loop
  closeWriter
;

byteOut
