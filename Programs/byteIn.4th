: byteIn
  "bytes" openByteReader
  begin
    dup
    readByte
    dup 0<
    if
      drop true
    else
      hex . decimal cr false
    then
  until
  closeByteReader
;

byteIn
