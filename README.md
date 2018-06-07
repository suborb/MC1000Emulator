# CCE MC-1000 Emulator

A quick hacked up conversion of the BrMC1000 emulator that's hosted here: http://www.ricbit.com/mundobizarro/brmc1000.php

The CPU core has been changed to this one: https://github.com/jsanchezv/Z80Core since the supplied one was buggy.

## Usage with z88dk

zcc +mc1000 [file.c]

Use File->Load Binary, select the .bin file

Then enter:

call 992

