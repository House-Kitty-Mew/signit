package com.example.fabricmod.util;

import java.nio.charset.StandardCharsets;

public final class Adler32 {
    private static final int MOD = 65521;
    public static int compute(String data) {
        int a=1, b=0;
        for(byte c: data.getBytes(StandardCharsets.UTF_8)){ int v=c&0xFF; a=(a+v)%MOD; b=(b+a)%MOD; }
        return (b<<16)|a;
    }
}
