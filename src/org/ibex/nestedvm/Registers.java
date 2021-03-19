// Copyright 2000-2005 the Contributors, as shown in the revision logs.
// Licensed under the Apache Public Source License 2.0 ("the License").
// You may not use this file except in compliance with the License.

package org.ibex.nestedvm;

interface Registers {
    // Register Names
    int ZERO = 0; // Immutable, hardwired to 0
    int AT = 1;  // Reserved for assembler
    int K0 = 26; // Reserved for kernel
    int K1 = 27; // Reserved for kernel
    int GP = 28; // Global pointer (the middle of .sdata/.sbss)
    int SP = 29; // Stack pointer
    int FP = 30; // Frame Pointer
    int RA = 31; // Return Address
    
    // Return values (caller saved)
    int V0 = 2;
    int V1 = 3;
    // Argument Registers (caller saved)
    int A0 = 4;
    int A1 = 5;
    int A2 = 6;
    int A3 = 7;
    // Temporaries (caller saved)
    int T0 = 8;
    int T1 = 9;
    int T2 = 10;
    int T3 = 11;
    int T4 = 12;
    int T5 = 13;
    int T6 = 14;
    int T7 = 15;
    int T8 = 24;
    int T9 = 25;
    // Saved (callee saved)
    int S0 = 16;
    int S1 = 17;
    int S2 = 18;
    int S3 = 19;
    int S4 = 20;
    int S5 = 21;
    int S6 = 22;
    int S7 = 23;
}
