800 constant WIDTH
600 constant HEIGHT

variable file

"tga_test.tga" openWriter file !

: writeByte
  file @ writeByte
;

: writeSize
  dup 255 and writeByte 8 >> 255 and writeByte
;

: black
  0 0 0
;

: blue
  0 0 255
;

: green
  0 255 0
;

: cyan
  0 255 255
;

: red
  255 0 0
;

: magenta
  255 0 255
;

: yellow
  255 255 0
;

: white
  255 255 255
;

: writeColor
  writeByte writeByte writeByte
;

: writeStripes
  WIDTH 0 do
    HEIGHT 0 do
      j WIDTH 8 / /  writeByte
    loop
  loop
;

0 writeByte
1 writeByte
1 writeByte
0 writeByte
0 writeByte
8 writeByte
0 writeByte
24 writeByte
0 writeByte
0 writeByte
0 writeByte
0 writeByte
WIDTH writeSize
HEIGHT writeSize 
8 writeByte
32 writeByte
black writeColor
blue writeColor
green writeColor
cyan writeColor
red writeColor
magenta writeColor
yellow writeColor
white writeColor
writeStripes
    
file @ closeWriter
